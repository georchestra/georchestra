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
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
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
class GeotoolsFeatureReader implements FeatureGeoFileReader {

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
			return readGmlFile(file, targetCRS);  
		case kml:
			return  readKmlFile(file, targetCRS);

		default:
			throw new IOException("Unsuported format: " + fileFormat.toString());
		}
	}


	/**
	 * Reads the GML file. The method try to read using GML2 if it cannot then try using GML3
	 *  
	 * @param file
	 * @param targetCRS
	 * @return {@link SimpleFeatureCollection}
	 * @throws IOException
	 */
	private SimpleFeatureCollection readGmlFile(File file,	CoordinateReferenceSystem targetCRS) throws IOException {
		
		SimpleFeatureCollection fc = null;
		try{
			fc = readGmlFile(file, targetCRS, Version.GML2);
		} catch(IOException e)  {
			fc = readGmlFile(file, targetCRS, Version.GML3);
		}
		return fc;
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
			StreamingParser parser = new StreamingParser(cfg , in,  SimpleFeature.class );
			
			int targetSRID = 0;
			if(targetCRS != null){
				targetSRID = CRS.lookupEpsgCode(targetCRS, true);
			}
			
			CoordinateReferenceSystem sourceCRS;
	        MathTransform mathTransform = null;
			ListFeatureCollection fc = null;
			SimpleFeature feature;
			while( (feature = (SimpleFeature) parser.parse()) != null){
				
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				
				// initializes the feature collection
				if (fc == null) {
					int srid = geom.getSRID();
					if(srid > 0 ){
						sourceCRS = CRS.decode("EPSG:"+ srid );
					} else {
			    		sourceCRS = CRS.decode("EPSG:4326" );
					}
					SimpleFeatureType type;
		    		if((targetCRS != null) && !sourceCRS.equals(targetCRS)){
			    		// transforms the feature type to the target crs,  creates the feature collection 
						// and finds the math transformation required
		    			type = SimpleFeatureTypeBuilder.retype(feature.getFeatureType(), targetCRS);

		    			mathTransform = CRS.findMathTransform(sourceCRS, targetCRS);
		    		} else {
		    			// uses the original feature type 
		    			type = SimpleFeatureTypeBuilder.retype(feature.getFeatureType(), sourceCRS);
		    		}
					fc = new ListFeatureCollection(type);

				}
				// reproject the feature's geometry it it is necessary before add the feature to the new feature collection.
	        	if(mathTransform  != null){
	        		// transformation is required
	        		Geometry reprojectedGeometry= JTS.transform(geom, mathTransform);
	        		reprojectedGeometry.setSRID(targetSRID);
					feature.setDefaultGeometry(reprojectedGeometry);
	        	}
	        	fc.add(feature);
			}
			if(fc == null){
				final String msg = "Fail reading GML file ("+ version + "). It cannot read the file " + file.getAbsoluteFile();
				LOG.warn(msg);
				throw new IOException(msg);
			}
			return fc;
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
			
		} finally {
		
			in.close();
		}
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

		Query query = new Query(schema.getTypeName(), Filter.INCLUDE);
		
		CoordinateReferenceSystem baseCRS = store.getSchema(schema.getTypeName()).getCoordinateReferenceSystem();
		query.setCoordinateSystem(baseCRS);
		if(targetCRS != null){
			query.setCoordinateSystemReproject(targetCRS);
		}
		
		SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures(query);
		
		return features;
	}
}