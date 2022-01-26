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

package org.georchestra.security;

import java.util.Collections;

import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Setter;

/**
 * Adds a {@code sec-organization} header with the full authenticated user's
 * {@link Organization} obtained from {@link OrganizationsApi} as a Base64
 * encoded JSON representation.
 * <p>
 * Note however, that the encoded {@code Organization} has no
 * {@link Organization#getMembers() members}, to avoid potential HTTP header
 * max-length overflow on large geOrchestra installations.
 * <p>
 * If an application really needs to access an organization's members list, it
 * can always use the {@link OrganizationsApi} directly instead.
 * <p>
 * This header provider is disabled by default, and reads which target request
 * services to add the header for from {@code headers-mapping.properties} just
 * like {@link LdapUserDetailsRequestHeaderProvider}.
 * <p>
 * Adding the {@code sec-organization} header by default to all services can be
 * enabled in through {@code send-json-sec-organization=true}.
 * <p>
 * Individual service names for which to add the {@code sec-organization}
 * request header must be configured as
 * {@code <service-name>.send-json-sec-organization=true}. For example:
 * 
 * <pre>
 * <code>
 * geonetwork.send-json-sec-organization=true
 * datafeeder.send-json-sec-organization=true 
 * </code>
 * </pre>
 * 
 * If this header provider is enabled globally, though, it can be disabled for
 * any specific service by setting the service specific config property to
 * {@code false}, as in {@code <service-name>.send-json-sec-organization=false}.
 * 
 * @see UserDetailsJSONRequestHeaderProvider
 */
public class UserOrganizationJSONRequestHeaderProvider extends JSONRequestHeaderProvider {

    static final String CONFIG_PROPERTY = "send-json-sec-organization";
    private static final String HEADER_NAME = "sec-organization";

    @Autowired
    @Setter(value = AccessLevel.PACKAGE)
    private UsersApi users;

    @Autowired
    @Setter(value = AccessLevel.PACKAGE)
    private OrganizationsApi orgs;

    public UserOrganizationJSONRequestHeaderProvider() {
        super(CONFIG_PROPERTY, HEADER_NAME);
    }

    protected @Override Organization getPayloadObject(String userName) {
        GeorchestraUser user = this.users.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found:" + userName));
        String orgShortName = user.getOrganization();
        if (!StringUtils.hasLength(orgShortName)) {
            return null;
        }
        Organization org = this.orgs.findByShortName(orgShortName)
                .orElseThrow(() -> new IllegalArgumentException("Org not found: " + orgShortName));

        return org.withMembers(Collections.emptyList());
    }
}
