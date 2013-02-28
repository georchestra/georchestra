package org.georchestra.dlform;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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
@RequestMapping("/extractorapp")
public class ExtractorApp extends AbstractApplication {

	protected ExtractorApp(PostGresqlConnection pgpool) {
		super(pgpool);
	}

	private final Log logger = LogFactory.getLog(getClass());

	private final String insertDlQuery = "INSERT INTO download.extractorapp_log (username, sessionid, first_name, second_name, " +
			"company, email, phone, comment, json_spec) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	private final String insertDataUseQuery = "INSERT INTO download.logtable_datause (logtable_id, datause_id) " +
			"VALUES (?,?);";
	
	private final String insertLayersQuery = "INSERT INTO download.extractorapp_layers(" +
            "extractorapp_log_id, projection, resolution, format, bbox_srs, " +
            "\"left\", bottom, \"right\", top, ows_url, ows_type, layer_name)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	private String jsonSpec;
	
	
	protected boolean isInvalid() {
		return super.isInvalid() || (jsonSpec == null);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public void handleGETRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		OutputStream out = null;
		JSONObject object   = new JSONObject();
		ResultSet resultSet = null;
		super.initializeVariables(request);
		Connection connection = null;
		jsonSpec =request.getParameter("json_spec");
		PreparedStatement st = null;
		
		try {
			connection = postgresqlConnection.getConnection();
			connection.setAutoCommit(false);
			out = response.getOutputStream();
			if (isInvalid()) {
				object.put("success", false);
				object.put("msg", "invalid form");
				out.write(object.toString().getBytes());
			} else {
				st = prepareFirstStatement(connection, insertDlQuery, Statement.RETURN_GENERATED_KEYS);

				st.setString(9, jsonSpec);
				
				st.executeUpdate();
				resultSet = st.getGeneratedKeys();
				resultSet.next();
				
				int idInserted = resultSet.getInt(1);
				
				insertDataUse(connection, insertDataUseQuery, idInserted);
				insertLayersLogs(connection, idInserted);
				connection.commit();
				
				object.put("success", true);
				object.put("msg", "Successfully added the record in database.");
				
				out.write(object.toString().getBytes());
			}
		} catch (Exception e) {
			connection.rollback();
			if (out != null) {
				out.write("Int'l Server Error: unable to handle request.".getBytes());
			}
			logger.error("Caught exception while executing service: ", e);
			response.setStatus(500);
		} finally {
			if (st != null) st.close();
			
			if (resultSet != null) {
				resultSet.close();
			}

			if (out != null) {
				out.close();
			}
			
			if (connection != null) {
				connection.setAutoCommit(true);
				connection.close();
			}
		}
	}
	
	protected void insertLayersLogs(Connection connection,  int idInserted) throws Exception {
		
		PreparedStatement st = null;
	    
		JSONObject obj 	= new JSONObject(jsonSpec);
		JSONObject jProp = obj.getJSONObject("globalProperties");
		
		String projection = jProp.getString("projection");
		double resolution = jProp.getDouble("resolution");
		String rasterFormat  = jProp.getString("rasterFormat");
		String vectorFormat  = jProp.getString("vectorFormat");
		
		JSONObject jBbox = jProp.getJSONObject("bbox");
		String bbox_srs = jBbox.getString("srs");
		JSONArray jValue = jBbox.getJSONArray("value");
		double left = jValue.getDouble(0);
		double bottom = jValue.getDouble(1);
		double right = jValue.getDouble(2);
		double top = jValue.getDouble(3);
		
		try {
			
			JSONArray jLayers = obj.getJSONArray("layers");
			for (int i =0;  i < jLayers.length() ; ++i) {
				JSONObject jLayer = jLayers.getJSONObject(i);
				String lProjection = jLayer.getString("projection").equals("null") ? projection : jLayer.getString("projection");
				double lResolution = jLayer.getString("resolution").equals("null") ? resolution : jLayer.getDouble("resolution");
				String lFormat  = jLayer.getString("format");
				String owsType  = jLayer.getString("owsType");
				String owsUrl  = jLayer.getString("owsUrl");
				String layerName  = jLayer.getString("layerName");
				
				double lLeft=left, lBottom=bottom, lRight=right, lTop=top;
				String lSrs=bbox_srs;
				
				if(!jLayer.getString("bbox").equals("null")) {
					JSONObject lBbox = jLayer.getJSONObject("bbox");
					lSrs = lBbox.getString("srs").equals("null") ? lSrs : lBbox.getString("srs");
					if(!lBbox.getString("value").equals("null")) {
						JSONArray lValue = lBbox.getJSONArray("value");
						lLeft = lValue.getDouble(0);
						lBottom = lValue.getDouble(1);
						lRight = lValue.getDouble(2);
						lTop = lValue.getDouble(3);
					}
				}
				
				if("WFS".equals(owsType)) {
					lFormat = lFormat.equals("null") ? vectorFormat : lFormat;
				} else if ("WCS".equals(owsType)) {
					lFormat = lFormat.equals("null") ? rasterFormat : lFormat;
				}
				
				st = connection.prepareStatement(insertLayersQuery);
				st.setInt(1, idInserted);
				st.setString(2, lProjection);
				st.setDouble(3, lResolution);
				st.setString(4, lFormat);
				st.setString(5, lSrs);
				st.setDouble(6, lLeft);
				st.setDouble(7, lBottom);
				st.setDouble(8, lRight);
				st.setDouble(9, lTop);
				st.setString(10, owsUrl);
				st.setString(11, owsType);
				st.setString(12, layerName);
				st.execute();
			}
		} catch(Exception e) {
			if(st != null) st.close();
			throw e;
		}
	}
}