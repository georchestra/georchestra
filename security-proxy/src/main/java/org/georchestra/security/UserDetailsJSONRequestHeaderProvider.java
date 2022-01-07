/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.AccessLevel;
import lombok.Setter;

/**
 * Adds a {@code sec-user} header with the full {@link GeorchestraUser} obtained
 * from {@link UsersApi} as a Base64 encoded JSON representation.
 * <p>
 * This header provider is disabled by default, and reads which target request
 * services to add the header for from {@code headers-mapping.properties} just
 * like {@link LdapUserDetailsRequestHeaderProvider}.
 * <p>
 * Adding the {@code sec-user} header by default to all services can be enabled
 * in through {@code send-json-sec-user=true}.
 * <p>
 * Individual service names for which to add the {@code sec-user} request header
 * must be configured as {@code <service-name>.send-json-sec-user=true}. For
 * example:
 * 
 * <pre>
 * <code>
 * geonetwork.send-json-sec-user=true
 * datafeeder.send-json-sec-user=true 
 * </code>
 * </pre>
 * 
 * If this header provider is enabled globally, though, it can be disabled for
 * any specific service by setting the service specific config property to
 * {@code false}, as in {@code <service-name>.send-json-sec-user=false}.
 * 
 * @see UserOrganizationJSONRequestHeaderProvider
 */
public class UserDetailsJSONRequestHeaderProvider extends JSONRequestHeaderProvider {

    static final String CONFIG_PROPERTY = "send-json-sec-user";
    private static final String HEADER_NAME = "sec-user";

    @Autowired
    @Setter(value = AccessLevel.PACKAGE)
    private UsersApi users;

    public UserDetailsJSONRequestHeaderProvider() {
        super(CONFIG_PROPERTY, HEADER_NAME);
    }

    protected @Override GeorchestraUser getPayloadObject(String userName) {
        return this.users.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found:" + userName));
    }
}
