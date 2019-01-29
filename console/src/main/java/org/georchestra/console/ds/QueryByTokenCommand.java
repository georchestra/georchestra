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

package org.georchestra.console.ds;

import java.sql.Connection;
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
				.append(DatabaseSchema.UID_COLUMN).append(",").append(DatabaseSchema.TOKEN_COLUMN ).append(",").append(DatabaseSchema.CREATION_DATE_COLUMN )
				.append(" FROM ").append(DatabaseSchema.SCHEMA_NAME + "." + DatabaseSchema.TABLE_USER_TOKEN)
				.append(" WHERE "+ DatabaseSchema.TOKEN_COLUMN + " = ?");
		
		return sql.toString();
	}
	

	@Override
	protected PreparedStatement prepareStatement(Connection connection) throws SQLException {
		PreparedStatement pStmt = connection.prepareStatement(getSQLStatement());

		pStmt.setString(1, this.token);

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
