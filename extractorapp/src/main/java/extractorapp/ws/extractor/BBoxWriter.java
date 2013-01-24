/**
 * 
 */
package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.GeoTools;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import extractorapp.ws.extractor.OGRFeatureWriter.FileFormat;

/**
 * Creates the a file that contains a bounding box geometry related with the extracted features.
 * 
 * @author Mauricio Pazos
 *
 */
public class BBoxWriter {
	
	private static final String GEOMETRY_PROPERTY = "geom";
	private ReferencedEnvelope bbox;
	private File baseDir;
	private FileFormat fileFormat;
	private ProgressListener progress;

	public BBoxWriter(ReferencedEnvelope bbox, File baseDir, FileFormat format, ProgressListener progress ){
		assert bbox != null;
		assert baseDir != null;
		assert format != null;
		
		this.bbox = bbox;
		this.baseDir = baseDir;
		this.fileFormat = format;
		this.progress = progress;
	}
	
	/**
	 * Write the bbox files in the required format
	 * @return 
	 * 
	 * @throws IOException
	 */
	public File[] generateFiles() throws IOException{
		
		// create the feature type for the bbox geometry
		SimpleFeatureType type = createFeatureType();
		
		// sets bbox feature the the attributes
		Polygon geom = createBBoxGeometry(this.bbox);
		SimpleFeature bboxFeature = createFeature(geom, type);

		// writes the file
        SimpleFeatureCollection features = DataUtilities.collection(new SimpleFeature[]{bboxFeature});
        
        FeatureWriterStrategy writer = new OGRFeatureWriter(this.progress, type,  this.baseDir, this.fileFormat, features);
        return writer.generateFiles();
	}

	private SimpleFeatureType createFeatureType() throws IOException {

		try {
			CoordinateReferenceSystem crs= this.bbox.getCoordinateReferenceSystem();
			Integer epsgCode= CRS.lookupEpsgCode(crs, false) ;
			
			SimpleFeatureType type = DataUtilities.createType("bounding", GEOMETRY_PROPERTY+":Polygon:srid="+epsgCode);
			
			return type;

		} catch (Exception e) {
			throw new IOException (e.getMessage());
		}
		
	}
	
	/**
	 * Create a feature with the polygon 
	 * @param geom
	 * @param type
	 * @return {@link SimpleFeature}
	 */
	private SimpleFeature createFeature(Polygon geom, SimpleFeatureType type) {

		SimpleFeature feature = DataUtilities.template(type);
		
		feature.setAttribute(GEOMETRY_PROPERTY, geom);
		
		return feature;
	}

	


	/**
	 * Creates a polygon geometry using the bbox as reference
	 * 
	 * @param bbox 
	 * @return a polygon
	 */
	private Polygon createBBoxGeometry( ReferencedEnvelope bbox){
		
		GeometryFactory geomFactory = JTSFactoryFinder.getGeometryFactory(GeoTools.getDefaultHints());

		Coordinate[] coordinates = new Coordinate[]{ 
					new Coordinate(bbox.getMinX(), bbox.getMaxY()),
					new Coordinate(bbox.getMaxX(), bbox.getMaxY()),
					new Coordinate(bbox.getMaxX(), bbox.getMinY()),
					new Coordinate(bbox.getMinX(), bbox.getMinY()),
					new Coordinate(bbox.getMinX(), bbox.getMaxY())
				};
		LinearRing shell = geomFactory.createLinearRing(coordinates);
		Polygon polygon = new Polygon(shell, new LinearRing[]{} , geomFactory); 
		
		return polygon;
	}
}
