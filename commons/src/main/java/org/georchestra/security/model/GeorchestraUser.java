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
package org.georchestra.security.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeorchestraUser implements Serializable {
    private static final long serialVersionUID = -1;

    /////// Default mandatory properties. /////
    /////// Some optional properties may be made mandatory on a per-application
    /////// basis /////

    /** Provided by request header {@code sec-username} */
    private String username;

    /** Provided by request header {@code sec-roles} */
    private List<String> roles = new ArrayList<>();

    /**
     * User's organization short name. Provided by request header {@code sec-org},
     * legacy way of identifying by LDAP's {@code org.cn} attribute, which may
     * change over time
     */
    private String organization;

    /////// Default optional properties. /////
    /////// Some may be made mandatory on a per-application basis /////

    /** Provided by request header {@code sec-userid} */
    private String id;

    /**
     * String that somehow represents the current version, may be a timestamp, a
     * hash, etc. Provided by request header {@code sec-lastupdated}
     */
    private String lastUpdated;

    /** Provided by request header {@code sec-firstname} */
    private String firstName;

    /** Provided by request header {@code sec-lastname} */
    private String lastName;

    /** Provided by request header {@code sec-email} */
    private String email;

    /** Provided by request header {@code sec-address} */
    private String postalAddress;

    /** Provided by request header {@code sec-tel} */
    private String telephoneNumber;

    /** Provided by request header {@code sec-title} */
    private String title;

    /** Provided by request header {@code sec-notes} */
    private String notes;

    /** Provided by request header {@code sec-ldap-warn} */
    private Boolean ldapWarn;

    /** Provided by request header {@code sec-ldap-remaining-days} */
    private String ldapRemainingDays;

    /** Maps to Account.getOAuth2Provider */
    private String oAuth2Provider;

    /** Maps to Account.getOAuth2Uid */
    private String oAuth2Uid;

    /** Provided by request header {@code sec-external-authentication} */
    private Boolean isExternalAuth = false;

    public void setRoles(List<String> roles) {
        this.roles = roles == null ? new ArrayList<>() : roles;
    }
}
