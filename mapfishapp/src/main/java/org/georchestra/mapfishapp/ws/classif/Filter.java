/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.mapfishapp.ws.classif;

/**
 * A filter wrapper class.
 * @author eric.lemoine@camptocamp.com
 */
public class Filter {

    private final org.opengis.filter.Filter filter;
    private final String name;

    /**
     * Create a filter wrapping an OpenGIS filter.<br />
     * @param _filter The OpenGIS filter.
     * @param _name The filter name.
     */
    public Filter(final org.opengis.filter.Filter _filter, final String _name) {
        filter = _filter;
        name = _name;
    }

    /**
     * Get the filter name.
     * @return The filter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the GIS filter.
     * @return The GIS filter.
     */
    public org.opengis.filter.Filter getGISFilter() {
        return filter;
    }
}
