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

import org.georchestra.ds.orgs.OrgExtLdapWrapper;
import org.georchestra.ds.orgs.OrgLdapWrapper;
import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.roles.RoleDaoImpl;
import org.georchestra.ds.users.AccountDaoImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

@Configuration
public class LdapDaoConfiguration {

    @Bean
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public RoleDaoImpl roleDao() {
        return new RoleDaoImpl();

    }

    @Bean
    public OrgsDaoImpl orgsDao() {
        return new OrgsDaoImpl();
    }

    @Bean
    public OrgLdapWrapper orgLdapWrapper() {
        return new OrgLdapWrapper();
    }

    @Bean
    public OrgExtLdapWrapper orgExtLdapWrapper() {
        return new OrgExtLdapWrapper();
    }

    @Bean
    public AccountDaoImpl accountDao(LdapTemplate ldapTemplate) {
        return new AccountDaoImpl(ldapTemplate);
    }

}
