/*
 * Copyright (C) 2021 by the geOrchestra PSC
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

import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * {@link PostgreSQLContainer PostgreSQL Container} adapted to run as a
 * georchestra geonetwork database
 * <p>
 * <ul>
 * <li>Image: {@code postgres:13-alpine}
 * <li>Exposed port: use {@link #getMappedPort(5432)} to obtain the local port
 * <li>database: {@code georchestra}
 * <li>username: {@code georchestra}
 * <li>password: {@code georchestra}
 * <li>init script:
 * {@code src/test/resources/testcontainers/GeonetworkPostgresContainer.sql}
 * <ul>
 * <li>{@code CREATE USER geonetwork WITH SUPERUSER PASSWORD 'georchestra';}
 * <li>{@code CREATE SCHEMA geonetwork AUTHORIZATION geonetwork;}
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Get the host mapped port for Postgres' {@code 5432} with
 * {@link #getMappedPort(int) getMappedPort(5432)}, or directly with
 * {@link #getMappedPostgresPort()}
 * <p>
 * For convenience, once the container is started, the mapped PostgreSQL port
 * will automatically set as a {@link System#getProperty System property}
 * {@code jdbc.port=<mapped port>}, following standard georchestra
 * datadirectory's property name.
 */
public class GeonetworkPostgresContainer extends PostgreSQLContainer<GeonetworkPostgresContainer> {

    static final String INIT_SCRIPT = "testcontainers/GeonetworkPostgresContainer.sql";

    public GeonetworkPostgresContainer() {
        super(DockerImageName.parse("postgres:13-alpine"));
        withCreateContainerCmdModifier(
                it -> it.withName("testcontainers-geonetwork-postgres-" + Base58.randomString(8)));
        withDatabaseName("georchestra");
        withUsername("georchestra");
        withPassword("georchestra");
        withInitScript(INIT_SCRIPT);
    }

    public GeonetworkPostgresContainer withLogToStdOut() {
        return withLogConsumer(outputFrame -> System.out.print("--- database: " + outputFrame.getUtf8String()));
    }

    public int getMappedPostgresPort() {
        return getMappedPort(5432);
    }

    protected @Override void doStart() {
        super.doStart();
        int mappedPort = getMappedPostgresPort();
        String host = super.getHost();
        System.setProperty("jdbc.port", String.valueOf(mappedPort));
        System.setProperty("jdbc.host", host);
        System.out.println("Automatically set system property jdbc.port=" + mappedPort);
        System.out.println("Automatically set system property jdbc.host=" + host);
    }
}
