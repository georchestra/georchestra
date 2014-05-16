package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.ogr.jni.JniOGRDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.util.ProgressListener;

/**
 * This writer sets the OGRDataStore that is responsible of generating the vector file in the format required.
 *
 * <p>
 * Note: this was written thinking in future extensions to support more format. Right now TAB format is my goal.
 * The extension should be very simple adding the new format and driver in the {@link FileFormat} enumerate type.
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
class OGRFeatureWriter implements FeatureWriterStrategy {
    private static final Log LOG = LogFactory.getLog(OGRFeatureWriter.class.getPackage().getName());

	/**
	 * Maintains the set of valid formats with theirs driver descriptors associated
	 */

	public  enum FileFormat{

		tab {
			@Override
			public String getDriver(){return "MapInfo File";}

			@Override
			public String[] getFormatOptions(){return new String[]{};}
		},
		mif {

			@Override
			public String getDriver() {	return "MapInfo File";}

			@Override
			public String[] getFormatOptions() { return new String[]{"FORMAT=MIF"};	}

		},
		shp {
			@Override
			public String getDriver(){return "ESRI shapefile";}

		},
		kml {
			@Override
			public String getDriver(){return "KML file";}

		};

		/**
		 * Returns the OGR driver for this format
		 * @return the driver
		 */
		public abstract String getDriver();

		/**
		 * Returns the options related with the indicated file format.
		 * @return the options for the file format
		 */
		public String[] getFormatOptions(){return null;}
	}

	private ProgressListener progresListener;
	private final SimpleFeatureType schema;
	private final File basedir;
	private final SimpleFeatureCollection features;
	private final FileFormat fileFormat;
	private final String[] options;

	/**
	 * New instance of {@link OGRFeatureWriter}
	 *
	 * @param progressListener
	 * @param schema		output schema
	 * @param basedir		output folder
	 * @param fileFormat 	output fileExtension
	 * @param features		input the set of Features to write
	 */
	public OGRFeatureWriter(
			ProgressListener progressListener,
			SimpleFeatureType schema,
			File basedir,
			FileFormat fileFormat,
			SimpleFeatureCollection features) {

		assert schema != null && basedir != null && features != null;

		this.progresListener = progresListener;
		checkSchema(schema);
		this.schema = schema;
		this.basedir = basedir;
		this.fileFormat = fileFormat;

		this.options = fileFormat.getFormatOptions();

		this.features = features;
	}


	/**
	 * checks whether the schema is valid.
	 * <p>
	 * This method will log some warnings whether the schema is not valid.
	 * </p>
	 * @param schema
	 */
	private boolean checkSchema(SimpleFeatureType schema) {

		boolean hasGeometry = false;
		boolean hasAttr = false;
		boolean nameLimitOK = true;
        for (int i = 0, j = 0; i < schema.getAttributeCount(); i++) {

            AttributeDescriptor ad = schema.getDescriptor(i);
            if (ad == schema.getGeometryDescriptor()) {
            	hasGeometry = true;
            } else {
            	hasAttr = true;
            }
            if(ad.getLocalName().length() > 10){
            	nameLimitOK = false;
            	LOG.warn("Some format requires that the properties' name have got less than 10 character. Take into account this warnning if you experiment problems."
            			+ " Schema: "+ schema.getTypeName() + " Property:" + ad.getLocalName());
            }
        }
        if(!hasGeometry){
        	LOG.warn("The Schema " + schema.getTypeName() + "doesn't contain a geomety property");
        }
        if(!hasAttr){
        	LOG.warn("The Schema " + schema.getTypeName() + "doesn't contain any alfanumeric property");
        }

        return hasGeometry && hasAttr && nameLimitOK;
	}


	/**
	 * Generate the file's vector specified
	 * @return array {@link File} of created files
	 */
	@Override
	public File[] generateFiles() throws IOException {

		Map<String, Serializable> map = new java.util.HashMap<String, Serializable>();

        final String pathName = this.basedir.getAbsolutePath() + File.separatorChar + FileUtils.createFileName(this.basedir.getAbsolutePath(), this.schema, this.fileFormat);
		map.put(OGRDataStoreFactory.OGR_NAME.key, pathName);
		map.put(OGRDataStoreFactory.OGR_DRIVER_NAME.key, this.fileFormat.getDriver());

		File[] files = new File[]{};
        OGRDataStore ds = null;
        try {
            ds = (OGRDataStore) DataStoreFinder.getDataStore(map);
            // Sometimes GeoTools is unable to find a datastore
            // even if an OGRDataStore can actually be created.
            // Trying another way ...
            if (ds == null) {
                try {
                    Class.forName("org.gdal.ogr.ogr");
                } catch (Throwable e) {
                    throw new IllegalStateException("OGRDataStore couldn't be created, please check GDAL librairies are correctly installed on your machine");
                }
                ds = (OGRDataStore) new JniOGRDataStoreFactory().createNewDataStore(map);
            }
            if (ds == null) {
            	throw new IllegalStateException("OGRDataStore couldn't be created, please check GDAL librairies are correctly installed on your machine");
            }
	        ds.createSchema(this.features, true, this.options); //TODO OGR require the following improvements:  use the output crs required (progress Listener should be a parameter)

	        files =  new File[]{new File( pathName)};

        } catch (NullPointerException e) {
        	LOG.error("OGRDataStore couldn't be created, please check GDAL librairies are correctly installed on your machine");
        	throw e;
        }
        finally {
            if(ds != null){
            	ds.dispose();
            }
        }
        return files;
	}

	protected DataStore getDataStore() throws  IOException{

		ShapefileDataStore ds = new ShapefileDataStore(basedir.toURI().toURL());
        if(!basedir.exists()){
            ds.createSchema(this.schema);
        }

        return ds;

	}

}
