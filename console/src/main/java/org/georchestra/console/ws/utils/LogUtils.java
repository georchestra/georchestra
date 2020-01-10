package org.georchestra.console.ws.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.UserSchema;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.backoffice.roles.RoleProtected;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogUtils {
    @Autowired
    private AdminLogDao logDao;

    @Autowired
    private RoleProtected roles;

    public void setLogDao(AdminLogDao logDao) {
        this.logDao = logDao;
    }

    private static final Log LOG = LogFactory.getLog(LogUtils.class.getName());

    /**
     * Create log to save and display.
     * 
     * @target String to identify org's
     * @type type AdminLogType of log event
     * @param values String that represent changed attributes
     */
    public AdminLogEntry createLog(String target, AdminLogType type, String values) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AdminLogEntry log = new AdminLogEntry();

        if (auth != null && auth.getName() != null && target != null) {
            String admin = auth.getName();
            // case where we don't need to log changes
            if (values == null || values.isEmpty()) {
                log = new AdminLogEntry(admin, target, type, new Date());
            } else {
                log = new AdminLogEntry(admin, target, type, new Date(), values);
            }
            if (logDao != null) {
                try {
                    logDao.save(log);
                } catch (DataIntegrityViolationException divex) {
                    // Value could be to large for field size
                    LOG.error("Could not save changed values for admin log, reset value : " + values, divex);
                    JSONObject errorsjson = new JSONObject();
                    errorsjson.put("error",
                            "Error while inserting admin log in database, see admin log file for more information");
                    log.setChanged(errorsjson.toString());
                    logDao.save(log);
                }

            }
        } else {
            LOG.info("Authentification Security Context is null.");
            log = null;
        }
        return log;
    }

    /**
     * Return JSONObject from informations to be save as log detail into database.
     * This allow to modify details before create log.
     * 
     * @param attributeName String
     * @param oldValue      String
     * @param newValue      String
     * @param type          AdminLogType
     * @return JSONObject
     */
    public JSONObject getLogDetails(String attributeName, String oldValue, String newValue, AdminLogType type) {
        JSONObject details = new JSONObject();
        details.put("field", attributeName != null ? attributeName : "");
        details.put("old", oldValue != null ? oldValue : "");
        details.put("new", newValue != null ? newValue : "");
        details.put("type", type != null ? type.toString() : "");
        return details;
    }

    /**
     * Full creation by log details creation and log creation directly.
     * 
     * @param target        String
     * @param attributeName String
     * @param oldValue      String
     * @param newValue      String
     * @param type          AdminLogType that could be any type of log
     */
    public void createAndLogDetails(String target, String attributeName, String oldValue, String newValue,
            AdminLogType type) {
        JSONObject details = getLogDetails(attributeName, oldValue, newValue, type);
        createLog(target, type, details.toString());
    }

    /**
     * Parse a list of accounts to log when a role was added to a user
     * 
     * @param roleId String role uid
     * @param users  List<Account>
     * @param admin  String as user's uid that realized modification
     * @param type   AdminLogType that could be any type of log
     */
    private void parseUsers(List<Account> users, AdminLogType type, JSONObject details) {
        if (users != null && !users.isEmpty()) {
            for (Account user : users) {
                // Add log entry when role was removed
                if (type != null && user.getUid() != null && details.length() > 0) {
                    createLog(user.getUid(), type, details.toString());
                }
            }
        }
    }

    /**
     * Parse a list of roles to log when a role was added to a user. This methods
     * identify custom and system roles
     * 
     * @param roles  String role as uid
     * @param users  List<Account>
     * @param admin  String as user's uid that realized modification
     * @param action boolean to identify if a role was removed (false) or added
     *               (true)
     */
    private void parseRoles(List<String> roles, List<Account> users, boolean action) {
        AdminLogType type;
        if (roles != null && !roles.isEmpty()) {
            // parse roles
            for (String roleName : roles) {
                // log details
                JSONObject details = new JSONObject();
                // get log details
                if (!this.roles.isProtected(roleName)) {
                    type = action ? AdminLogType.CUSTOM_ROLE_ADDED : AdminLogType.CUSTOM_ROLE_REMOVED;
                    details = getLogDetails(roleName, null, roleName, type);
                } else {
                    type = action ? AdminLogType.SYSTEM_ROLE_ADDED : AdminLogType.SYSTEM_ROLE_REMOVED;
                    details = getLogDetails(roleName, roleName, null, type);
                }
                details.put("isRole", true);
                parseUsers(users, type, details);
            }
        }
    }

    /**
     * Parse a list of roles to log for each users if a role was added or removed
     * 
     * @param roles  String role uid
     * @param users  List<Account>
     * @param admin  String as user's uid that realized modification
     * @param action boolean to identify if a role was removed (false) or added
     *               (true)
     */
    public void logRolesUsersAction(List<String> putRole, List<String> deleteRole, List<Account> users) {
        // role is added to users
        if (putRole != null && !putRole.isEmpty()) {
            parseRoles(putRole, users, true);
        } else if (deleteRole != null && !deleteRole.isEmpty()) {
            // role is removed for users
            parseRoles(deleteRole, users, false);
        }
    }

    /**
     * Log org update found in json object.
     * 
     * @param org  Org instance to update
     * @param json Json document to take information from
     * @throws JSONException If something went wrong during information extraction
     *                       from json document
     */
    public void logOrgChanged(Org org, JSONObject json) throws IOException {
        final int MAX_CITIES = 32;
        String id = json.optString(Org.JSON_ID);
        // log name changed
        if (!org.getName().equals(json.optString(Org.JSON_NAME))) {
            createAndLogDetails(id, Org.JSON_NAME, org.getName(), json.optString(Org.JSON_NAME),
                    AdminLogType.ORG_ATTRIBUTE_CHANGED);
        }
        // log short name changed
        if (!org.getShortName().equals(json.optString(Org.JSON_SHORT_NAME))) {
            createAndLogDetails(json.optString(Org.JSON_SHORT_NAME), Org.JSON_SHORT_NAME, org.getShortName(),
                    json.optString(Org.JSON_ID), AdminLogType.ORG_ATTRIBUTE_CHANGED);
        }

        // get area differences between old and new list
        // Make sure Cities exist
        int rmLen = 0;
        int addLen = 0;
        List<String> removed = new ArrayList<>();
        List<Object> added = new ArrayList<>();

        if (org.getCities() != null && json.optJSONArray(Org.JSON_CITIES) != null) {
            removed = org.getCities().stream().filter(p -> !json.optJSONArray(Org.JSON_CITIES).toList().contains(p))
                    .collect(Collectors.toList());
            rmLen = removed.size();
            added = json.optJSONArray(Org.JSON_CITIES).toList().stream().filter(p -> !org.getCities().contains(p))
                    .collect(Collectors.toList());
            addLen = added.size();
        }

        // create log
        if (rmLen > 0 || addLen > 0) {
            String oldCities = rmLen > 0 && rmLen < MAX_CITIES ? removed.toString() : "";
            String newCities = addLen > 0 && addLen < MAX_CITIES ? added.toString() : "";
            JSONObject details = getLogDetails(Org.JSON_CITIES, oldCities, newCities,
                    AdminLogType.ORG_ATTRIBUTE_CHANGED);
            details.put("added", addLen);
            details.put("removed", rmLen);
            createLog(id, AdminLogType.ORG_ATTRIBUTE_CHANGED, details.toString());
        }
    }

    /**
     * Log update orgExt from json object.
     *
     * @param orgExt OrgExt instance to update
     * @param json   Json document to take information from
     * @throws JSONException If something went wrong during information extraction
     *                       from json document
     */
    public void logOrgExtChanged(OrgExt orgExt, JSONObject json) {
        String orgId = orgExt.getId();
        AdminLogType type = AdminLogType.ORG_ATTRIBUTE_CHANGED;
        // log orgType changed
        if (!orgExt.getOrgType().equals(json.optString(OrgExt.JSON_ORG_TYPE))) {
            createAndLogDetails(orgId, OrgExt.JSON_ORG_TYPE, orgExt.getOrgType(), json.optString(OrgExt.JSON_ORG_TYPE),
                    type);
        }

        if (!orgExt.getAddress().equals(json.optString(OrgExt.JSON_ADDRESS))) {
            createAndLogDetails(orgId, OrgExt.JSON_ADDRESS, orgExt.getAddress(), json.optString(OrgExt.JSON_ADDRESS),
                    type);
        }
        // log description changed
        if (!orgExt.getDescription().equals(json.optString(Org.JSON_DESCRIPTION))) {
            createAndLogDetails(orgId, Org.JSON_DESCRIPTION, orgExt.getDescription(),
                    json.optString(Org.JSON_DESCRIPTION), type);
        }
        // log web site url changed
        if (!orgExt.getUrl().equals(json.optString(Org.JSON_URL))) {
            createAndLogDetails(orgId, Org.JSON_URL, orgExt.getUrl(), json.optString(Org.JSON_URL), type);
        }
        // log logo changed
        if (!orgExt.getLogo().equals(json.get("logo"))) {
            createAndLogDetails(orgId, Org.JSON_LOGO, null, null, type);
        }
    }

    /**
     * Compare each attributes between original and modified account Log changed as
     * JSON For each modification from body request
     * 
     * @param modified Account
     * @param original Account before change
     */
    public void logChanges(Account modified, Account original) {
        String target = modified.getUid();
        final AdminLogType type = AdminLogType.USER_ATTRIBUTE_CHANGED;

        if (modified.getDescription() != null && !modified.getDescription().equals(original.getDescription())) {
            // log description changed
            createAndLogDetails(target, UserSchema.DESCRIPTION_KEY, original.getDescription(),
                    modified.getDescription(), type);
        }
        if (modified.getUid() != null && !modified.getUid().equals(original.getUid())) {
            // log uid changed
            createAndLogDetails(target, UserSchema.UID_KEY, original.getUid(), modified.getUid(), type);
        }
        if (modified.getCommonName() != null && !modified.getCommonName().equals(original.getCommonName())) {
            // log CN changed
            createAndLogDetails(target, UserSchema.COMMON_NAME_KEY, original.getCommonName(), modified.getCommonName(),
                    type);
        }
        if (modified.getSurname() != null && !modified.getSurname().equals(original.getSurname())) {
            // log SN changed
            createAndLogDetails(target, UserSchema.SURNAME_KEY, original.getSurname(), modified.getSurname(), type);
        }
        if (modified.getEmail() != null && !modified.getEmail().equals(original.getEmail())) {
            // log email changed
            createAndLogDetails(target, UserSchema.MAIL_KEY, original.getEmail(), modified.getEmail(), type);
        }
        if (modified.getOrg() != null && !modified.getOrg().equals(original.getOrg())) {
            // log org changed
            createAndLogDetails(target, UserSchema.ORG_KEY, original.getOrg(), modified.getOrg(), type);
        }
        if (modified.getPhone() != null && !modified.getPhone().equals(original.getPhone())) {
            // log phone changed
            createAndLogDetails(target, UserSchema.TELEPHONE_KEY, original.getPhone(), modified.getPhone(), type);
        }
        if (modified.getPostalAddress() != null && !modified.getPostalAddress().equals(original.getPostalAddress())) {
            // log postal adress changed
            createAndLogDetails(target, UserSchema.POSTAL_ADDRESS_KEY, original.getPostalAddress(),
                    modified.getPostalAddress(), type);
        }
        if (modified.getGivenName() != null && !modified.getGivenName().equals(original.getGivenName())) {
            // log GN changed
            createAndLogDetails(target, UserSchema.GIVEN_NAME_KEY, original.getGivenName(), modified.getGivenName(),
                    type);
        }
        if (modified.getTitle() != null && !modified.getTitle().equals(original.getTitle())) {
            // log title changed
            createAndLogDetails(target, UserSchema.TITLE_KEY, original.getTitle(), modified.getTitle(), type);
        }
        if (modified.getPostOfficeBox() != null && !modified.getPostOfficeBox().equals(original.getPostOfficeBox())) {
            // log post office changed
            createAndLogDetails(target, UserSchema.POST_OFFICE_BOX_KEY, original.getPostOfficeBox(),
                    modified.getPostOfficeBox(), type);
        }
        if (modified.getStreet() != null && !modified.getStreet().equals(original.getStreet())) {
            // log street changed
            createAndLogDetails(target, UserSchema.STREET_KEY, original.getStreet(), modified.getStreet(), type);
        }
        if (modified.getLocality() != null && !modified.getLocality().equals(original.getLocality())) {
            // log L changed
            createAndLogDetails(target, UserSchema.LOCALITY_KEY, original.getLocality(), modified.getLocality(), type);
        }
        if (modified.getFacsimile() != null && !modified.getFacsimile().equals(original.getFacsimile())) {
            // log fax changed
            createAndLogDetails(target, UserSchema.FACSIMILE_KEY, original.getFacsimile(), modified.getFacsimile(),
                    type);
        }

        if (modified.getPostalCode() != null && !modified.getPostalCode().equals(original.getPostalCode())) {
            // log postal code changed
            createAndLogDetails(target, UserSchema.POSTAL_CODE_KEY, original.getPostalCode(), modified.getPostalCode(),
                    type);
        }
        if (modified.getContext() != null && !modified.getContext().equals(original.getContext())) {
            // log context changed
            createAndLogDetails(target, UserSchema.CONTEXT_KEY, original.getContext(), modified.getContext(), type);
        }

        // special cases when the attribute changed to get null value
        String oldValue;
        String newValue;
        if (modified.getShadowExpire() == null || original.getShadowExpire() == null) {
            // log shadow expire changed
            oldValue = original.getShadowExpire() != null ? original.getShadowExpire().toString() : "";
            newValue = modified.getShadowExpire() != null ? modified.getShadowExpire().toString() : "";
            if (!newValue.equals(oldValue)) {
                createAndLogDetails(target, UserSchema.SHADOW_EXPIRE_KEY, oldValue, newValue, type);
            }
        }
        if (modified.getPrivacyPolicyAgreementDate() == null || original.getPrivacyPolicyAgreementDate() == null) {
            // log privacy policy agreement date changed
            oldValue = original.getPrivacyPolicyAgreementDate() != null
                    ? original.getPrivacyPolicyAgreementDate().toString()
                    : "";
            newValue = modified.getPrivacyPolicyAgreementDate() != null
                    ? modified.getPrivacyPolicyAgreementDate().toString()
                    : "";
            if (!newValue.equals(oldValue)) {
                createAndLogDetails(target, UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY, oldValue, newValue, type);
            }
        }
        if (modified.getManager() == null || original.getManager() == null) {
            // log privacy policy agreement date changed
            oldValue = original.getManager() != null ? original.getManager().toString() : "";
            newValue = modified.getManager() != null ? modified.getManager().toString() : "";
            if (!newValue.equals(oldValue)) {
                createAndLogDetails(target, UserSchema.MANAGER_KEY, oldValue, newValue, type);
            }
        }
    }
}
