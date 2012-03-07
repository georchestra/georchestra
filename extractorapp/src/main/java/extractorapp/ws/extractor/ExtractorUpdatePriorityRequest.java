package extractorapp.ws.extractor;

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
    	
    	final String uuid = jsonRequest.getString(TaskDescriptor.UUID_KEY);

    	final String strPriority = jsonRequest.getString(TaskDescriptor.PRIORITY_KEY);
		ExecutionPriority priority = ExecutionPriority.valueOf(strPriority);

		ExtractorUpdatePriorityRequest request = new ExtractorUpdatePriorityRequest(uuid, priority);
    	
		return request ;
    }

    /**
     * New instance of {@link ExtractorUpdatePriorityRequest}
     * 
     * @param uuid
     * @param intPriority it should be one of the enumerated values defined in {@link ExecutionPriority}} 
     * @return {@link ExtractorUpdatePriorityRequest}
     */
	public static ExtractorUpdatePriorityRequest newInstance(final String uuid, final int intPriority) {

		ExecutionPriority priority = null;
		for (ExecutionPriority p: ExecutionPriority.values()) {
			if(p.ordinal() == intPriority){
				priority = p;
				break;
			}
		}
		if(priority == null){
			throw new IllegalArgumentException("the priority value: "+ intPriority +" is not valid.");
		}

		ExtractorUpdatePriorityRequest request = new ExtractorUpdatePriorityRequest(uuid, priority);
    	
		return request ;
	}
}
