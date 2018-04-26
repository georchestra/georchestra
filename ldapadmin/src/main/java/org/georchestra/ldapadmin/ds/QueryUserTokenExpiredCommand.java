/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
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
