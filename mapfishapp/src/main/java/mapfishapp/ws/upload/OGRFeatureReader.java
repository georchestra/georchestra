/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.ogr.jni.JniOGR;
import org.geotools.data.ogr.jni.JniOGRDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;

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
final class OGRFeatureReader {
	
	private static final Log LOG = LogFactory.getLog(OGRFeatureReader.class.getPackage().getName());


	/**
	 * 
	 * File format available.
	 *
	 */
	public enum FileFormat {
		tab {
			@Override
			public String getDriver() {
				return "MapInfo File";
			}

			@Override
			public String[] getFormatOptions() {
				return new String[] {};
			}
		},
		mif {
			@Override
			public String getDriver() {
				return "MapInfo File";
			}

			@Override
			public String[] getFormatOptions() {
				return new String[] { "FORMAT=MIF" };
			}
		},
		shp {
			@Override
			public String getDriver() {
				return "ESRI shapefile";
			}
		},
		gml {
			@Override
			public String getDriver() {
				return "GML";
			}
		},
		kml {
			@Override
			public String getDriver() {
				return "KML";
			}

		},
		gpx {
			@Override
			public String getDriver() {
				return "GPX";
			}

		};
		

		/**
		 * Returns the OGR driver for this format.
		 * 
		 * @return the driver
		 */
		public abstract String getDriver();

		/**
		 * @return Options for this format
		 */
		public String[] getFormatOptions() {
			return null; //default implementation
		}

		/**
		 * Returns the enumerated value associated to the extension file name
		 *  
		 * @param ext
		 * @return FileFormat enumerated value or null if it doesn't exist.
		 */
		public static FileFormat getFileFormat(String ext) {
			
			if("tab".equalsIgnoreCase(ext))	return tab;
			if("mif".equalsIgnoreCase(ext))	return mif;
			if("shp".equalsIgnoreCase(ext))	return shp;
			if("gml".equalsIgnoreCase(ext))	return gml;
			if("gpx".equalsIgnoreCase(ext))	return gpx;
			if("kml".equalsIgnoreCase(ext))	return kml;
			
			return null;
		}
	}

	private File basedir;
	private FileFormat fileFormat;
	private SimpleFeatureCollection features;

	public OGRFeatureReader(File basedir, FileFormat fileFormat) {

		assert  basedir != null && features != null;

		this.basedir = basedir;
		this.fileFormat = fileFormat;

	}

	/**
	 * Returns the set of features maintained in the geofile.
	 * 
	 * @return {@link SimpleFeatureCollection}
	 * @throws IOException
	 */
	public SimpleFeatureCollection getFeatureCollection() throws IOException {

		try{
			String ogrName = this.basedir.getAbsolutePath();
			String ogrDriver = this.fileFormat.getDriver();
			
			JniOGRDataStoreFactory jniFactory = JniOGRDataStoreFactory.class.newInstance();
			OGRDataStore store = new OGRDataStore(ogrName, ogrDriver, null,  new JniOGR() );
	        SimpleFeatureSource source = store.getFeatureSource(store.getTypeNames()[0]);

	        return source.getFeatures();
			
		} catch(Exception e ){
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}

}
