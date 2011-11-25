package extractorapp.ws.acceptance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import extractorapp.ws.extractor.task.ExtractionTask;

public class CheckFormAcceptance {
	
	private BasicDataSource basicDataSource;
	private boolean activated = false;

	private static final Log LOG = LogFactory.getLog(ExtractionTask.class.getPackage().getName());

	public CheckFormAcceptance(String _activated, String jdbcUrl) {

		activated = "true".equalsIgnoreCase(_activated);
		
		if (activated) {
			basicDataSource = new BasicDataSource();

			basicDataSource.setDriverClassName("org.postgresql.Driver");

			basicDataSource.setTestOnBorrow(true);

			basicDataSource.setPoolPreparedStatements(true);
			basicDataSource.setMaxOpenPreparedStatements(-1);

			basicDataSource.setDefaultReadOnly(false);
			basicDataSource.setDefaultAutoCommit(false);

			basicDataSource.setUrl(jdbcUrl);
		}
	}
	public boolean isFormAccepted(String session, String username, String jsonSpec) {

		if (activated == false) return true;

		boolean ret = false;
		
		Connection connection = null;
		PreparedStatement checkformentryst = null;
		ResultSet rs = null;
		
		try {
			connection = basicDataSource.getConnection();

			
			String sel = "SELECT " +
						"			COUNT(id) " +
						"FROM " +
						"			download.extractorapp_log " +
						"WHERE " +
						"			(sessionid = ? OR (username = ?  AND username != 'anonymousUser')) " +
						"AND " +
						"           json_spec = ?;";
			
			checkformentryst  = connection.prepareStatement(sel);
			// TODO : session is useless here, because
			// it is not a stable identifier between security-proxified webapps
			// anyway it is not used in case of anonymous extraction requests.
			checkformentryst.setString(1, session);
			checkformentryst.setString(2, username);
			// Extra \n to be removed with the trim() call
			checkformentryst.setString(3, jsonSpec.trim());
			
			rs = checkformentryst.executeQuery();
			
			rs.next();
			int numResults = rs.getInt(1);
			
			ret = (numResults > 0);
			
			
		} catch (Exception e) {
			LOG.error("Error occured while trying to check form validation", e);
		} finally {
			if (rs != null) try { rs.close(); } catch (Exception e) {}
			if (checkformentryst != null) try { checkformentryst.close(); } catch (Exception e) {}
			if (connection != null) try { connection.close(); } catch (Exception e) {}
		}
		return ret;
	}

}
