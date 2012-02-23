/**
 * 
 */
package extractorapp.ws.extractor;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

import extractorapp.ws.extractor.task.ExecutionMetadata;
import extractorapp.ws.extractor.task.ExecutionPriority;
import extractorapp.ws.extractor.task.ExecutionState;

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
     * Returns the queue of tasks as a json object. The tasks are added to a json object where 
     * each task object is made from a {@link ExecutionMetadata}
     *    
     * <pre>
     * 
     * <b>JSON format:</b> {"taskQueue":[ {"priority":value,value:value, "uuid":value}, ...]}
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
    		ExecutionPriority priority = metadata.getPriority();
    		ExecutionState state = metadata.getState();

    		JSONObject jsonTask = new JSONObject();
    		jsonTask.put("uuid", uuid);
    		jsonTask.put("priority", priority.toString());
    		jsonTask.put("state", state.toString());

    		jsonTaskArray.put(i, jsonTask);
    		i++;
		}
    	
    	JSONWriter jsonTaskQueue = new JSONStringer()
							.object()
								.key("taskQueue")
								.value(jsonTaskArray)
							.endObject();
    	
		String strTaskQueue = jsonTaskQueue.toString();
		
		return strTaskQueue;
	}

}
