/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.Query;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.ogr.jni.JniOGR;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * OGR Feature Reader.
 * 
 * <p>
 * This class is responsible of retrieving the features stored in different file format.
 * </p>
 * <p>
 * The available file format are enumerated in the {@link FileFormat}.
 * </p>
 * 
 * 
 * @author Mauricio Pazos
 * 
 */
final class OGRFeatureReader implements FeatureFileReaderImplementor {

	private static final Log LOG = LogFactory.getLog(OGRFeatureReader.class.getPackage().getName());


	public OGRFeatureReader() {

	}


	/**
	 * Returns the set of features maintained in the geofile, reprojected in the target CRS
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(final File basedir, final FileFormat fileFormat, final CoordinateReferenceSystem targetCRS) throws IOException {
		assert  basedir != null && fileFormat != null;

		try{
			String ogrName = basedir.getAbsolutePath();
			String ogrDriver = fileFormat.getDriver();

			OGRDataStore store = new OGRDataStore(ogrName, ogrDriver, null,  new JniOGR() );
			String[] typeNames = store.getTypeNames();
			if(typeNames.length ==  0 ){
				final String  msg= "The file " + ogrName + " could not be read using the OGR driver " + ogrDriver;
				LOG.error(msg);
				throw new IOException(msg);
			}
	        final String typeName =  typeNames[0];
			SimpleFeatureSource source = store.getFeatureSource(typeName);

			Query query = new Query(typeName, Filter.INCLUDE);
			// if the CRS was set the features must be transformed when the query is executed.
			if(targetCRS != null){
				query.setCoordinateSystemReproject(targetCRS);
			}

			SimpleFeatureCollection features = source.getFeatures(query);

			return features;
		} catch(IOException e ){
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}


	@Override
	public SimpleFeatureCollection getFeatureCollection(File basedir, FileFormat fileFormat)
			throws IOException {
			
		return getFeatureCollection(basedir, fileFormat, null);
	}
	
	
	@Override
	public FileFormat[] getFormats() {


		// TODO Auto-generated method stub

		return null;
	}

}