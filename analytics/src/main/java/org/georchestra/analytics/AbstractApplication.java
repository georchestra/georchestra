package org.georchestra.analytics;

import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.analytics.util.CSVUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract class for all controllers from the webapp Analytics.
 * @author fgravin
 *
 */
public abstract class AbstractApplication {
	
	protected final Log logger = LogFactory.getLog(getClass().getPackage().getName());
	
	int month = 1;
	int year = 2012;
	int start = 0;
	int limit = 25;
	String sort = "count DESC";
	String filter = "";
	
	/**
	 * check all the parameters from the request and fill class attributes.
	 * Return false if any parameters is missing or bad composed.
	 * 
	 * @param request
	 * @param msg
	 * @return
	 */
	protected boolean getAllParameters(HttpServletRequest request, StringBuilder msg) {
		
		try {
			if(!getDateParameters(request)) return false;

			start = Integer.valueOf(request.getParameter("start"));
			limit = Integer.valueOf(request.getParameter("limit"));
			JSONObject obj = new JSONArray(request.getParameter("sort")).getJSONObject(0);
			sort = obj.getString("property") + " " + obj.getString("direction");
			filter = request.getParameter("filter");
			
		} catch (JSONException e) {
			msg.append("Error in sort JSON format");
			return false;
		} catch (NumberFormatException e) {
			msg.append("One param is missing");
			return false;
		}
		return true;
	}
	
	/**
	 * Check the month and yea parameters and fill the class attributes.
	 * Return false if one's missing or bad formatted
	 * @param request
	 * @return
	 */
	protected boolean getDateParameters(HttpServletRequest request) {
		
		try {
			month = Integer.valueOf(request.getParameter("month"));
			year  = Integer.valueOf(request.getParameter("year"));
		}
		catch (NumberFormatException e) {
			return false;
		}

		if (month<1 || month > 12 || year < 2000) {
			return false;
		}
		return true;
	}	
	
	/**
	 * Send the unsuccessful response containing success:false, and the message
	 * from an error
	 * 
	 * @param out response stream to write the message in
	 * @param msg error message
	 * @throws Exception
	 */
	protected void sendSuccessFalse(final OutputStream out, final String msg) throws Exception {
		JSONObject object = new JSONObject();
		object.put("success", false);
		object.put("msg", "invalid params => " + msg);
		out.write(object.toString().getBytes());
	}
	
	/**
	 * Report an error if exception is thrown during the SQL or JSON process.
	 * 
	 * @param out response stream to write an error message
	 * @param response
	 * @param e
	 * @throws Exception
	 */
	protected void reportError(final OutputStream out, HttpServletResponse response, Exception e) throws Exception {
		if (out != null) {
			out.write("Internal Server Error: unable to handle request.".getBytes());
		}
		logger.error("Caught exception while executing service: ", e);
		response.setStatus(500);
	}
	
	/**
	 * Update the response ContentType and header to make the navigator download the CSV file
	 * 
	 * @param csv the CSV content as String
	 * @param filename the filename the CSV will be saved
	 * @param response
	 * @throws Exception
	 */
	protected void respondCSV(String csv, final String filename, HttpServletResponse response) throws Exception {
		response.setContentType("text/csv");
		response.setContentLength(csv.getBytes().length);
		response.setHeader("Content-Disposition", "attachment; filename=\""+filename+CSVUtil.CSV_EXT+"\"");
		response.getWriter().write(csv); 
	}
	
	/**
	 * Generic method from all WS. Will call the stretegy.process method which refer to the
	 * WS' model. This model will return result as JSONObject. This object will be return
	 * in response. If no error occurs, the response will contain success:true, the total
	 * number of result, and a array of results.
	 * If an error is thrown during the request, a JSON response with success:false will be return.
	 * If an error occurs during SQL or JSON process, an exception will be returned.
	 * 
	 * @param request
	 * @param response
	 * @param strategy contain the method to call the model an retrieve results
	 * @throws Exception
	 */
	protected void getStats(HttpServletRequest request, HttpServletResponse response, StrategyController strategy) throws Exception {
		
		OutputStream out  = response.getOutputStream();
		StringBuilder msg = new StringBuilder();
		
		if(!this.getAllParameters(request, msg)) {
			sendSuccessFalse(out, msg.toString());
		}
		else {
			try {
				JSONObject object  = strategy.process();
				out.write(object.toString().getBytes());		
				
			} catch (Exception e) {
				reportError(out, response, e);
			} 
		}
	}
	
	/**
	 * Generic method from all WS. Will call the stretegy.process method which refer to the
	 * WS' model. This model will return result as JSONObject. This object will then be
	 * parsed to write a CSV file which be return in the response, using response header as
	 * CSV file.
	 * 
	 * @param request
	 * @param response
	 * @param strategy contain the method to call the model an retrieve results
	 * @throws Exception
	 */
	protected void exportCSV(HttpServletRequest request, HttpServletResponse response, String csvFileName, StrategyController strategy) throws Exception {

		if(!this.getDateParameters(request)) {
			OutputStream out  = response.getOutputStream();
			sendSuccessFalse(out, "Invalid parameters");
		}
		else {
			String csv=null;
			try {
				JSONObject object  = strategy.process();
				csv = CSVUtil.JSONToCSV(object);
				csvFileName = String.valueOf(year) + "-" + String.format("%02d", month) + "-" + csvFileName;
		
			} catch (Exception e) {
				OutputStream out  = response.getOutputStream();
				reportError(out, response, e);
			}
			respondCSV(csv, csvFileName, response);
		}
	}
	
	/**
	 * Inner Abstract class to pass the call of the specific model to the generic
	 * getStats and exportCSV methods.
	 *
	 */
	protected abstract class StrategyController {
		
		protected abstract JSONObject process() throws SQLException, JSONException;
	}
}