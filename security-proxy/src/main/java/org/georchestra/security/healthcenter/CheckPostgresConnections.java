/**
 * 
 */
package org.georchestra.security.healthcenter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This the status of postgres connections
 * 
 * @author Mauricio Pazos
 *
 */
final class CheckPostgresConnections {

    private static final Log LOGGER = LogFactory.getLog(CheckPostgresConnections.class.getPackage().getName());
    

	public static List<Map<String, Object>> findConnections(String host, Integer port, String database, String user, String password, String clientName) throws IOException {

		List<Map<String, Object>> connectionList; 
		Connection  connection = null;
		try {
			
			DBConnectionProvider connProvider = PostgresConnectionProvider.getInstance(host, port, database ,user, password, clientName);
			connection = connProvider.getConnection();
		
			ConnectionStatsCommand cmd = new ConnectionStatsCommand();
			cmd.setConnection(connection);
			cmd.execute();
			connectionList = cmd.getResult();

		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new IOException(e);
			
		} finally{
			
			try {
				if(connection != null) connection.close();
				
			} catch (SQLException e) {
				LOGGER.fatal(e.getMessage());
				throw new IOException(e);
			}
			
		}
		return connectionList;
	}

	
}
