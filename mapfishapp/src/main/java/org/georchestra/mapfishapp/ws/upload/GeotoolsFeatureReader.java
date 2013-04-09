/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.mif.MIFDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Parser;
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

	/* (non-Javadoc)
	 * @see org.georchestra.mapfishapp.ws.upload.FeatureFileReaderImplementor#getFeatureCollection()
	 */
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

	        SimpleFeature f = null;

	        MathTransform mathTransform = null;
			ListFeatureCollection list = null;
	        while ((f = (SimpleFeature) parser.parse()) != null) {
	        	if(list == null){
	        		SimpleFeatureType featureType = f.getFeatureType();
	        		list = new ListFeatureCollection(featureType);

	        		// EPSG:4326 is assumed for kml file
	        		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
	        		if(!sourceCRS.equals(targetCRS)){
	        			mathTransform = CRS.findMathTransform(sourceCRS, targetCRS);
	        		}
	        	}
	        	if(mathTransform  != null){
	        		
	        		Geometry srcGeometry = (Geometry) f.getDefaultGeometry();
	        		Geometry reprojectedGeometry= JTS.transform(srcGeometry, mathTransform);
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
			GML gml = new GML(version);
			SimpleFeatureCollection features = gml.decodeFeatureCollection(in);
			
			if( !targetCRS.equals(features.getSchema().getCoordinateReferenceSystem()) ) {
				features = new ReprojectingFeatureCollection(features, targetCRS);
			}
			return features;
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		} finally{
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
		
		HashMap params = new HashMap();
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

	private SimpleFeatureCollection retrieveFeatures(
				final String typeName,
				final DataStore store, 
				final CoordinateReferenceSystem crs)
			throws IOException {
		
		SimpleFeatureType schema = store.getSchema(typeName);

		SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());
		
		Query query = new Query(schema.getTypeName(), Filter.INCLUDE);
		if(crs != null){
			query.setCoordinateSystemReproject(crs);
		}
		SimpleFeatureCollection features = featureSource.getFeatures(query);
		
		return features;
	}
}