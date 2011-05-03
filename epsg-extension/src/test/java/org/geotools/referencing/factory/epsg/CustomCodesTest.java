/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.factory.epsg;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Tests {@link CustomCodes}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class CustomCodesTest extends TestCase {
    /**
     * The factory to test.
     */
    private CustomCodes factory;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CustomCodesTest.class);
    }

    /**
     * Run the test from the command line.
     * Options: {@code -verbose}.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Creates a test case with the specified name.
     */
    public CustomCodesTest(final String name) {
        super(name);
    }

    /**
     * Gets the authority factory for ESRI.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = (CustomCodes) ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG",
                new Hints(Hints.CRS_AUTHORITY_FACTORY, CustomCodes.class));
    }

    /**
     * Tests the authority code.
     */
    public void testAuthority(){
        final Citation authority = factory.getAuthority();
        assertNotNull(authority);
        assertEquals("European Petroleum Survey Group", authority.getTitle().toString());
        assertTrue (Citations.identifierMatches(authority, "EPSG"));
        assertFalse(Citations.identifierMatches(authority, "ESRI"));
        assertTrue(factory instanceof CustomCodes);
    }

    /**
     * Tests the vendor.
     */
    public void testVendor(){
        final Citation vendor = factory.getVendor();
        assertNotNull(vendor);
        assertEquals("Geotools", vendor.getTitle().toString());
    }

    /**
     * Tests that codes NOT in custom file is accessible
     */
    public void test3005() throws FactoryException {
        CoordinateReferenceSystem expected = CRS.decode("EPSG:3005");
        
        assertNotNull(expected);
    }

    /**
     * Tests the {@code 9999999} code.
     */
    public void test9999999() throws FactoryException {
        CoordinateReferenceSystem actual, expected;
        expected = factory.createCoordinateReferenceSystem("9999999");
        actual   = CRS.decode("EPSG:9999999");
        assertSame(expected, actual);

        Collection<ReferenceIdentifier> ids = actual.getIdentifiers();
        assertTrue (ids.contains(new NamedIdentifier(Citations.EPSG, "9999999")));
    }

    /**
     * Tests the extensions through a URI.
     *
     * @see http://jira.codehaus.org/browse/GEOT-1563
     */
    public void testURI() throws FactoryException {
        final String id = "9999999";
        final CoordinateReferenceSystem crs = CRS.decode("EPSG:" + id);
        assertSame(crs, CRS.decode("urn:x-ogc:def:crs:EPSG:6.11.2:" + id));
        assertSame(crs, CRS.decode("http://www.opengis.net/gml/srs/epsg.xml#" + id));
    }
}
