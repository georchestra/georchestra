/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ds.users;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.naming.Name;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.LdapDaoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.PresentFilter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;

import lombok.NonNull;

/**
 * This class is responsible of maintaining the user accounts (CRUD operations).
 *
 * @author Mauricio Pazos
 */
@SuppressWarnings("deprecation")
public class AccountDaoImpl implements AccountDao {
    private static final Log LOG = LogFactory.getLog(AccountDaoImpl.class.getName());

    private LdapDaoProperties props;

    @Autowired
    public void setLdapDaoProperties(LdapDaoProperties ldapDaoProperties) {
        this.props = ldapDaoProperties;
    }

    private AccountContextMapper attributMapper;
    private LdapTemplate ldapTemplate;

    @Autowired
    public AccountDaoImpl(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @PostConstruct
    public void init() {
        this.attributMapper = new AccountContextMapper(props.getPendingUserSearchBaseDN(),
                props.getOrgSearchBaseDN() + "," + props.getBasePath(),
                props.getPendingOrgSearchBaseDN() + "," + props.getBasePath());
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public synchronized void insert(@NonNull final Account account)
            throws DataServiceException, DuplicatedUidException, DuplicatedEmailException {

        checkMandatoryFields(account);

        // checks unique uid

        String uid = account.getUid().toLowerCase();
        try {
            findByUID(uid);

            throw new DuplicatedUidException("there is a user with this user identifier (uid): " + account.getUid());

        } catch (NameNotFoundException e1) {
            // if no account with the given UID can be found, then the new
            // account can be added.
            LOG.debug("User with uid " + uid + " not found, account can be created");
        }

        // checks unique email
        try {
            findByEmail(account.getEmail().trim());

            throw new DuplicatedEmailException("there is a user with this email: " + account.getEmail());

        } catch (NameNotFoundException e1) {
            // if no other accounts with the same e-mail exists yet, then the
            // new account can be added.
            LOG.debug("No account with the mail " + account.getEmail() + ", account can be created.");
        }

        // inserts the new user account
        try {
            Name dn = buildUserDn(account);

            DirContextAdapter context = new DirContextAdapter(dn);
            mapToContext(account, context);

            // Maps the password separately
            if (account.getSASLUser() == null) {
                context.setAttributeValue(UserSchema.USER_PASSWORD_KEY, account.getPassword());
            }

            this.ldapTemplate.bind(dn, context, null);

        } catch (NameNotFoundException e) {
            throw new DataServiceException(e);
        }
    }

    @Override
    public synchronized void update(final Account account) throws DataServiceException, DuplicatedEmailException {
        checkMandatoryFields(account);

        // checks unique email
        try {

            // if the email is found in other account different that this
            // account, the new email cannot be used.
            Account foundAccount = findByEmail(account.getEmail());

            if (!foundAccount.getUid().equals(account.getUid())) {
                throw new DuplicatedEmailException(
                        "There is already an existing user with this email: " + account.getEmail());
            }

        } catch (NameNotFoundException e1) {
            // if it doesn't exist an account with this e-mail the it can be
            // part of the updated account.
            LOG.debug("Updated account with email " + account.getEmail() + " does not exist, update possible.");
        }

        // update the entry in the ldap tree
        Name dn = buildUserDn(account);
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        mapToContext(account, context);

        ldapTemplate.modifyAttributes(context);
    }

    @Override
    public synchronized void update(Account account, Account modified)
            throws DataServiceException, DuplicatedEmailException, NameNotFoundException {
        if (!Objects.equals(account.getUniqueIdentifier(), modified.getUniqueIdentifier())) {
            modified.setUniqueIdentifier(account.getUniqueIdentifier());
        }
        if (hasUserDnChanged(account, modified)) {
            ldapTemplate.rename(buildUserDn(account), buildUserDn(modified));
        }
        update(modified);
    }

    @Override
    public boolean hasUserDnChanged(Account account, Account modified) {
        return !buildUserDn(account).equals(buildUserDn(modified));
    }

    @Override
    public boolean hasUserLoginChanged(Account account, Account modified) {
        return !account.getUid().equals(modified.getUid());
    }

    @Override
    public synchronized void delete(Account account) throws NameNotFoundException {
        this.ldapTemplate.unbind(buildUserDn(account), true);
    }

    @Override
    public Account findByUID(final String uid) throws NameNotFoundException {
        if (uid == null) {
            throw new NameNotFoundException("Cannot find user with uid : " + uid + " in LDAP server");
        }
        try {
            Account a = (Account) ldapTemplate.lookup(buildUserDn(uid.toLowerCase(), false),
                    UserSchema.ATTR_TO_RETRIEVE, attributMapper);
            if (a != null) {
                return a;
            }
        } catch (NameNotFoundException e) {
            try {
                Account a = (Account) ldapTemplate.lookup(buildUserDn(uid.toLowerCase(), true),
                        UserSchema.ATTR_TO_RETRIEVE, attributMapper);
                if (a != null) {
                    return a;
                }
            } catch (Exception ex) {
            }
            ;
        }
        throw new NameNotFoundException("Cannot find user with uid : " + uid + " in LDAP server");
    }

    /**
     * {@inheritDoc}
     *
     * @throws DataServiceException
     *
     * @implNote look up is performed by LDAP attribute
     *           {@code georchestraObjectIdentifier}
     */
    @Override
    public Account findById(@NonNull UUID id) throws NameNotFoundException, DataServiceException {
        // TODO: optimize, me have no time
        // final String georchestraObjectIdentifier = id.toString();
        // final AccountSearcher search = propertyEquals(UserSchema.UUID_KEY,
        // georchestraObjectIdentifier);
        List<Account> matches = this.findAll(a -> id.equals(a.getUniqueIdentifier()));
        if (matches.size() == 1)
            return matches.get(0);
        if (matches.isEmpty()) {
            throw new NameNotFoundException(UserSchema.UUID_KEY + " not found: " + id);
        }
        throw new IllegalStateException("Multiple accounts with the same id: " + id);
    }

    @Override
    public List<Account> findByShadowExpire() {
        return new AccountSearcher().and(new PresentFilter("shadowExpire"))
                .and(new EqualsFilter("objectClass", "shadowAccount")).getActiveOrPendingAccounts();
    }

    @Override
    public Account findByEmail(final String email) throws DataServiceException, NameNotFoundException {
        List<Account> accountList = new AccountSearcher().and(new EqualsFilter(UserSchema.MAIL_KEY, email))
                .getActiveOrPendingAccounts();
        if (accountList.isEmpty()) {
            throw new NameNotFoundException("There is no user with this email: " + email);
        }
        return accountList.get(0);
    }

    @Override
    public Account findByOAuth2Uid(final String oAuth2Provider, final String oAuth2Uid)
            throws DataServiceException, NameNotFoundException {
        List<Account> accountList = new AccountSearcher().and(new EqualsFilter(UserSchema.OAUTH2_UID_KEY, oAuth2Uid))
                .and(new EqualsFilter(UserSchema.OAUTH2_PROVIDER_KEY, oAuth2Provider)).getActiveOrPendingAccounts();
        if (accountList.isEmpty()) {
            throw new NameNotFoundException(
                    "There is no user with this oAuth2Provider: " + oAuth2Provider + " and oAuth2Uid: " + oAuth2Uid);
        }
        return accountList.get(0);
    }

    @Override
    public List<Account> findByRole(final String role) throws DataServiceException, NameNotFoundException {
        Name memberOfValue = LdapNameBuilder.newInstance(props.getBasePath()).add(props.getRoleSearchBaseDN())
                .add("cn", role).build();
        return new AccountSearcher().and(new EqualsFilter("memberOf", memberOfValue.toString()))
                .getActiveOrPendingAccounts();
    }

    @Override
    public List<Account> findFilterBy(final ProtectedUserFilter filterProtected) throws DataServiceException {
        List<Account> allUsers = new AccountSearcher().getActiveOrPendingAccounts();
        return filterProtected.filterUsersList(allUsers);
    }

    @Override
    public List<Account> findAll(Predicate<Account> filter) throws DataServiceException {
        return new AccountSearcher().getActiveOrPendingAccounts().stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public boolean exists(final String uid) {

        try {
            ldapTemplate.lookup(buildUserDn(uid.toLowerCase(), false));
            return true;
        } catch (NameNotFoundException ex) {
            try {
                ldapTemplate.lookup(buildUserDn(uid.toLowerCase(), true));
                return true;
            } catch (NameNotFoundException e) {
            }
            return false;
        }
    }

    /**
     * Generate a new uid based on the provided uid
     *
     * @param
     *
     * @return the proposed uid
     */
    @Override
    public String generateUid(String uid) {

        String newUid = UidGenerator.next(uid);

        while (exists(newUid)) {

            newUid = UidGenerator.next(newUid);
        }

        return newUid;
    }

    public String buildFullUserDn(Account account) {
        return String.format("%s,%s", buildUserDn(account.getUid(), account.isPending()), props.getBasePath());
    }

    private LdapName buildUserDn(Account account) {
        return buildUserDn(account.getUid(), account.isPending());
    }

    @Override
    public void changePassword(final String uid, final String password) throws DataServiceException {

        if (StringUtils.isEmpty(uid)) {
            throw new IllegalArgumentException("uid is required");
        }
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password is required");
        }

        DirContextOperations context = ldapTemplate.lookupContext(buildUserDn(uid, false));

        // the following action removes the old password. It there are two
        // passwords (old and new password) they will
        // be replaced by a single user password
        LdapShaPasswordEncoder lspe = new LdapShaPasswordEncoder();
        String encrypted = lspe.encode(password);

        context.setAttributeValue(UserSchema.USER_PASSWORD_KEY, encrypted);

        ldapTemplate.modifyAttributes(context);
    }

    private void checkMandatoryFields(Account account) throws IllegalArgumentException {

        if (account.getUid().length() <= 0) {
            throw new IllegalArgumentException("uid is required");
        }
        if (account.getGivenName().length() <= 0) {
            throw new IllegalArgumentException("Given name (cn) is required");
        }
        if (account.getSurname().length() <= 0) {
            throw new IllegalArgumentException("surname name (sn) is required");
        }
        if (account.getEmail().length() <= 0) {
            throw new IllegalArgumentException("email is required");
        }
        if (account.getCommonName().length() == 0) {
            throw new IllegalArgumentException("common name is required");
        }
    }

    /**
     * Maps the following the account object to the following LDAP entry schema:
     *
     * @param account
     * @param context
     */
    private void mapToContext(Account account, DirContextOperations context) {

        Set<String> objectClass = new HashSet<>();

        if (context.getStringAttributes("objectClass") != null) {
            Collections.addAll(objectClass, context.getStringAttributes("objectClass"));
        }
        Collections.addAll(objectClass, "top", "person", "organizationalPerson", "inetOrgPerson", "shadowAccount",
                "georchestraUser", "ldapPublicKey");

        if (account.getSshKeys() == null || account.getSshKeys().length == 0) {
            objectClass.remove("ldapPublicKey");
        }
        context.setAttributeValues("objectClass", objectClass.toArray());

        // person attributes
        setAccountField(context, UserSchema.SURNAME_KEY, account.getSurname());

        setAccountField(context, UserSchema.COMMON_NAME_KEY, account.getCommonName());

        setAccountField(context, UserSchema.DESCRIPTION_KEY, account.getDescription());

        setAccountField(context, UserSchema.TELEPHONE_KEY, account.getPhone());

        setAccountField(context, UserSchema.MOBILE_KEY, account.getMobile());

        // organizationalPerson attributes
        setAccountField(context, UserSchema.TITLE_KEY, account.getTitle());

        setAccountField(context, UserSchema.STREET_KEY, account.getStreet());

        setAccountField(context, UserSchema.LOCALITY_KEY, account.getLocality());

        setAccountField(context, UserSchema.FACSIMILE_KEY, account.getFacsimile());

        setAccountField(context, UserSchema.ROOM_NUMBER_KEY, account.getRoomNumber());

        // inetOrgPerson attributes
        setAccountField(context, UserSchema.GIVEN_NAME_KEY, account.getGivenName());

        setAccountField(context, UserSchema.UID_KEY, account.getUid().toLowerCase());

        setAccountField(context, UserSchema.MAIL_KEY, account.getEmail());

        setAccountField(context, UserSchema.POSTAL_ADDRESS_KEY, account.getPostalAddress());

        setAccountField(context, UserSchema.POSTAL_CODE_KEY, account.getPostalCode());

        setAccountField(context, UserSchema.REGISTERED_ADDRESS_KEY, account.getRegisteredAddress());

        setAccountField(context, UserSchema.POST_OFFICE_BOX_KEY, account.getPostOfficeBox());

        setAccountField(context, UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY, account.getPhysicalDeliveryOfficeName());

        setAccountField(context, UserSchema.STATE_OR_PROVINCE_KEY, account.getStateOrProvince());

        setAccountField(context, UserSchema.HOME_POSTAL_ADDRESS_KEY, account.getHomePostalAddress());

        if (account.getSshKeys() == null || account.getSshKeys().length > 0) {
            context.setAttributeValues(UserSchema.SSH_KEY, account.getSshKeys());
        }
        if (account.getManager() != null)
            setAccountField(context, UserSchema.MANAGER_KEY, "uid=" + account.getManager() + ","
                    + props.getUserSearchBaseDN().toString() + "," + props.getBasePath());
        else
            setAccountField(context, UserSchema.MANAGER_KEY, null);

        // Return shawdow Expire field as yyyy-mm-dd
        if (account.getShadowExpire() != null)
            setAccountField(context, UserSchema.SHADOW_EXPIRE_KEY,
                    String.valueOf(account.getShadowExpire().getTime() / 1000));
        else
            setAccountField(context, UserSchema.SHADOW_EXPIRE_KEY, null);

        // georchestraUser attributes
        if (account.getPrivacyPolicyAgreementDate() != null)
            setAccountField(context, UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY,
                    String.valueOf(account.getPrivacyPolicyAgreementDate().toEpochDay()));
        else
            setAccountField(context, UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY, null);

        if (null == account.getUniqueIdentifier()) {
            account.setUniqueIdentifier(UUID.randomUUID());
        }
        String suuid = account.getUniqueIdentifier().toString();
        setAccountField(context, UserSchema.UUID_KEY, suuid);

        setAccountField(context, UserSchema.NOTE_KEY, account.getNote());

        setAccountField(context, UserSchema.CONTEXT_KEY, account.getContext());

        if (new SASLPasswordWrapper(context).getPasswordType().equals(PasswordType.SASL)
                || !StringUtils.isEmpty(account.getSASLUser())) {
            String saslAccountAsPassword = StringUtils.isEmpty(account.getSASLUser()) ? null
                    : "{SASL}" + account.getSASLUser();
            setAccountField(context, UserSchema.USER_PASSWORD_KEY, saslAccountAsPassword);
        }

        setAccountField(context, UserSchema.OAUTH2_PROVIDER_KEY, account.getOAuth2Provider());
        setAccountField(context, UserSchema.OAUTH2_UID_KEY, account.getOAuth2Uid());
    }

    private void setAccountField(DirContextOperations context, String fieldName, Object value) {

        if (!isNullValue(value)) {
            context.setAttributeValue(fieldName, value);
        } else {
            Object[] values = context.getObjectAttributes(fieldName);
            if (values != null) {
                if (values.length == 1) {
                    LOG.info("Removing attribute " + fieldName);
                    context.removeAttributeValue(fieldName, values[0]);
                } else {
                    LOG.error("Multiple values encountered for field " + fieldName + ", expected a single value");
                }
            }
        }
    }

    static public class AccountContextMapper implements ContextMapper<Account> {

        private final Pattern pattern;
        private LdapName pendingUserSearchBaseDN;

        public AccountContextMapper(LdapName pendingUserSearchBaseDN, String orgBasePath, String pendingOrgBasePath) {
            this.pendingUserSearchBaseDN = pendingUserSearchBaseDN;
            this.pattern = Pattern
                    .compile(String.format("([^=,]+)=([^=,]+),((%s)|(%s))$", orgBasePath, pendingOrgBasePath));
        }

        @Override
        public Account mapFromContext(Object ctx) {

            DirContextAdapter context = (DirContextAdapter) ctx;
            Set<String> sshKeys = context.getAttributeSortedStringSet(UserSchema.SSH_KEY);

            String suuid = context.getStringAttribute(UserSchema.UUID_KEY);
            UUID uuid = null == suuid ? null : UUID.fromString(suuid);

            Account account = AccountFactory.createFull(uuid, context.getStringAttribute(UserSchema.UID_KEY),
                    context.getStringAttribute(UserSchema.COMMON_NAME_KEY),
                    context.getStringAttribute(UserSchema.SURNAME_KEY),
                    context.getStringAttribute(UserSchema.GIVEN_NAME_KEY),
                    context.getStringAttribute(UserSchema.MAIL_KEY), context.getStringAttribute(UserSchema.TITLE_KEY),
                    context.getStringAttribute(UserSchema.TELEPHONE_KEY),
                    context.getStringAttribute(UserSchema.DESCRIPTION_KEY),
                    context.getStringAttribute(UserSchema.POSTAL_ADDRESS_KEY),
                    context.getStringAttribute(UserSchema.POSTAL_CODE_KEY),
                    context.getStringAttribute(UserSchema.REGISTERED_ADDRESS_KEY),
                    context.getStringAttribute(UserSchema.POST_OFFICE_BOX_KEY),
                    context.getStringAttribute(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY),
                    context.getStringAttribute(UserSchema.STREET_KEY),
                    context.getStringAttribute(UserSchema.LOCALITY_KEY),
                    context.getStringAttribute(UserSchema.FACSIMILE_KEY),
                    context.getStringAttribute(UserSchema.HOME_POSTAL_ADDRESS_KEY),
                    context.getStringAttribute(UserSchema.MOBILE_KEY),
                    context.getStringAttribute(UserSchema.ROOM_NUMBER_KEY),
                    context.getStringAttribute(UserSchema.STATE_OR_PROVINCE_KEY),
                    context.getStringAttribute(UserSchema.MANAGER_KEY), context.getStringAttribute(UserSchema.NOTE_KEY),
                    context.getStringAttribute(UserSchema.CONTEXT_KEY), null, // Org will be filled later
                    sshKeys == null ? new String[0] : (String[]) sshKeys.toArray(new String[sshKeys.size()]), null,
                    context.getStringAttribute(UserSchema.OAUTH2_PROVIDER_KEY),
                    context.getStringAttribute(UserSchema.OAUTH2_UID_KEY));

            String rawShadowExpire = context.getStringAttribute(UserSchema.SHADOW_EXPIRE_KEY);
            if (rawShadowExpire != null) {
                Long shadowExpire = Long.parseLong(rawShadowExpire);
                shadowExpire *= 1000; // Convert to milliseconds
                account.setShadowExpire(new Date(shadowExpire));
            }

            String rawLastLogin = context.getStringAttribute(UserSchema.LASTLOGIN_KEY);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssz");
            if (rawLastLogin != null) {
                LocalDate lastLogin = LocalDate.from(fmt.parse(rawLastLogin));
                account.setLastLogin(lastLogin);
            }
            String rawCreationDate = context.getStringAttribute(UserSchema.CREATIONDATE_KEY);
            if (rawCreationDate != null) {
                LocalDate creationDate = LocalDate.from(fmt.parse(rawCreationDate));
                account.setCreationDate(creationDate);
            }

            // The privacy policy agreement date is stored in the LDAP as epoch day (days
            // since 1970-01-01), as in Unix shadow
            String rawPrivacyPolicyAgreementDate = context
                    .getStringAttribute(UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY);
            if (rawPrivacyPolicyAgreementDate != null) {
                Long privacyPolicyAgreementDate = Long.parseLong(rawPrivacyPolicyAgreementDate);
                account.setPrivacyPolicyAgreementDate(LocalDate.ofEpochDay(privacyPolicyAgreementDate));
            }

            // Set Organization
            String org = null;

            SortedSet<String> roles = context.getAttributeSortedStringSet("memberOf");
            if (roles != null) {
                Iterator<String> it = roles.iterator();
                while (it.hasNext()) {
                    String role = it.next();
                    Matcher m = this.pattern.matcher(role);

                    // Skip roles
                    if (!m.matches())
                        continue;

                    // Check organization cardinality
                    if (org != null)
                        throw new RuntimeException("More than one org per user on " + account.getCommonName());

                    org = m.group(2);
                }
                if (org != null)
                    account.setOrg(org);
            }

            account.setPending(context.getDn().startsWith(pendingUserSearchBaseDN));
            tryToSetPasswordTypeAndGuessSaslUser(context, account);
            return account;
        }

        private void tryToSetPasswordTypeAndGuessSaslUser(DirContextAdapter context, Account account) {
            SASLPasswordWrapper saslPasswordWrapper = new SASLPasswordWrapper(context);
            account.setPasswordType(saslPasswordWrapper.getPasswordType());
            account.setSASLUser(saslPasswordWrapper.getUserName());
        }
    }

    private boolean isNullValue(Object value) {

        if (value == null)
            return true;

        if (value instanceof String && (StringUtils.isEmpty(value.toString()))) {
            return true;
        }

        return false;
    }

    private LdapName buildUserDn(String uid, boolean pending) {
        LdapNameBuilder builder = LdapNameBuilder.newInstance();
        builder.add(pending ? props.getPendingUserSearchBaseDN() : props.getUserSearchBaseDN());
        builder.add("uid", uid);
        return builder.build();
    }

    private class AccountSearcher {

        private AndFilter filter;

        public List<Account> getActiveOrPendingAccounts() {
            SearchControls sc = createSearchControls();
            List<Account> active = ldapTemplate.search(props.getUserSearchBaseDN(), filter.encode(), sc,
                    attributMapper);
            List<Account> pending = ldapTemplate.search(props.getPendingUserSearchBaseDN(), filter.encode(), sc,
                    attributMapper);
            return Stream.concat(active.stream(), pending.stream()).collect(toList());
        }

        public AccountSearcher() {
            filter = new AndFilter().and(new EqualsFilter("objectClass", "inetOrgPerson"))
                    .and(new EqualsFilter("objectClass", "organizationalPerson"))
                    .and(new EqualsFilter("objectClass", "person"))
                    .and(new EqualsFilter("objectClass", "georchestraUser"));
        }

        public AccountSearcher and(Filter filter) {
            this.filter.and(filter);
            return this;
        }

        private SearchControls createSearchControls() {
            SearchControls sc = new SearchControls();
            sc.setReturningAttributes(UserSchema.ATTR_TO_RETRIEVE);
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            return sc;
        }
    }

    static class SASLPasswordWrapper {
        private PasswordType passwordType;
        private String userName;

        public SASLPasswordWrapper(DirContextOperations context) {
            try {
                byte[] rawPassword = (byte[]) context.getObjectAttribute(UserSchema.USER_PASSWORD_KEY);
                String password = new String(rawPassword);
                int typeIndexLast = password.lastIndexOf("}");
                passwordType = PasswordType.valueOf(password.substring(1, typeIndexLast).toUpperCase());
                if (passwordType == PasswordType.SASL) {
                    userName = password.substring(typeIndexLast + 1);
                }
            } catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
                passwordType = PasswordType.UNKNOWN;
            }
        }

        public PasswordType getPasswordType() {
            return passwordType;
        }

        public String getUserName() {
            return userName;
        }
    }

}
