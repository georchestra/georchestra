/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.GML.Version;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.mif.MIFDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.StreamingParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This class is a façade to the Geotools data management implementations.
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
			FileFormat.gml,
			FileFormat.kml };
	
	public GeotoolsFeatureReader() {
	}

	@Override
	public FileFormat[] getFormatList() {
		return formats;
	}

	@Override
	public SimpleFeatureCollection getFeatureCollection(final File file, final FileFormat fileFormat) throws IOException {

		return getFeatureCollection(file, fileFormat, null);
	}
	
	@Override
	public SimpleFeatureCollection getFeatureCollection(
			final File file,
			final FileFormat fileFormat, 
			final CoordinateReferenceSystem targetCRS)
		throws IOException {
		
		assert file != null && fileFormat != null;

		switch(fileFormat){
		case shp:
			return readShpFile(file, targetCRS);
		case mif:
			return readMifFile(file, targetCRS);
		case gml:
			// TODO it is necessary to figure out how to retrieve the gml version from file
			return  readGmlFile(file, targetCRS, Version.GML2);
		case kml:
			return  readKmlFile(file, targetCRS);

		default:
			throw new IOException("Unsuported format: " + fileFormat.toString());
		}
	}


	/**
	 * Creates a feature collection from a kml file. CRS EPSG:4326 is assumed for the kml file.
	 * 
	 * @param file
	 * @param targetCRS
	 * @return
	 * @throws IOException
	 */
	private SimpleFeatureCollection readKmlFile(File file, CoordinateReferenceSystem targetCRS) throws IOException {
        
		InputStream in = new FileInputStream(file);
		
		try{
			StreamingParser parser = new StreamingParser(new KMLConfiguration(), in, KML.Placemark);

    		// as default EPSG:4326 is assumed
	        int defaultSRID = 4326;
    		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + defaultSRID);

	        MathTransform mathTransform = null;
    		if((targetCRS != null) && !sourceCRS.equals(targetCRS) ){
    			mathTransform = CRS.findMathTransform(sourceCRS, targetCRS);
    		}

    		// parse  the kml file and create the feature collection
			ListFeatureCollection list = null;
	        SimpleFeature f = null;
	        while ((f = (SimpleFeature) parser.parse()) != null) {
        	
        		Geometry geom = (Geometry) f.getDefaultGeometry();
        		int srid = geom.getFactory().getSRID();
        		if(srid > 0 ){
            		geom.setSRID(srid);
        		} else {
            		geom.setSRID(defaultSRID);
        		}
        				
	        	if(list == null){
	        		list = new ListFeatureCollection(f.getFeatureType());
	        	}
	        	if(mathTransform  != null){
	        		// transformation is required
	        		Geometry reprojectedGeometry= JTS.transform(geom, mathTransform);
					f.setDefaultGeometry(reprojectedGeometry);
	        	}
	        	list.add(f);
	        }
	        return list;
	        
		} catch (Exception e){
			LOG.error(e.getMessage());
			throw new IOException(e);
			
		} finally{
			in.close();
		}
	}

	/**
	 * Creates a feature collection from a GML file.
	 * 
	 * @param file a gml file
	 * @param targetCRS target crs
	 * @param version	gml version
	 * @return {@link SimpleFeatureCollection}
	 * @throws IOException
	 */
	private SimpleFeatureCollection readGmlFile(
			final File file,
			final CoordinateReferenceSystem targetCRS,
			final Version version) 
		throws IOException{
		
		InputStream in = new FileInputStream( file );
		try {
			Configuration cfg = (version == Version.GML2)
					? new org.geotools.gml2.GMLConfiguration()
					: new org.geotools.gml3.GMLConfiguration();
			StreamingParser parser = new StreamingParser(cfg , in,  new QName(org.geotools.gml2.GML.NAMESPACE, "featureMember") );
			
			CoordinateReferenceSystem sourceCRS;
	        MathTransform mathTransform = null;
			ListFeatureCollection list = null;
			SimpleFeature feature;
			while( (feature = (SimpleFeature) parser.parse()) != null){
				
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				if (list == null) {
					// the first time sets the feature collection with the schema and crs
					list = new ListFeatureCollection(feature.getFeatureType());
					int srid = geom.getFactory().getSRID();
					if(srid > 0 ){
						sourceCRS = CRS.decode("EPSG:"+ srid );
					} else {
			    		sourceCRS = CRS.decode("EPSG:4326" );
					}
		    		if((targetCRS != null) && !sourceCRS.equals(targetCRS)){
		    			mathTransform = CRS.findMathTransform(sourceCRS, targetCRS);
		    		}
				}
	        	if(mathTransform  != null){
	        		// transformation is required
	        		Geometry reprojectedGeometry= JTS.transform(geom, mathTransform);
					feature.setDefaultGeometry(reprojectedGeometry);
	        	}
	        	list.add(feature);
			}
			return list;
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
			
		} finally {
		
			in.close();
		}
		
		
		// the following strategy doesn't work		
// the following strategy doesn't work		
//		InputStream in = new FileInputStream( file );
//		try {
//			GML gml = new GML(version);
//			SimpleFeatureCollection features = gml.decodeFeatureCollection(in);
//			
//			if( !targetCRS.equals(features.getSchema().getCoordinateReferenceSystem()) ) {
//				features = new ReprojectingFeatureCollection(features, targetCRS);
//			}
//			return features;
//			
//		} catch (Exception e) {
//			LOG.error(e.getMessage());
//			throw new IOException(e);
//		} finally{
//			in.close();
//		}
		
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

        MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(MIFDataStoreFactory.PARAM_PATH.key, file.getAbsolutePath());
        DataStore store = storeFactory.createDataStore(params);

		String typeName = FilenameUtils.getBaseName(file.getAbsolutePath());

		SimpleFeatureCollection features = retrieveFeatures(typeName, store, crs);

		return features;
	}


	/**
	 * Reads the features from Shape file.
	 * 
	 * @param file
	 * @return {@link SimpleFeatureCollection}
	 * 
	 * @throws IOException
	 */
	private SimpleFeatureCollection readShpFile(final File file, final CoordinateReferenceSystem crs) throws IOException {

		ShapefileDataStoreFactory storeFactory = new ShapefileDataStoreFactory();

		FileDataStore store = storeFactory.createDataStore(file.toURI().toURL());

		String typeName = FilenameUtils.getBaseName(file.getAbsolutePath());

		SimpleFeatureCollection features = retrieveFeatures(typeName, store, crs);

		return features;
	}

	/**
	 * Retrieves the features from store
	 * 
	 * @param typeName
	 * @param store
	 * @param targetCRS
	 * @return
	 * @throws IOException
	 */
	private SimpleFeatureCollection retrieveFeatures(
				final String typeName,
				final DataStore store, 
				final CoordinateReferenceSystem targetCRS)
			throws IOException {
		
		SimpleFeatureType schema = store.getSchema(typeName);

		SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());
		
		Query query = new Query(schema.getTypeName(), Filter.INCLUDE);
		if(targetCRS != null){
			query.setCoordinateSystemReproject(targetCRS);
		}
		SimpleFeatureCollection features = featureSource.getFeatures(query);
		
		return features;
	}
}