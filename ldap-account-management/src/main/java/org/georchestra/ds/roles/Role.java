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

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a Role stored in the LDAP tree.
 *
 * @author Mauricio Pazos
 */
public interface Role extends Comparable<Role> {

    final String USER = "USER";

    /**
     * @param uniqueId the immutable unique identifier for this role
     */
    void setUniqueIdentifier(UUID uniqueId);

    /**
     * @return the immutable unique identifier for this role
     */
    UUID getUniqueIdentifier();

    /**
     * @return the name of this role
     */
    @JsonProperty("cn")
    String getName();

    void setName(String cn);

    /**
     * Users of this role
     *
     * @return the list of user
     */
    @JsonProperty("users")
    List<String> getUserList();

    @JsonProperty("orgs")
    List<String> getOrgList();

    void setUserList(List<String> userUidList);

    /**
     * adds a user to this role
     *
     * @param userUid a user dn
     */
    void addUser(String userUid);

    void addOrg(String orgUid);

    void addMembers(String[] members);

    void setDescription(String description);

    String getDescription();

    void setFavorite(boolean isFavorite);

    @JsonProperty("isFavorite")
    boolean isFavorite();

    boolean isPending();

    void setPending(boolean pending);

}
