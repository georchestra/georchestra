/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.ServiceInfo;
import org.geotools.data.mif.MIFDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pvalsecc.misc.FileUtilities;

/**
 * This implementation is a façade to the Geotools store implementations.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
class GeotoolsFeatureReader implements FeatureFileReaderImplementor {

	private static final Log LOG = LogFactory.getLog(GeotoolsFeatureReader.class.getPackage().getName());
	
	private static final FileFormat[] formats = new FileFormat[] {
			FileFormat.shp, 
			FileFormat.mif, 
			FileFormat.gpx, 
			FileFormat.gml,
			FileFormat.kml };

	public GeotoolsFeatureReader() {
	}


	@Override
	public FileFormat[] getFormatList() {
		// TODO Auto-generated method stub
		return formats;
	}

	@Override
	public SimpleFeatureCollection getFeatureCollection(File file,
			FileFormat fileFormat, CoordinateReferenceSystem targetCRS)
			throws IOException {
		assert file != null && fileFormat != null;

		// TODO Auto-generated method stub
		switch(fileFormat){
		case shp:
			return readShpFile(file, targetCRS);

		case mif:
			return readMifFile(file, targetCRS);

		default:
			throw new IOException("Unsuported format: " + fileFormat.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.georchestra.mapfishapp.ws.upload.FeatureFileReaderImplementor#getFeatureCollection()
	 */
	@Override
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat) throws IOException {

		return getFeatureCollection(file, fileFormat, null);

	}

	/**
	 * Reads the features from MIF file.
	 * 
	 * @param file
	 * @return {@link SimpleFeatureCollection}
	 * 
	 * @throws IOException
	 */
	private SimpleFeatureCollection readMifFile(final File file, final CoordinateReferenceSystem crs) throws IOException{

		HashMap params = new HashMap();
        try {
        	Integer code = CRS.lookupEpsgCode(crs, true);
        	params.put(MIFDataStoreFactory.PARAM_COORDSYS.key, code );
        } catch (Exception e) {
            LOG.warn("unable to convert "+ crs  + " to a EPSG code", e);
        }
		
        params.put(MIFDataStoreFactory.PARAM_PATH.key, file.getAbsolutePath());
        
		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);

		String typeName = FilenameUtils.getBaseName(file.getAbsolutePath());
		SimpleFeatureType schema = store.getSchema(typeName);

		SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures();

		return features;
	}

	/**
	 * Reads the features from Shape file.
	 * 
	 * @param basedir
	 * @return {@link SimpleFeatureCollection}
	 * 
	 * @throws IOException
	 */
	private SimpleFeatureCollection readShpFile(final File basedir, CoordinateReferenceSystem crs) throws IOException {

		ShapefileDataStoreFactory storeFactory = new ShapefileDataStoreFactory();

		FileDataStore store = storeFactory.createDataStore(basedir.toURI().toURL());

		SimpleFeatureType schema = store.getSchema();
		SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures();

		return features;
	}

}