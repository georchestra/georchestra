package org.georchestra.analytics.util;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Static Class providing utilities concerning CSV format
 * @author fgravin
 *
 */
public class CSVUtil {

	public static final String CSV_SEP = ";";
    public static final String RES_FIELD = "results";
    public static final String CSV_EXT = ".csv";
    
    /**
     * Format a JSON Object to a CSV String. All sub objects of the field
     * "RES_FIELD" are parsed and format to the CSV
     * 
     * @param obj
     * @return CSV String 
     * @throws JSONException
     */
    public static final String JSONToCSV(JSONObject obj) throws JSONException {
    	StringBuilder sBuilder = new StringBuilder();
    	JSONArray jArr = obj.getJSONArray(RES_FIELD);
    	
    	for(int i=0;i<jArr.length();++i) {
    		JSONObject curObj = jArr.getJSONObject(i);
			
    		@SuppressWarnings("unchecked")
			Iterator<String> it = curObj.keys();
    		
    		while(it.hasNext()) {
    			String key = it.next();
    			sBuilder.append(curObj.getString(key));
    			sBuilder.append(CSV_SEP);
    		}
    		sBuilder.append("\r\n");
    	}
    	return sBuilder.toString();
    }
}

