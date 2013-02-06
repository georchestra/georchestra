/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.IOException;
import java.io.StringWriter;

import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Utility class. 
 * <p>
 * Contains useful methods to transform features to json object.
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
final class GeotoolsJSONUtil {
	
	private GeotoolsJSONUtil(){
		// utility class
	}

	/**
	 * Build a json object that contains the feature's properties provided
	 * 
	 * @param feature
	 * 
	 * @return {@link JSONObject} based on the feature's values
	 * @throws JSONException 
	 */
	public static JSONObject asJSONObject(final SimpleFeature feature) throws JSONException {

		try {
			FeatureJSON fjson = new FeatureJSON();
			StringWriter writer = new StringWriter();
			
			// fjson.setEncodeNullValues(true);
			fjson.setFeatureType(feature.getFeatureType());
			fjson.writeFeature(feature, writer);

			JSONTokener jsonTokener = new JSONTokener(writer.toString());
			JSONObject obj = new JSONObject(jsonTokener);
			
	        return obj;
	        
		} catch (IOException e) {
			throw new JSONException(e.getMessage());
		}

	}

}
