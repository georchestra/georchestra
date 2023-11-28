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
package org.georchestra.testcontainers.rabbitmq;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * A <a href="https://www.testcontainers.org">Testconainers</a> container to run
 * georchestra's Rabbitmq server as a JUnit {@code @Rule} based on
 * {@code bitnami/rabbitmq:3.12}.
 * <p>
 * Get the host mapped port for {@code 5672} with {@link #getMappedPort(int)
 * getMappedPort(389)}, or directly with {@link #getMappedRabbitmqPort()}
 * <p>
 * For convenience, once the container is started, the mapped Rabbitmq port will
 * automatically set as a {@link System#getProperty System property}
 * {@code rabbitmqPort=<mapped port>}, following standard georchestra
 * datadirectory's property name.
 */
public class RabbitmqContainer extends GenericContainer<RabbitmqContainer> {

    public RabbitmqContainer() {
        this(DockerImageName.parse("bitnami/rabbitmq:3.12"));
    }

    RabbitmqContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(5672);
        addEnv("RABBITMQ_USERNAME", "georchestra");
        addEnv("RABBITMQ_PASSWORD", "georchestra");
        addEnv("RABBITMQ_PORT", "5672");

        withCreateContainerCmdModifier(it -> it.withName("testcontainers-rabbitmq-" + Base58.randomString(8)));

        // waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));
    }

    public RabbitmqContainer withLogToStdOut() {
        return withLogConsumer(outputFrame -> System.out.print("--- rabbitmq: " + outputFrame.getUtf8String()));
    }

    public int getMappedRabbitmqPort() {
        return getMappedPort(5672);
    }

    protected @Override void doStart() {
        super.doStart();
        int mappedLdapPort = getMappedRabbitmqPort();
        System.setProperty("rabbitmqPort", String.valueOf(mappedLdapPort));
        System.out.println("Automatically set system property rabbitmqPort=" + mappedLdapPort);
    }
}
