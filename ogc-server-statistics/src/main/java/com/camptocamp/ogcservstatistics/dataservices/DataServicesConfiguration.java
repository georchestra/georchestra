/**
 * 
 */
package com.camptocamp.ogcservstatistics.dataservices;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
	public Connection getConnection() throws SQLException {

		if(this.connection == null){
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
