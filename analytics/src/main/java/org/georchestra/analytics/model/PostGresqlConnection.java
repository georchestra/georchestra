package org.georchestra.analytics.model;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class PostGresqlConnection {

	private BasicDataSource basicDataSource;
	
	public PostGresqlConnection (String jdbcUrl) {

		basicDataSource = new BasicDataSource();

		basicDataSource.setDriverClassName("org.postgresql.Driver");

		basicDataSource.setTestOnBorrow(true);

		basicDataSource.setPoolPreparedStatements(true);
		basicDataSource.setMaxOpenPreparedStatements(-1);

		basicDataSource.setDefaultReadOnly(false);
		basicDataSource.setDefaultAutoCommit(false);

		basicDataSource.setUrl(jdbcUrl);
	}

    public Connection getConnection() throws SQLException
    {
        return basicDataSource.getConnection();
    }  

}

