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

	private static boolean OGR_AVAILABLE;
	static{
		OGR_AVAILABLE = OGRFeatureReader.isOK();
	}

	protected FeatureFileReaderImplementor readerImpl = null;

	/**
	 * Creates a new instance of {@link FeatureFileReader}.
	 * 
	 * @param basedir file to read
	 * @param fileFormat the format
	 */
	public FeatureFileReader() {
		this.readerImpl = createImplementationStrategy();
	}

	/**
	 * Creates a new instance of {@link FeatureFileReader}. The reader will use the implementation provided as parameter.
	 * 
	 * @param impl
	 */
	public FeatureFileReader(FeatureFileReaderImplementor impl) {
		
		this.readerImpl = impl;
	}

	/**
	 * @return the list of available format depending on the reader implementation.
	 */
	public FileFormat[] getFormatList(){

		return this.readerImpl.getFormatList();
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

		return getFeatureCollection(file, fileFormat, null);
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
		
		try{
			return  this.readerImpl.getFeatureCollection(file, fileFormat, targetCrs);
			
		} catch(Exception e){

			if (!(this.readerImpl instanceof GeotoolsFeatureReader)) {
				// switches to geotools implementation

				OGR_AVAILABLE = false;
				this.readerImpl = new GeotoolsFeatureReader();

				return this.readerImpl.getFeatureCollection(file, fileFormat, targetCrs);
			} else {
				throw new IOException(e);
			}
			
				
		}
	}

	/**
	 * Selects which of the implementations must be created.
	 */
	private static FeatureFileReaderImplementor createImplementationStrategy(){

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
	private static boolean isOgrAvailable() {
		
		return OGR_AVAILABLE;
	}

}
