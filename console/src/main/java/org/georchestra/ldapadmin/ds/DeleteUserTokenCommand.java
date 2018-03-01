/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.georchestra.lib.sqlcommand.AbstractUpdateCommand;

/**
 * Deletes the user_token association.
 * 
 * @author Mauricio Pazos
 *
 */
final class DeleteUserTokenCommand extends AbstractUpdateCommand{


	private static final String SQL= "DELETE FROM "+ DatabaseSchema.SCHEMA_NAME + "." + DatabaseSchema.TABLE_USER_TOKEN + " WHERE "+ DatabaseSchema.UID_COLUMN + " = ?";
	
	private String uid;
	
	public void setUid(String uid) {
		
		this.uid =uid;
	}


	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

        assert this.connection != null: "database connection is null, use setConnection";

        PreparedStatement pStmt = this.connection.prepareStatement(SQL);

        pStmt.setString(1, this.uid);
		
		return pStmt;
	}
	
}
