/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.gateway.config;

import java.util.Optional;

import lombok.Data;

@Data
public class HeaderMappings {
    /** Append the standard {@literal sec-proxy=true} header to proxied requests */
    private Optional<Boolean> proxy = Optional.empty();

    /** Append the standard {@literal sec-username} header to proxied requests */
    private Optional<Boolean> username = Optional.empty();

    /** Append the standard {@literal sec-roles} header to proxied requests */
    private Optional<Boolean> roles = Optional.empty();

    /** Append the standard {@literal sec-org} header to proxied requests */
    private Optional<Boolean> org = Optional.empty();

    /** Append the standard {@literal sec-orgname} header to proxied requests */
    private Optional<Boolean> orgname = Optional.empty();

    /** Append the standard {@literal sec-email} header to proxied requests */
    private Optional<Boolean> email = Optional.empty();

    /** Append the standard {@literal sec-firstname} header to proxied requests */
    private Optional<Boolean> firstname = Optional.empty();

    /** Append the standard {@literal sec-firstname} header to proxied requests */
    private Optional<Boolean> lastname = Optional.empty();

    /** Append the standard {@literal sec-tel} header to proxied requests */
    private Optional<Boolean> tel = Optional.empty();

    private Optional<Boolean> jsonUser = Optional.empty();
    private Optional<Boolean> jsonOrganization = Optional.empty();
}
