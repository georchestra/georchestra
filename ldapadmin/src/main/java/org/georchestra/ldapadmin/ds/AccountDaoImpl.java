/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ldapadmin.ds;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.dao.AdminLogDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.georchestra.ldapadmin.model.AdminLogEntry;
import org.georchestra.ldapadmin.model.AdminLogType;
import org.georchestra.ldapadmin.ws.newaccount.UidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AbstractFilter;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.PresentFilter;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

/**
 * This class is responsible of maintaining the user accounts (CRUD operations).
 *
 * @author Mauricio Pazos
 */
public final class AccountDaoImpl implements AccountDao {

    private LdapTemplate ldapTemplate;
    private GroupDao groupDao;
    private String uniqueNumberField = "employeeNumber";
    private LdapRdn userSearchBaseDN;
    private AtomicInteger uniqueNumberCounter = new AtomicInteger(-1);

    @Autowired
    private AdminLogDao logDao;

    private static final Log LOG = LogFactory.getLog(AccountDaoImpl.class.getName());

    private String basePath;
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }

    @Autowired
    public AccountDaoImpl(LdapTemplate ldapTemplate, GroupDao groupDao) {

        this.ldapTemplate = ldapTemplate;
        this.groupDao = groupDao;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setUniqueNumberField(String uniqueNumberField) {
        this.uniqueNumberField = uniqueNumberField;
    }

    public void setUserSearchBaseDN(String userSearchBaseDN) {
        this.userSearchBaseDN = new LdapRdn(userSearchBaseDN);
    }

    public void setLogDao(AdminLogDao logDao) {
        this.logDao = logDao;
    }

    /**
     * @see {@link AccountDao#insert(Account, String, String)}
     */
    @Override
    public synchronized void insert(final Account account, final String groupID, final String originLogin) throws DataServiceException,
            DuplicatedUidException, DuplicatedEmailException {

        assert account != null;

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
            Name dn = buildDn(uid);
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
            filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
            filter.and(new EqualsFilter("objectClass", "person"));

            Integer uniqueNumber = findUniqueNumber(filter, uniqueNumberField, this.uniqueNumberCounter, ldapTemplate);
            DirContextAdapter context = new DirContextAdapter(dn);
            mapToContext(uniqueNumber, account, context);

            // Maps the password separately
            context.setAttributeValue(UserSchema.USER_PASSWORD_KEY, account.getPassword());

            this.ldapTemplate.bind(dn, context, null);

            this.groupDao.addUser(groupID, account.getUid(), originLogin);

        } catch (NameNotFoundException e) {
            throw new DataServiceException(e);
        }
    }

    static Integer findUniqueNumber(AbstractFilter searchFilter, final String uniqueNumberField,
            AtomicInteger uniqueNumber, LdapTemplate ldapTemplate) {
        if (uniqueNumberField == null || uniqueNumberField.trim().isEmpty()) {
            return null;
        }
        if (uniqueNumber.get() < 0) {
            @SuppressWarnings("unchecked")
            final List<Integer> uniqueIds = ldapTemplate.search(DistinguishedName.EMPTY_PATH, searchFilter.encode(),
                    new AttributesMapper() {
                        @Override
                        public Object mapFromAttributes(Attributes attributes) throws NamingException {
                            final Attribute attribute = attributes.get(uniqueNumberField);
                            if (attribute == null) {
                                return 0;
                            }
                            final Object number = attribute.get();
                            if (number != null) {
                                try {
                                    return Integer.valueOf(number.toString());
                                } catch (NumberFormatException e) {
                                    return 0;
                                }
                            }
                            return 0;
                        }
                    });

            for (Integer uniqueId : uniqueIds) {
                if (uniqueId != null && uniqueId > uniqueNumber.get()) {
                    uniqueNumber.set(uniqueId);
                }
            }
            if (uniqueNumber.get() < 0) {
                uniqueNumber.set(0);
            }
            uniqueNumber.incrementAndGet();
        }

        boolean isUnique = false;
        while (!isUnique) {
            AndFilter filter = new AndFilter();
            filter.and(searchFilter);
            filter.and(new EqualsFilter(uniqueNumberField, uniqueNumber.get()));
            isUnique = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), new AccountContextMapper())
                    .isEmpty();
            uniqueNumber.incrementAndGet();
        }
        return uniqueNumber.get();
    }

    /**
     * @see {@link AccountDao#update(Account, String)}
     */
    @Override
    public synchronized void update(final Account account, String originLogin) throws DataServiceException, DuplicatedEmailException {

        // checks mandatory fields
        if (account.getUid().length() == 0) {
            throw new IllegalArgumentException("uid is required");
        }
        if (account.getSurname().length() == 0) {
            throw new IllegalArgumentException("surname is required");
        }
        if (account.getCommonName().length() == 0) {
            throw new IllegalArgumentException("common name is required");
        }
        if (account.getGivenName().length() == 0) {
            throw new IllegalArgumentException("given name is required");
        }

        // checks unique email
        try {

            // if the email is found in other account different that this
            // account, the new email cannot be used.
            Account foundAccount = findByEmail(account.getEmail());

            if (!foundAccount.getUid().equals(account.getUid())) {
                throw new DuplicatedEmailException("There is already an existing user with this email: "
                        + account.getEmail());
            }

        } catch (NameNotFoundException e1) {
            // if it doesn't exist an account with this e-mail the it can be
            // part of the updated account.
            LOG.debug("Updated account with email " + account.getEmail() + " does not exist, update possible.");
        }

        // update the entry in the ldap tree
        Name dn = buildDn(account.getUid());
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        mapToContext(null /* don't update number */, account, context);

        ldapTemplate.modifyAttributes(context);

        // Add log entry for this modification
        if(originLogin != null) {
            AdminLogEntry log = new AdminLogEntry(originLogin, account.getUid(), AdminLogType.LDAP_ATTRIBUTE_CHANGE, new Date());
            this.logDao.save(log);
        }
    }

    /**
     * @see {@link AccountDao#update(Account, Account, String)}
     */
    @Override
    public synchronized void update(Account account, Account modified, String originLogin) throws DataServiceException, DuplicatedEmailException, NameNotFoundException {
       if (! account.getUid().equals(modified.getUid())) {
           ldapTemplate.rename(buildDn(account.getUid()), buildDn(modified.getUid()));
           for (Group g : groupDao.findAllForUser(account.getUid())) {
               groupDao.modifyUser(g.getName(), account.getUid(), modified.getUid());
           }
       }
       update(modified, originLogin);
    }

    /**
     * Removes the user account and the reference included in the group
     *
     * @param uid user to delete from LDAP
     * @param originLogin login of admin that request deletion
     *
     * @see {@link AccountDao#delete(String, String)}
     */
    @Override
    public synchronized void delete(final String uid, final String originLogin) throws DataServiceException, NameNotFoundException {

        this.groupDao.deleteUser(uid, originLogin);
        this.ldapTemplate.unbind(buildDn(uid), true);

    }

    /**
     * @see {@link AccountDao#findAll()}
     */
    @Override
    public List<Account> findAll() throws DataServiceException {
        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(UserSchema.ATTR_TO_RETRIEVE);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        EqualsFilter filter = new EqualsFilter("objectClass", "person");
        return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), sc, new AccountContextMapper());
    }
    
    @Override
    public List<Account> find(final ProtectedUserFilter filterProtected, Filter f) {
        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(UserSchema.ATTR_TO_RETRIEVE);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        AndFilter and = new AndFilter();
        and.and( new EqualsFilter("objectClass", "person"));
        and.and(f);
        List<Account> l = ldapTemplate.search(DistinguishedName.EMPTY_PATH, and.encode(), sc, new AccountContextMapper());
        return filterProtected.filterUsersList(l);
    }

    @Override
    public List<Account> findFilterBy(final ProtectedUserFilter filterProtected) throws DataServiceException {

        List<Account> allUsers = findAll();

        List<Account> list = filterProtected.filterUsersList(allUsers);

        return list;
    }

    /**
     * @see {@link AccountDao#findByUID(String)}
     */
    @Override
    public Account findByUID(final String uid) throws NameNotFoundException{

        if(uid == null)
            throw new NameNotFoundException("Cannot find user with uid : " + uid + " in LDAP server");

        Account a = (Account) ldapTemplate.lookup(buildDn(uid.toLowerCase()), UserSchema.ATTR_TO_RETRIEVE, new AccountContextMapper());
        if(a == null)
            throw new NameNotFoundException("Cannot find user with uid : " + uid + " in LDAP server");
        else
            return a;

    }

    /**
     * @see {@link AccountDao#findByEmail(String)}
     */
    @Override
    public Account findByEmail(final String email) throws DataServiceException, NameNotFoundException {

        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(UserSchema.ATTR_TO_RETRIEVE);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mail", email));

        List<Account> accountList = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(),sc,
                new AccountContextMapper());
        if (accountList.isEmpty()) {
            throw new NameNotFoundException("There is no user with this email: " + email);
        }
        Account account = accountList.get(0);

        return account;
    }

    public boolean exist(final String uid) throws DataServiceException {

        try {
            DistinguishedName dn = buildDn(uid.toLowerCase());
            ldapTemplate.lookup(dn);
            return true;
        } catch (NameNotFoundException ex) {
            return false;
        }
    }

    /**
     * Create an ldap entry for the user
     *
     * @param uid
     *            user id
     * @return
     */
    private DistinguishedName buildDn(String uid) {
        DistinguishedName dn = new DistinguishedName();
        dn.add(userSearchBaseDN);
        dn.add("uid", uid);

        return dn;
    }

    /**
     * Checks that mandatory fields are present in the {@link Account}
     */
    private void checkMandatoryFields(Account a) throws IllegalArgumentException {

        // required by the account entry
        if (a.getUid().length() <= 0) {
            throw new IllegalArgumentException("uid is required");
        }

        // required field in Person object
        if (a.getGivenName().length() <= 0) {
            throw new IllegalArgumentException("Given name (cn) is required");
        }
        if (a.getSurname().length() <= 0) {
            throw new IllegalArgumentException("surname name (sn) is required");
        }
        if (a.getEmail().length() <= 0) {
            throw new IllegalArgumentException("email is required");
        }

    }

    /**
     * Maps the following the account object to the following LDAP entry schema:
     *
     * @param uniqueNumber
     * @param account
     * @param context
     */
    private void mapToContext(Integer uniqueNumber, Account account, DirContextOperations context) {

        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson",
                "inetOrgPerson" });

        // person attributes
        if (uniqueNumber != null) {
            setAccountField(context, uniqueNumberField, uniqueNumber.toString());
        }
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

        // additional
        setAccountField(context, UserSchema.ORG_KEY, account.getOrg());

        setAccountField(context, UserSchema.POSTAL_ADDRESS_KEY, account.getPostalAddress());

        setAccountField(context, UserSchema.POSTAL_CODE_KEY, account.getPostalCode());

        setAccountField(context, UserSchema.REGISTERED_ADDRESS_KEY, account.getRegisteredAddress());

        setAccountField(context, UserSchema.POST_OFFICE_BOX_KEY, account.getPostOfficeBox());

        setAccountField(context, UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY, account.getPhysicalDeliveryOfficeName());

        setAccountField(context, UserSchema.STATE_OR_PROVINCE_KEY, account.getStateOrProvince());

        setAccountField(context, UserSchema.ORG_UNIT_KEY, account.getOrganizationalUnit());
        
        setAccountField(context, UserSchema.HOME_POSTAL_ADDRESS_KEY, account.getHomePostalAddress());

        if(account.getManager() != null)
            setAccountField(context, UserSchema.MANAGER_KEY, "uid=" + account.getManager() + "," + this.userSearchBaseDN.toString() + "," + this.getBasePath());
        
        setAccountField(context, UserSchema.CONTEXT_KEY, account.getContext());
    }

    private void setAccountField(DirContextOperations context, String fieldName, Object value) {

        if (!isNullValue(value)) {
            context.setAttributeValue(fieldName, value);
        } else {
            Object[] values = context.getObjectAttributes(fieldName);
            if (values != null) {
                if (values.length == 1) {
                    LOG.info("Removing attribue " + fieldName);
                    context.removeAttributeValue(fieldName, values[0]);
                } else {
                    LOG.error("Multiple values encountered for field " + fieldName + ", expected a single value");
                }
            }
        }
    }

    public static class AccountContextMapper implements ContextMapper {

        @Override
        public Object mapFromContext(Object ctx) {

            DirContextAdapter context = (DirContextAdapter) ctx;

            Account account = AccountFactory.createFull(context.getStringAttribute(UserSchema.UID_KEY),
                    context.getStringAttribute(UserSchema.COMMON_NAME_KEY),
                    context.getStringAttribute(UserSchema.SURNAME_KEY),
                    context.getStringAttribute(UserSchema.GIVEN_NAME_KEY),
                    context.getStringAttribute(UserSchema.MAIL_KEY),

                    context.getStringAttribute(UserSchema.ORG_KEY), context.getStringAttribute(UserSchema.TITLE_KEY),

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
                    context.getStringAttribute(UserSchema.ORG_UNIT_KEY),

                    context.getStringAttribute(UserSchema.HOME_POSTAL_ADDRESS_KEY),
                    context.getStringAttribute(UserSchema.MOBILE_KEY),
                    context.getStringAttribute(UserSchema.ROOM_NUMBER_KEY),
                    context.getStringAttribute(UserSchema.STATE_OR_PROVINCE_KEY),
                    context.getStringAttribute(UserSchema.MANAGER_KEY),
                    context.getStringAttribute(UserSchema.CONTEXT_KEY));

            String rawShadowExpire = context.getStringAttribute(UserSchema.SHADOW_EXPIRE_KEY);
            if(rawShadowExpire != null){
                Long shadowExpire = Long.parseLong(rawShadowExpire);
                shadowExpire *= 1000; // Convert to milliseconds
                account.setShadowExpire(new Date(shadowExpire));
            }

            return account;
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

    @Override
    public void changePassword(final String uid, final String password) throws DataServiceException {

        if (StringUtils.isEmpty(uid)) {
            throw new IllegalArgumentException("uid is required");
        }
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password is required");
        }

        // update the entry in the ldap tree
        Name dn = buildDn(uid);
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        // the following action removes the old password. It there are two
        // passwords (old and new password) they will
        // be replaced by a single user password
        LdapShaPasswordEncoder lspe = new LdapShaPasswordEncoder();
        String encrypted = lspe.encodePassword(password, String.valueOf(System.currentTimeMillis()).getBytes());

        context.setAttributeValue("userPassword", encrypted);

        ldapTemplate.modifyAttributes(context);
    }

    /**
     * Adds the new password in the user password array. The new password is
     * maintained in array with two userPassword attributes.
     *
     * <pre>
     * Format:
     * userPassword[0] : old password
     * userPassword[1] : new password
     * </pre>
     *
     * @see {@link AccountDao#addNewPassword(String, String)}
     */
    @Override
    public void addNewPassword(String uid, String newPassword) {
        if (uid.length() == 0) {
            throw new IllegalArgumentException("uid is required");
        }
        if (newPassword.length() == 0) {
            throw new IllegalArgumentException("new password is required");
        }
        LdapShaPasswordEncoder lspe = new LdapShaPasswordEncoder();
        String encrypted = lspe.encodePassword(newPassword, String.valueOf(System.currentTimeMillis()).getBytes());
        // update the entry in the LDAP tree
        Name dn = buildDn(uid);
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        final String pwd = "userPassword";
        Object[] pwdValues = context.getObjectAttributes(pwd);
        if (pwdValues.length < 2) {
            // adds the new password
            context.addAttributeValue(pwd, encrypted, false);
        } else {
            // update the last password with the new password
            pwdValues[1] = newPassword;
            context.setAttributeValues(pwd, pwdValues);
        }

        ldapTemplate.modifyAttributes(context);
    }

    /**
     * Generate a new uid based on the provided uid
     *
     * @param
     *
     * @return the proposed uid
     */
    @Override
    public String generateUid(String uid) throws DataServiceException {

        String newUid = UidGenerator.next(uid);

        while (exist(newUid)) {

            newUid = UidGenerator.next(newUid);
        }

        return newUid;
    }


    @Override
    public List<Account> findByShadowExpire() {

        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(UserSchema.ATTR_TO_RETRIEVE);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "shadowAccount"));
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new PresentFilter("shadowExpire"));

        return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(),sc, new AccountContextMapper());

    }
}
