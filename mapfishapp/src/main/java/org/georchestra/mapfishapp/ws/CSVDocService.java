package org.georchestra.mapfishapp.ws;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This service handles the storage and the loading of a csv file on a temporary directory.
 * 
 * @author yoann buch  - yoann.buch@gmail.com
 *
 */

public class CSVDocService extends A_DocService {

    public static final String FILE_EXTENSION = ".csv";
    public static final String MIME_TYPE = "text/csv";
    public static final String CSV_SEP = ";";
    public static final String COLUMNS_FIELD = "columns";
    public static final String RECORDS_FIELD = "data";
    
    public CSVDocService(final long maxDocAgeInMinutes, final String tempDir) {
        super(maxDocAgeInMinutes, FILE_EXTENSION, MIME_TYPE, tempDir);
    }

    /**
     * Convert content in JSON format to CSV
     * @throws DocServiceException
     */
    @Override
    protected void preSave() throws DocServiceException {
        _content = JSONtoCSV(_content);
    }
    
    /**
     * Convert content in JSON format to CSV
     * @param jsonData file content in JSON
     * @return file content in CSV
     * @throws DocServiceException
     */
    private String JSONtoCSV(String jsonData) throws DocServiceException {
        StringBuilder sBuilder = new StringBuilder();
        try {
            
            // create JSON Object from data
            JSONTokener tokener = new JSONTokener(jsonData);
            JSONObject jObj = new JSONObject(tokener);

            // get columns name
            JSONArray cols = jObj.getJSONArray(COLUMNS_FIELD); // if wrong value provided error is catched
            for(int i = 0; i < cols.length(); i++) {
                sBuilder.append(cols.get(i));
                if(i < cols.length() - 1) {
                    sBuilder.append(CSV_SEP);
                }
            }
            
            // add newline 
            sBuilder.append("\r\n");
            
            // get records
            JSONArray records = jObj.getJSONArray(RECORDS_FIELD); // if wrong value provided error is catched
            for(int recordIndex = 0; recordIndex < records.length(); recordIndex++) {
                // values must be arrays
                if(!(records.get(recordIndex) instanceof JSONArray)) {
                    throw new DocServiceException(RECORDS_FIELD + " must be arrays", HttpServletResponse.SC_BAD_REQUEST);
                }
                
                // extract values
                JSONArray array = (JSONArray)records.get(recordIndex);
                for(int arrayIndex = 0; arrayIndex < array.length(); arrayIndex++) {
                    sBuilder.append(array.get(arrayIndex));

                    // add separator except for the last value
                    if(arrayIndex < array.length() - 1) {
                        sBuilder.append(CSV_SEP);
                    }
                }
                
                // add newline 
                sBuilder.append("\r\n");
            }         
        }
        catch(JSONException jExc) {
            throw new DocServiceException(jExc.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
        
        return sBuilder.toString();
    }

}
