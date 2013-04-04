/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.ServiceInfo;
import org.geotools.data.mif.MIFDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This implementation is a façade to the Geotools store implementations.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
class GeotoolsFeatureReader implements FeatureFileReaderImplementor {

	private static final FileFormat[] formats = new FileFormat[] {
			FileFormat.shp, 
			FileFormat.mif, 
			FileFormat.gpx, 
			FileFormat.gml,
			FileFormat.kml };

	

	public GeotoolsFeatureReader() {
	}


	@Override
	public FileFormat[] getFormats() {
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
	 * @param basedir
	 * @return {@link SimpleFeatureCollection}
	 * 
	 * @throws IOException
	 */
	private SimpleFeatureCollection readMifFile(File basedir, CoordinateReferenceSystem crs) throws IOException{

		HashMap params = new HashMap();
//        try {
//            Integer crs = CRS.lookupEpsgCode(schema.getCoordinateReferenceSystem(), true);
//            params.put(MIFDataStore.PARAM_SRID, crs );
//        } catch (FactoryException e) {
//            LOG.warn("unable to convert "+schema.getCoordinateReferenceSystem()+" to a EPSG code", e);
//        }
		
        params.put(MIFDataStoreFactory.PARAM_PATH, basedir.getAbsolutePath());
        
		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);

		ServiceInfo info = store.getInfo();
		URI uriSchema  = info.getSchema();
		
		SimpleFeatureType schema = store.getSchema(uriSchema.toString());

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