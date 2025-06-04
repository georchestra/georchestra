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
package org.georchestra.testcontainers.ldap;

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
public class GeorchestraLdapContainer extends GenericContainer<GeorchestraLdapContainer> {

    public GeorchestraLdapContainer() {
        this(DockerImageName.parse("georchestra/ldap:latest"));
    }

    GeorchestraLdapContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(389);
//        addFixedExposedPort(hostPort, 389);
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

    public int getMappedLdapPort() {
        return getMappedPort(389);
    }

    protected @Override void doStart() {
        super.doStart();
        int mappedLdapPort = getMappedLdapPort();
        String host = getHost();
        System.setProperty("ldapPort", String.valueOf(mappedLdapPort));
        System.setProperty("ldapHost", host);
        System.out.println("Automatically set system property ldapPort=" + mappedLdapPort);
        System.out.println("Automatically set system property ldapHost=" + host);
    }
}
