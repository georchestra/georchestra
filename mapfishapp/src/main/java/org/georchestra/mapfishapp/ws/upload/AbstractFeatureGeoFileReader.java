/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Defines the abstract interface (Bridge Pattern). This class is responsible of create the implementation OGR or Geotools for the 
 * feature reader. Thus the client don't need to create a specific reader implementation.
 * 
 * @author Mauricio Pazos
 */
class AbstractFeatureGeoFileReader implements FeatureGeoFileReader{

	
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
	 * 
	 * <p>
	 * The default implementation will be OGR if it was installed in the system.
	 * </p>
	 * 
	 * @param basedir file to read
	 * @param fileFormat the format
	 */
	public AbstractFeatureGeoFileReader() {
		setReaderImpl(createImplementationStrategy());
	}
	

	/**
	 * Creates a new instance of {@link AbstractFeatureGeoFileReader}. The reader will use the implementation provided as parameter.
	 * 
	 * @param impl
	 */
	public AbstractFeatureGeoFileReader(FeatureGeoFileReader impl) {
		
		setReaderImpl(impl);
	}

	/**
	 * @return the list of available format depending on the reader implementation.
	 */
	@Override
	public FileFormat[] getFormatList(){

		return getReaderImpl().getFormatList();
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
			return  getReaderImpl().getFeatureCollection(file, fileFormat, targetCrs);
			
		} catch(IOException e){

			if (!(getReaderImpl() instanceof GeotoolsFeatureReader)) {
				
				switchToGeotoolsImplementation();

				// now try to read using the geotools implementation
				try {
					return getReaderImpl().getFeatureCollection(file, fileFormat, targetCrs);
					
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
	 * switches to geotools implementation
	 */
	private void switchToGeotoolsImplementation() {
		setReaderImpl( new GeotoolsFeatureReader() );
	}


	/**
	 * Selects which of the implementations must be created.
	 */
	private static FeatureGeoFileReader createImplementationStrategy(){

		FeatureGeoFileReader implementor = null; 
		if( OGRFeatureReader.isOK() ){

			implementor = new OGRFeatureReader();

		} else { 

			implementor = new GeotoolsFeatureReader();
		}
		return implementor;
	}



}
