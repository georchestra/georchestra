/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

package org.georchestra.ogcservstatistics;

/**
 * @author Mauricio Pazos
 *
 */
public class OGCServStatisticsException extends Exception {

    /**
     * serialization
     */
    private static final long serialVersionUID = -5109217524588114531L;

    /**
     * 
     */
    public OGCServStatisticsException() {
        super();
    }

    /**
     * @param message
     */
    public OGCServStatisticsException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OGCServStatisticsException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public OGCServStatisticsException(String message, Throwable cause) {
        super(message, cause);
    }

}
