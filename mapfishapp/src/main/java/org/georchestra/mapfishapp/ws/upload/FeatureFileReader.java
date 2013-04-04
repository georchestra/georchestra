/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Defines the abstract interface (Bridge Pattern). This class is responsible of create the implementation OGR or Geotools for the 
 * feature reader. Thus the client don't need to create a specific reader implementation. 
 * 
 * @author Mauricio Pazos
 */
class FeatureFileReader {

	private final FeatureFileReaderImplementor readerImpl;

	/**
	 * Creates a reader
	 * 
	 * @param basedir file to read
	 * @param fileFormat the format
	 */
	public FeatureFileReader() {
		this.readerImpl = createImplementationStrategy();
	}

	/**
	 * @return the list of available format depending on the reader implementation.
	 */
	public FileFormat[] getFormatList(){

		return this.readerImpl.getFormats();
	}


	/**
	 * Returns the feature collection contained by the file.
	 * 
	 * @param file
	 * @param fileFormat
	 * @param targetCrs crs used to reproject the returned feature collection
	 * 
	 * @return {@link SimpleFeatureCollection}
	 * 
	 * @throws IOException
	 */
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat) throws IOException {

		return this.readerImpl.getFeatureCollection(file, fileFormat);
	}

	/**
	 * Returns the feature collection contained by the file. The features will be reprojected to the target CRS
	 * 
	 * @param file path and file name
	 * @param fileFormat 
	 * @param targetCrs crs used to reproject the returned feature collection
	 * 
	 * @return {@link SimpleFeatureCollection}
	 * @throws IOException
	 */
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat, final CoordinateReferenceSystem targetCrs) throws IOException {

		return this.readerImpl.getFeatureCollection(file, fileFormat, targetCrs);
	}

	/**
	 * Selects which of the implementations must be created.
	 */
	private FeatureFileReaderImplementor createImplementationStrategy(){

		FeatureFileReaderImplementor implementor = null; 
		if( isOgrAvailable() ){

			implementor = new OGRFeatureReader();

		} else { // by default the geotools implementation is created

			implementor = new GeotoolsFeatureReader();
		}
		return implementor;
	}


	/**
	 * Decides what is the implementation must be instantiate.
	 *  
	 * @return true if ogr is available in the platform.
	 */
	private boolean isOgrAvailable() {

		return false;
	}

}
