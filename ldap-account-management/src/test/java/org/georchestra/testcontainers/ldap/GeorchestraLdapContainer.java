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
package org.georchestra.testcontainers.ldap;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * A <a href="https://www.testcontainers.org">Testconainers</a> container to run
 * georchestra's LDAP server as a JUnit {@code @Rule}, exposing the container's
 * port {@code 389} on the host's {@code 11389} by default.
 */
public class GeorchestraLdapContainer extends GenericContainer<GeorchestraLdapContainer> {

    public static final int DEFAULT_HOST_PORT = 11389;

    public GeorchestraLdapContainer() {
        this(DEFAULT_HOST_PORT);
    }

    public GeorchestraLdapContainer(int hostPort) {
        this(DockerImageName.parse("georchestra/ldap:latest"), hostPort);
    }

    GeorchestraLdapContainer(final DockerImageName dockerImageName, int hostPort) {
        super(dockerImageName);
        withExposedPorts(389);
        addFixedExposedPort(hostPort, 389);
        addEnv("SLAPD_ORGANISATION", "georchestra");
        addEnv("SLAPD_DOMAIN", "georchestra.org");
        addEnv("SLAPD_PASSWORD", "secret");
        addEnv("SLAPD_LOG_LEVEL", "32768");

        super.withLogConsumer(outputFrame -> System.out.print("--- ldap: " + outputFrame.getUtf8String()));

        withCreateContainerCmdModifier(it -> it.withName("testcontainers-georchestra-ldap-" + Base58.randomString(8)));

        // this is faster than Wait.forHealthcheck() which is set every 30secs
        // in georchestra/ldap's Dockerfile
        waitingFor(Wait.forLogMessage(".*slapd starting.*\\n", 1));
    }
}
