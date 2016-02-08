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

package org.georchestra.ogcservstatistics.dataservices;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * WARNING Removes all logs from the table ogc_services_log
 * 
 * @author Mauricio Pazos
 *
 */
final public class DeleteAllCommand extends AbstractDataCommand {

	/**
	 * This method will execute a SQL Delete!
	 * 
	 * @see org.georchestra.ogcservstatistics.dataservices.DataCommand#execute()
	 */
	@Override
	public void execute() throws DataCommandException {

		//PreparedStatement pStmt=null;
		Statement pStmt=null;
        try {
			pStmt = this.connection.createStatement();
			pStmt.execute("DELETE FROM ogcstatistics.OGC_SERVICES_LOG");
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataCommandException(e);
		} finally{
            try {
                if(pStmt != null) pStmt.close();
                
            } catch (SQLException e1) {
                throw new DataCommandException(e1.getMessage());
            } 
		}
	}
}
