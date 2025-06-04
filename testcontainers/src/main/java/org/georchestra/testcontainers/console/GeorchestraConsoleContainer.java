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
package org.georchestra.testcontainers.console;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * A <a href="https://www.testcontainers.org">Testconainers</a> container to run
 * georchestra's Console server as a JUnit {@code @Rule} based on
 * {@code georchestra/console:latest}.
 * <p>
 * Get the host mapped port for {@code 8080} with {@link #getMappedPort(int)
 * getMappedPort(8080)}, or directly with {@link #getMappedConsolePort()}
 * <p>
 * For convenience, once the container is started, the mapped console app port
 * will automatically set as a {@link System#getProperty System property}
 * {@code console.port=<mapped port>}.
 */
public class GeorchestraConsoleContainer extends GenericContainer<GeorchestraConsoleContainer> {

    public GeorchestraConsoleContainer() {
        this(DockerImageName.parse("georchestra/console:latest"));
    }

    GeorchestraConsoleContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withEnv("JAVA_OPTIONS", "-Dorg.eclipse.jetty.annotations.AnnotationParser.LEVEL=OFF");

        withExposedPorts(8080);
        withCreateContainerCmdModifier(
                it -> it.withName("testcontainers-georchestra-console-" + Base58.randomString(8)));

        waitingFor(Wait.forHttp("/console/internal/users")//
                .withHeader("sec-proxy", "true")//
                .withHeader("sec-username", "testadmin")//
                .withHeader("sec-roles", "ROLE_ADMINISTRATOR")//
        );
    }

    public GeorchestraConsoleContainer withLogToStdOut() {
        return withLogConsumer(outputFrame -> System.out.print("--- console: " + outputFrame.getUtf8String()));
    }

    public int getMappedConsolePort() {
        return getMappedPort(8080);
    }

    protected @Override void doStart() {
        super.doStart();
        int mappedPort = getMappedConsolePort();
        String host = getHost();
        System.setProperty("console.port", String.valueOf(mappedPort));
        System.setProperty("console.host", host);
        System.out.println("Automatically set system property console.port=" + mappedPort);
        System.out.println("Automatically set system property console.host=" + host);
    }
}
