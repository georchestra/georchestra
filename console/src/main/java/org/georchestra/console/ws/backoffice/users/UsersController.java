/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.users;

import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.*;
import org.georchestra.console.dto.*;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.users.GDPRAccountWorker.DeletedAccountSummary;
import org.georchestra.console.ws.backoffice.utils.RequestUtil;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web Services to maintain the User information.
 *
 * <p>
 * This class provides the operations to access the data layer to update and
 * read the user data. Those operations will be consistent with the business
 * rules.
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
@Controller
public class UsersController {

    private static final Log LOG = LogFactory.getLog(UsersController.class.getName());

    private static final String BASE_MAPPING = "/private";
    private static final String REQUEST_MAPPING = BASE_MAPPING + "/users";
    private static final String PUBLIC_REQUEST_MAPPING = "/public/users";
    private static GrantedAuthority ROLE_SUPERUSER = AdvancedDelegationDao.ROLE_SUPERUSER;

    @Value("${gdpr.allowAccountDeletion:true}")
    private Boolean gdprAllowAccountDeletion;

    private AccountDao accountDao;

    @Autowired
    private OrgsDao orgDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private AdvancedDelegationDao advancedDelegationDao;

    @Autowired
    private @Setter GDPRAccountWorker gdprInfoWorker;

    @Autowired
    private Boolean warnUserIfUidModified = false;

    private UserRule userRule;

    @Autowired
    private EmailFactory emailFactory;

    @Autowired
    protected LogUtils logUtils;

    public void setEmailFactory(EmailFactory emailFactory) {
        this.emailFactory = emailFactory;
    }

    public void setOrgDao(OrgsDao orgDao) {
        this.orgDao = orgDao;
    }

    public void setDelegationDao(DelegationDao delegationDao) {
        this.delegationDao = delegationDao;
    }

