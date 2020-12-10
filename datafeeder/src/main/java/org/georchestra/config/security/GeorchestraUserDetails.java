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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class GeorchestraUserDetails implements UserDetails {
    private static final long serialVersionUID = -8672954222635750682L;

    /** Provided by request header {@code sec-username} */
    private @NonNull String username;

    /** Provided by request header {@code sec-roles} */
    private @NonNull List<String> roles;

    /** Provided by request header {@code sec-email} */
    private @Getter String email;

    /** Provided by request header {@code sec-firstname} */
    private @Getter String firstName;

    /** Provided by request header {@code sec-lastname} */
    private @Getter String lastName;

    /** Provided by request header {@code sec-org} */
    private @Getter String organization;

    /** Provided by request header {@code sec-orgname} */
    private @Getter String organizationName;

    /** {@code true} if request header {@code sec-username} is {@code null} */
    private boolean anonymous;

    public GeorchestraUserDetails(@NonNull String userName, @NonNull List<String> roles, String email, String firstName,
            String lastName, String organization, String organizationName, boolean anonymous) {
        this.anonymous = anonymous;
        this.username = userName;
        this.roles = Collections.unmodifiableList(new ArrayList<>(roles));
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.organizationName = organizationName;
    }

    /**
     * @return user name as given by request header {@code sec-username}
     */
    public @Override String getUsername() {
        return username;
    }

    /**
     * @return role names as given by request header {@code sec-roles} wrapped in
     *         {@link SimpleGrantedAuthority} instances
     */
    @JsonIgnore
    public @Override Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    /**
     * @return {@code null}
     */
    public @Override String getPassword() {
        return null;
    }

    /**
     * @return {@code true}
     */
    public @Override boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @return {@code true}
     */
    public @Override boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @return {@code true}
     */
    public @Override boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @return {@code true}
     */
    public @Override boolean isEnabled() {
        return true;
    }

}
