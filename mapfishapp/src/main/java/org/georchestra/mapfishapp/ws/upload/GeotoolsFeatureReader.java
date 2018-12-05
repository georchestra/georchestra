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

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.GML.Version;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.geotools.xml.Configuration;
import org.geotools.xml.PullParser;
import org.json.JSONArray;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This class is a façade to the Geotools data management implementations.
 *
 *
 * @author Mauricio Pazos
 *
 */
public class GeotoolsFeatureReader implements FeatureGeoFileReader {

    private static final Log   LOG     = LogFactory.getLog(GeotoolsFeatureReader.class.getPackage().getName());

    private final FileFormat[] formats = new FileFormat[] {
                                                        FileFormat.shp,
                                                        FileFormat.gml,
                                                        FileFormat.kml };

    public GeotoolsFeatureReader() {}

    @Override
    public JSONArray getFormatListAsJSON() {
        JSONArray ret = new JSONArray();

        FileFormat[] ff = getFormatList();
        for (FileFormat f: ff) {
            ret.put(f.toString());
        }
        return ret;
    }

    @Override
    public FileFormat[] getFormatList() {
        return formats;
    }

    @Override
    public SimpleFeatureCollection getFeatureCollection(
            final File file,
            final FileFormat fileFormat) throws IOException,
            UnsupportedGeofileFormatException, ProjectionException {

        return getFeatureCollection(file, fileFormat, null);
    }

    @Override
    public SimpleFeatureCollection getFeatureCollection(final File file,
            final FileFormat fileFormat,
            final CoordinateReferenceSystem targetCRS) throws IOException,
            UnsupportedGeofileFormatException, ProjectionException {

        assert file != null && fileFormat != null;

        switch (fileFormat) {
        case shp:
            return readShpFile(file, targetCRS);
        case gml:
            return readGmlFile(file, targetCRS);
        case kml:
            return readKmlFile(file, targetCRS);

        default:
            throw new UnsupportedGeofileFormatException("Unsuported format: "
                    + fileFormat.toString());
        }
    }

    /**
     * Reads the GML file. The method try to read using GML2 if it cannot then
     * try using GML3
     *
     * @param file
     * @param targetCRS
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     */
    private SimpleFeatureCollection readGmlFile(File file,
            CoordinateReferenceSystem targetCRS) throws IOException, ProjectionException {

        SimpleFeatureCollection fc = null;
        try {
            fc = readGmlFile(file, targetCRS, Version.GML2);
        } catch (IOException e) {
            LOG.warn("fails reading with GML2 reader. Try using GML3");

            fc = readGmlFile(file, targetCRS, Version.GML3);

            LOG.warn("GML3 readier ends successful");
        }
        return fc;
    }

    /**
     * Creates a feature collection from a kml file. CRS EPSG:4326 is assumed
     * for the kml file.
     *
     * @param file
     * @param targetCRS
     * @return
     * @throws IOException
     */
    private SimpleFeatureCollection readKmlFile(final File file, final CoordinateReferenceSystem targetCRS) throws IOException {


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
     * @param file
     *            a gml file
     * @param targetCRS
     *            target crs
     * @param version
     *            gml version
     *
     * @return {@link SimpleFeatureCollection}
     * @throws IOException, ProjectionException
     */
    private SimpleFeatureCollection readGmlFile(final File file,
            final CoordinateReferenceSystem targetCRS, final Version version)
            throws IOException, ProjectionException {

        InputStream in = new FileInputStream(file);
        try {
            Configuration cfg = (version == Version.GML2) ? new org.geotools.gml2.GMLConfiguration()
                    : new org.geotools.gml3.GMLConfiguration();
            PullParser parser = new PullParser(cfg, in, SimpleFeature.class);

            int targetSRID = 0;
            if (targetCRS != null) {
                targetSRID = CRS.lookupEpsgCode(targetCRS, true);
            }

            CoordinateReferenceSystem sourceCRS;
            MathTransform mathTransform = null;
            ListFeatureCollection fc = null;
            SimpleFeature feature;
            while ((feature = (SimpleFeature) parser.parse()) != null) {

                Geometry geom = (Geometry) feature.getDefaultGeometry();

                // initializes the feature collection using the crs and the
                // feature type of the first feature
                if (fc == null) {

                    int srid = geom.getSRID();
                    if (srid > 0) {
                        sourceCRS = CRS.decode("EPSG:" + srid);
                    } else {
                        sourceCRS = CRS.decode("EPSG:4326"); // if the crs is not present 4326 is assumed
                    }

                    SimpleFeatureType type;
                    if ((targetCRS != null) && !sourceCRS.equals(targetCRS)) {
                        // transforms the feature type to the target crs, creates the feature collection
                        // and finds the math transformation required
                        type = SimpleFeatureTypeBuilder.retype(
                                feature.getFeatureType(), targetCRS);

                        mathTransform = CRS.findMathTransform(sourceCRS,
                                targetCRS, true);
                    } else {
                        // uses the original feature type
                        type = SimpleFeatureTypeBuilder.retype(
                                feature.getFeatureType(), sourceCRS);
                    }
                    fc = new ListFeatureCollection(type);

                }
                // reproject the feature's geometry it it is necessary before
                // add the feature to the new feature collection.
                if (mathTransform != null) {
                    // transformation is required
                    Geometry reprojectedGeometry = JTS.transform(geom,
                            mathTransform);
                    reprojectedGeometry.setSRID(targetSRID);
                    feature.setDefaultGeometry(reprojectedGeometry);
                }
                fc.add(feature);
            }
            if (fc == null) {
                final String msg = "Fail reading GML file (" + version + "). It cannot read the file "+ file.getAbsoluteFile();
                LOG.warn(msg);
                throw new IOException(msg);
            }
            return fc;
        } catch (ProjectionException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new IOException(e);

        } finally {

            in.close();
        }
    }

    /**
     * Reads the features from Shape file.
     *
     * @param file
     * @return {@link SimpleFeatureCollection}
     *
     * @throws IOException
     */
    private SimpleFeatureCollection readShpFile(final File file, final CoordinateReferenceSystem crs) throws IOException {

        ShapefileDataStoreFactory storeFactory = new ShapefileDataStoreFactory();

        FileDataStore store = storeFactory.createDataStore(file.toURL());

        String typeName = FilenameUtils.getBaseName(file.getAbsolutePath());

        SimpleFeatureCollection features = retrieveFeatures(typeName, store,
                crs);

        return features;
    }

    /**
     * Retrieves the features from store
     *
     * @param typeName
     * @param store
     * @param targetCRS
     *            when the target crs is provided the reatures are reprojected
     * @return
     * @throws IOException
     */
    private SimpleFeatureCollection retrieveFeatures(final String typeName,
            final DataStore store, final CoordinateReferenceSystem targetCRS)
            throws IOException {

        SimpleFeatureType schema = store.getSchema(typeName);

        Query query = new Query(schema.getTypeName(), Filter.INCLUDE);

        CoordinateReferenceSystem baseCRS = store.getSchema(
                schema.getTypeName()).getCoordinateReferenceSystem();
        query.setCoordinateSystem(baseCRS);
        if (targetCRS != null) {
            query.setCoordinateSystemReproject(targetCRS);
        }

        SimpleFeatureSource featureSource = store.getFeatureSource(schema
                .getTypeName());

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