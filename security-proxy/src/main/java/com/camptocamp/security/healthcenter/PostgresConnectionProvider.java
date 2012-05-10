/**
 * 
 */
package com.camptocamp.security.healthcenter;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.postgresql.PGConnection;

/**
 * This class is responsible maintains the connection to Postgres database.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
final class PostgresConnectionProvider implements DBConnectionProvider {


	private static final PostgresConnectionProvider THIS = new PostgresConnectionProvider();
	
	private Connection connection= null;
	//private String jdbcURL = "jdbc:postgresql://localhost:5432/postgres"; //FIXME despite the service access to the postgres metadata it is necessary a database. We could use the default database "postgres". Right now pigma is used. 
	private String jdbcURL = "jdbc:postgresql://localhost:5432/pigma";
	private String user;
	private String password;
	private String clientApp;

	private PostgresConnectionProvider(){
		//singleton
	}
	
	public static synchronized DBConnectionProvider getInstance(final String user, final String password, final String clientApp) {
		
		THIS.user = user;
		THIS.password = password;
		THIS.clientApp = clientApp;
		
		return THIS;
	}
	
	/* (non-Javadoc)
	 * @see com.camptocamp.security.healthcenter.DBConnectionProvider#getConnection()
	 */
	@Override
	public Connection getConnection() throws ConnectException {
		
		try{
			if(connection != null){
				synchronized (connection) {
					if(connection.isClosed()){
						connection = null;
					}
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
			throw new ConnectException(e.getMessage());
		}
		if(connection == null){
			synchronized (this) {
				try {
					Properties connProp = getConnectionProperties();

					this.connection = DriverManager.getConnection(this.jdbcURL, connProp);

					//this.connection.setClientInfo("application_name", this.clientApp); is abstract method in jdbc3 the following is a workaround
					PreparedStatement stmt = this.connection.prepareStatement("SET application_name TO '" + this.clientApp + "'");
					stmt.execute();

				} catch (SQLException e) {
					e.printStackTrace();
					throw new ConnectException(e.getMessage());
				}
			}
		}
		return connection;
	}

	private Properties getConnectionProperties() {
		Properties connProp = new Properties();
		connProp.setProperty("user", this.user);
		connProp.setProperty("password", this.password);
		//connProp.setProperty("application_name", this.clientName); right now this don't work. The workaround is to execute the SET application_name = 'an Application Name'
		return connProp;
	}

}
