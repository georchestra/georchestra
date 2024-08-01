/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.ds.orgs;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Extended organization properties, used internally as a composition
 * relationship for {@link Org} to handle non standard LDAP org properties.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
class OrgExt extends ReferenceAware implements Cloneable {

    private UUID uniqueIdentifier;
    private String id;
    private String orgType = null;
    // these attribute default values are the empty string to match how they're
    // mapped to the ldap context and keep equals and hashCode consistency
    private String address = "";
    private String description = "";
    private String url = "";
    private String logo = "";
    private String note = "";
    private String mail = "";

    @Override
    public OrgExt clone() {
        try {
            return (OrgExt) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
