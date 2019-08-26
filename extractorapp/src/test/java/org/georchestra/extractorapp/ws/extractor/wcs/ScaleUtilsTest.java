package org.georchestra.extractorapp.ws.extractor.wcs;

import static org.junit.Assert.*;

import javax.measure.converter.ConversionException;
import javax.measure.unit.Unit;

import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

public class ScaleUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetUnit() throws NoSuchAuthorityCodeException, FactoryException {
		Unit u1 = ScaleUtils.getUnit(CRS.decode("EPSG:4326"));
		assertTrue("°".equals(u1.toString()));
		Unit u2 = ScaleUtils.getUnit(CRS.decode("EPSG:2154"));
		assertTrue("m".equals(u2.toString()));
		boolean nosuchAuthEx = false;
		try {
			ScaleUtils.getUnit(CRS.decode("Wrong:EPSG"));
		} catch (NoSuchAuthorityCodeException e) {
			nosuchAuthEx = true;
		} catch (Throwable e) {
			fail("Unexpected: " + e.getMessage());
		}
		assertTrue("Should have encountered a NoSuchAuthorityCodeException", nosuchAuthEx);
	}

	@Test
	public final void testFromCrsToMeter() throws Exception {
		boolean npeExpected = false;
		try {
			ScaleUtils.fromCrsToMeter(1, null);
		} catch (NullPointerException e) {
			npeExpected = true;
		}
		assertTrue("Should have encountered a NullPointerException", npeExpected);

		double res = ScaleUtils.fromCrsToMeter(10, CRS.decode("EPSG:32667"));
		// 10 meters = ~ 3.048 feet
		assertTrue("10 meters should be ~ 3.048 feet", ((res > 3.04) && (res < 3.05)));

		// unable to go from degrees to meters
		boolean convExEncountered = false;
		try {
			ScaleUtils.fromCrsToMeter(10, CRS.decode("EPSG:4326"));
		} catch (ConversionException e) {
			convExEncountered = true;
		}
		assertTrue("Should have encountered a ConverstionException", convExEncountered);
	}

}
