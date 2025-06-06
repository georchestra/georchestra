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

package org.georchestra.console.ws.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Validation class for user and org forms
 *
 * Possible values: *
 *
 * There are hardcoded mandatory fields for user and organizations creation:
 *
 * mandatory user fields: * email * uid * password * confirmPassword
 *
 * mandatory org fields: * name
 *
 */
public class Validation {

    private Set<String> requiredUserFields;
    private Set<String> requiredOrgFields;

    /**
     * Create a validation class with field list formated as comma separated list.
     * List can contains spaces.
     *
     * @param requiredFields comma separated list of required fields (ex: "surname,
     *                       orgType, orgAddress")
     */
    public Validation(String requiredFields) {

        String[] configuredFields = requiredFields.split("\\s*,\\s*");
        this.requiredUserFields = new HashSet<String>();
        this.requiredOrgFields = new HashSet<String>();

        // Add mandatory fields for user
        this.requiredUserFields.add("email");
        this.requiredUserFields.add("newEmail");
        this.requiredUserFields.add("uid");
        this.requiredUserFields.add("password");
        this.requiredUserFields.add("confirmPassword");

        // Add mandatory field for org
        this.requiredOrgFields.add("name");
        this.requiredOrgFields.add("shortName");
        this.requiredOrgFields.add("type");

        // Extract all fields starting by Org and change next letter to lower case
        // orgShortName --> shortName
        Pattern regexp = Pattern.compile("^org([A-Z].*)$");
        for (String field : configuredFields) {
            field = field.trim();
            if (field.length() == 0)
                continue;
            Matcher m = regexp.matcher(field);
            if (m.matches()) {
                // This is a org field, so remove 'org' prefix
                String match = m.group(1);
                match = match.substring(0, 1).toLowerCase() + match.substring(1);
                this.requiredOrgFields.add(match);
            } else {
                // This is a user field
                this.requiredUserFields.add(field);
            }
        }
    }

    /**
     * Return a set of required fields for user creation or update
     *
     * @return a Set that contains all required fields for user forms.
     */
    public Set<String> getRequiredUserFields() {
        return this.requiredUserFields;
    }

    /**
     * Return a set of required fields for organization creation or update
     *
     * @return a Set that contains all required fields for organization forms.
     */
    public Set<String> getRequiredOrgFields() {
        return this.requiredOrgFields;
    }

    /**
     * Return true if specified field is required for user creation or update
     *
     * @param field field to check requirement
     * @return true id 'field' is required for user forms
     */
    public boolean isUserFieldRequired(String field) {
        return this.requiredUserFields.contains(field);
    }

    /**
     * Return true if specified field is required for organization creation or
     * update
     *
     * @param field field to check requirement
     * @return true id 'field' is required for organization forms
     */
    public boolean isOrgFieldRequired(String field) {
        return this.requiredOrgFields.contains(field);
    }

    public void validateUserField(String field, String value, Errors errors) {
        if (!validateUserField(field, value))
            errors.rejectValue(field, "error.required", "required");
    }

    public boolean validateUserFieldWithSpecificMsg(String field, String value, Errors errors) {
        if (!validateUserField(field, value)) {
            errors.rejectValue(field, String.format("%s.error.required", field), "required");
            return false;
        }
        return true;
    }

    protected boolean validateUserField(String field, String value) {
        return !this.isUserFieldRequired(field) || StringUtils.hasLength(value);
    }

    public void validateAgreedField(boolean value, Errors errors, String rejectValue) {
        if (!value)
            errors.rejectValue(rejectValue, "error.required", "required");
    }

    public boolean validateOrgField(String field, JSONObject json) {
        try {
            return !this.isOrgFieldRequired(field) || (json.has(field) && StringUtils.hasLength(json.getString(field)));
        } catch (JSONException e) {
            return false;
        }
    }

    public void validateOrgField(String field, String value, Errors errors) {
        if (!validateOrgField(field, value)) {
            errors.rejectValue(String.format("org%s", StringUtils.capitalize(field)), "error.required", "required");
        }
    }

    public boolean validateOrgField(String field, String value) {
        return !this.isOrgFieldRequired(field) || StringUtils.hasLength(value);
    }

    public boolean validateUrl(String value) {
        if (value == null || value.length() == 0) {
            return true;
        }
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean validateUrlFieldWithSpecificMsg(String fullyQualifiedField, String value, Errors errors) {
        if (!validateUrl(value)) {
            errors.rejectValue(fullyQualifiedField, "error.badUrl", "badUrl");
            return false;
        }
        return true;
    }

    public boolean validateOrgUnicityByUniqueId(OrgsDao orgDao, JSONObject changes) {
        String orgUniqueId = "";
        String uuid = "";

        if (changes == null) {
            return true;
        }
        // case - not value to verify
        if (!changes.has("orgUniqueId")) {
            return true;
        }

        orgUniqueId = changes.getString("orgUniqueId");

        if (orgUniqueId == null || orgUniqueId.isEmpty()) {
            return true;
        }

        // test if org with same value already exists
        Org findByOrgUniqueId = orgDao.findByOrgUniqueId(orgUniqueId);
        // No org exists with this field value
        if (findByOrgUniqueId == null) {
            return true;
        }

        // No uuid to compare means that's an org creation
        // and at this step, an org already exists with this orgUniqueId
        if (!changes.has("uuid")) {
            // we can't validate this orgUniqueId that already exists
            return false;
        }

        uuid = changes.getString("uuid");

        if (uuid.isEmpty() || uuid == null) {
            return false;
        }
        // Control if this is same org update
        return Objects.equals(findByOrgUniqueId.getUniqueIdentifier(), UUID.fromString(uuid));
    }

    public boolean validateOrgUniqueIdField(OrgsDao orgDao, JSONObject changes, Errors errors) {
        Boolean isValid = true;
        if (changes.has("orgUniqueId") && !this.validateOrgUnicityByUniqueId(orgDao, changes)) {
            errors.rejectValue("orgUniqueId", "error.orgUniqueIdExists", "orgUniqueIdExists");
            isValid = false;
        }
        return isValid;
    }

    public boolean validateOrgUnicity(OrgsDao orgDao, JSONObject changes) {
        Boolean isValid = true;
        if (changes.has("orgUniqueId") && !this.validateOrgUnicityByUniqueId(orgDao, changes)) {
            isValid = false;
        }
        return isValid;
    }
}
