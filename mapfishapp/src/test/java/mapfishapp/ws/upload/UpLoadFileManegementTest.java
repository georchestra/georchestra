/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.ogr.OGRDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.junit.Test;

/**
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManegementTest {

	/**
	 * Test method for {@link mapfishapp.ws.upload.UpLoadFileManegement#featureCollectionToJSON(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test 
	public void testFeatureCollectionToJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = getTestData();
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManegement fm = new UpLoadFileManegement(fd, directory);
		
		String jsonFeatures = fm.getFeatureCollectionAsJSON( );
		
		System.out.println("RESULT: " +  jsonFeatures);
	}
	
	
	@Test
    public void testJSONTranformation() throws Exception {
    	
        //FeatureCollection features = loadFeatures(STATE_POP, Query.ALL);
    	final String fileName = getTestData();
        OGRDataStore store = new OGRDataStore(fileName, "ESRI shapefile", null);
		
        SimpleFeatureSource source = store.getFeatureSource(store.getTypeNames()[0]);

		FeatureJSON fjson = new FeatureJSON();
		fjson.setEncodeFeatureCollectionBounds(true);
		fjson.setEncodeFeatureCollectionCRS(true);

    	SimpleFeatureCollection featureCollection = source.getFeatures();
		StringWriter writer = new StringWriter();
		fjson.writeFeatureCollection(featureCollection, writer);

		System.out.println("GEOJSON: " + writer.toString());
    }	
	
	private String getWorkingDirectory() throws IOException{
		String current = new File( "." ).getCanonicalPath();
		String directory = current + "/src/main/resources/mapfishapp/ws/upload/";

		return directory;
	}
	private String getTestData() throws IOException{
		
		String fileName = getWorkingDirectory() + "points-4326.shp";

		return fileName;
	}

}
