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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ReTypingFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.AdaptorFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.PullParser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.io.Closeables;

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

    private static final CoordinateReferenceSystem KMLCRS;
    static {
        try {
            KMLCRS = CRS.decode("EPSG:4326", true);
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        }
    }
    /**
     * base feature type for kml features, used when no Schema element is specified
     */
    private static final SimpleFeatureType KMLFeatureType21 = buildKMLFeatureType(org.geotools.kml.KML.Placemark);
    private static final SimpleFeatureType KMLFeatureType22 = buildKMLFeatureType(org.geotools.kml.v22.KML.Placemark);

    private Configuration configuration;
    private SimpleFeatureType defaultSchema;
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
    public KmlFeatureSource(File file)
            throws IOException, UnsupportedGeofileFormatException, XMLStreamException, FactoryConfigurationError {

        this.file = file;

        this.configuration = getConfig(this.file);

        if (configuration instanceof org.geotools.kml.v22.KMLConfiguration) {
            this.defaultSchema = KMLFeatureType22;
        } else {
            this.defaultSchema = KMLFeatureType21;
        }

    }

    /**
     * Read the kml version from file in order to set the kml configuration.
     *
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    private Configuration getConfig(File f) throws IOException, UnsupportedGeofileFormatException {

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
                        if (namespaceURI.startsWith("http://www.opengis.net/kml/")
                                || namespaceURI.startsWith("http://earth.google.com/kml/")) {
                            String version = namespaceURI.substring(4 + namespaceURI.indexOf("kml/"));
                            if ("2.2".equals(version)) {
                                return new org.geotools.kml.v22.KMLConfiguration();
                            } else if ("2.1".equals(version) || "2.0".equals(version)) {
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
     * @param query
     * @return {@link ListFeatureCollection }
     */
    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
        SimpleFeatureCollection collection = toFeatureCollection();

        CoordinateReferenceSystem sourceCrs = collection.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem targetCRS = query.getCoordinateSystemReproject();
        if (targetCRS != null && !CRS.equalsIgnoreMetadata(sourceCrs, targetCRS)) {
            collection = new ReprojectingFeatureCollection(collection, targetCRS);
        }

        // Remove the Style property
        SimpleFeatureType schema = collection.getSchema();
        if (null != schema.getDescriptor("Style")) {
            String[] properties = schema.getDescriptors().stream().map(PropertyDescriptor::getName)
                    .map(Name::getLocalPart).filter(name -> !"Style".equals(name)).collect(Collectors.toList())
                    .toArray(new String[0]);
            SimpleFeatureType targetType;
            try {
                targetType = DataUtilities.createSubType(schema, properties);
            } catch (SchemaException e) {
                throw new IOException(e);
            }
            collection = new ReTypingFeatureCollection(collection, targetType);
        }
        return collection;
    }

    private SimpleFeatureCollection toFeatureCollection() throws IOException {
        SimpleFeatureCollection collection = new PullParserFeatureCollection(this.configuration, this.file,
                this.defaultSchema);

        GeometryDescriptor geometryDescriptor = collection.getSchema().getGeometryDescriptor();
        CoordinateReferenceSystem crs = geometryDescriptor.getCoordinateReferenceSystem();
        if (null == crs) {
            FeatureCollection<SimpleFeatureType, SimpleFeature> forceCrsCollection;
            try {
                forceCrsCollection = new ForceCoordinateSystemFeatureResults(collection, KMLCRS);
            } catch (SchemaException e) {
                throw new IOException(e);
            }
            collection = DataUtilities.simple(forceCrsCollection);
        }

        return collection;
    }

    private static class PullParserFeatureCollection extends AdaptorFeatureCollection {

        private final Configuration configuration;
        private final File file;
        private final QName qname;

        private List<PullParserIterator> openIterators = new CopyOnWriteArrayList<>();

        public PullParserFeatureCollection(Configuration configuration, File file, SimpleFeatureType defaultSchema) {
            super(file.getName(), defaultSchema);
            this.configuration = configuration;
            this.file = file;
            Name typeName = defaultSchema.getName();
            this.qname = new QName(typeName.getNamespaceURI(), typeName.getLocalPart());
            // check if there're features, might have a different schema than the default
            // (by means of Schema/[SimpleField]+ elements in the KML file)
            Iterator<SimpleFeature> iterator = openIterator();
            try {
                SimpleFeature feature = Iterators.getNext(iterator, null);
                if (feature != null) {
                    super.schema = feature.getType();
                }
            } finally {
                closeIterator(iterator);
            }
        }

        public @Override ReferencedEnvelope getBounds() {
            return null;
        }

        /**
         * @return {@code Integer.MAX_VALUE} as specified by the method contract when
         *         the number of items is unknown
         */
        public @Override int size() {
            return Integer.MAX_VALUE;
        }

        protected @Override Iterator<SimpleFeature> openIterator() {
            PullParserIterator iterator;
            try {
                iterator = new PullParserIterator(configuration, file, qname);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            openIterators.add(iterator);
            return iterator;
        }

        protected @Override void closeIterator(Iterator<SimpleFeature> iterator) {
            boolean removed = openIterators.remove(iterator);
            if (removed) {
                ((PullParserIterator) iterator).close();
            }
        }

        private static class PullParserIterator extends AbstractIterator<SimpleFeature> implements Closeable {
            private PullParser xsdPullParser;

            private InputStream stream;

            public PullParserIterator(Configuration configuration, File file, QName qname) throws IOException {
                this.stream = new FileInputStream(file);
                this.xsdPullParser = new PullParser(configuration, stream, qname);
            }

            public @Override void close() {
                Closeables.closeQuietly(stream);
            }

            protected @Override SimpleFeature computeNext() {
                SimpleFeature feature;
                try {
                    feature = (SimpleFeature) this.xsdPullParser.parse();
                } catch (XMLStreamException | IOException | SAXException e) {
                    close();
                    throw new IllegalStateException(e);
                }
                return feature == null ? endOfData() : feature;
            }

        }
    }

    private static SimpleFeatureType buildKMLFeatureType(QName typeName) {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setNamespaceURI(typeName.getNamespaceURI());
        tb.setName(typeName.getLocalPart());

        // &lt;element minOccurs="0" name="name" type="string"/&gt;
        tb.add("name", String.class);
        // &lt;element default="1" minOccurs="0" name="visibility" type="boolean"/&gt;
        tb.add("visibility", Boolean.class);
        // &lt;element default="1" minOccurs="0" name="open" type="boolean"/&gt;
        tb.add("open", Boolean.class);
        // &lt;element minOccurs="0" name="address" type="string"/&gt;
        tb.add("address", String.class);
        // &lt;element minOccurs="0" name="phoneNumber" type="string"/&gt;
        tb.add("phoneNumber", String.class);
        // &lt;element minOccurs="0" name="Snippet" type="kml:SnippetType"/&gt;
        // tb.add("Snippet",String.class):
        // &lt;element minOccurs="0" name="description" type="string"/&gt;
        tb.add("description", String.class);
        // &lt;element minOccurs="0" ref="kml:LookAt"/&gt;
        tb.add("LookAt", Point.class);
        // &lt;element minOccurs="0" ref="kml:TimePrimitive"/&gt;
        // tb.add("TimePrimitive", ...);
        // &lt;element minOccurs="0" ref="kml:styleUrl"/&gt;
        tb.add("Style", FeatureTypeStyle.class);
        // &lt;element maxOccurs="unbounded" minOccurs="0" ref="kml:StyleSelector"/&gt;

        // &lt;element minOccurs="0" ref="kml:Region"/&gt;
        tb.add("Region", LinearRing.class, KMLCRS);
        tb.add("Geometry", Geometry.class, KMLCRS);
        // Force the default geometry attribute to the Geometry, otherwise LookAt or
        // Region would take precedence
        tb.setDefaultGeometry("Geometry");
        return tb.buildFeatureType();
    }
}
