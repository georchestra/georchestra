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

package org.georchestra.lib.sqlcommand;

import java.sql.SQLException;

public class DataCommandException extends Exception {

    /**
     * for serialization
     */
    private static final long serialVersionUID = -5196425322579527757L;

    public DataCommandException(String message) {
        super(message);
    }

    public DataCommandException(SQLException e) {
        super(e);
    }

    public DataCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
