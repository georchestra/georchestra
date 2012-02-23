/**
 * 
 */
package extractorapp.ws.extractor;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mauricio Pazos
 *
 */
final class ExtractorUpdateAllPrioritiesRequest {

	public static final String  operationName = "updateAllPriorities";
    public static final String	UUID_LIST_KEY	= "uuidList";
    public static final String	UUID_KEY     	= "uuid";

    private List<String> uuidList;
    
	private ExtractorUpdateAllPrioritiesRequest(List<String> uuids) {
		
		assert uuids != null;
		
		uuidList = uuids;
	}

	public List<String> asList() {

    	return uuidList;
	}

	/**
	 * 
	 * @param postUuidList format {["uuid1", "uuid2", ....]}
	 * @return
	 * @throws JSONException
	 */
	public static ExtractorUpdateAllPrioritiesRequest parseJson(String postUuidList) throws JSONException {

    	JSONObject jsonRequest = JSONUtil.parseStringToJSon(postUuidList);
    	
    	JSONArray uuidArray =  jsonRequest.names();
		List<String> uuids = new LinkedList<String>();
		
    	for (int i = 0; i < uuidArray.length(); i++) {

    		String uuid = uuidArray.getString(i);
			uuids.add(uuid );
		}
    	
		ExtractorUpdateAllPrioritiesRequest request = new ExtractorUpdateAllPrioritiesRequest(uuids);
		
		return request;
	}

}
