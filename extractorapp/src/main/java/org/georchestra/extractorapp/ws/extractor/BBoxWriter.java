/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.georchestra.extractorapp.ws.extractor.OGRFeatureWriter.FileFormat;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Creates the a file that contains a bounding box geometry related with the extracted features.
 * 
 * @author Mauricio Pazos
 *
 */
public class BBoxWriter {

	// Properties of the bbox FeatureType 
	private static final String GEOMETRY_PROPERTY = "bounding_geom";
	private static final String ID_PROPERTY = "bounding_id";
	
	
	private ReferencedEnvelope bbox;
	private File baseDir;
	private FileFormat fileFormat;
	private ProgressListener progress;
	private CoordinateReferenceSystem requestedCRS;

	/**
	 * New instance of BBoxWriter
	 * 
	 * @param bbox the bbox used to create the polygon
	 * @param baseDir where the file is created
	 * @param format 
	 * @param requestedCRS CRS used to project the polygon associated to the bbox
	 * @param progress 
	 */
	public BBoxWriter(ReferencedEnvelope bbox, File baseDir, FileFormat format, CoordinateReferenceSystem requestedCRS, ProgressListener progress ){
		assert bbox != null;
		assert baseDir != null;
		assert format != null;
		assert requestedCRS !=null;
		
		this.bbox = bbox;
		this.baseDir = baseDir;
		this.fileFormat = format;
		this.requestedCRS = requestedCRS;
		this.progress = progress;
	}
	
	/**
	 * Writes the bbox files in the required format
	 * 
	 * @return the set of {@link File} created  
	 * 
	 * @throws IOException
	 */
	public File[] generateFiles() throws IOException{
		
		// create the feature type for the bbox geometry
		SimpleFeatureType type = createFeatureType();
		
		// sets bbox feature the the attributes
		Geometry geom = createBBoxGeometry(this.bbox, this.requestedCRS);
		SimpleFeature bboxFeature = createFeature(geom, type);

		// writes the file
        SimpleFeatureCollection features = DataUtilities.collection(new SimpleFeature[]{bboxFeature});
        
        FeatureWriterStrategy writer = new OGRFeatureWriter(this.progress, type,  this.baseDir, this.fileFormat, features);
        return writer.generateFiles();
	}

	/**
	 * Creates the feature type for the bbox feature
	 * @return
	 * @throws IOException
	 */
	private SimpleFeatureType createFeatureType() throws IOException {

		try {
			Integer epsgCode= CRS.lookupEpsgCode(this.requestedCRS, false) ;
			
			SimpleFeatureType type = DataUtilities.createType(
										"bounding", 
										GEOMETRY_PROPERTY +":Polygon:srid="+epsgCode +"," +
										ID_PROPERTY + ":Integer");
			return type;

		} catch (Exception e) {
			throw new IOException (e.getMessage());
		}
		
	}
	
	/**
	 * Creates a feature with the polygon 
	 * @param geom
	 * @param type
	 * @return {@link SimpleFeature}
	 */
	private SimpleFeature createFeature(Geometry geom, SimpleFeatureType type) {

		SimpleFeature feature = DataUtilities.template(type);
		
		feature.setAttribute(ID_PROPERTY, 1); // this field is required by mif/mid format
		feature.setAttribute(GEOMETRY_PROPERTY, geom);
		
		return feature;
	}


	/**
	 * Creates a polygon or multipolygon geometry using the bbox as reference. The new polygon will be
	 * in the target crs.
	 * 
	 * @param envelope 
	 * @param geomClass required geometry 
	 * @param epsgCode 
	 * @return a Polygon or MultiPolygon geometry 
	 * @throws IOException 
	 */
	private Polygon createBBoxGeometry( ReferencedEnvelope envelope, CoordinateReferenceSystem targetCrs) throws IOException{
		
		try {
			
			Polygon polygon = JTS.toGeometry(envelope.toBounds(targetCrs));
			
			polygon.setSRID(CRS.lookupEpsgCode(targetCrs, false));
			
			return polygon;
			
		} catch (Exception e) {
			throw new IOException(e);
		} 
	}
}
