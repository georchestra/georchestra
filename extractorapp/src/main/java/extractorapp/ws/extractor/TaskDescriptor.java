/**
 * 
 */
package extractorapp.ws.extractor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import extractorapp.ws.extractor.task.ExecutionMetadata;
import extractorapp.ws.extractor.task.ExecutionPriority;
import extractorapp.ws.extractor.task.ExecutionState;

/**
 * Maintains the task information.
 * 
 * @author Mauricio Pazos
 *
 */
final class TaskDescriptor {

	/** keys used in the json object */
	public static final String UUID_KEY = "uuid";
    public static final String PRIORITY_KEY = "priority";
    public static final String STATE_KEY = "status";
    public static final String REQUESTOR_KEY = "requestor";
    public static final String SPEC_KEY = "spec";
    public static final String REQUEST_TS_KEY =  "request_ts";
    public static final String BEGIN_TS_KEY = "begin_ts";
    public static final String END_TS_KEY = "end_ts";
	
	private final String id;
	private final ExecutionPriority priority;
	private final ExecutionState state;

	private final String requestor;
	private final JSONObject spec;
	
	private final Date requestTime; 
	private final Date beginTime;
	private final Date endTime;

	/**
	 * @param strJsonTask task string using json syntax
	 */
	public TaskDescriptor(final String strJsonTask) {
		
		try {
			JSONObject jsonTask = new JSONObject(strJsonTask);
	    	this.id  = jsonTask.getString(UUID_KEY);

	    	final int intPriority = jsonTask.getInt(PRIORITY_KEY);
			this.priority = toPriority(intPriority);
			
			final String strState = jsonTask.getString(STATE_KEY);
			this.state = ExecutionState.valueOf(strState);

			this.requestor = jsonTask.getString(REQUESTOR_KEY);
			this.spec = jsonTask.getJSONObject(SPEC_KEY);
			
			String strReqDate = jsonTask.getString(REQUEST_TS_KEY);
			this.requestTime = toDate(strReqDate);
			
			String strBeginDate = jsonTask.getString(BEGIN_TS_KEY);
			this.beginTime = toDate(strBeginDate);

			String strEndDate = jsonTask.getString(END_TS_KEY);
			this.endTime = toDate(strEndDate);
			
		} catch (JSONException e) {
			throw new IllegalArgumentException("Tasck error:" + e.getMessage() );
		}
		
	}

	/**
	 * Constructor for copy
	 * @param toCopy
	 */
	public TaskDescriptor(ExecutionMetadata toCopy) {
		
		this.id = toCopy.getUuid();
		this.priority = toCopy.getPriority();
		this.state = toCopy.getState();
		this.requestor = toCopy.getRequestor();
		try {
			this.spec = JSONUtil.parseStringToJSon( toCopy.getSpec() );
		} catch (JSONException e) {
			throw new IllegalArgumentException("Spec error:" + e.getMessage() );
		}
		this.requestTime = toCopy.getRequestTime();

		this.beginTime = toCopy.getBeginTime();
		this.endTime = toCopy.getEndTime();
	}

	public String getID() {
		return this.id;
	}


	public ExecutionState getState() {
		return state;
	}

	public String getRequestor() {
		return requestor;
	}

	public JSONObject getSpec() {
		return spec;
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public ExecutionPriority getPriority() {
		return this.priority;
	}

	public ExecutionState getStatus() {
		return this.state;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}
	
	public static Date toDate(final String strDate) {
		if("".equals(strDate) )return null;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = dateFormatter.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return date;
		
	}
	
	public static ExecutionPriority toPriority(int intPriority){
		
		for (ExecutionPriority p: ExecutionPriority.values()) {
			if(p.ordinal() == intPriority){
				return p;
			}
		}
		throw new IllegalStateException("Illegal execution priority" +intPriority);
	}
	
	public static String formatDate(Date date) {
		
		if(date == null) return "";
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormatter.format(date);
		
		return strDate;
		
	}
	
}
