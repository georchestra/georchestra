package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Feature geofile reader interface.
 *
 * <p>
 * The implementations of this interface provide the access to the feature which are stored in specific file formats.
 * </p>
 *
 * @author Mauricio Pazos
 */
interface FeatureGeoFileReader {

    /**
     * Returns the set of features maintained in the geofile.
     *
     * @param file
     * @param fileFormat
     *
     * @return {@link SimpleFeatureCollection}
     * @throws IOException
     *             , UnsupportedGeofileFormatException
     *             , ProjectionException
     */
    SimpleFeatureCollection getFeatureCollection(final File file,
            final FileFormat fileFormat) throws IOException,
            UnsupportedGeofileFormatException, ProjectionException;

    /**
     * Returns the set of features maintained in the geofile, reprojected in the
     * target CRS.
     *
     * @param file
     *            path and file name
     * @param fileFormat
     * @param targetCrs
     *
     * @return {@link SimpleFeatureCollection} in the target CRS
     *
     * @throws IOException
     *             , UnsupportedGeofileFormatException
     */
    SimpleFeatureCollection getFeatureCollection(final File file,
            final FileFormat fileFormat,
            final CoordinateReferenceSystem targetCrs) throws IOException,
            UnsupportedGeofileFormatException, ProjectionException;

    /**
     * @return List of available {@link FileFormat}
     */
    FileFormat[] getFormatList();

    /**
     * Returns true if the file format is supported
     *
     * @param fileFormat
     * @return
     */
    boolean isSupportedFormat(FileFormat fileFormat);

	boolean allowsGeoToolsFallback();

}