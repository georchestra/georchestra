/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.ogcservstatistics.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics;
import org.georchestra.ogcservstatistics.dataservices.DataServicesConfiguration;
import org.georchestra.ogcservstatistics.dataservices.DeleteAllCommand;
import org.junit.Assert;
import org.junit.rules.ExternalResource;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * 
 */
public class IntegrationTestSupport extends ExternalResource {

    protected @Override void before() {
        Properties props = setupConfig();
        OGCServiceStatistics.configure(props);
    }

    private Properties setupConfig() {
        Properties props = loadDefaultProperties();
        String databaseUrl = getDatabaseUrl();
        checkConnection(databaseUrl);
        System.err.println("Database URL: " + databaseUrl);
        props.setProperty("log4j.appender.OGCSERVICES.jdbcURL", databaseUrl);
        props.setProperty("log4j.appender.OGCSERVICES.activated", "true");
        props.setProperty("log4j.reset", "true");
        return props;
    }

    private void checkConnection(String databaseUrl) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(databaseUrl);
        ds.setUser("georchestra");
        ds.setPassword("georchestra");
        Awaitility.given().pollDelay(Duration.ONE_SECOND).pollInterval(Duration.FIVE_SECONDS).await()
                .atMost(Duration.ONE_MINUTE).until(() -> connectionSucceeded(ds, databaseUrl));
    }

    private boolean connectionSucceeded(PGSimpleDataSource ds, String databaseUrl) {
        try (Connection c = ds.getConnection()) {
            System.err.println("Successfully connected to " + databaseUrl);
            return true;
        } catch (SQLException e) {
            System.err.println("Awaiting for database to become available: " + databaseUrl);
        }
        return false;
    }

    public void disableAppender() {
        Properties props = setupConfig();
        props.setProperty("log4j.appender.OGCSERVICES.activated", "false");
        OGCServiceStatistics.configure(props);
    }

    private Properties loadDefaultProperties() {
        Properties props = new Properties();
        try (InputStream resource = getClass()
                .getResourceAsStream("/org/georchestra/ogcservstatistics/log4j.properties")) {
            props.load(new InputStreamReader(resource, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    public String getDatabaseUrl() {
        return String.format("jdbc:postgresql://localhost:%d/georchestra", getDatabasePort());
    }

    public int getDatabasePort() {
        String portArg = System.getProperty("psql_port");
        if (null == portArg || portArg.trim().isEmpty()) {
            portArg = System.getenv("psql_port");
        }
        Assert.assertNotNull(portArg);
        return Integer.parseInt(portArg);
    }

    public void deleteAllEntries() {
        try (Connection connection = DataServicesConfiguration.getInstance().getConnection()) {
            DeleteAllCommand cmd = new DeleteAllCommand();
            cmd.setConnection(connection);
            cmd.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
