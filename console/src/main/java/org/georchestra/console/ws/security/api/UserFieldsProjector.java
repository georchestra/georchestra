/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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
package org.georchestra.console.ws.security.api;

import org.georchestra.security.model.GeorchestraUser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to project GeorchestraUser fields dynamically. This avoids
 * modifying the commons module while allowing field selection.
 */
public class UserFieldsProjector {

    private static final Set<String> VALID_FIELDS = Set.of("id", "username", "firstName", "lastName", "email",
            "organization", "roles", "lastUpdated", "postalAddress", "telephoneNumber", "title", "notes", "ldapWarn",
            "ldapRemainingDays", "oAuth2Provider", "oAuth2Uid", "oAuth2OrgId", "isExternalAuth");

    private static final Map<String, FieldGetter> FIELD_GETTERS = new HashMap<>();

    static {
        FIELD_GETTERS.put("id", GeorchestraUser::getId);
        FIELD_GETTERS.put("username", GeorchestraUser::getUsername);
        FIELD_GETTERS.put("firstName", GeorchestraUser::getFirstName);
        FIELD_GETTERS.put("lastName", GeorchestraUser::getLastName);
        FIELD_GETTERS.put("email", GeorchestraUser::getEmail);
        FIELD_GETTERS.put("organization", GeorchestraUser::getOrganization);
        FIELD_GETTERS.put("roles", GeorchestraUser::getRoles);
        FIELD_GETTERS.put("lastUpdated", GeorchestraUser::getLastUpdated);
        FIELD_GETTERS.put("postalAddress", GeorchestraUser::getPostalAddress);
        FIELD_GETTERS.put("telephoneNumber", GeorchestraUser::getTelephoneNumber);
        FIELD_GETTERS.put("title", GeorchestraUser::getTitle);
        FIELD_GETTERS.put("notes", GeorchestraUser::getNotes);
        FIELD_GETTERS.put("ldapWarn", GeorchestraUser::getLdapWarn);
        FIELD_GETTERS.put("ldapRemainingDays", GeorchestraUser::getLdapRemainingDays);
        FIELD_GETTERS.put("oAuth2Provider", GeorchestraUser::getOAuth2Provider);
        FIELD_GETTERS.put("oAuth2Uid", GeorchestraUser::getOAuth2Uid);
        FIELD_GETTERS.put("oAuth2OrgId", GeorchestraUser::getOAuth2OrgId);
        FIELD_GETTERS.put("isExternalAuth", GeorchestraUser::getIsExternalAuth);
    }

    @FunctionalInterface
    private interface FieldGetter {
        Object get(GeorchestraUser user);
    }

    /**
     * Projects a user to a Map containing only the requested fields.
     *
     * @param user   the user to project
     * @param fields the fields to include (if null or empty, all fields are
     *               included)
     * @return a Map with field names as keys and field values as values
     */
    public static Map<String, Object> project(GeorchestraUser user, List<String> fields) {
        Set<String> requestedFields = (fields == null || fields.isEmpty()) ? VALID_FIELDS
                : fields.stream().filter(VALID_FIELDS::contains).collect(Collectors.toSet());

        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : requestedFields) {
            FieldGetter getter = FIELD_GETTERS.get(field);
            if (getter != null) {
                result.put(field, getter.get(user));
            }
        }
        return result;
    }

    /**
     * Projects a list of users to a list of Maps containing only the requested
     * fields.
     *
     * @param users  the users to project
     * @param fields the fields to include (if null or empty, all fields are
     *               included)
     * @return a list of Maps with projected fields
     */
    public static List<Map<String, Object>> projectAll(List<GeorchestraUser> users, List<String> fields) {
        return users.stream().map(user -> project(user, fields)).collect(Collectors.toList());
    }
}
