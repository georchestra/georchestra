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

			@Override
			public String[] getFormatOptions() {
				return null;
			}
		},
		gml {
			@Override
			public String getDriver() {
				return "GML";
			}

			@Override
			public String[] getFormatOptions() {
				return null;
			}
		};
		// FIXME more format are required (kml, gpx)
		

		/**
		 * Returns the OGR driver for this format
		 * 
		 * @return the driver
		 */
		public abstract String getDriver();

		public abstract String[] getFormatOptions();

		public static FileFormat getFileType(String ext) {
			
			if("tab".equalsIgnoreCase(ext))	return tab;
			if("mif".equalsIgnoreCase(ext))	return mif;
			if("shp".equalsIgnoreCase(ext))	return shp;
			if("gml".equalsIgnoreCase(ext))	return gml;
			
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
