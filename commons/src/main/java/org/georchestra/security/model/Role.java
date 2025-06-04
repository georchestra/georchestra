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

package org.georchestra.security.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public @Data class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Uniquely identifies a role. Format could be UUID, but it's at the discretion
     * of the provider
     */
    private String id;
    /**
     * Role name, provides a unique identity for the role, but can mutate over time
     */
    private String name;
    /**
     * Role intended purpose
     */
    private String description;

    /**
     * String that somehow represents the current version, may be a timestamp, a
     * hash, etc. Provided by request header {@code sec-lastupdated}
     */
    private String lastUpdated;

    /**
     * List of {@link GeorchestraUser#getUsername() user names} that belong to this
     * role
     */
    private List<String> members = new ArrayList<>();

    public void setMembers(List<String> members) {
        this.members = members == null ? new ArrayList<>() : members;
    }

}
