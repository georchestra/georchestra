/**
 * 
 */
package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * This abstract class defines the template strategy required to write different sort of vector files.
 * 
 * @author Mauricio Pazos
 *
 */
abstract class FileFeatureWriter implements FeatureWriterStrategy {

	protected ProgressListener progresListener;
	protected SimpleFeatureType schema;
	protected File basedir;
	protected FeatureCollection<SimpleFeatureType, SimpleFeature> features;
	

	/**
	 * Sets the strategy parameters
	 * 
	 * @param progresListener
	 * @param schema	 output schema
	 * @param basedir	 output base folder
	 * @param features	 the input set of features to write
	 */
	public FileFeatureWriter(
			ProgressListener progresListener,
			SimpleFeatureType schema,
			File basedir,
			SimpleFeatureCollection features) {

		this.progresListener = progresListener;
		this.schema = schema;
		this.basedir = basedir;
		this.features = features;
	
	}
	
	/**
	 * Generates a vector files in the specified format
	 * 
	 * @throws IOException 
	 */
	@Override
	public File[] generateFiles() throws IOException {
		
        DatastoreFactory ds = getDatastoreFactory();
    
        // the sources features are projected in the requested output projections
        CoordinateReferenceSystem outCRS = this.features.getSchema().getCoordinateReferenceSystem();
        WriteFeatures writeFeatures = new WriteFeatures(this.schema, this.basedir, outCRS, ds);

        this.features.accepts(writeFeatures, this.progresListener);
        
        writeFeatures.close();

        return writeFeatures.getShapeFiles ();
	}

	/**
	 * @return a {@link DatastoreFactory} instance
	 * @throws IOException
	 */
	protected abstract DatastoreFactory getDatastoreFactory() throws  IOException;
	
	
	
}
