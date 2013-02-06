/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;

/**
 * 
 * 
 * 
 * @author Mauricio Pazos
 * 
 */
final class OGRFeatureReader {

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
	private String[] options;
	private SimpleFeatureCollection features;

	public OGRFeatureReader(File basedir, FileFormat fileFormat) {

		assert  basedir != null && features != null;

		this.basedir = basedir;
		this.fileFormat = fileFormat;
		
		this.options = fileFormat.getFormatOptions();

	}

	public SimpleFeatureCollection getFeatureCollection() throws IOException {

		String ogrName = this.basedir.getAbsolutePath();
		String ogrDriver = this.fileFormat.getDriver();
		OGRDataStore store = new OGRDataStore(ogrName, ogrDriver, null);
		
        SimpleFeatureSource source = store.getFeatureSource(store.getTypeNames()[0]);

        return source.getFeatures();
	}

}
