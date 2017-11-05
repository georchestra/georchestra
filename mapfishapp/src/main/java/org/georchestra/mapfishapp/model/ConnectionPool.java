/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

