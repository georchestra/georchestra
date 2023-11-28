package org.georchestra.testcontainers.smtp;

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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

/**
 * A <a href="https://www.testcontainers.org">Testconainers</a> container to run
 * SMTP server as a JUnit {@code @Rule} based on
 * {@code camptocamp/smtp-sink:latest}.
 * <p>
 * Get the host mapped port for {@code 25} with {@link #getMappedPort(int)
 * getMappedPort(25)}, or directly with {@link #getMappedSmtpPort()}
 * <p>
 * For convenience, once the container is started, the mapped SMTP port will
 * automatically set as a {@link System#getProperty System property}
 * {@code ldapPort=<mapped port>}, following standard georchestra
 * datadirectory's property name.
 */
public class SmtpContainer extends GenericContainer<SmtpContainer> {

    public SmtpContainer() {
        this(DockerImageName.parse("camptocamp/smtp-sink:latest"));
    }

    SmtpContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(25);

        withCreateContainerCmdModifier(it -> it.withName("testcontainers-smtp-" + Base58.randomString(8)));
    }

    public SmtpContainer withLogToStdOut() {
        return withLogConsumer(outputFrame -> System.out.print("--- smtp: " + outputFrame.getUtf8String()));
    }

    public int getMappedSmtpPort() {
        return getMappedPort(25);
    }

    protected @Override void doStart() {
        super.doStart();
        int mappedLdapPort = getMappedSmtpPort();
        System.setProperty("smtpPort", String.valueOf(mappedLdapPort));
        System.out.println("Automatically set system property smtpHost=" + mappedLdapPort);
    }
}
