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

package org.georchestra.mapfishapp.ws.upload;

/**
 * GeoFile formats.
 * <p>
 * </p>
 *
 * @author Mauricio Pazos
 */
public enum FileFormat {

    shp, mif, gml, kml;

    public static boolean contains(final String ext) {

        String lcExt = ext.toLowerCase();

        FileFormat[] formats = values();
        for (int i = 0; i < formats.length; i++) {

            if (formats[i].toString().equals(lcExt)) {
                return true;
            }
        }
        return false;
    }
}