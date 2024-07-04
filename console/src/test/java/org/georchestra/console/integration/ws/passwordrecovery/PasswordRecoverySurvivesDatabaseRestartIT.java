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
package org.georchestra.console.integration.ws.passwordrecovery;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.console.integration.IntegrationTestSupport;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.ws.passwordrecovery.PasswordRecoveryFormBean;
import org.georchestra.console.ws.passwordrecovery.PasswordRecoveryFormController;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;

/**
 * Integration test case for
 * https://github.com/georchestra/georchestra/issues/2195
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class PasswordRecoverySurvivesDatabaseRestartIT extends ConsoleIntegrationTest {

    public @Rule @Autowired IntegrationTestSupport support;

    private @Autowired DataSource dataSource;

    private @Value("${dataSource.maxPoolSize}") int maxPoolSize;

    private @Value("jdbc:postgresql://${pgsqlHost}:${pgsqlPort}/${pgsqlDatabase}") String pgConnectionUrl;

    private @Autowired PasswordRecoveryFormController controller;

    public @Before void before() {
        assertTrue(maxPoolSize > 0);
    }

    public @Test void testSurvivesDatabaseRestartPOST() throws Exception {

        forceCloseConnectionsBehindPool();

        PasswordRecoveryFormBean bean = (PasswordRecoveryFormBean) support//
                .perform(get("/account/passwordRecovery?email=psc+testuser@georchestra.org"))//
                .andExpect(status().isOk())//
                .andReturn()//
                .getModelAndView()//
                .getModel()//
                .get("passwordRecoveryFormBean");

        controller.setPublicUrl("http://georchestra.test.org");
        controller.setEmailFactory(Mockito.mock(EmailFactory.class));

        HttpServletRequest request = new MockHttpServletRequest("POST", "/account/passwordRecovery");
        BindingResult resultErrors = Mockito.mock(BindingResult.class);
        SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);

        String token = controller.generateToken(request, bean, resultErrors, sessionStatus);
        assertNotNull(token);
    }

    private void forceCloseConnectionsBehindPool() throws SQLException {
        // make sure all connections in the pool are initialized before forcing them to
        // disconnect
        Connection testConnection = null;
        List<Connection> consumeAll = new ArrayList<>();
        for (int i = 0; i < maxPoolSize; i++) {
            try {
                Connection connection = dataSource.getConnection();
                consumeAll.add(connection);
                if (i == 0) {
                    testConnection = connection;
                }
            } catch (SQLException timedOut) {
                //
            }
        }
        for (Connection c : consumeAll) {
            if (c != testConnection) {
                c.close();
            }
        }

        assertThat(countConnections(testConnection), greaterThanOrEqualTo(maxPoolSize - 1));

        // create a connection outside the connection pool
        try (Connection conn = DriverManager.getConnection(pgConnectionUrl, support.psqlUser(),
                support.psqlPassword())) {
            final int initialConnections = countConnections(conn);
            assertThat(initialConnections, greaterThanOrEqualTo(maxPoolSize));

            final int terminateConnections = terminateConnections(conn);
            assertThat(terminateConnections, greaterThanOrEqualTo(maxPoolSize - 1));
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        try {
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
        } finally {
            testConnection.close();
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
