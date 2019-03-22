/*
 * Copyright (C) 2019 by the geOrchestra PSC
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
package org.georchestra.console.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Integration test case for
 * https://github.com/georchestra/georchestra/issues/2195 making sure the
 * connection pool works as expected in the event of a server
 * restart/connections dropped
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class ConnectionPoolBehaviorIT {

    public @Rule @Autowired IntegrationTestSupport support;

    private @Autowired DataSource ds;

    private @Value("${dataSource.maxPoolSize}") int maxPoolSize;

    private @Value("${jdbcUrl}") String pgConnectionUrl;

    public @Before void before() {
        assertTrue(maxPoolSize > 0);
    }

    public @Test void testConnectionPoolDiscardsServerSideClosedConnections()
            throws SQLException, InterruptedException {
        // make sure all connections in the pool are initialized before forcing them to
        // disconnect
        Connection testConnection = null;
        List<Connection> consumeAll = new ArrayList<>();
        for (int i = 0; i < maxPoolSize; i++) {
            try {
                Connection connection = ds.getConnection();
                consumeAll.add(connection);
                if (testConnection == null) {// keep one to be used after the server closed all connections
                    testConnection = connection;
                }
            } catch (SQLException timeout) {
                continue;
            }
        }

        assertThat(countConnections(testConnection), greaterThanOrEqualTo(maxPoolSize - 1));

        // create a connection outside the connection pool and use it to tell the server
        // to close all other connections
        try (Connection conn = DriverManager.getConnection(pgConnectionUrl, support.psqlUser(),
                support.psqlPassword())) {
            final int initialConnections = countConnections(conn);
            assertThat(initialConnections, greaterThanOrEqualTo(maxPoolSize - 1));

            final int terminateConnections = terminateConnections(conn);
            assertThat(terminateConnections, greaterThanOrEqualTo(maxPoolSize - 1));
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        try {// now using the connection previously gotten from the pool should fail
            countConnections(testConnection);
            fail("Expected SQL exception 'terminating connection due to administrator command'");
        } catch (SQLException expected) {
            String msg = expected.getMessage();
            assertThat(msg, containsString("terminating connection due to administrator command"));
            try {
                countConnections(testConnection);
                fail("Expected SQL exception 'terminating connection due to administrator command'");
            } catch (SQLException e2) {
                assertThat(e2.getMessage(), containsString("This connection has been closed"));
            }
        }

        // but the pool should be able to provision up to maxPoolSize connections again
        for (int i = 0; i < maxPoolSize; i++) {
            try (Connection newConnection = ds.getConnection()) {
                assertThat(countConnections(newConnection), greaterThanOrEqualTo(1));
            }
        }

        // closing the failed connections shouldn'e return them to the pool
        for (Connection c : consumeAll) {
            c.close();
        }

        for (int i = 0; i < maxPoolSize; i++) {
            try (Connection newConnection = ds.getConnection()) {
                assertThat(countConnections(newConnection), greaterThanOrEqualTo(1));
            }
        }
    }

    private int countConnections(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT sum(numbackends) FROM pg_stat_database;")) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new IllegalStateException();
                }
            }
        }
    }

    private int terminateConnections(Connection c) throws SQLException {
        final String terminateConnections = "SELECT pg_terminate_backend(pg_stat_activity.pid) "
                + "FROM pg_stat_activity "
                + "WHERE pg_stat_activity.datname = (SELECT current_database()) AND pid <> pg_backend_pid();";

        int numTerminated = 0;
        try (Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery(terminateConnections)) {
                while (rs.next()) {
                    numTerminated++;
                }
            }
        }
        return numTerminated;
    }

}
