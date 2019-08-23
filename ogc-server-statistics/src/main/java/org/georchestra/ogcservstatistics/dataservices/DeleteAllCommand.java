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

package org.georchestra.ogcservstatistics.dataservices;

import static org.georchestra.ogcservstatistics.dataservices.LogColumns.QUALIFIED_TABLE_NAME;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * WARNING Removes all logs from the table ogc_services_log
 * 
 * @author Mauricio Pazos
 *
 */
public final class DeleteAllCommand extends AbstractDataCommand {

	/**
	 * This method will execute a SQL Delete!
	 * 
	 * @see org.georchestra.ogcservstatistics.dataservices.DataCommand#execute()
	 */
	@Override
	public void execute() throws DataCommandException {
		try (Statement pStmt = this.connection.createStatement()) {
			pStmt.execute(String.format("DELETE FROM %s", QUALIFIED_TABLE_NAME));
		} catch (SQLException e) {
			throw new DataCommandException(e);
		}
	}
}
