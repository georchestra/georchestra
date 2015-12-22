/**
 * 
 */
package org.georchestra.security.healthcenter;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

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
	private String jdbcURL = "jdbc:postgresql://";
	private String host;
	private Integer port;
	private String database;
	private String user;
	private String password;
	private String clientApp;


	private PostgresConnectionProvider(){
		//singleton
	}
	
	public static synchronized DBConnectionProvider getInstance(final String host, final Integer port, final String database, final String user, final String password, final String clientApp) {
		
		THIS.host = host;
		THIS.port = port;
		THIS.database = database;
		THIS.user = user;
		THIS.password = password;
		THIS.clientApp = clientApp;
		
		return THIS;
	}
	
	/* (non-Javadoc)
	 * @see org.georchestra.security.healthcenter.DBConnectionProvider#getConnection()
	 */
	@Override
	public Connection getConnection() throws ConnectException {
		
		try{
			if(this.connection != null){
				synchronized (this.connection) {
					if(this.connection.isClosed()){
						this.connection = null;
					}
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
			throw new ConnectException(e.getMessage());
		}
		if(this.connection == null){
			synchronized (this) {
				try {
					Properties connProp = getConnectionProperties();

					StringBuilder url = new StringBuilder(40);
					url.append(this.jdbcURL).append(this.host);
					url.append(':').append(this.port);
					url.append('/').append(this.database);
					this.connection = DriverManager.getConnection(url.toString(), connProp);
 
					//this.connection.setClientInfo("application_name", this.clientApp); is abstract method in jdbc3 the following is a workaround
					PreparedStatement stmt = this.connection.prepareStatement("SET application_name TO '" + this.clientApp + "'");
					stmt.execute();

				} catch (SQLException e) {
					e.printStackTrace();
					throw new ConnectException(e.getMessage());
				}
			}
		}
		return this.connection;
	}

	private Properties getConnectionProperties() {
		Properties connProp = new Properties();
		connProp.setProperty("user", this.user);
		connProp.setProperty("password", this.password);
		//connProp.setProperty("application_name", this.clientName); right now this don't work. The workaround is to execute the SET application_name = 'an Application Name'
		return connProp;
	}

}
