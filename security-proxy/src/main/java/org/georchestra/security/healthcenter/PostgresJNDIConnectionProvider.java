package org.georchestra.security.healthcenter;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


class PostgresJNDIConnectionProvider implements DBConnectionProvider  {
	
	private static final DBConnectionProvider THIS = new PostgresJNDIConnectionProvider();
	private DataSource dataSource;
	
	/**
	 * see getInstance
	 */
	private PostgresJNDIConnectionProvider(){
		// singleton
	}
	
	/**
	 * @return the instance of {@link PostgresJNDIConnectionProvider}
	 */
	public static DBConnectionProvider getInstance() {
		return THIS;
	}
	

	/**
	 * @see org.georchestra.security.healthcenter.DBConnectionProvider#getConnection()
	 */
	@Override
	public Connection getConnection() throws ConnectException {
		try {
			if(dataSource == null ){
				synchronized (this) {
					dataSource = getDataSource();
				}
			}
			return dataSource.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConnectException(e.getMessage());
		}
	}
	
	/**
	 * @return {@link DataSource}
	 * @throws IOException
	 */
	private DataSource getDataSource() throws IOException{

		try {
			InitialContext cxt = new InitialContext();

			dataSource = (DataSource) cxt.lookup("java:/comp/env/jdbc/postgres");
			
			if( dataSource == null){

				throw new IOException("Error openning connection: Data source not found");
			}
		
			return dataSource;
			
		} catch (NamingException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}



	
	
	
}
