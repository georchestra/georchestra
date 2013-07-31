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
class AbstractFeatureGeoFileReader implements FeatureGeoFileReader{

	/** check if the OGR implementation is live */
	private static boolean OGR_AVAILABLE;
	static{
		OGR_AVAILABLE = OGRFeatureReader.isOK();
	}

	protected FeatureGeoFileReader readerImpl = null;

	/**
	 * Creates a new instance of {@link AbstractFeatureGeoFileReader}.
	 * 
	 * @param basedir file to read
	 * @param fileFormat the format
	 */
	public AbstractFeatureGeoFileReader() {
		this.readerImpl = createImplementationStrategy();
	}
	

	/**
	 * Creates a new instance of {@link AbstractFeatureGeoFileReader}. The reader will use the implementation provided as parameter.
	 * 
	 * @param impl
	 */
	public AbstractFeatureGeoFileReader(FeatureGeoFileReader impl) {
		
		this.readerImpl = impl;
	}

	/**
	 * @return the list of available format depending on the reader implementation.
	 */
	@Override
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
	 * @throws UnsupportedGeofileFormatException 
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat) throws IOException, UnsupportedGeofileFormatException {

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
	 * @throws UnsupportedGeofileFormatException 
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat, final CoordinateReferenceSystem targetCrs) throws IOException, UnsupportedGeofileFormatException {
		
		try{
			return  this.readerImpl.getFeatureCollection(file, fileFormat, targetCrs);
			
		} catch(IOException e){

			if (!(this.readerImpl instanceof GeotoolsFeatureReader)) {
				// switches to geotools implementation

				OGR_AVAILABLE = false;
				this.readerImpl = new GeotoolsFeatureReader();

				// now try to read using the geotools implementation
				try {
					return this.readerImpl.getFeatureCollection(file, fileFormat, targetCrs);
					
				} catch (UnsupportedGeofileFormatException gtUnsupoortFileFormat) {
					throw gtUnsupoortFileFormat;
				}
			} else {
				// it is Geotools implementation, so this class cannot manage the exception;
				throw new IOException(e);
			}
				
		} catch (UnsupportedGeofileFormatException e) {
			throw e;
		}
	}

	/**
	 * Selects which of the implementations must be created.
	 */
	private static FeatureGeoFileReader createImplementationStrategy(){

		FeatureGeoFileReader implementor = null; 
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
