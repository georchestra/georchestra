/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

package org.georchestra.ogcservstatistics.log4j;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import javax.sql.DataSource;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.georchestra.ogcservstatistics.OGCServStatisticsException;
import org.georchestra.ogcservstatistics.dataservices.DataServicesConfiguration;
import org.georchestra.ogcservstatistics.dataservices.InsertCommand;

/**
 * This appender is responsible to record the OGC services in the configured
 * database table.
 * <p>
 * <b>Usage:</b> There are two ways of configuring the target database this
 * appender will insert log entries to. The first and preferred one since
 * geOrchestra 18.12, is that the client code calls
 * {@link OGCServicesAppender#setDataSource(DataSource)} with an appropriately
 * configured connection pool.
 * <p>
 * Additionally, and for backwards compatibility, the database URL and
 * connection credentials can be set on the {@code log4j.properties}
 * configuration as follows:
 * 
 * <pre>
 * <code>
 * log4j.rootLogger= INFO, OGCSERVICES
 * log4j.appender.OGCSERVICES=org.georchestra.ogcservstatistics.log4j.OGCServicesAppender
 * log4j.appender.OGCSERVICES.activated=true
 * log4j.appender.OGCSERVICES.jdbcURL=jdbc:postgresql://localhost:5432/testdb
 * log4j.appender.OGCSERVICES.databaseUser=postgres
 * log4j.appender.OGCSERVICES.databasePassword=postgres
 * log4j.appender.OGCSERVICES.bufferSize=1
 * </code>
 * </pre>
 * <p>
 * Note: you could improve the performance increasing the <b>bufferSize</b>
 * value.
 * </p>
 * 
 * <p>
 * To load the configuration you should include the following code:
 * </p>
 * 
 * <pre>
 * 
 * static {
 *     final String file = "[install-dir]/log4j.properties";
 *     PropertyConfigurator.configure(file);
 * }
 * </pre>
 * <p>
 * To log a message you should use the following idiom:
 * </p>
 * 
 * <pre>
 * final Date time = calendar.getTime();
 * final String user = getUserName();
 * final String request = "http://ns383241.ovh.net:80/geoserver/wfs/...";
 * String ogcServiceMessage = OGCServiceMessageFormatter.format(user, time, request);
 * LOGGER.info(ogcServiceMessage);
 * </pre>
 * 
 * 
 * @author Mauricio Pazos
 */
public class OGCServicesAppender extends AppenderSkeleton {

    protected String databaseUser = "";

    protected String databasePassword = "";

    private String jdbcURL = "";

    /**
     * Activated true: it log ogc services false: it does not log ogc services
     */
    protected boolean activated = false;

    private static DataServicesConfiguration dataServiceConfiguration = DataServicesConfiguration.getInstance();

    public OGCServicesAppender() {
        super();
    }

    public String getJdbcURL() {
        return this.jdbcURL;
    }

    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public @Deprecated int getBufferSize() {
        return 1;
    }

    public @Deprecated void setBufferSize(int newBufferSize) {
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * This hook method called after set all appender properties. In this case the
     * configuration is set.
     * 
     */
    @Override
    public void activateOptions() {
        dataServiceConfiguration.initialize(getJdbcURL(), getDatabaseUser(), getDatabasePassword());
    }

    public static void setDataSource(DataSource dataSource) {
        Objects.requireNonNull(dataSource, "dataSource can't be null");
        dataServiceConfiguration.initialize(dataSource);
    }

    /**
     * Appends the OGC Service in the table.
     * 
     * The string present in buffer is parsed, if it is an interesting OGC service
     * then extracts the data required to insert a row in the table.
     * 
     * @implNote This method is called from inside the {@code synchronized} method
     *           {@link AppenderSkeleton#doAppend}, as it calls
     *           {@link OGCServiceParser#parseLog} and runs one
     *           {@link InsertCommand} per parsed entry, the job is done
     *           asynchronously on the platforms' default {@link ForkJoinPool} to
     *           avoid hindering application performance.
     */
    @Override
    protected void append(final LoggingEvent event) {
        // do not run if not activated or closed
        if (!this.activated && !this.closed)
            return;

        CompletableFuture.runAsync(() -> {
            try {
                // let it finish if the task was issued even if the appender was closed after
                // the fact
                if (!this.activated) {
                    return;
                }
                String msg = event.getRenderedMessage();
                List<Map<String, Object>> logList = OGCServiceParser.parseLog(msg);
                insert(logList);
            } catch (Exception ex) {
                errorHandler.error("Failed to insert the ogc service record", ex, ErrorCode.WRITE_FAILURE);
            }
        });
    }

    private void insert(List<Map<String, Object>> ogcServiceRecords) {

        try (Connection c = dataServiceConfiguration.getConnection()) {
            for (Map<String, Object> entry : ogcServiceRecords) {
                InsertCommand cmd = new InsertCommand();
                cmd.setConnection(c);
                cmd.setRowValues(entry);
                cmd.execute();
            }
        } catch (Exception e) {

            errorHandler.error("Failed to insert the log", e, ErrorCode.WRITE_FAILURE);
        }

    }

    @Override
    public void finalize() {
        close();
    }

    /**
     * Release all the allocated resources
     */
    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public boolean requiresLayout() {
        return false; // does not require layout configuration
    }

}
