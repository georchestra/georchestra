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

import lombok.Data;

public @Data class Organization implements Serializable {
    private static final long serialVersionUID = -1;

    /**
     * Provided by request header {@code sec-org}, legacy way of identifying by
     * LDAP's {@code org.cn} attribute, which may change over time
     */
    private String id;

    /** Provided by request header {@code sec-orgid}, usually stable UUID */
    private String uniqueId;

    /**
     * Provided by request header {@code sec-orgname}, due to legacy LDAP mapping
     * {@code sec-orgname=org.o}
     */
    private String name;

    /** Provided by request header {@code sec-org-linkage} */
    private String linkage;

    /** Provided by request header {@code sec-org-address} */
    private String postalAddress;

    /** Provided by request header {@code sec-org-category} */
    private String category;

    /** Provided by request header {@code sec-org-description} */
    private String description;

    /** Provided by request header {@code sec-org-notes} */
    private String notes;

    /**
     * String that somehow represents the current version, may be a timestamp, a
     * hash, etc. Provided by request header {@code sec-lastupdated}
     */
    private String lastUpdated;
}