/**
 * 
 */
package extractorapp.ws.extractor;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Utility method to manage JSON object
 * 
 * @author Mauricio Pazos
 *
 */
public final class JSONUtil {
	
	
	private JSONUtil(){
		// utility class
	}
    /**
     * Takes a string and parses it to a JSON object structure
     */
    public static JSONObject parseStringToJSon(String jsonData) throws JSONException {
        final String trimmed = jsonData.trim ();
        final JSONTokener tokener = new JSONTokener (trimmed);
        JSONObject jsonObject = new JSONObject (tokener);
        return jsonObject;
    }

}
