package org.georchestra.mapfishapp.model;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class ConnectionPool {

	private BasicDataSource basicDataSource;
	
	public ConnectionPool (String jdbcUrl) {

		basicDataSource = new BasicDataSource();

		basicDataSource.setDriverClassName("org.postgresql.Driver");

		basicDataSource.setTestOnBorrow(true);

		basicDataSource.setPoolPreparedStatements(true);
		basicDataSource.setMaxOpenPreparedStatements(-1);

		basicDataSource.setDefaultReadOnly(false);
		basicDataSource.setDefaultAutoCommit(true);

		basicDataSource.setUrl(jdbcUrl);
	}

    public Connection getConnection() throws SQLException
    {
        return basicDataSource.getConnection();
    }

}

