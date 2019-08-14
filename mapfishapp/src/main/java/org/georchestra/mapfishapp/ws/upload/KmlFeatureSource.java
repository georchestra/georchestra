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

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.PullParser;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * <p>
 * Access to the kml file. This implementation support kml 2.1 and 2.2.
 *
 *
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
class KmlFeatureSource {

    private static final Log LOG = LogFactory.getLog(GeotoolsFeatureReader.class.getPackage().getName());

    private Configuration configuration;
    private QName qname;
    private File file;

    /**
     * Creates a source to read the features present in the kml file
     *
     * @param file
     * @throws IOException
     * @throws UnsupportedGeofileFormatException If the kml format is not supported
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException 
     */
    public KmlFeatureSource(File file) throws IOException, UnsupportedGeofileFormatException, XMLStreamException, FactoryConfigurationError {

        this.file = file;

        this.configuration = getConfig(this.file);

        if (configuration instanceof org.geotools.kml.v22.KMLConfiguration) {
            this.qname = org.geotools.kml.v22.KML.Placemark;
        } else {
            this.qname = org.geotools.kml.KML.Placemark;

        }

    }

    /**
     * Read the kml version from file in order to set the kml configuration.
     *
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    private Configuration getConfig(File f)
            throws IOException, UnsupportedGeofileFormatException{

        // detect the kml version from input stream
        try (InputStream in = new FileInputStream(f)) {
            // e.g.: <kml xmlns="http://www.opengis.net/kml/2.2"
            // xmlns:kml="http://www.opengis.net/kml/2.2"
            // xmlns:atom="http://www.w3.org/2005/Atom">
            XMLStreamReader reader;
            try {
                reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
            } catch (XMLStreamException | FactoryConfigurationError e) {
                throw new IOException(e);
            }
            int tag;
            do {
                tag = reader.next();
                if (tag == START_ELEMENT && reader.getNamespaceCount() > 0) {
                    for (int i = 0; i < reader.getNamespaceCount(); i++) {
                        String namespaceURI = reader.getNamespaceURI(i);
                        if (namespaceURI.startsWith("http://www.opengis.net/kml/")) {
                            String version = namespaceURI.substring("http://www.opengis.net/kml/".length());
                            if (version.equals("2.2")) {
                                return new org.geotools.kml.v22.KMLConfiguration();
                            } else if (version.equals("2.1") || (version.equals("2.0"))) {
                                return new org.geotools.kml.KMLConfiguration();
                            } else {
                                final String message = "KML version detected: " + version
                                        + ". There is no available binding for this version";
                                LOG.error(message);
                                throw new UnsupportedGeofileFormatException(message);
                            }
                        }
                    }
                }
            } while (tag != START_ELEMENT && tag != END_DOCUMENT);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        return null;
    }
    
    /**
     * Reads the kml file
     * <p>
     * Note: only Filter.INCLUDE is implemented
     * </p>
     * 
     * @param q
     * @return {@link ListFeatureCollection }
     */
    public SimpleFeatureCollection getFeatures(Query q) throws IOException {

        InputStream is = new FileInputStream(this.file);
        try {

            CoordinateReferenceSystem sourceCRS = q.getCoordinateSystem();
            CoordinateReferenceSystem targetCRS = q.getCoordinateSystemReproject();

            MathTransform mathTransform = null;
            if ((targetCRS != null) && !sourceCRS.equals(targetCRS)) {
                mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            }

            PullParser parser = new PullParser(configuration, is, this.qname);

            ListFeatureCollection list = null;
            SimpleFeature f;

            while ((f = (SimpleFeature) parser.parse()) != null) {

                Geometry geom = (Geometry) f.getDefaultGeometry();

                int srid = geom.getFactory().getSRID();
                if (srid < 0) {
                    srid = 4326; // set the default
                    sourceCRS = CRS.decode("EPSG:" + srid);
                }
                geom.setSRID(srid);

                if (mathTransform != null) {
                    // transformation is required
                    Geometry reprojectedGeometry = JTS.transform(geom, mathTransform);
                    f.setDefaultGeometry(reprojectedGeometry);
                }
                if (list == null) {
                    list = new ListFeatureCollection(f.getFeatureType());
                }

                list.add(f);
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        } finally {
            is.close();
        }

    }

}
