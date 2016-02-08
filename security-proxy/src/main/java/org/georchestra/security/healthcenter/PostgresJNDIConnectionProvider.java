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
