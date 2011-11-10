/**
 * 
 */
package com.camptocamp.ogcservstatistics.dataservices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Retrieve all records present in the ogc service table
 * 
 * @author Mauricio Pazos
 *
 */
final public class RetrieveAllCommand extends AbstractQueryCommand{

	final static String DATE_COLUMN = "date";
	final static String USER__COLUMN = "user_name";
	final static String SERVICE_COLUMN = "service";
	final static String LAYER_COLUMN = "layer";
	
	final static String SQL = " SELECT "+ DATE_COLUMN+ "," + USER__COLUMN +","+ SERVICE_COLUMN+","+LAYER_COLUMN 
							+ " FROM OGC_SERVICES_LOG"
							+ " ORDER BY "+ DATE_COLUMN+ "," + USER__COLUMN +","+ SERVICE_COLUMN+","+LAYER_COLUMN;
 	
	protected PreparedStatement prepareStatement() throws SQLException{

		PreparedStatement pStmt = this.connection.prepareStatement(SQL);
		
		return pStmt;
	}

	protected Map<String, Object> getRow(ResultSet rs) throws SQLException{
		
		Map<String,Object> row = new HashMap<String, Object>(4);
		row.put(DATE_COLUMN, rs.getDate(DATE_COLUMN));
		row.put(USER__COLUMN, rs.getString(USER__COLUMN));
		row.put(SERVICE_COLUMN, rs.getString(SERVICE_COLUMN));
		row.put(LAYER_COLUMN, rs.getString(LAYER_COLUMN));
		
		return row;
	}

}
