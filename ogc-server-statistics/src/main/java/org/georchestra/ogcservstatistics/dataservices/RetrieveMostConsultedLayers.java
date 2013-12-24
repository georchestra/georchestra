package org.georchestra.ogcservstatistics.dataservices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * List of the N most consulted layers
 * 
 * @author Mauricio Pazos
 *
 */
final public class RetrieveMostConsultedLayers extends AbstractQueryCommand {
	
	final static String LAYER_COLUMN 		= "layer";
	final static String CONNECTIONS_COLUMN 	= "connections";

	/**
	 * builds the sql query taking into account if a month is or isn't specified.
	 * 
	 * @return the sql statement
	 */
	private String getSQLStatement(){

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ")
				.append(LAYER_COLUMN)
				.append(",count(").append(LAYER_COLUMN).append(") as ").append(CONNECTIONS_COLUMN)
				.append(" FROM ogcstatistics.OGC_SERVICES_LOG");
		if(this.month > 0){
			sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? AND EXTRACT(MONTH FROM date) = ?");
		} else {
			sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? ");
		}
		sql.append(" GROUP BY ").append(LAYER_COLUMN);
		sql.append(" ORDER BY ").append(CONNECTIONS_COLUMN).append(" DESC");
		sql.append(" LIMIT ?");
		
		return sql.toString();
	}
	

	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

		PreparedStatement pStmt = this.connection.prepareStatement(getSQLStatement());
		assert year > 0 :"year is expected";

		pStmt.setInt(1, this.year);

		//if the month was specified then set it in the statement
		if(this.month > 0){ 
			pStmt.setInt(2, this.month);
			assert this.limit > 0;
			pStmt.setInt(3, this.limit);
		} else {
			assert this.limit > 0;
			pStmt.setInt(2, this.limit);
		}

		
		return pStmt;
	}

	@Override
	protected Map<String, Object> getRow(ResultSet rs) throws SQLException {
		Map<String,Object> row = new HashMap<String, Object>(4);
		row.put(LAYER_COLUMN, rs.getString(LAYER_COLUMN));
		row.put(CONNECTIONS_COLUMN, rs.getInt(CONNECTIONS_COLUMN));
		
		return row;
	}


}
