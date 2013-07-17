/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import java.util.List;

import org.georchestra.extractorapp.ws.extractor.task.ExecutionMetadata;
import org.georchestra.extractorapp.ws.extractor.task.ExecutionState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;


/**
 * Maintains the The response data for the getTaskQueue operation.
 * 
 * @author Mauricio Pazos
 *
 */
final class ExtractorGetTaskQueueResponse {

	
	private List<ExecutionMetadata> taskQueue;

	private ExtractorGetTaskQueueResponse(List<ExecutionMetadata> taskQueue){
		this.taskQueue = taskQueue;
		
	}
	
	public static ExtractorGetTaskQueueResponse newInstance(
			List<ExecutionMetadata> taskQueue) {		
		return new ExtractorGetTaskQueueResponse(taskQueue);
	}

	/**
     * Returns the tasks as a json object. The tasks are added to a json object where 
     * each task object is made from a {@link ExecutionMetadata}
     *    
     * <pre>
     * 
     * <b>JSON format:</b> {"tasks":[ {"uuid":"value", "priority":value,"status":value,...}, ...]}
     * 
     * </pre>
     * 
	 * @return the list of task as a json array 
	 * @throws JSONException 
	 */
	public String asJsonString() throws JSONException {

		JSONArray jsonTaskArray = new JSONArray();
		int i = 0;
    	for (ExecutionMetadata metadata : this.taskQueue) {
    		
    		String uuid = metadata.getUuid();
    		String requestor= metadata.getRequestor();
    		Integer priority = metadata.getPriority().ordinal();
    		ExecutionState status = metadata.getState();
    		JSONObject spec = new JSONObject( metadata.getSpec() );  
    		String requestTimeStamp = TaskDescriptor.formatDate(metadata.getRequestTime());
    		
    		String beginTimeStamp = TaskDescriptor.formatDate(metadata.getBeginTime());
    		String endTimeStamp = TaskDescriptor.formatDate(metadata.getEndTime());

    		JSONObject jsonTask = new JSONObject();
    		jsonTask.put(TaskDescriptor.UUID_KEY, uuid);
    		jsonTask.put(TaskDescriptor.REQUESTOR_KEY, requestor);
    		jsonTask.put(TaskDescriptor.PRIORITY_KEY, priority);
    		jsonTask.put(TaskDescriptor.STATE_KEY, status.toString());
    		jsonTask.put(TaskDescriptor.SPEC_KEY, spec);
    		jsonTask.put(TaskDescriptor.REQUEST_TS_KEY, requestTimeStamp);
    		jsonTask.put(TaskDescriptor.BEGIN_TS_KEY, beginTimeStamp);
    		jsonTask.put(TaskDescriptor.END_TS_KEY, endTimeStamp);

    		jsonTaskArray.put(i, jsonTask);
    		i++;
		}
    	
    	JSONWriter jsonTaskQueue = new JSONStringer()
							.object()
								.key("tasks")
								.value(jsonTaskArray)
							.endObject();
    	
		String strTaskQueue = jsonTaskQueue.toString();
		
		return strTaskQueue;
	}


}
