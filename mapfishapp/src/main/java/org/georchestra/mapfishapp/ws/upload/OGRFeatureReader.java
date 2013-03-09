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
		 * @param fileExtension file extension
		 * @return FileFormat enumerated value or null if it doesn't exist.
		 */
		public static FileFormat getFileFormat(String fileExtension) {
			
			if("tab".equalsIgnoreCase(fileExtension))	return tab;
			if("mif".equalsIgnoreCase(fileExtension))	return mif;
			if("shp".equalsIgnoreCase(fileExtension))	return shp;
			if("gml".equalsIgnoreCase(fileExtension))	return gml;
			if("gpx".equalsIgnoreCase(fileExtension))	return gpx;
			if("kml".equalsIgnoreCase(fileExtension))	return kml;
			
			return null;
		}
	}
	
	private File basedir;
	private FileFormat fileFormat;
	private CoordinateReferenceSystem targetCRS;

	/**
	 * New instance of OGRFeatureReader.
	 * 
	 * @param file the file that contains the features to read
	 * @param fileFormat the file format
	 * @param targetCRS the coordinate reference that will be used to transform the feature collection 
	 */
	public OGRFeatureReader(File file, FileFormat fileFormat, CoordinateReferenceSystem crs) {

		assert  file != null && fileFormat != null && crs != null;

		this.basedir = file;
		this.fileFormat = fileFormat;
		this.targetCRS = crs;
	}
	
	/**
	 * New instance of OGRFeatureReader.
	 * 
	 * @param file the file that contains the features to read
	 * @param fileFormat the file format
	 */
	public OGRFeatureReader(File basedir, FileFormat fileFormat) {

		assert  basedir != null && fileFormat != null;

		this.basedir = basedir;
		this.fileFormat = fileFormat;
		this.targetCRS = null;

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
			
			OGRDataStore store = new OGRDataStore(ogrName, ogrDriver, null,  new JniOGR() );
	        final String typeName = store.getTypeNames()[0];
			SimpleFeatureSource source = store.getFeatureSource(typeName);

			Query query = new Query(typeName, Filter.INCLUDE);
			// if the CRS was set the features must be transformed when the qury is executed.
			if(this.targetCRS != null){
				query.setCoordinateSystemReproject(this.targetCRS);
			}
			
			SimpleFeatureCollection features = source.getFeatures(query);
			
			return features;

		} catch(Exception e ){
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}

}
