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
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

public @Data class Organization implements Serializable {
    private static final long serialVersionUID = -1;

    /** Provided by request header {@code sec-org} */
    private String id;

    /** Provided by request header {@code sec-orgname} */
    private String name;

    private String shortName;

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

    private List<String> cities = new LinkedList<>();
    private List<String> members = new LinkedList<>();
}