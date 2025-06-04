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

package org.georchestra.ds;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;

@Accessors(chain = true)
public class LdapDaoProperties {

    @Getter
    @Setter
    String basePath;

    @Getter
    @Setter
    String roleSearchBaseDN;

    @Getter
    @Setter
    String orgSearchBaseDN;

    @Getter
    @Setter
    String pendingOrgSearchBaseDN;

    @Getter
    @Setter
    String[] orgTypeValues;

    @Getter
    LdapName userSearchBaseDN;

    @Getter
    LdapName pendingUserSearchBaseDN;

    public LdapDaoProperties setOrgTypeValues(String orgTypeValues) {
        this.orgTypeValues = orgTypeValues.split("\\s*,\\s*");
        return this;
    }

    public LdapDaoProperties setUserSearchBaseDN(String userSearchBaseDN) {
        this.userSearchBaseDN = LdapNameBuilder.newInstance(userSearchBaseDN).build();
        return this;
    }

    public LdapDaoProperties setPendingUserSearchBaseDN(String pendingUserSearchBaseDN) {
        this.pendingUserSearchBaseDN = LdapNameBuilder.newInstance(pendingUserSearchBaseDN).build();
        return this;
    }
}
