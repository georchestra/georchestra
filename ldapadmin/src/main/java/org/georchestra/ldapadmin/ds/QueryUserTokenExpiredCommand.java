/**
 *
 */
package org.georchestra.ldapadmin.ds;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Searches the tokens before date provided.
 *
 * @author Mauricio Pazos
 *
 */
class QueryUserTokenExpiredCommand extends org.georchestra.lib.sqlcommand.AbstractQueryCommand {


	private Date beforeDate;

	public void setBeforeDate(final Date beforeDate){
		this.beforeDate = beforeDate;
	}

	/**
	 * builds the sql query
	 *
	 * @return the sql statement
	 */
	private String getSQLStatement(){

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ")
				.append(DatabaseSchema.UID_COLUMN).append(",").append(DatabaseSchema.TOKEN_COLUMN ).append(",").append(DatabaseSchema.CREATION_DATE_COLUMN )
				.append(" FROM ").append(DatabaseSchema.SCHEMA_NAME + "." + DatabaseSchema.TABLE_USER_TOKEN)
				.append(" WHERE "+DatabaseSchema.CREATION_DATE_COLUMN +" <= ?");

		return sql.toString();
	}

	/**
	 * Prepares the Statement setting the year and month.
	 */
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

		PreparedStatement pStmt = this.connection.prepareStatement(getSQLStatement());

		Timestamp time = new Timestamp(this.beforeDate.getTime());

		pStmt.setTimestamp(1, time);

		return pStmt;
	}


	@Override
	protected Map<String, Object> getRow(ResultSet rs) throws SQLException {

		Map<String,Object> row = new HashMap<String, Object>(3);
		row.put(DatabaseSchema.UID_COLUMN, rs.getString(DatabaseSchema.UID_COLUMN));
		row.put(DatabaseSchema.TOKEN_COLUMN, rs.getString(DatabaseSchema.TOKEN_COLUMN));
		row.put(DatabaseSchema.CREATION_DATE_COLUMN, rs.getTimestamp(DatabaseSchema.CREATION_DATE_COLUMN));

		return row;
	}



}
