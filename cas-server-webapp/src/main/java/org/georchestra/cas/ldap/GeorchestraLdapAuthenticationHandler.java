/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.cas.ldap;

import java.security.GeneralSecurityException;
import java.util.Collection;

import javax.security.auth.login.AccountException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.LdapAuthenticationHandler;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.ldaptive.BindRequest;
import org.ldaptive.Connection;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.auth.Authenticator;

/**
 * Extends Ldap authentication handler by checking whether the user is pending or valid.
 *
 * @author Jesse on 6/26/2014.
 */
public class GeorchestraLdapAuthenticationHandler extends LdapAuthenticationHandler {

    private String adminUser;
    private String adminPassword;
    private String baseDn;
    private String roleSearchFilter;
    private String roleRoleAttribute;
    private String pendingRoleName;

    private DefaultConnectionFactory connectionFactory;

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public void setRoleSearchFilter(String roleSearchFilter) {
        this.roleSearchFilter = roleSearchFilter;
    }

    public void setRoleRoleAttribute(String roleRoleAttribute) {
        this.roleRoleAttribute = roleRoleAttribute;
    }

    public void setPendingRoleName(String pendingRoleName) {
        this.pendingRoleName = pendingRoleName;
    }
    /**
     * Creates a new authentication handler that delegates to the given authenticator.
     *
     * @param authenticator Ldaptive authenticator component.
     */
    public GeorchestraLdapAuthenticationHandler(@NotNull Authenticator authenticator,
                                                @NotNull String adminUser,
                                                @NotNull String adminPassword,
                                                @NotNull String baseDn,
                                                @NotNull String roleSearchFilter,
                                                @NotNull String roleRoleAttribute,
                                                @NotNull String pendingRoleName) {
        super(authenticator);
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.baseDn = baseDn;
        this.roleSearchFilter = roleSearchFilter;
        this.roleRoleAttribute = roleRoleAttribute;
        this.pendingRoleName = pendingRoleName;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential upc)
            throws GeneralSecurityException, PreventedException {
        final HandlerResult handlerResult = super.authenticateUsernamePasswordInternal(upc);

        final Connection conn = this.connectionFactory.getConnection();
        try {
            BindRequest bindRequest = new BindRequest(adminUser, new Credential(adminPassword));
            conn.open(bindRequest);

            SearchOperation search = new SearchOperation(conn);
            final String searchFilter = this.roleSearchFilter.replace("{1}", upc.getUsername());
            SearchResult result = search.execute(
                    new SearchRequest(this.baseDn, searchFilter, this.roleRoleAttribute)).getResult();

            if (result.getEntries().isEmpty()) {
                throw new AccountException("User has no roles.");
            }
            for (LdapEntry entry : result.getEntries()) {
                final Collection<String> roleNames = entry.getAttribute(this.roleRoleAttribute).getStringValues();
                for (String name : roleNames) {
                    if (name.equals(this.pendingRoleName)) {
                        throw new AccountException("User is still a pending user.");
                    }
                }
            }
        } catch (LdapException e) {
            throw new PreventedException("Unexpected LDAP error", e);
        } finally {
            conn.close();
        }

        return handlerResult;
    }

    public void setConnectionFactory(DefaultConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
