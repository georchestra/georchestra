/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.config.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.google.common.base.Splitter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j(topic = "org.georchestra.config.security")
public class GeorchestraUserDetails implements UserDetails {
    private static final long serialVersionUID = -8672954222635750682L;

    static final String SEC_USERID = org.georchestra.commons.security.SecurityHeaders.SEC_USERID;
    static final String SEC_LASTUPDATED = org.georchestra.commons.security.SecurityHeaders.SEC_LASTUPDATED;
    static final String SEC_USERNAME = org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
    static final String SEC_FIRSTNAME = org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
    static final String SEC_LASTNAME = org.georchestra.commons.security.SecurityHeaders.SEC_LASTNAME;
    static final String SEC_ROLES = org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
    static final String SEC_EMAIL = org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
    static final String SEC_TEL = org.georchestra.commons.security.SecurityHeaders.SEC_TEL;
    static final String SEC_ADDRESS = "sec-address";
    static final String SEC_TITLE = "sec-title";
    static final String SEC_NOTES = "sec-notes";

    static final String SEC_ORG = org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
    static final String SEC_ORGID = org.georchestra.commons.security.SecurityHeaders.SEC_ORGID;
    static final String SEC_ORGNAME = org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;
    static final String SEC_ORG_LINKAGE = "sec-org-linkage";
    static final String SEC_ORG_ADDRESS = "sec-org-address";
    static final String SEC_ORG_CATEGORY = "sec-org-category";
    static final String SEC_ORG_DESCRIPTION = "sec-org-description";
    static final String SEC_ORG_LASTUPDATED = org.georchestra.commons.security.SecurityHeaders.SEC_ORG_LASTUPDATED;

    private @NonNull GeorchestraUser user;

    /** {@code true} if request header {@code sec-username} is {@code null} */
    private boolean anonymous;

    @Getter(onMethod = @__({ @Override }))
    private final String password = null;

    @Getter(onMethod = @__({ @Override }))
    private final boolean accountNonExpired = true;

    @Getter(onMethod = @__({ @Override }))
    private final boolean accountNonLocked = true;

    @Getter(onMethod = @__({ @Override }))
    private final boolean credentialsNonExpired = true;

    @Getter(onMethod = @__({ @Override }))
    private final boolean enabled = true;

    /**
     * @return user name as given by request header {@code sec-username}
     */
    public @Override String getUsername() {
        return user.getUsername();
    }

    /**
     * @return role names as given by request header {@code sec-roles} wrapped in
     *         {@link SimpleGrantedAuthority} instances
     */
    public @Override Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public static GeorchestraUserDetails fromHeaders(Map<String, String> headers) {
        final boolean anonymous = !headers.containsKey(SEC_USERNAME);
        final GeorchestraUser user;
        if (anonymous) {
            user = buildUserFromHeaders(Collections.singletonMap(SEC_USERNAME, "anonymousUser"));
        } else {
            user = buildUserFromHeaders(headers);
        }
        return new GeorchestraUserDetails(user, anonymous);
    }

    public static GeorchestraUser buildUserFromHeaders(Map<String, String> headers) {
        Organization organization = buildOrganization(headers);

        String userId = getHeader(headers, SEC_USERID);
        String username = getHeader(headers, SEC_USERNAME);
        String lastUpdated = getHeader(headers, SEC_LASTUPDATED);
        List<String> roles = extractRoles(headers);
        String firstName = getHeader(headers, SEC_FIRSTNAME);
        String lastName = getHeader(headers, SEC_LASTNAME);
        String email = getHeader(headers, SEC_EMAIL);
        String address = getHeader(headers, SEC_ADDRESS);
        String telephoneNumber = getHeader(headers, SEC_TEL);
        String title = getHeader(headers, SEC_TITLE);
        String notes = getHeader(headers, SEC_NOTES);

        GeorchestraUser user = new GeorchestraUser();
        user.setId(userId);
        user.setLastUpdated(lastUpdated);
        user.setUsername(username);
        user.setRoles(roles);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setOrganization(organization);
        user.setEmail(email);
        user.setPostalAddress(address);
        user.setTelephoneNumber(telephoneNumber);
        user.setTitle(title);
        user.setNotes(notes);
        return user;
    }

    public static @NonNull Organization buildOrganization(Map<String, String> headers) {
        String uniqueId = getHeader(headers, SEC_ORGID);
        String organization = getHeader(headers, SEC_ORG);
        String organizationName = getHeader(headers, SEC_ORGNAME);
        String linkage = getHeader(headers, SEC_ORG_LINKAGE);
        String postalAddress = getHeader(headers, SEC_ORG_ADDRESS);
        String category = getHeader(headers, SEC_ORG_CATEGORY);
        String description = getHeader(headers, SEC_ORG_DESCRIPTION);
        String lastUpdated = getHeader(headers, SEC_ORG_LASTUPDATED);

        Organization org = new Organization();
        org.setUniqueId(uniqueId);
        org.setId(organization);
        org.setName(organizationName);
        org.setLinkage(linkage);
        org.setPostalAddress(postalAddress);
        org.setCategory(category);
        org.setDescription(description);
        org.setLastUpdated(lastUpdated);
        return org;
    }

    private static String getHeader(Map<String, String> headers, String headerName) {
        String value = headers.get(headerName);
        String decoded = SecurityHeaders.decode(value);
        if (StringUtils.hasLength(value)) {
            if (Objects.equals(value, decoded))
                log.debug("Found header {}={}", headerName, value);
            else
                log.debug("Found header {}={} ({})", headerName, value, decoded);
        } else {
            log.info("NOT Found header {}", headerName);
        }
        return decoded;
    }

    private static List<String> extractRoles(Map<String, String> headers) {
        String rolesHeader = getHeader(headers, SEC_ROLES);
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptyList();
        }
        return Splitter.on(';').omitEmptyStrings().trimResults().splitToList(rolesHeader);
    }
}
