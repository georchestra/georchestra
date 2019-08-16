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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.geotools.wfs.GML.Version;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.PullParser;
import org.json.JSONArray;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

/**
 * This class is a façade to the Geotools data management implementations.
 *
 * @author Mauricio Pazos
 */
public class GeotoolsFeatureReader implements FeatureGeoFileReader {

    private static final Log LOG = LogFactory.getLog(GeotoolsFeatureReader.class.getPackage().getName());

    private final FileFormat[] formats = new FileFormat[] { FileFormat.shp, FileFormat.gml, FileFormat.kml,
            FileFormat.geojson };

    @Override
    public JSONArray getFormatListAsJSON() {
        JSONArray ret = new JSONArray();

        FileFormat[] ff = getFormatList();
        for (FileFormat f : ff) {
            ret.put(f.toString());
        }
        return ret;
    }

    @Override
    public FileFormat[] getFormatList() {
        return formats;
    }

    @Override
    public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat)
            throws IOException, UnsupportedGeofileFormatException, ProjectionException {

        return getFeatureCollection(file, fileFormat, null);
    }

    @Override
    public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat,
            final CoordinateReferenceSystem targetCRS)
            throws IOException, UnsupportedGeofileFormatException, ProjectionException {

        assert file != null && fileFormat != null;

        switch (fileFormat) {
        case shp:
            return readShpFile(file, targetCRS);
        case gml:
            return readGmlFile(file, targetCRS);
        case kml:
            return readKmlFile(file, targetCRS);
        case geojson:
            return readGeoJSONFile(file, targetCRS);
        default:
            throw new UnsupportedGeofileFormatException("Unsuported format: " + fileFormat.toString());
        }
    }

    private SimpleFeatureCollection readGeoJSONFile(File file, CoordinateReferenceSystem targetCRS)
            throws IOException, ProjectionException {

        final FeatureJSON fjson = new FeatureJSON();
        fjson.setEncodeFeatureCollectionCRS(true);
        fjson.setEncodeFeatureCollectionBounds(true);

        try (InputStream in = new FileInputStream(file)) {
            // Build a schema with properties from all features, in case the file contains
            // mixed type features
            boolean nullValuesEncoded = false;
            SimpleFeatureType targetSchema = fjson.readFeatureCollectionSchema(in, nullValuesEncoded);
            fjson.setFeatureType(targetSchema);
        }

        try (InputStream in = new FileInputStream(file)) {
            SimpleFeatureCollection sfc = (SimpleFeatureCollection) fjson.readFeatureCollection(in);

            SimpleFeatureType sft = sfc.getSchema();
            CoordinateReferenceSystem sourceCRS = sft.getCoordinateReferenceSystem();
            if (sourceCRS == null) {
                sourceCRS = CRS.decode("EPSG:4326");
                sfc = new ForceCoordinateSystemFeatureResults(sfc, sourceCRS);
            }
            if (targetCRS != null && !CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
                sfc = new ReprojectingFeatureCollection(sfc, targetCRS);
            }
            return sfc;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new IOException(e);
        }
    }

    /**
     * Reads the GML file.
     * <p>
     * Implementation note: the method try to read using GML3, since it's a superset
     * of GML2, but if it fails it'll try the GML2 parser nonetheless
     *
     * @param file
     * @param targetCRS
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     */
    private SimpleFeatureCollection readGmlFile(File file, CoordinateReferenceSystem targetCRS)
            throws IOException, ProjectionException {
        try {
            return readGmlFile(file, targetCRS, Version.GML3);
        } catch (IOException | RuntimeException e) {
            LOG.info("Failure reading with GML3 parser. Trying with GML2");
        }
        return readGmlFile(file, targetCRS, Version.GML2);
    }

    /**
     * Creates a feature collection from a kml file. CRS EPSG:4326 is assumed for
     * the kml file.
     *
     * @param file
     * @param targetCRS
     * @return
     * @throws IOException
     */
    private SimpleFeatureCollection readKmlFile(final File file, final CoordinateReferenceSystem targetCRS)
            throws IOException {

        try {
            // as default EPSG:4326 is assumed
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

            KmlFeatureSource reader = new KmlFeatureSource(file);
            Query q = new Query();
            q.setFilter(Filter.INCLUDE);
            q.setCoordinateSystem(sourceCRS);
            q.setCoordinateSystemReproject(targetCRS);

            SimpleFeatureCollection list = reader.getFeatures(q);

            return list;

        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new IOException(e);

        }
    }

    /**
     * Creates a feature collection from a GML file.
     *
     * @param file      a gml file
     * @param targetCRS target crs
     * @param version   gml version
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     */
    private SimpleFeatureCollection readGmlFile(final File file, final CoordinateReferenceSystem targetCRS,
            final Version version) throws IOException {

        final CoordinateReferenceSystem sourceCRS;

        SimpleFeatureCollection result;

        try (InputStream in = new FileInputStream(file)) {
            Configuration cfg = (version == Version.GML2) ? new org.geotools.gml2.GMLConfiguration()
                    : new org.geotools.gml3.GMLConfiguration();

            PullParser parser = new PullParser(cfg, in, SimpleFeature.class);

            SimpleFeature feature = (SimpleFeature) parser.parse();
            if (feature == null) {
                final String msg = String.format("Fail reading GML file (%s). It cannot read the file %s", version,
                        file.getAbsoluteFile());
                LOG.warn(msg);
                throw new IOException(msg);
            }

            Geometry geom = (Geometry) feature.getDefaultGeometry();
            // initializes the feature collection using the crs and the
            // feature type of the first feature
            int srid = geom != null && geom.getSRID() == 0 ? 4326 : geom.getSRID();
            sourceCRS = CRS.decode("EPSG:" + srid);
            SimpleFeatureType type = feature.getFeatureType();
            ListFeatureCollection fc = new ListFeatureCollection(type);

            do {
                fc.add(feature);
            } while ((feature = (SimpleFeature) parser.parse()) != null);

            result = new ForceCoordinateSystemFeatureResults(fc, sourceCRS);
        } catch (XMLStreamException | SAXException | FactoryException | SchemaException e) {
            LOG.error(e.getMessage());
            throw new IOException(e);
        }
        if (targetCRS != null && !CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            result = new ReprojectingFeatureCollection(result, targetCRS);
        }
        return result;
    }

    /**
     * Reads the features from Shape file.
     *
     * @param file
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     */
    private SimpleFeatureCollection readShpFile(final File file, final CoordinateReferenceSystem crs)
            throws IOException {

        ShapefileDataStoreFactory storeFactory = new ShapefileDataStoreFactory();

        FileDataStore store = storeFactory.createDataStore(file.toURI().toURL());

        String typeName = FilenameUtils.getBaseName(file.getAbsolutePath());

        SimpleFeatureCollection features = retrieveFeatures(typeName, store, crs);

        return features;
    }

    /**
     * Retrieves the features from store
     *
     * @param typeName
     * @param store
     * @param targetCRS when the target crs is provided the reatures are reprojected
     * @return
     * @throws IOException
     */
    private SimpleFeatureCollection retrieveFeatures(final String typeName, final DataStore store,
            final CoordinateReferenceSystem targetCRS) throws IOException {

        SimpleFeatureType schema = store.getSchema(typeName);

        Query query = new Query(schema.getTypeName(), Filter.INCLUDE);

        CoordinateReferenceSystem baseCRS = store.getSchema(schema.getTypeName()).getCoordinateReferenceSystem();
        query.setCoordinateSystem(baseCRS);
        if (targetCRS != null) {
            query.setCoordinateSystemReproject(targetCRS);
        }

        SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());

        SimpleFeatureCollection features = featureSource.getFeatures(query);

        return features;
    }

    @Override
    public boolean isSupportedFormat(FileFormat fileFormat) {
        for (FileFormat supported : this.formats) {
            if (fileFormat == supported) {
                return true;
            }
        }
        return false;
    }
}