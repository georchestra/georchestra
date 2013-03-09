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
@RequestMapping("/geonetwork")
public class GeoNetwork extends AbstractApplication {

	protected GeoNetwork(PostGresqlConnection pgpool) {
		super(pgpool);
	}

	private final Log logger = LogFactory.getLog(getClass());
	
	private final String insertDlQuery = "INSERT INTO download.geonetwork_log (username, sessionid, first_name, second_name, " +
			"company, email, phone, comment, metadata_id, filename) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	private final String insertDataUseQuery = "INSERT INTO download.logtable_datause (logtable_id, datause_id) " +
			"VALUES (?,?);";
	
	private String fileName;
	private int metadataId;
	
	protected boolean isInvalid() {
		return super.isInvalid() || (fileName == null) || (metadataId == -1);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public void handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		handlePOSTRequest(request, response);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public void handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		OutputStream out = null;
		JSONObject object   = new JSONObject();
			
		super.initializeVariables(request);
		
		fileName     = request.getParameter("fname");
		metadataId   = request.getParameter("id") != null ? Integer.parseInt(request.getParameter("id")) : -1;

		Connection connection = null;
		ResultSet resultSet = null;
		
		try {
			connection = postgresqlConnection.getConnection();
			connection.setAutoCommit(false);
			out = response.getOutputStream();

			// Check form validity
			if (isInvalid()) {
				object.put("success", false);
				object.put("msg", "invalid form");
				out.write(object.toString().getBytes());
			} else {
				PreparedStatement st = prepareFirstStatement(connection, insertDlQuery, Statement.RETURN_GENERATED_KEYS);

				st.setInt(9, metadataId);
				st.setString(10, fileName);

				st.executeUpdate();
				resultSet = st.getGeneratedKeys();
				resultSet.next();
				
				int idInserted = resultSet.getInt(1);
				
				insertDataUse(connection, insertDataUseQuery, idInserted);
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
			logger.error("Cauht exception while executing service: ", e);
			response.setStatus(500);
		} finally {

			if (out != null) {
				out.close();
			}
			if (resultSet != null) { resultSet.close(); }

			if (connection != null) { 
				connection.setAutoCommit(true);
				connection.close(); 
			}
		}
	}

}