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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j(topic = "org.georchestra.config.security")
public class GeorchestraUserDetails implements UserDetails {

    private static final long serialVersionUID = -8672954222635750682L;

    /**
     * Parser for SEC_USER, see {@link #decodeUserFromJSON}
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // full user details as json.
    static final String SEC_USER = org.georchestra.commons.security.SecurityHeaders.SEC_USER;

    // user details as individual headers...
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
    static final String SEC_ORG_NOTES = "sec-org-notes";

    private @NonNull GeorchestraUser user;

    private @NonNull Optional<Organization> organization;

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
        return user.getRoles().stream().map(this::addRolePrefix).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String addRolePrefix(String roleName) {
        return "ROLE_" + roleName;
    }

    public static GeorchestraUserDetails fromHeaders(Map<String, String> headers) {
        final boolean anonymous = !headers.containsKey(SEC_USERNAME) && !headers.containsKey(SEC_USER);
        final GeorchestraUser user;
        final Optional<Organization> org;

        if (anonymous) {
            user = buildUserFromHeaders(Collections.singletonMap(SEC_USERNAME, "anonymousUser"));
            org = Optional.empty();
        } else {
            user = buildUserFromHeaders(headers);
            org = buildOrgFromHeaders(headers);
            if (!StringUtils.hasLength(user.getId())) {
                String username = user.getUsername();
                log.debug("No unique id provided for user. Using username as identifier: " + username);
                user.setId(username);
            }
        }
        return new GeorchestraUserDetails(user, org, anonymous);
    }

    public static Optional<Organization> buildOrgFromHeaders(Map<String, String> headers) {
        final String fullJsonOrg = getHeader(headers, "sec-organization");
        Organization org = null;
        if (StringUtils.hasLength(fullJsonOrg)) {
            log.debug("Decoding org from sec-organization header");
            org = decodeOrgFromJSON(fullJsonOrg);
        } else if (headers.containsKey(SEC_ORGID) || headers.containsKey(SEC_ORGNAME)) {
            log.debug("Decoding org from sec-org* individual headers");
            org = buildOrgFromIndividualHeaders(headers);
        } else {
            log.debug("Organization info not provided");
        }
        return Optional.ofNullable(org);
    }

    private static Organization buildOrgFromIndividualHeaders(Map<String, String> headers) {
        String shortName = getHeader(headers, SEC_ORG);
        String id = getHeader(headers, SEC_ORGID);
        String name = getHeader(headers, SEC_ORGNAME);
        String category = getHeader(headers, SEC_ORG_CATEGORY);
        String lastUpdated = getHeader(headers, SEC_ORG_LASTUPDATED);
        String linkage = getHeader(headers, SEC_ORG_LINKAGE);
        String notes = getHeader(headers, SEC_ORG_NOTES);
        String postalAddress = getHeader(headers, SEC_ORG_ADDRESS);
        String description = getHeader(headers, SEC_ORG_DESCRIPTION);

        Organization org = new Organization();
        org.setShortName(shortName);
        org.setId(id);
        org.setName(name);
        org.setDescription(description);
        org.setCategory(category);
        org.setLastUpdated(lastUpdated);
        org.setLinkage(linkage);
        org.setNotes(notes);
        org.setPostalAddress(postalAddress);

        return org;
    }

    private static Organization decodeOrgFromJSON(String fullJsonOrg) {
        try {
            return OBJECT_MAPPER.readValue(fullJsonOrg, Organization.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GeorchestraUser buildUserFromHeaders(Map<String, String> headers) {
        final String fullJsonUser = getHeader(headers, "sec-user");
        GeorchestraUser user;
        if (StringUtils.hasLength(fullJsonUser)) {
            log.info("Decoding user from sec-user header");
            user = decodeUserFromJSON(fullJsonUser);
        } else {
            user = buildUserFromIndividualHeaders(headers);
        }
        return user;
    }

    private static GeorchestraUser buildUserFromIndividualHeaders(Map<String, String> headers) {
        String userId = getHeader(headers, SEC_USERID);
        String username = getHeader(headers, SEC_USERNAME);
        String organization = getHeader(headers, SEC_ORG);
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

    private static GeorchestraUser decodeUserFromJSON(String fullJsonUser) {
        try {
            return OBJECT_MAPPER.readValue(fullJsonUser, GeorchestraUser.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            log.trace("NOT Found header {}", headerName);
        }
        return decoded;
    }

    private static List<String> extractRoles(Map<String, String> headers) {
        String rolesHeader = getHeader(headers, SEC_ROLES);
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptyList();
        }
        return Splitter.on(';').omitEmptyStrings().trimResults()//
                .splitToStream(rolesHeader)//
                .map(GeorchestraUserDetails::stripRolesPrefix)//
                .collect(Collectors.toList());
    }

    private static String stripRolesPrefix(String role) {
        return (role == null || !role.startsWith("ROLE_")) ? role : role.substring("ROLE_".length());
    }

}
