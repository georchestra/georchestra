package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Feature file reader interface.
 * 
 * <p>
 * The implementations of this interface provides the access to the feature which are stored in specific file formats. 
 * </p>
 * 
 * @author Mauricio Pazos
 */
interface FeatureFileReaderImplementor {
	
	/**
	 * Returns the set of features maintained in the geofile.
	 * 
	 * @param file
	 * @param fileFormat
	 * 
	 * @return {@link SimpleFeatureCollection}
	 * @throws IOException
	 */
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat) throws IOException;

	/**
	 * Returns the set of features maintained in the geofile, reprojected in the target CRS.
	 * 
	 * @param file path and file name
	 * @param fileFormat
	 * @param targetCrs
	 * 
	 * @return {@link SimpleFeatureCollection} in the target CRS
	 * 
	 * @throws IOException
	 */
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat, final CoordinateReferenceSystem targetCrs) throws IOException;

	/**
	 * @return List of available format
	 */
	public FileFormat[] getFormatList();

}