/*
 * Copyright (C) 2009-2026 by the geOrchestra PSC
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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * A <a href="https://www.testcontainers.org">Testconainers</a> container to run
 * georchestra's LDAP server as a JUnit {@code @Rule} based on
 * {@code georchestra/ldap:latest}.
 * <p>
 * Get the host mapped port for {@code 389} with {@link #getMappedPort(int)
 * getMappedPort(389)}, or directly with {@link #getMappedLdapPort()}
 * <p>
 * For convenience, once the container is started, the mapped LDAP port will
 * automatically set as a {@link System#getProperty System property}
 * {@code ldapPort=<mapped port>}, following standard georchestra
 * datadirectory's property name.
 */
@Slf4j
public class GeorchestraLdapContainer extends GenericContainer<GeorchestraLdapContainer> {

    private int mappedLdapPort;

    public GeorchestraLdapContainer() {
        this(DockerImageName.parse("georchestra/ldap:latest"));
    }

    GeorchestraLdapContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(389);
        addEnv("SLAPD_ORGANISATION", "georchestra");
        addEnv("SLAPD_DOMAIN", "georchestra.org");
        addEnv("SLAPD_PASSWORD", "secret");
        addEnv("SLAPD_LOG_LEVEL", "32768");

        withCreateContainerCmdModifier(it -> it.withName("testcontainers-georchestra-ldap-" + Base58.randomString(8)));

        // this is faster than Wait.forHealthcheck() which is set every 30secs
        // in georchestra/ldap's Dockerfile
        waitingFor(Wait.forLogMessage(".*slapd starting.*\\n", 1));
    }

    public GeorchestraLdapContainer withLogToStdOut() {
        return withLogConsumer(outputFrame -> System.out.print("--- ldap: " + outputFrame.getUtf8String()));
    }

    /**
     * Returns the mapped LDAP port. The getter should be called after having started the container
     * (e.g. `doStart()` having been instanciated).
     *
     * @return the port number locally mapped to the usual LDAP port (389).
     */
    public int getMappedLdapPort() {
        return mappedLdapPort;
    }

    protected @Override void doStart() {
        super.doStart();
        mappedLdapPort = getMappedPort(389);

        log.info("Automatically set system property ldapPort=" + mappedLdapPort);
        log.info("Automatically set system property ldapHost=" + getHost());
    }
}
