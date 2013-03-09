/**
 * 
 */
package org.georchestra.security.healthcenter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.georchestra.ogcservstatistics.dataservices.AbstractQueryCommand;

/**
 * Retrieves from postgres the connection's state maintained in 
 * the <code>pg_catalog.pg_stat_activity</code> table.
 * 
 * @author Mauricio Pazos
 *
 */
final class ConnectionStatsCommand extends AbstractQueryCommand{

	public final static String DATABASE_NAME = "datname";
	public final static String USER_NAME = "usename";
	public final static String APPLICATION_NAME = "application_name";
	public final static String CLIENT_ADDR = "client_addr";
	public final static String BACKEND_START = "backend_start";
	
	final static String SQL = 	" SELECT "+ DATABASE_NAME +","+ USER_NAME +","+ APPLICATION_NAME + ","+CLIENT_ADDR +","+ BACKEND_START +
								" FROM pg_catalog.pg_stat_activity "; 
			
	protected PreparedStatement prepareStatement() throws SQLException{

		PreparedStatement pStmt = this.connection.prepareStatement(SQL);
		
		return pStmt;
	}

	protected Map<String, Object> getRow(ResultSet rs) throws SQLException{
		
		Map<String,Object> row = new HashMap<String, Object>(4);
		row.put(DATABASE_NAME, rs.getString(DATABASE_NAME));
		row.put(USER_NAME, rs.getString(USER_NAME));
		row.put(APPLICATION_NAME, rs.getString(APPLICATION_NAME));
		row.put(CLIENT_ADDR, rs.getString(CLIENT_ADDR));
		row.put(BACKEND_START, rs.getTimestamp(BACKEND_START));
		
		return row;
	}
	
	
	
}
