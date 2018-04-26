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

import java.sql.*;

/**
 * This Singleton maintains the configuration data required to access to the database where
 * the ogc services are logged.  
 * 
 * @author Mauricio Pazos
 *
 */
public final class DataServicesConfiguration {

	
	private static final DataServicesConfiguration THIS = new DataServicesConfiguration();
	private Connection connection;
	private String user;
	private String password;
	private String jdbcURL;
	
	private DataServicesConfiguration(){
		
	}
	
	public static DataServicesConfiguration getInstance(){

		return THIS;
	}
	
	public void setJdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL;
	}
	

	public void setUser(String user) {

		this.user = user;
	}

	public void setPassword(String password) {
		
		this.password = password;
	}


	/**
	 * The connection to database
	 * 
	 * @return {@link Connection}
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException, ClassNotFoundException {

		Class.forName("org.postgresql.Driver");

		if ((this.connection == null) || (this.connection.isClosed())) {
			synchronized (this) {
				this.connection = DriverManager.getConnection(this.jdbcURL, this.user, this.password);
			}
		}

		return this.connection;
	}

	public void closeConnection() throws SQLException {

		if (this.connection != null) {
			synchronized (this) {
				if (!this.connection.isClosed()) {
					this.connection.close();
				}
				this.connection = null;
			}
		}
	}


}
