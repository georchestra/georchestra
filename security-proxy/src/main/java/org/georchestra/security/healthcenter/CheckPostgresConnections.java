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

package org.georchestra.security.healthcenter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This the status of postgres connections
 * 
 * @author Mauricio Pazos
 *
 */
final class CheckPostgresConnections {

    private static final Log LOGGER = LogFactory.getLog(CheckPostgresConnections.class.getPackage().getName());
    

	public static List<Map<String, Object>> findConnections(String host, Integer port, String database, String user, String password, String clientName) throws IOException {

		List<Map<String, Object>> connectionList; 
		Connection  connection = null;
		try {
			
			DBConnectionProvider connProvider = PostgresConnectionProvider.getInstance(host, port, database ,user, password, clientName);
			connection = connProvider.getConnection();
		
			ConnectionStatsCommand cmd = new ConnectionStatsCommand();
			cmd.setConnection(connection);
			cmd.execute();
			connectionList = cmd.getResult();

		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new IOException(e);
			
		} finally{
			
			try {
				if(connection != null) connection.close();
				
			} catch (SQLException e) {
				LOGGER.fatal(e.getMessage());
				throw new IOException(e);
			}
			
		}
		return connectionList;
	}

	
}
