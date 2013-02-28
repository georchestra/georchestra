package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
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
			
			@Override
			public String[] getFormatOptions(){return null;}
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
		public abstract String[] getFormatOptions();
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
		this.schema = schema;
		this.basedir = basedir;
		this.fileFormat = fileFormat;
		
		this.options = fileFormat.getFormatOptions();
		
		this.features = features;
	}


	/**
	 * Generate the file's vector specified
	 */
	@Override
	public File[] generateFiles() throws IOException {
        
		Map<String, Serializable> map = new java.util.HashMap<String, Serializable>();
		
        final String pathName = this.basedir.getAbsolutePath() + "/"+ FileUtils.createFileName(this.basedir.getAbsolutePath(), this.schema, this.fileFormat);
		map.put(OGRDataStoreFactory.OGR_NAME.key, pathName);
		map.put(OGRDataStoreFactory.OGR_DRIVER_NAME.key, this.fileFormat.getDriver());
		
		File[] files = new File[]{};
        OGRDataStore ds = null;
        try {
            ds = (OGRDataStore) DataStoreFinder.getDataStore(map);
            
	        ds.createSchema(this.features, true, this.options); //TODO OGR require the following improvements:  use the output crs required (progress Listener should be a parameter)
	        
	        files =  new File[]{new File( pathName)};
	        
        } finally {
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
