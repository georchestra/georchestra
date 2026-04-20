/*
 * Copyright (C) 2009-2026 by the geOrchestra PSC
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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch user retrieval with optional field projection.
 * <p>
 * Example request body:
 *
 * <pre>
 * {
 *   "usernames": ["testadmin", "testuser"],
 *   "fields": ["firstName", "lastName", "email"]
 * }
 * </pre>
 *
 * If {@code fields} is null or empty, all fields are returned.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBatchRequest {

    /**
     * List of user IDs to retrieve.
     */
    private List<String> usernames;

    /**
     * Optional list of fields to include in the response. If null or empty, all
     * fields are returned. Valid field names: id, username, firstName, lastName,
     * email, organization, roles, lastUpdated, postalAddress, telephoneNumber,
     * title, notes, isExternalAuth
     */
    private List<String> fields;
}
