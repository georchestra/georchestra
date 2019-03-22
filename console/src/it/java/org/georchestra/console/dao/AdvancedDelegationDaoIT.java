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
package org.georchestra.console.dao;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.georchestra.console.integration.IntegrationTestSupport;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class AdvancedDelegationDaoIT {

    public @Rule @Autowired IntegrationTestSupport support;

    private @Autowired AdvancedDelegationDao delegate;

    private @Autowired ComboPooledDataSource ds;

    private @Value("${dataSource.maxPoolSize:10}") int maxConnections;
    private @Value("${dataSource.timeout:1000}") int timeoutMillis;

    public @Before void before() {
        assertThat(timeoutMillis, Matchers.greaterThan(0));
    }

    /**
     * The connection pool should timeout within reasonable limits of the configured
     * timeout. See https://github.com/georchestra/georchestra/issues/2395
     */
    public @Test void testConnectionTimeoutSet() {
        await().atMost(10 * timeoutMillis, TimeUnit.MILLISECONDS).until(() -> exhaustConnectionPool());
    }

    private boolean exhaustConnectionPool() {
        List<Connection> allConnections = IntStream.range(0, maxConnections).mapToObj(i -> {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                return null;
            }
        }).filter(c -> c != null).collect(Collectors.toList());
        // REVISIT: spring initialization is eating one connection. May be fixed when
        // upgrading the spring version.
        assertTrue(allConnections.size() == maxConnections || allConnections.size() == maxConnections - 1);
        try {
            ds.getConnection();
        } catch (SQLException expected) {
            allConnections.forEach(t -> {
                try {
                    t.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return true;
        }
        return false;
    }

    public @Test void testFindByOrg_closesJdbcResources() {
        for (int i = 0; i < maxConnections; i++) {
            try {
                assertNotNull(delegate.findByOrg("notAnActualOrg"));
            } catch (SQLException e) {
                throw new RuntimeException("Failed before reaching max connections", e);
            }
        }
        try {
            assertNotNull(delegate.findByOrg("notAnActualOrg"));
        } catch (SQLException e) {
            throw new RuntimeException("Looks like JDBC resources are not closed", e);
        }
    }

    public @Test void testFindByRole_closesJdbcResources() {
        for (int i = 0; i < maxConnections; i++) {
            try {
                assertNotNull(delegate.findByRole("notAnActualRole"));
            } catch (SQLException e) {
                throw new RuntimeException("Failed before reaching max connections", e);
            }
        }
        try {
            assertNotNull(delegate.findByRole("notAnActualRole"));
        } catch (SQLException e) {
            throw new RuntimeException("Looks like JDBC resources are not closed", e);
        }
    }

    public @Test void testFindUsersUnderDelegation_closesJdbcResources() {
        for (int i = 0; i < maxConnections; i++) {
            try {
                assertNotNull(delegate.findUsersUnderDelegation("notAnActualAdmin"));
            } catch (Exception e) {
                throw new RuntimeException("Failed before reaching max connections", e);
            }
        }
        try {
            assertNotNull(delegate.findUsersUnderDelegation("notAnActualAdmin"));
        } catch (Exception e) {
            throw new RuntimeException("Looks like JDBC resources are not closed", e);
        }
    }

}
