/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.xsd.util.XSDSchemaLocator;
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
import org.geotools.gml2.GMLConfiguration;
import org.geotools.gml2.GMLSchema;
import org.geotools.gml2.bindings.GML2ParsingUtils;
import org.geotools.gtxml.GTXML;
import org.geotools.xml.StreamingParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

/**
 * This class is a façade to the Geotools store implementations.
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
			
			// TODO it is necesary to figure out how to retrieve the gml version from file
			return  readGmlFile(file, targetCRS, Version.GML2);

		default:
			throw new IOException("Unsuported format: " + fileFormat.toString());
		}
	}

	private SimpleFeatureCollection readGmlFile(
			final File file,
			final CoordinateReferenceSystem targetCRS,
			final Version version) 
		throws IOException{

		InputStream in = new FileInputStream( file );
		try {
			GML gml2 = new GML(version);
			SimpleFeatureCollection features = gml2.decodeFeatureCollection(in);
			
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

//		SimpleFeatureType schema = store.getSchema();
//		SimpleFeatureSource featureSource = store.getFeatureSource(schema.getTypeName());
//		
//		Query query = new Query(schema.getTypeName(), Filter.INCLUDE);
//		if(crs != null){
//			query.setCoordinateSystemReproject(crs);
//		}
//		SimpleFeatureCollection features = featureSource.getFeatures(query);
//
//		return features;
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