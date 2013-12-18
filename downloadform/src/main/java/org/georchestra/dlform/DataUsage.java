package org.georchestra.dlform;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * data_usage controller
 * 
 * author: pmauduit
 */

@Controller
@RequestMapping("/data_usage")
public class DataUsage {

	private final Log logger = LogFactory.getLog(getClass());

	private PostGresqlConnection postgresqlConnection;
	
	public DataUsage(PostGresqlConnection pgpool) {
		postgresqlConnection = pgpool;
	}

	private JSONArray getUsage() throws Exception {
		JSONArray ret = new JSONArray ();
		Connection connection = null;
		Statement sql = null;
		ResultSet results = null;
		try {
			connection = postgresqlConnection.getConnection();
			sql = connection.createStatement();

			results = sql
					.executeQuery("SELECT * FROM downloadform.data_use");
			ResultSetMetaData md = results.getMetaData();
			if (results != null) {
				while (results.next()) {
					HashMap<String, String> record = new HashMap<String, String>();
					record.put(md.getColumnLabel(1), results.getString(1));
					record.put(md.getColumnLabel(2), results.getString(2));
					
					ret.put(record);
				}
			}
			results.close();
		} finally {
			if (results != null) results.close();
			if (sql != null) sql.close();
			if (connection != null) connection.close();
		}
		return ret;

	}
	@RequestMapping(method = RequestMethod.POST)
	public void handlePOSTRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		handleGETRequest(request, response);
	}
	@RequestMapping(method = RequestMethod.GET)
	public void handleGETRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setHeader("Content-Type", "application/json; charset=UTF-8");
		OutputStream out = null;
		JSONObject object = new JSONObject();
		try {
			  object.put("rows", getUsage());
			  out = response.getOutputStream();
			  out.write(object.toString().getBytes("UTF-8"));
		} catch (Exception e) {
		    logger.debug("Failure obtaining the datausage", e);
		    throw e;
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

}