/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.json.JSONArray;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Defines the abstract interface (Bridge Pattern). This class is responsible of
 * create the implementation Geotools for the feature reader. Thus the
 * client don't need to create a specific reader implementation.
 *
 * @author Mauricio Pazos
 */
public class AbstractFeatureGeoFileReader implements FeatureGeoFileReader {

    private static final Log LOG = LogFactory.getLog(AbstractFeatureGeoFileReader.class.getPackage().getName());

    protected FeatureGeoFileReader readerImpl = null;

    private FeatureGeoFileReader getReaderImpl() {
        LOG.info("Using implementation: " + this.readerImpl.getClass().getName());
        return this.readerImpl;
    }

    private void setReaderImpl(FeatureGeoFileReader readerImpl) {
        LOG.info("It was set: " + readerImpl.getClass().getName());
        this.readerImpl = readerImpl;
    }

    /**
     * Creates a new instance of {@link AbstractFeatureGeoFileReader}.
     */
    public AbstractFeatureGeoFileReader() {
        setReaderImpl(new GeotoolsFeatureReader());
    }

    /**
     * @return the list of available format depending on the reader
     *         implementation.
     */
    @Override
    public FileFormat[] getFormatList() {
        return getReaderImpl().getFormatList();
    }

    public JSONArray getFormatListAsJSON() {
        JSONArray ret = new JSONArray();

        FileFormat[] ff = getFormatList();
        for (FileFormat f: ff) {
            ret.put(f.toString());
        }
        return ret;
    }

    /**
     * Returns the feature collection contained by the file.
     *
     * @param file
     * @param fileFormat
     *
     * @return {@link SimpleFeatureCollection}
     *
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    @Override
    public SimpleFeatureCollection getFeatureCollection(File file, FileFormat fileFormat)
            throws IOException, UnsupportedGeofileFormatException, ProjectionException {

        return getFeatureCollection(file, fileFormat, null);
    }

    /**
     * Returns the feature collection contained by the file. The features will
     * be reprojected to the target CRS
     *
     * @param file
     *            path and file name
     * @param fileFormat
     * @param targetCrs
     *            crs used to reproject the returned feature collection
     *
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    @Override
    public SimpleFeatureCollection getFeatureCollection(File file, FileFormat fileFormat, CoordinateReferenceSystem targetCrs)
            throws IOException, UnsupportedGeofileFormatException, ProjectionException {

        return getReaderImpl().getFeatureCollection(file, fileFormat, targetCrs);
    }

    @Override
    public boolean isSupportedFormat(FileFormat fileFormat) {
        return this.readerImpl.isSupportedFormat(fileFormat);
    }
}
