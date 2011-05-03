/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import org.opengis.referencing.FactoryException;
import org.geotools.factory.Hints;


/**
 * Provides custom {@linkplain org.opengis.referencing.crs.CoordinateReferenceSystem Coordinate Reference Systems}.
 * Those CRS will be registered in {@code "EPSG"} name space.
 * 
 * They will find the properties file by looking in the System properties with the
 * CUSTOM_EPSG_FILE key.  The key should reference a URL for accessing the properties file
 * 
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime
 */
public class CustomCodes extends FactoryUsingWKT {
    /**
     * The default filename to read. This file will be searched in the
     * {@code org/geotools/referencing/factory/espg} directory in the
     * classpath or in a JAR file.
     *
     * @see #getDefinitionsURL
     */
    public static final String SYS_PROP_KEY = "CUSTOM_EPSG_FILE";

    /**
     * Constructs an authority factory using the default set of factories.
     */
    public CustomCodes() {
        this(null);
    }

    /**
     * Constructs an authority factory using a set of factories created from the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints.
     */
    public CustomCodes(final Hints hints) {
        super(hints, DEFAULT_PRIORITY - 2);
    }

    /**
     * Returns the URL to the property file that contains CRS definitions.
     * The default implementation returns the URL to the {@value #FILENAME} file.
     *
     * @return The URL, or {@code null} if none.
     */
    @Override
    protected URL getDefinitionsURL() {
        try {
            URL url = new URL(System.getProperty(SYS_PROP_KEY));
            // quickly test url
            InputStream stream = url.openStream();
            stream.read();
            stream.close();
            return url;
        } catch (Throwable e) {
            System.err.println("Unable to use the system property: "+SYS_PROP_KEY+" to find custom code.  Using the default epsg.properties in the epsg jar");
            return CustomCodes.class.getResource("epsg.properties");
        }
    }

    /**
     * Prints a list of codes that duplicate the ones provided in the {@link DefaultFactory}.
     * The factory tested is the one registered in {@link org.geotools.referencing.ReferencingFactoryFinder}.  By default, this
     * is this {@code UnnamedExtension} class backed by the {@value #FILENAME} property file.
     * This method can be invoked from the command line in order to check the content of the
     * property file. Valid arguments are:
     * <p>
     * <table>
     *   <tr><td>{@code -test}</td><td>Try to instantiate all CRS and reports any failure
     *       to do so.</td></tr>
     *   <tr><td>{@code -duplicated}</td><td>List all codes from the WKT factory that are
     *       duplicating a code from the SQL factory.</td></tr>
     * </table>
     *
     * @param  args Command line arguments.
     * @throws FactoryException if an error occured.
     */
    public static void main(final String[] args) throws FactoryException {
        main(args, CustomCodes.class);
    }
}
