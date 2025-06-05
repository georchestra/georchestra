/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.console.ws.editorgdetails;

import lombok.Data;

final @Data class EditOrgDetailsFormBean implements java.io.Serializable {
    private static final long serialVersionUID = -5836489312467203512L;
    private String id;
    private String name;
    private String shortName;
    private String description;
    private String address;
    private String url;
    private String orgType;
    private String mail;
    private String orgUniqueId;
}
