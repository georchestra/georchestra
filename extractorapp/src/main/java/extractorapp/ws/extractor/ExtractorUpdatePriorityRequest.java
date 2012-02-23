package extractorapp.ws.extractor;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import extractorapp.ws.extractor.task.ExecutionPriority;

/**
 * Encapsulates the parameters required the priority of a task
 * 
 * @author Mauricio Pazos
 *
 */
class ExtractorUpdatePriorityRequest {

	public static String operationName = "updatePriority";
    public static final String	UUID_KEY = "uuid";
    public static final String PRIORITY_KEY	= "priority";
    
    public final String	_uuid;
    public final ExecutionPriority	_priority;
    
    
    public ExtractorUpdatePriorityRequest(String uuid, ExecutionPriority priority) {

    	assert uuid != null && priority != null;
    	
        _uuid = uuid;
        _priority = priority;
    }
    
    /**
     * Makes a new instance of {@link ExtractorUpdatePriorityRequest}
     * 
     * @param jsonData a {"uuid":value, "priority": value}
     * @return {@link ExtractorUpdatePriorityRequest}
     * @throws JSONException
     */
    public static ExtractorUpdatePriorityRequest parseJson (String jsonData) throws JSONException{
    	
    	JSONObject jsonRequest = JSONUtil.parseStringToJSon(jsonData);
    	
    	final String uuid = jsonRequest.getString(UUID_KEY);

    	final String strPriority = jsonRequest.getString(PRIORITY_KEY);
		ExecutionPriority priority = ExecutionPriority.valueOf(strPriority);

		ExtractorUpdatePriorityRequest request = new ExtractorUpdatePriorityRequest(uuid, priority);
    	
		return request ;
    }

	public static ExtractorUpdatePriorityRequest newInstance(String uuid, String strPriority) {

		ExecutionPriority priority = ExecutionPriority.valueOf(strPriority);

		ExtractorUpdatePriorityRequest request = new ExtractorUpdatePriorityRequest(uuid, priority);
    	
		return request ;
	}
    
    
    
	
}
