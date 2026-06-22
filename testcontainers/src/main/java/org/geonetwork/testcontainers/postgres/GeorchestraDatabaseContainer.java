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
package org.geonetwork.testcontainers.postgres;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * A <a href="https://www.testcontainers.org">Testconainers</a> container to run
 * the full georchestra's PostgreSQL database server as a JUnit {@code @Rule}
 * based on {@code georchestra/database:latest}.
 * <p>
 * Get the host mapped port for Postgres' {@code 5432} with
 * {@link #getMappedPort(int) getMappedPort(5432)}, or directly with
 * {@link #getMappedDatabasePort()}
 * <p>
 * For convenience, once the container is started, the mapped PostgreSQL port
 * will automatically set as a {@link System#getProperty System property}
 * {@code jdbc.port=<mapped port>}, and {@code jdbc.host=<host>}, with
 * {@link #getHost()}'s value (may not be the local machine).
 */
@Slf4j
public class GeorchestraDatabaseContainer extends GenericContainer<GeorchestraDatabaseContainer> {

    public GeorchestraDatabaseContainer() {
        this(DockerImageName.parse("georchestra/database:latest"));
    }

    private int mappedPort;

    GeorchestraDatabaseContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(5432);
        addEnv("POSTGRES_USER", "georchestra");
        addEnv("POSTGRES_PASSWORD", "georchestra");

        withCreateContainerCmdModifier(
                it -> it.withName("testcontainers-georchestra-database-" + Base58.randomString(8)));

        waitingFor(new LogMessageWaitStrategy()//
                .withRegEx(".*database system is ready to accept connections.*\\s")//
                .withTimes(2)//
                .withStartupTimeout(Duration.of(60, SECONDS))//
        );
    }

    public GeorchestraDatabaseContainer withLogToStdOut() {
        return withLogConsumer(outputFrame -> System.out.print("--- database: " + outputFrame.getUtf8String()));
    }

    public int getMappedDatabasePort() {
        return mappedPort;
    }

    protected @Override void doStart() {
        super.doStart();
        mappedPort = getMappedPort(5432);
        String host = super.getHost();
        log.info("jdbc.port=" + mappedPort);
        log.info("jdbc.host=" + host);
    }
}
