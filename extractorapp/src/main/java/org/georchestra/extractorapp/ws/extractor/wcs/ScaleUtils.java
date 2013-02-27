package org.georchestra.extractorapp.ws.extractor.wcs;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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

	public static double fromMeterToCrs(double value,
			CoordinateReferenceSystem crs) {
		Unit<?> unit = getUnit(crs);
		UnitConverter converter = SI.METER.getConverterTo(unit);
		return converter.convert(value);
	}

	public static double fromCrsToMeter(double value,
			CoordinateReferenceSystem crs) {
		Unit<?> unit = getUnit(crs);
		UnitConverter converter = unit.getConverterTo(javax.measure.unit.SI.METER);
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
