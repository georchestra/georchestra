package org.georchestra.mapfishapp.model;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class ConnectionPool {

	private BasicDataSource basicDataSource;

	@Autowired
	private GeorchestraConfiguration georchestraConfiguration;

	private String jdbcUrl;

	public ConnectionPool() {}

	public ConnectionPool (String jdbcUrl) {
	    this.jdbcUrl = jdbcUrl;
	}

    public void init() {
        String actualJdbcUrl = jdbcUrl;

        if (georchestraConfiguration.activated()) {
            String supersededJdbcUrl = georchestraConfiguration.getProperty("jdbcUrl");
            if (supersededJdbcUrl != null) {
                actualJdbcUrl = supersededJdbcUrl;
            }
        }

        basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName("org.postgresql.Driver");
        basicDataSource.setTestOnBorrow(true);
        basicDataSource.setPoolPreparedStatements(true);
        basicDataSource.setMaxOpenPreparedStatements(-1);
        basicDataSource.setDefaultReadOnly(false);
        basicDataSource.setDefaultAutoCommit(true);

        basicDataSource.setUrl(actualJdbcUrl);
    }
    /**

     *
     * @param jdbcUrl
     */
	public void setJdbcUrl(String jdbcUrl) {
	    this.jdbcUrl = jdbcUrl;
	}

    public Connection getConnection() throws SQLException
    {
        return basicDataSource.getConnection();
    }

}

