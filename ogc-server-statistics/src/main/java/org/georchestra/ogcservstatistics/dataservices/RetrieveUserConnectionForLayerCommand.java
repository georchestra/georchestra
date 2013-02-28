/**
 * 
 */
package org.georchestra.ogcservstatistics.dataservices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * For each layer : list of users and number of connections
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public final class RetrieveUserConnectionForLayerCommand extends AbstractQueryCommand {

	final static String LAYER_COLUMN 		= "layer";
	final static String USER_COLUMN 		= "user_name";
	final static String CONNECTIONS_COLUMN 	= "connections";
	
	/**
	 * builds the sql query taking into account if a month is or isn't specified
	 * @return the sql statement
	 */
	private String getSQLStatement(){

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ").append(LAYER_COLUMN ).append(",").append(USER_COLUMN ).append(",count("+USER_COLUMN+") as ").append(CONNECTIONS_COLUMN)
				.append(" FROM OGC_SERVICES_LOG");
		if(this.month > 0){
			sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? AND EXTRACT(MONTH FROM date) = ?");
		} else {
			sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? ");
		}
		sql.append(" GROUP BY ").append( LAYER_COLUMN ).append( ",").append(USER_COLUMN);
		sql.append(" ORDER BY ").append( LAYER_COLUMN ).append( ",").append(USER_COLUMN);
		
		return sql.toString();
	}
	
	/**
	 * Creates the {@link PreparedStatement} for the SQL statement.
	 */
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

		PreparedStatement pStmt = this.connection.prepareStatement(getSQLStatement());
		assert year > 0 :"year is expected";

		pStmt.setInt(1, this.year);

		//if the month was specified then sets it in the statement
		if(this.month > 0){
			pStmt.setInt(2, this.month);
		}
		
		return pStmt;
	}
	

	/**
	 * Assigns the result set values to the map
	 * 
	 * @param rs 
	 * 
	 * @return pair user, connections
	 */
	@Override
	protected Map<String, Object> getRow(ResultSet rs) throws SQLException {
		
		Map<String,Object> row = new HashMap<String, Object>(2);
		row.put(LAYER_COLUMN, rs.getString(LAYER_COLUMN));
		row.put(USER_COLUMN, rs.getString(USER_COLUMN));
		row.put(CONNECTIONS_COLUMN, rs.getInt(CONNECTIONS_COLUMN));
		
		return row;
	}

}
