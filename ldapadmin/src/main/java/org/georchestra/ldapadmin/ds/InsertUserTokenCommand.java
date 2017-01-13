/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.georchestra.lib.sqlcommand.AbstractUpdateCommand;

/**
 * Inserts the <b>token</b> associated to the user in the table "USER_TOKEN".
 *
 * @author Mauricio Pazos
 */
final class InsertUserTokenCommand extends AbstractUpdateCommand{


	private static final String SQL_INSERT= "INSERT INTO "+ DatabaseSchema.SCHEMA_NAME + "." + DatabaseSchema.TABLE_USER_TOKEN+ " ("+DatabaseSchema.UID_COLUMN+","+ DatabaseSchema.TOKEN_COLUMN+ ","+DatabaseSchema.CREATION_DATE_COLUMN+") VALUES (?, ?, ?)";

	private Map<String, Object> rowValues;

	/**
	 * Sets the uid and token in the command.
	 * To
	 *
	 * @param row (UID_COLUMN, value)(TOKEN_COLUMN, value) (TIMESTAMP_COLUMN, value)
	 */
	public void setRowValues(final Map<String, Object> row) {

		assert row.keySet().size() == 3;

		this.rowValues = row;
	}


	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

        assert this.connection != null: "database connection is null, use setConnection";

        PreparedStatement pStmt = this.connection.prepareStatement(SQL_INSERT);

        pStmt.setString(1, (String)this.rowValues.get(DatabaseSchema.UID_COLUMN));
		pStmt.setString(2, (String)this.rowValues.get(DatabaseSchema.TOKEN_COLUMN));
		pStmt.setTimestamp(3, (Timestamp) this.rowValues.get(DatabaseSchema.CREATION_DATE_COLUMN));

		return pStmt;
	}


}
