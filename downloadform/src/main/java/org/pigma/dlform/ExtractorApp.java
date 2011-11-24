package org.pigma.dlform;

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




}