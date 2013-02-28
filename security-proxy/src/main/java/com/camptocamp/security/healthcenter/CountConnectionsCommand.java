/**
 * 
 */
package com.camptocamp.security.healthcenter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.georchestra.ogcservstatistics.dataservices.AbstractQueryCommand;

/**
 * This command counts the open connections in Postres data base
 * 
 * @author Mauricio Pazos
 *
 */
final class CountConnectionsCommand extends AbstractQueryCommand{

	final static String CONNECTION_COUNT = "count_connections";
	
	final static String SQL = " SELECT count(*) as "+CONNECTION_COUNT+" FROM pg_catalog.pg_stat_activity "; 
			
	protected PreparedStatement prepareStatement() throws SQLException{

		PreparedStatement pStmt = this.connection.prepareStatement(SQL);
		
		return pStmt;
	}

	protected Map<String, Object> getRow(ResultSet rs) throws SQLException{
		
		Map<String,Object> row = new HashMap<String, Object>(4);
		row.put(CONNECTION_COUNT, rs.getInt(CONNECTION_COUNT));
		
		return row;
	}

	public Integer getCountConnections() {

		List<Map<String, Object>> result = getResult();
		
		if(result.size() > 0){
			Map<String,Object> row= result.get(0);
			Integer count = (Integer)row.get(CONNECTION_COUNT);
			
			return count;
		}
		return 0;
	}

}
