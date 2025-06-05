/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ogcservstatistics.dataservices;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * This Singleton maintains the configuration data required to access to the
 * database where the ogc services are logged.
 *
 * @author Mauricio Pazos
 *
 */
public final class DataServicesConfiguration {

    private static final DataServicesConfiguration THIS = new DataServicesConfiguration();

    private DataSource dataSource;

    private DataServicesConfiguration() {

    }

    public static DataServicesConfiguration getInstance() {

        return THIS;
    }

    public void initialize(String jdbcURL, String user, String password) {
        PGSimpleDataSource nonPoolingDS = null;
        if (jdbcURL != null && !jdbcURL.trim().isEmpty()) {
            nonPoolingDS = new PGSimpleDataSource();
            nonPoolingDS.setUrl(jdbcURL);
            nonPoolingDS.setUser(user);
            nonPoolingDS.setPassword(password);
        }
        this.dataSource = nonPoolingDS;
    }

    public void initialize(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * A connection to database, make sure the client code closes it.
     *
     * @return {@link Connection}
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
