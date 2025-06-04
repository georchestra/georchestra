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

package org.georchestra.ds.roles;

/**
 * Defines the name of the role's fields. They are consistent with the LDAP
 * specification.
 *
 * @author Mauricio Pazos
 *
 */
public interface RoleSchema {

    String UUID_KEY = "georchestraObjectIdentifier";
    String COMMON_NAME_KEY = "cn"; // role name
    String DESCRIPTION_KEY = "description";
    String MEMBER_KEY = "member";
    String FAVORITE_KEY = "businessCategory";
    String FAVORITE_VALUE = "favorite";
    String FAVORITE_JSON_KEY = "isFavorite";

}
