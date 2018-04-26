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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.Query;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.ogr.jni.JniOGR;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * OGR Feature Reader.
 *
 * <p>
 * This class is responsible of retrieving the features stored in different file
 * format.
 * </p>
 * <p>
 * The available file format are load from ogr driver.
 * </p>
 *
 *
 * @author Mauricio Pazos
 *
 */
final class OGRFeatureReader implements FeatureGeoFileReader {

    private static final Log LOG = LogFactory.getLog(OGRFeatureReader.class.getPackage().getName());

    private static class OGRDriver {

        private final String   name;
        private final String[] options;

        public OGRDriver(final String name, final String[] options) {
            this.name = name;
            this.options = options;
        }

        public OGRDriver(final String name) {
            this.name = name;
            this.options = null;
        }

        public String getName() {
            return name;
        }

        public String[] getOptions() {
            return options;
        }

        @Override
        public String toString() {
            return "OGRDriver [name=" + name + ", options="
                    + Arrays.toString(options) + "]";
        }

    }

    private static Map<FileFormat, OGRDriver> DRIVERS = Collections.synchronizedMap(new HashMap<FileFormat, OGRDriver>());

    public OGRFeatureReader() throws IOException {
        loadFormats();
    }

    public int formatCount() {

        return DRIVERS.keySet().size();
    }

    /**
     * Load the available formats
     *
     * @throws IOException
     */
    private static Map<FileFormat, OGRDriver> loadFormats() throws IOException {

        // if the driver list is empty then try to load the ogr drivers

        if (DRIVERS.isEmpty()) {
            try {
                JniOGR ogr = new JniOGR();

                // retrieves the available drivers, if they are required by
                // geOrchestra, they will be added in the list of available
                // formats.
                for (int i = 0; i < ogr.GetDriverCount(); i++) {

                    Object driver = ogr.GetDriver(i);
                    String name = ogr.DriverGetName(driver);
                    LOG.info("try to load driver: " + name);

                    if ("MapInfo File".equalsIgnoreCase(name)) {

                        loadDriver(FileFormat.tab, new OGRDriver(name,
                                new String[] {}));

                        loadDriver(FileFormat.mif, new OGRDriver(name,
                                new String[] { "FORMAT=MIF" }));

                    } else if ("ESRI shapefile".equalsIgnoreCase(name)) {

                        loadDriver(FileFormat.shp, new OGRDriver(name));

                    } else if ("GML".equalsIgnoreCase(name)) {

                        loadDriver(FileFormat.gml, new OGRDriver(name));

                    } else if ("KML".equalsIgnoreCase(name)) {

                        loadDriver(FileFormat.kml, new OGRDriver(name));

                    } else if ("GPX".equalsIgnoreCase(name)) {

                        loadDriver(FileFormat.gpx, new OGRDriver(name));

                    } else if ("OSM".equalsIgnoreCase(name)) {

                        loadDriver(FileFormat.osm, new OGRDriver(name));
                    }
                }

            } catch (Error e) { // catch all exception in the OGR
                LOG.warn("the OGR version installed on the system does not have the drivers required by mapfishapp", e);
                throw new IOException();
            }
        }
        return DRIVERS;
    }

    private static void loadDriver(FileFormat format, OGRDriver ogrDriver) {

        DRIVERS.put(format, ogrDriver);

        LOG.info("format:" + format + " Driver:  " + ogrDriver + " was loaded.");
    }

    /**
     * The list of available ogr drivers can change according to the
     * implementation (and the build options).
     *
     * Returns the list of {@link FileFormat} provided by the OGR instance.
     */
    @Override
    public FileFormat[] getFormatList() {

        Set<FileFormat> keySet = DRIVERS.keySet();
        FileFormat[] formats = keySet.toArray(new FileFormat[keySet.size()]);

        return formats;
    }

    /**
     * Returns the set of features maintained in the geofile, reprojected in the
     * target CRS.
     *
     * @throws IOException
     *             , UnsupportedGeofileFormatException
     */
    @Override
    public SimpleFeatureCollection getFeatureCollection(final File file,
            final FileFormat fileFormat,
            final CoordinateReferenceSystem targetCRS) throws IOException,
            UnsupportedGeofileFormatException {
        assert file != null && fileFormat != null;

        try {
            String fullFileName = file.getAbsolutePath();

            OGRDriver driver = DRIVERS.get(fileFormat);
            if (driver == null) {
                throw new UnsupportedGeofileFormatException(
                        "The file format is not supported: " + fileFormat);
            }
            String ogrDriver = driver.getName();

            OGRDataStore store = new OGRDataStore(fullFileName, ogrDriver, null, new JniOGR());
            String[] typeNames = store.getTypeNames();
            if (typeNames.length == 0) {
                final String msg = "The file " + fullFileName
                        + " could not be read using the OGR driver "
                        + ogrDriver;
                LOG.error(msg);
                throw new IOException(msg);
            }
            final String typeName = typeNames[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);

            Query query = new Query(typeName, Filter.INCLUDE);
            // if the OGRDataStore does not have a CRS, it can lead to issues
            // afterwards, while trying to reproject the features.
            CoordinateReferenceSystem scrs = source.getSchema().getCoordinateReferenceSystem();
            if (scrs == null) {
                LOG.error("error with the provided data: Unable to find the underlying coordinate reference system");
                // Note: pure geotools implementation should fallback afterwards
                throw new ProjectionException("Unknown SRS on the provided data");
            }
            // if the CRS was set the features must be transformed when the
            // query is executed.
            if (targetCRS != null) {
                query.setCoordinateSystemReproject(targetCRS);
            }

            SimpleFeatureCollection features = source.getFeatures(query);

            return features;

        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public SimpleFeatureCollection getFeatureCollection(File basedir,
            FileFormat fileFormat) throws IOException,
            UnsupportedGeofileFormatException {

        return getFeatureCollection(basedir, fileFormat, null);
    }

    /**
     * Checks whether all drivers required are available.
     *
     * @return true means that the drivers are available.
     */
    public static boolean isOK() {

        try {
            Class.forName("org.geotools.data.ogr.jni.JniOGR");

        } catch (Throwable e) {

            LOG.info("gdal/ogr is not available in the system", e);

            return false;
        }

        // checks the driver status
        try {

            Map<FileFormat, OGRDriver> loadFormats = loadFormats();
            if (loadFormats.isEmpty()) {
                LOG.warn("the list of ogr drivers is empty");
                return false;
            }

        } catch (IOException e) {
            LOG.warn(
                    "the ogr installed in the system doesn't have the drivers required by mapfish",
                    e);
            return false;
        }

        LOG.info("gdal/ogr is available in the system");

        return true;
    }

    @Override
    public boolean isSupportedFormat(FileFormat fileFormat) {

        for (FileFormat supported : DRIVERS.keySet()) {
            if (fileFormat == supported) {
                return true;
            }
        }
        return false;
    }

	@Override
	public boolean allowsGeoToolsFallback() {
		return true;
	}

}