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

package org.georchestra.extractorapp.ws.extractor.wcs;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import tec.uom.se.unit.Units;

/**
 * Methods for calculating the ScaleDenominator
 * 
 * @author jesse
 */
final class ScaleUtils {

    static final double ACCURACY = 0.00000001;
//	private static final int MAX_ITERATIONS = 10;
//	private static Envelope WORLD = new Envelope(-180, 180, -90, 90);

    private ScaleUtils() {
    }

    public static Unit<?> getUnit(CoordinateReferenceSystem crs) {
        return crs.getCoordinateSystem().getAxis(0).getUnit();
    }

    public static double fromMeterToCrs(double value, CoordinateReferenceSystem crs) {
        Unit<Length> unit = (Unit<Length>) getUnit(crs);
        UnitConverter converter = Units.METRE.getConverterTo(unit);
        return converter.convert(value);
    }

    public static double fromCrsToMeter(double value, CoordinateReferenceSystem crs) {
        Unit<Length> unit = (Unit<Length>) getUnit(crs);
        UnitConverter converter = unit.getConverterTo(Units.METRE);
        return converter.convert(value);
    }

    /**
     * Determines if the crs is a lat/long crs (has angular units)
     * 
     * @return true if the crs is a latlong crs (has angular units)
     */
    public static boolean isLatLong(CoordinateReferenceSystem crs) {
        Unit<?> unit = getUnit(crs);
        Unit<?> degrees = getUnit(DefaultGeographicCRS.WGS84);
        boolean isLatLong = CRS.equalsIgnoreMetadata(unit, degrees);
        return isLatLong;
    }
}
