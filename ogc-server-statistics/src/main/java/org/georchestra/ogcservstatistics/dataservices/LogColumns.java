/*
 * Copyright (C) 2019 by the geOrchestra PSC
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
package org.georchestra.ogcservstatistics.dataservices;

/**
 * Constants for log's table and column names
 */
public final class LogColumns {

    public static final String QUALIFIED_TABLE_NAME = "ogcstatistics.OGC_SERVICES_LOG";

    public static final String DATE_COLUMN = "date";
    public static final String USER_COLUMN = "user_name";
    public static final String SERVICE_COLUMN = "service";
    public static final String LAYER_COLUMN = "layer";
    public static final String REQUEST_COLUMN = "request";
    public static final String ORG_COLUMN = "org";
    public static final String SECROLE_COLUMN = "roles";

    private LogColumns() {
        // private constructor, force class being purely a utility class
    }
}
