/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;

/**
 * This class implements the shape file writing strategy
 * 
 * @author Mauricio Pazos
 *
 */
final class ShpFeatureWriter extends FileFeatureWriter {

	/**
	 * New instance of {@link OGRFeatureWriter}
	 * 
	 * @param progressListener 
	 * @param schema		output schema
	 * @param basedir		output folder
	 * @param features		input the set of Features to write
	 */
	public ShpFeatureWriter(
			ProgressListener progresListener, 
			SimpleFeatureType schema,
			File basedir,
			SimpleFeatureCollection features) {

		super(progresListener, schema, basedir, features);
	
	}

	/**
	 * @return {@link ShpDatastoreFactory}
	 */
	@Override
	protected DatastoreFactory getDatastoreFactory() throws  IOException{
		return new ShpDatastoreFactory();
	}

}
