package extractorapp.ws.extractor;

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
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import extractorapp.ws.extractor.WfsExtractor.GeomType;

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
	 * 
	 */
	public  enum FileFormat{
		tab;
		
		public String getDriver(FileFormat ext) throws IOException{
			switch (ext) {
			case tab:
				return "MapInfo File";

			default:
				throw new IOException("there is not a driver for the extension file: " + ext);
			}
		}
	}
	
	private ProgressListener progresListener;
	private SimpleFeatureType schema;
	private File basedir;
	private SimpleFeatureCollection features;
	private FileFormat fileFormat; 
	
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

		this.progresListener = progresListener;
		this.schema = schema;
		this.basedir = basedir;
		this.fileFormat = fileFormat;
		this.features = features;
	}

	/**
	 * Generate the file's vector specified
	 */
	@Override
	public File[] generateFiles() throws IOException {
        
		Map<String, Serializable> map = new java.util.HashMap<String, Serializable>();
		
        final String pathName = this.basedir.getAbsolutePath() + "/"+ createFileName(this.basedir.getAbsolutePath(), this.schema, this.fileFormat.tab);
		map.put(OGRDataStoreFactory.OGR_NAME.key, pathName);
		map.put(OGRDataStoreFactory.OGR_DRIVER_NAME.key, this.fileFormat.getDriver(this.fileFormat));
		
		File[] files = new File[]{};
        OGRDataStore ds = null;
        try {
            ds = (OGRDataStore) DataStoreFinder.getDataStore(map);
            
	        ds.createSchema(this.features, true, new String[]{}); //TODO OGR require the following improvements:  use the output crs required (progress Listener should be a parameter)
	        
	        files =  new File[]{new File( pathName)};
	        
        } finally {
            if(ds != null){
            	ds.dispose();
            }
        }		
        return files;
	}
	
	/**
	 * Creates a new the file's name. 
	 * 
	 * TODO refactoring this code is similar to WriteFeature.getDatastore.  
	 * 
	 * @param baseDir
	 * @param type
	 * @param ext
	 * @return a file name
	 */
	private static String createFileName(final String baseDir, final SimpleFeatureType type, final FileFormat ext){
		
		String layerName = type.getTypeName();
		Class<?> geomClass = type.getGeometryDescriptor().getType().getBinding();
				
        GeomType geomType = WfsExtractor.GeomType.lookup (geomClass);
				
        String newName = FileUtils.toSafeFileName(layerName + "_" + geomType+"."+ext);
        
        File file = new File(newName);
        for (int i = 1; file.exists(); i++) {
            newName = layerName + "_" + geomType + i;
            newName = FileUtils.toSafeFileName(newName+"."+ext);
            file = new File(baseDir, newName + "." + ext);
        }        
        
        return newName;
		
	}

	protected DataStore getDataStore() throws  IOException{

		ShapefileDataStore ds = new ShapefileDataStore(basedir.toURI().toURL());
        if(!basedir.exists()){
            ds.createSchema(this.schema);
        }
        
        return ds;
		
	}

}
