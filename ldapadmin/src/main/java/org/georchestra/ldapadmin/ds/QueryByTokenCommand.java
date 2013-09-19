/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Searches the user_token association which matches with the provided token.
 * 
 * @author Mauricio Pazos
 *
 */
final class QueryByTokenCommand extends org.georchestra.lib.sqlcommand.AbstractQueryCommand{


	private String token;

	
	public void setToken(String token) {
	
		
		this.token = token;
	}

	/**
	 * builds the sql query 
	 * 
	 * @return the sql statement
	 */
	private String getSQLStatement(){

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ")
				.append(DatabaseSchema.UID_COLUMN).append(",").append(DatabaseSchema.TOKEN_COLUMN ).append(",").append(DatabaseSchema.CREATEION_DATE_COLUMN )
				.append(" FROM ").append(DatabaseSchema.TABLE_USER_TOKEN)
				.append(" WHERE "+ DatabaseSchema.TOKEN_COLUMN + " = ?");
		
		return sql.toString();
	}
	

	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		PreparedStatement pStmt = this.connection.prepareStatement(getSQLStatement());

		pStmt.setString(1, this.token);

		return pStmt;
	}

	@Override
	protected Map<String, Object> getRow(ResultSet rs) throws SQLException {
		
		Map<String,Object> row = new HashMap<String, Object>(3);
		row.put(DatabaseSchema.UID_COLUMN, rs.getString(DatabaseSchema.UID_COLUMN));
		row.put(DatabaseSchema.TOKEN_COLUMN, rs.getString(DatabaseSchema.TOKEN_COLUMN));
		row.put(DatabaseSchema.CREATEION_DATE_COLUMN, rs.getTimestamp(DatabaseSchema.CREATEION_DATE_COLUMN));
		
		return row;
	}


}