    public void setAdvancedDelegationDao(AdvancedDelegationDao advancedDelegationDao) {
        this.advancedDelegationDao = advancedDelegationDao;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public void setWarnUserIfUidModified(boolean warnUserIfUidModified) {
        this.warnUserIfUidModified = warnUserIfUidModified;
    }

    public void setGdprAllowAccountDeletion(Boolean gdprAllowAccountDeletion) {
        this.gdprAllowAccountDeletion = gdprAllowAccountDeletion;
    }

    @Autowired
    public UsersController(AccountDao dao, UserRule userRule) {
        this.accountDao = dao;
        this.userRule = userRule;
    }

    /**
     * Returns array of users using json syntax.
     *
     * <pre>
     *
     *	[
     *	    {
     *	        "org": "Zogak",
     *	        "givenName": "Walsh",
     *	        "sn": "Atkins",
     *	        "uid": "watkins"
     *	    },
     *	        ...
     *	]
     * </pre>
     *
     * @throws IOException
     */
    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<SimpleAccount> findAll() throws DataServiceException {

        ProtectedUserFilter filter = new ProtectedUserFilter(this.userRule.getListUidProtected());
        List<Account> list = this.accountDao.findFilterBy(filter);
        Collections.sort(list);

        // Retrieve organizations list to display org name instead of org DN
        List<Org> orgs = this.orgDao.findAll();
        Map<String, String> orgNames = new HashMap();
        for (Org org : orgs)
            orgNames.put(org.getId(), org.getName());

        List<SimpleAccount> res = new LinkedList();
        for (Account account : list) {
            SimpleAccount simpleAccount = new SimpleAccount(account);
            // Set Org Name with the human readable org name
            simpleAccount.setOrgName(orgNames.get(account.getOrg()));
            res.add(simpleAccount);
        }

        return res;
    }

    /**
     * Returns the detailed information of the user.
     *
     * <p>
     * If the user identifier is not present in the ldap store an
     * {@link IOException} will be throw.
     * </p>
     * <p>
     * URL Format: [BASE_MAPPING]/users/{uid}
     * </p>
     * <p>
     * Example: [BASE_MAPPING]/users/hsimpson
     * </p>
     *
     */
    @RequestMapping(value = REQUEST_MAPPING
            + "/{uid:.+}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Account findByUid(@PathVariable String uid)
            throws AccessDeniedException, NameNotFoundException, DataServiceException {

        // Check for protected accounts
        if (this.userRule.isProtected(uid))
            throw new AccessDeniedException("The user is protected: " + uid);

        // Check delegation
        this.checkAuthorization(uid);

        return this.accountDao.findByUID(uid);

    }

    /**
     * Returns the profile of current user.
     *
     * <p>
     * URL Format: [BASE_MAPPING]/users/profile
     * </p>
     *
     * returns following format :
     *
     * <pre>
     * {
     *   uid: "testuser",
     *   org: "psc",
     *   roles: ["USER", "MOD_EXTRACTORAPP"]
     * }
     * </pre>
     */
    @GetMapping(value = REQUEST_MAPPING + "/profile", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String myProfile(HttpServletRequest request) throws JSONException, DataServiceException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .map(s -> s.replaceFirst("ROLE_", "")).collect(Collectors.toList());
        Account a = accountDao.findByUID(auth.getName());

        JSONObject res = new JSONObject();
        res.put("uid", auth.getName());
        res.put("roles", roles);
        res.put("org", a.getOrg());

        return res.toString();
    }

    /**
     * <p>
     * Creates a new user.
     * </p>
     *
     * <pre>
     * <b>Request</b>
     *
     * user data:
     * {
     *  "sn": "surname",
     *	"givenName": "first name",
     *	"mail": "e-mail",
     * 	"telephoneNumber": "telephone"
     *	"facsimileTelephoneNumber": "value",
     * 	"street": "street",
     * 	"postalCode": "postal code",
     *	"l": "locality",
     * 	"postOfficeBox": "the post office box",
     *  "org": "the_organization"
     * }
     *
     * where <b>sn, givenName, mail</b> are mandatories
     * </pre>
     *
     * <pre>
     * <b>Response</b>
     *
     * <b>- Success case</b>
     *
     * The generated uid is added to the user data. So, a succeeded response should look like:
     * {
     * 	<b>"uid": generated uid</b>
     *
     *  "sn": "surname",
     *	"givenName": "first name",
     *	"mail": "e-mail",
     * 	"telephoneNumber": "telephone"
     *	"facsimileTelephoneNumber": "value",
     * 	"street": "street",
     * 	"postalCode": "postal code",
     *	"l": "locality",
     * 	"postOfficeBox": "the post office box"
     * }
     * </pre>
     *
     * <pre>
     * <b>- Error case</b>
     * If the provided e-mail exists in the LDAP store the response will contain:
     *
     * 	{ \"success\": false, \"error\": \"duplicated_email\"}
     *
     * Error: 409 conflict with the current state of resource
     *
     * </pre>
     *
     * @param request HTTP POST data contains the user data
     * @throws IOException
     */
    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Account create(HttpServletRequest request)
            throws IOException, DuplicatedEmailException, DataServiceException, DuplicatedUidException {

        final Account account = createAccountFromRequestBody(request.getInputStream());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verify that org is under delegation if user is not SUPERUSER
        String requestOriginator = auth.getName();
        if (!callerIsSuperUser()) {
            DelegationEntry delegation = this.delegationDao.findOne(requestOriginator);
            if (delegation != null && !Arrays.asList(delegation.getOrgs()).contains(account.getOrg()))
                throw new AccessDeniedException("Org not under delegation");
        }

        if (this.userRule.isProtected(account.getUid())) {
            throw new AccessDeniedException("The user is protected: " + account.getUid());
        }

        // Saves the user in the LDAP
        accountDao.insert(account, requestOriginator);

        roleDao.addUser(Role.USER, account, requestOriginator);

        orgDao.linkUser(account);

        logUtils.createLog(account.getUid(), AdminLogType.USER_CREATED, null);

        return account;
    }

    public boolean callerIsSuperUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().contains(ROLE_SUPERUSER);
    }

    /**
     * Modifies the user data using the fields provided in the request body.
     * <p>
     * The fields that are not present in the parameters will remain untouched in
     * the LDAP store.
     * </p>
     * <p>
     * The request format is: [BASE_MAPPING]/users/{uid}
     * </p>
     * <p>
     * The request body should contains a the fields to modify using the JSON
     * syntax.
     * </p>
     * <p>
     * Example:
     * </p>
     *
     * <pre>
     * <b>Request</b>
     * [BASE_MAPPING]/users/hsimpson
     *
     * <b>Body request: </b>
     * {"sn": "surname",
     *  "givenName": "first name",
     *  "mail": "e-mail",
     *  "telephoneNumber": "telephone",
     *  "facsimileTelephoneNumber": "value",
     * 	"street": "street",
     *  "postalCode": "postal code",
     *  "l": "locality",
     *  "postOfficeBox": "the post office box"
     * }
     *
     * </pre>
     *
     * @param request
     *
     * @throws IOException           if the uid does not exist or fails to access to
     *                               the LDAP store.
     * @throws NameNotFoundException
     */
    @RequestMapping(value = REQUEST_MAPPING
            + "/{uid:.+}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Account update(@PathVariable String uid, HttpServletRequest request)
            throws IOException, NameNotFoundException, DataServiceException, DuplicatedEmailException, ParseException,
            JSONException, MessagingException {

        if (this.userRule.isProtected(uid)) {
            throw new AccessDeniedException("The user is protected, it cannot be updated: " + uid);
        }

        // check if user is under delegation for delegated admins
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        this.checkAuthorization(uid);

        // searches the account
        Account originalAcount = this.accountDao.findByUID(uid);
        Account modifiedAccount = modifyAccount(AccountFactory.create(originalAcount), request.getInputStream());
        boolean isPendingValidation = originalAcount.isPending() && !modifiedAccount.isPending();

        if (!modifiedAccount.getOrg().equals(originalAcount.getOrg())) {
            if (!auth.getAuthorities().contains(ROLE_SUPERUSER))
                if (!Arrays.asList(this.delegationDao.findOne(auth.getName()).getOrgs())
                        .contains(originalAcount.getOrg()))
                    throw new AccessDeniedException("User not under delegation");
            orgDao.unlinkUser(originalAcount);
        }

        accountDao.update(originalAcount, modifiedAccount, auth.getName());

        // log update modifications
        logUtils.logChanges(modifiedAccount, originalAcount);

        if (!modifiedAccount.getOrg().equals(originalAcount.getOrg())) {
            if (!auth.getAuthorities().contains(ROLE_SUPERUSER))
                if (!Arrays.asList(this.delegationDao.findOne(auth.getName()).getOrgs())
                        .contains(modifiedAccount.getOrg()))
                    throw new AccessDeniedException("User not under delegation");
            orgDao.linkUser(modifiedAccount);
        }

        if (accountDao.hasUserDnChanged(originalAcount, modifiedAccount)) {
            // account was validated by a moderator, notify user
            if (isPendingValidation) {
                // send validation email to user
                this.emailFactory.sendAccountWasCreatedEmail(request.getSession().getServletContext(),
                        modifiedAccount.getEmail(), modifiedAccount.getCommonName(), modifiedAccount.getUid());
            }
            roleDao.modifyUser(originalAcount, modifiedAccount);

            // log pending user validation
            if (isPendingValidation) {
                logUtils.createLog(modifiedAccount.getUid(), AdminLogType.PENDING_USER_ACCEPTED, null);
            }
        }

        if (accountDao.hasUserLoginChanged(originalAcount, modifiedAccount)) {
            DelegationEntry delegationEntry = delegationDao.findOne(originalAcount.getUid());
            if (delegationEntry != null) {
                delegationDao.delete(delegationEntry);
                delegationEntry.setUid(modifiedAccount.getUid());
                delegationDao.save(delegationEntry);
            }
        }
        if (accountDao.hasUserLoginChanged(originalAcount, modifiedAccount) && warnUserIfUidModified) {
            this.emailFactory.sendAccountUidRenamedEmail(request.getSession().getServletContext(),
                    modifiedAccount.getEmail(), modifiedAccount.getCommonName(), modifiedAccount.getUid());
        }
        return modifiedAccount;
    }

    /**
     * Deletes the user.
     * <p>
     * The user account and its associated roles are removed from the LDAP database,
     * as well as any delegation linkage. Additionally, all the GDPR sensitive data
     * collected for the account is obfuscated so that its untraceable back to the
     * user.
     * <p>
     * The request format is:
     *
     * <pre>
     * [BASE_MAPPING]/users/{uid}
     * </pre>
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{uid:.+}", method = RequestMethod.DELETE, produces = "application/json")
    public void delete(@PathVariable String uid, HttpServletRequest request, HttpServletResponse response)
            throws IOException, DataServiceException, NameNotFoundException {

        if (this.userRule.isProtected(uid)) {
            throw new AccessDeniedException("The user is protected, it cannot be deleted: " + uid);
        }

        // check if user is under delegation for delegated admins
        this.checkAuthorization(uid);

        final Account account = accountDao.findByUID(uid);
        final String requestOriginator = request.getHeader("sec-username");
        deleteAccount(account, requestOriginator);
        ResponseUtil.writeSuccess(response);
    }

    private void deleteAccount(Account account, String requestOriginator) throws DataServiceException {
        accountDao.delete(account, requestOriginator);
        roleDao.deleteUser(account, requestOriginator);

        // Also delete delegation if exists
        if (delegationDao.findOne(account.getUid()) != null) {
            delegationDao.delete(account.getUid());
        }

        // log when a user is removed according to pending status
        if (account.isPending()) {
            logUtils.createLog(account.getUid(), AdminLogType.PENDING_USER_REFUSED, null);
        } else {
            logUtils.createLog(account.getUid(), AdminLogType.USER_DELETED, null);
        }
    }

    /**
     * Deletes the account of the calling user and obfuscates records of GDPR
     * sensitive information.
     *
     * @return summary of records anonymized as a result
     */
    @RequestMapping(method = RequestMethod.POST, value = "/account/gdpr/delete", produces = "application/json")
    public ResponseEntity<DeletedUserDataInfo> deleteCurrentUserAndGDPRData(HttpServletResponse response)
            throws DataServiceException {

        /*
         * Disabling this endpoint if the gdpr.allowAccountDeletion property is set to
         * false.
         */
        if (!gdprAllowAccountDeletion) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        String accountId = SecurityContextHolder.getContext().getAuthentication().getName();

        final Account account = accountDao.findByUID(accountId);

        if (this.userRule.isProtected(account.getUid()))
            throw new AccessDeniedException("The user is protected, it cannot be deleted: " + account.getUid());

        LOG.info(String.format("GDPR: user %s requested to delete his records", accountId));

        deleteAccount(account, accountId);

        DeletedAccountSummary summary = gdprInfoWorker.deleteAccountRecords(account);

        DeletedUserDataInfo responseValue = toPresentation(accountId, summary);

        return new ResponseEntity<>(responseValue, HttpStatus.OK);
    }

    private DeletedUserDataInfo toPresentation(final String accountId, DeletedAccountSummary summary) {
        DeletedUserDataInfo responseValue = DeletedUserDataInfo.builder().account(accountId)//
                .metadata(summary.getMetadataRecords())//
                .extractor(summary.getExtractorRecords())//
                .geodocs(summary.getGeodocsRecords())//
                .metadata(summary.getMetadataRecords())//
                .ogcStats(summary.getOgcStatsRecords())//
                .build();
        return responseValue;
    }

    @RequestMapping(value = PUBLIC_REQUEST_MAPPING + "/requiredFields", method = RequestMethod.GET)
    public void getUserCreationRequiredFields(HttpServletResponse response) throws IOException {
        try {
            JSONArray fields = new JSONArray();
            fields.put(UserSchema.UID_KEY);
            fields.put(UserSchema.MAIL_KEY);
            fields.put(UserSchema.SURNAME_KEY);
            fields.put(UserSchema.GIVEN_NAME_KEY);
            ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
        }
    }

    /**
     * Modify only the account's fields that are present in the request body.
     *
     * @param account
     * @param inputStream
     *
     * @return the modified account
     *
     * @throws IOException
     */
    private Account modifyAccount(Account account, ServletInputStream inputStream)
            throws IOException, JSONException, ParseException, IllegalArgumentException {

        String strUser = FileUtils.asString(inputStream);
        JSONObject json = new JSONObject(strUser);

        String givenName = RequestUtil.getFieldValue(json, UserSchema.GIVEN_NAME_KEY);
        if (givenName != null) {
            account.setGivenName(givenName);
        }

        String surname = RequestUtil.getFieldValue(json, UserSchema.SURNAME_KEY);
        if (surname != null) {
            account.setSurname(surname);
        }

        String email = RequestUtil.getFieldValue(json, UserSchema.MAIL_KEY);
        if (email != null) {
            account.setEmail(email);
        }

        String postalAddress = RequestUtil.getFieldValue(json, UserSchema.POSTAL_ADDRESS_KEY);
        if (postalAddress != null) {
            account.setPostalAddress(postalAddress);
        }

        String postOfficeBox = RequestUtil.getFieldValue(json, UserSchema.POST_OFFICE_BOX_KEY);
        if (postOfficeBox != null) {
            account.setPostOfficeBox(postOfficeBox);
        }

        String postalCode = RequestUtil.getFieldValue(json, UserSchema.POSTAL_CODE_KEY);
        if (postalCode != null) {
            account.setPostalCode(postalCode);
        }

        String street = RequestUtil.getFieldValue(json, UserSchema.STREET_KEY);
        if (street != null) {
            account.setStreet(street);
        }

        String locality = RequestUtil.getFieldValue(json, UserSchema.LOCALITY_KEY);
        if (locality != null) {
            account.setLocality(locality);
        }

        String phone = RequestUtil.getFieldValue(json, UserSchema.TELEPHONE_KEY);
        if (phone != null) {
            account.setPhone(phone);
        }

        String facsimile = RequestUtil.getFieldValue(json, UserSchema.FACSIMILE_KEY);
        if (facsimile != null) {
            account.setFacsimile(facsimile);
        }

        String title = RequestUtil.getFieldValue(json, UserSchema.TITLE_KEY);
        if (title != null) {
            account.setTitle(title);
        }

        String description = RequestUtil.getFieldValue(json, UserSchema.DESCRIPTION_KEY);
        if (description != null) {
            account.setDescription(description);
        }

        String manager = RequestUtil.getFieldValue(json, UserSchema.MANAGER_KEY);
        account.setManager(manager);

        String note = RequestUtil.getFieldValue(json, UserSchema.NOTE_KEY);
        if (note != null) {
            account.setNote(note);
        }

        String context = RequestUtil.getFieldValue(json, UserSchema.CONTEXT_KEY);
        if (context != null) {
            account.setContext(context);
        }
        String saslUser = RequestUtil.getFieldValue(json, "saslUser");
        if (saslUser != null) {
            account.setSASLUser(saslUser);
        }

        String commonName = AccountFactory.formatCommonName(account.getGivenName(), account.getSurname());
        account.setCommonName(commonName);

        String uid = RequestUtil.getFieldValue(json, UserSchema.UID_KEY);
        if (uid != null) {
            account.setUid(uid);
        }

        String org = RequestUtil.getFieldValue(json, UserSchema.ORG_KEY);
        if (org != null)
            account.setOrg(org);

        String shadowExpire = RequestUtil.getFieldValue(json, UserSchema.SHADOW_EXPIRE_KEY);
        if (shadowExpire != null) {
            if ("".equals(shadowExpire))
                account.setShadowExpire(null);
            else
                account.setShadowExpire((new SimpleDateFormat("yyyy-MM-dd")).parse(shadowExpire));
        }

        String privacyPolicyAgreementDate = RequestUtil.getFieldValue(json,
                UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY);
        if (privacyPolicyAgreementDate != null) {
            if ("".equals(privacyPolicyAgreementDate))
                account.setPrivacyPolicyAgreementDate(null);
            else
                try {
                    account.setPrivacyPolicyAgreementDate(LocalDate.parse(privacyPolicyAgreementDate));
                } catch (DateTimeParseException e) {
                    LOG.error(e.getMessage());
                    throw new IllegalArgumentException(e);
                }
        }

        try {
            account.setPending(json.getBoolean(UserSchema.PENDING));
        } catch (JSONException e) {
        }

        return account;
    }

    /**
     * Create a new account from the body request.
     */
    private Account createAccountFromRequestBody(ServletInputStream is) throws IllegalArgumentException, IOException {

        JSONObject json;
        try {
            json = new JSONObject(FileUtils.asString(is));
        } catch (JSONException e) {
            LOG.error(e.getMessage());
            throw new IOException(e);
        }

        String givenName = RequestUtil.getFieldValue(json, UserSchema.GIVEN_NAME_KEY);
        String surname = RequestUtil.getFieldValue(json, UserSchema.SURNAME_KEY);
        String email = RequestUtil.getFieldValue(json, UserSchema.MAIL_KEY);
        String postalAddress = RequestUtil.getFieldValue(json, UserSchema.POSTAL_ADDRESS_KEY);
        String postOfficeBox = RequestUtil.getFieldValue(json, UserSchema.POST_OFFICE_BOX_KEY);
        String postalCode = RequestUtil.getFieldValue(json, UserSchema.POSTAL_CODE_KEY);
        String street = RequestUtil.getFieldValue(json, UserSchema.STREET_KEY);
        String locality = RequestUtil.getFieldValue(json, UserSchema.LOCALITY_KEY);
        String phone = RequestUtil.getFieldValue(json, UserSchema.TELEPHONE_KEY);
        String facsimile = RequestUtil.getFieldValue(json, UserSchema.FACSIMILE_KEY);
        String title = RequestUtil.getFieldValue(json, UserSchema.TITLE_KEY);
        String description = RequestUtil.getFieldValue(json, UserSchema.DESCRIPTION_KEY);
        String manager = RequestUtil.getFieldValue(json, UserSchema.MANAGER_KEY);
        String note = RequestUtil.getFieldValue(json, UserSchema.NOTE_KEY);
        String context = RequestUtil.getFieldValue(json, UserSchema.CONTEXT_KEY);
        String org = RequestUtil.getFieldValue(json, UserSchema.ORG_KEY);
        String sshKeys = RequestUtil.getFieldValue(json, UserSchema.SSH_KEY);
        String[] sshKeysA = new String[0];
        String saslUser = RequestUtil.getFieldValue(json, "saslUser");

        if (!StringUtils.isEmpty(sshKeys)) {
            sshKeysA = sshKeys.split("\n"); // TODO what would be the most convenient delimiter ?
        }

        if (givenName == null) {
            throw new IllegalArgumentException("First Name is required");
        }
        if (surname == null) {
            throw new IllegalArgumentException("Last Name is required");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        // Use specified login if not empty
        String uid = RequestUtil.getFieldValue(json, UserSchema.UID_KEY);
        if (!StringUtils.hasLength(uid))
            try {
                uid = createUid(givenName, surname);
            } catch (DataServiceException e) {
                LOG.error(e.getMessage());
                throw new IOException(e);
            }

        String commonName = AccountFactory.formatCommonName(givenName, surname);

        Account a = AccountFactory.createFull(uid, commonName, surname, givenName, email, title, phone, description,
                postalAddress, postalCode, "", postOfficeBox, "", street, locality, facsimile, "", "", "", "", manager,
                note, context, org, sshKeysA, saslUser);

        String shadowExpire = RequestUtil.getFieldValue(json, UserSchema.SHADOW_EXPIRE_KEY);
        if (StringUtils.hasLength(shadowExpire)) {
            try {
                a.setShadowExpire((new SimpleDateFormat("yyyy-MM-dd")).parse(shadowExpire));
            } catch (ParseException e) {
                LOG.error(e.getMessage());
                throw new IllegalArgumentException(e);
            }
        }

        String privacyPolicyAgreementDate = RequestUtil.getFieldValue(json,
                UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY);
        if (StringUtils.hasLength(privacyPolicyAgreementDate)) {
            try {
                a.setPrivacyPolicyAgreementDate(LocalDate.parse(privacyPolicyAgreementDate));
            } catch (DateTimeParseException e) {
                LOG.error(e.getMessage());
                throw new IllegalArgumentException(e);
            }
        }

        return a;
    }

    /**
     * Creates a uid based on the given name and surname
     *
     * @param givenName
     * @param surname
     * @return return the proposed uid
     *
     * @throws DataServiceException
     */
    private String createUid(String givenName, String surname) throws DataServiceException {

        String proposedUid = normalizeString(givenName.toLowerCase().charAt(0) + surname.toLowerCase());

        if (!this.accountDao.exist(proposedUid)) {
            return proposedUid;
        } else {
            return this.accountDao.generateUid(proposedUid);
        }
    }

    /**
     * Check Authorization of current logged user against specified uid and throw a
     * AccessDeniedException if current user is not SUPERUSER and user 'uid' is not
     * under the delegation.
     *
     * @param uid Identifier of user to search in delegation of connected user
     *
     * @throws AccessDeniedException if current user does not have permission to
     *                               edit user 'uid'
     */
    private void checkAuthorization(String uid) {
        // check if user is under delegation for delegated admins
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(AdvancedDelegationDao.ROLE_SUPERUSER))
            if (!this.advancedDelegationDao.findUsersUnderDelegation(auth.getName()).contains(uid))
                throw new AccessDeniedException("User " + uid + " not under delegation");
    }

    /**
     * Deaccentuate a string and remove non-word characters
     *
     * references: http://stackoverflow.com/a/8523728 and
     * http://stackoverflow.com/a/2397830
     *
     * @param string an accentuated string, eg. "Joá+o"
     * @return return the deaccentuated string, eg. "Joao"
     */
    public static String normalizeString(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\\W", "");
    }
}
