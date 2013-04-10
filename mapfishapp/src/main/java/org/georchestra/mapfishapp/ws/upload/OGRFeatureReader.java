/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	
	private static class OGRDriver{
		
		private final String name;
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
	}
	
	private static Map<FileFormat, OGRDriver> DRIVERS = Collections.synchronizedMap(new HashMap<FileFormat, OGRDriver>());
	
	static{
		
		DRIVERS.put( FileFormat.tab, new OGRDriver("MapInfo File", new String[] {} ) );
		DRIVERS.put( FileFormat.mif, new OGRDriver("MapInfo File", new String[] { "FORMAT=MIF" }) );
		DRIVERS.put( FileFormat.shp, new OGRDriver("ESRI shapefile") );
		DRIVERS.put( FileFormat.gml, new OGRDriver("GML" ) );
		DRIVERS.put( FileFormat.kml, new OGRDriver("KML") );
		DRIVERS.put( FileFormat.gpx, new OGRDriver("GPX") );
	}

	public OGRFeatureReader() {

	}

	
	@Override
	public FileFormat[] getFormatList() {

		FileFormat [] fileFormats = new FileFormat[DRIVERS.keySet().size()];
		int i = 0;
		for (FileFormat format : DRIVERS.keySet()) {
			
			fileFormats[i++] = format;
		}
		return fileFormats;
	}
	


	/**
	 * Returns the set of features maintained in the geofile, reprojected in the target CRS
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(final File basedir, final FileFormat fileFormat, final CoordinateReferenceSystem targetCRS) throws IOException {
		assert  basedir != null && fileFormat != null;

		try{
			String ogrName = basedir.getAbsolutePath();

			OGRDriver driver =  DRIVERS.get(fileFormat);
			String ogrDriver = driver.getName();

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
	

}