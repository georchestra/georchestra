/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.console.ds;

/**
 * Provide information about the database schema
 *
 * @author Mauricio Pazos
 *
 */
interface DatabaseSchema {

    final static String TABLE_USER_TOKEN = "user_token";
    final static String SCHEMA_NAME = "console";

    // columns
    final static String UID_COLUMN = "uid";
    final static String TOKEN_COLUMN = "token";
    final static String ADDITIONAL_INFO = "additional_info";
    final static String CREATION_DATE_COLUMN = "creation_date";

}
