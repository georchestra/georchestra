/**
 * 
 */
package com.camptocamp.security.healthcenter;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is responsible maintains the connection to Postgres database.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
final class PostgresConnectionProvider implements DBConnectionProvider {


	private static final DBConnectionProvider THIS = new PostgresConnectionProvider();
	private Connection connection= null;
//	private String jdbcURL = "jdbc:postgresql://localhost:5432/testdb";
//	private String user="postgres";
//	private String password= "admin";
	// TODO this attributes should be configurable	
	private String jdbcURL = "jdbc:postgresql://localhost:5432/pigma";
	private String user="www-data";
	private String password= "www-data";

	private PostgresConnectionProvider(){
		//singleton
	}
	
	public static DBConnectionProvider getInstance() {
		return THIS;
	}

	
	/* (non-Javadoc)
	 * @see com.camptocamp.security.healthcenter.DBConnectionProvider#getConnection()
	 */
	@Override
	public Connection getConnection() throws ConnectException {
		
		try{
			if(connection != null){
				if(connection.isClosed()){
					connection = null;
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
			throw new ConnectException(e.getMessage());
		}
		if(connection == null){
			synchronized (this) {
				try {
					this.connection = DriverManager.getConnection(this.jdbcURL, this.user, this.password);
					
				} catch (SQLException e) {
					e.printStackTrace();
					throw new ConnectException(e.getMessage());
				}
			}
		}
		return connection;
	}

}
