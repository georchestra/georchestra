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
package org.georchestra.console.integration;

import org.geonetwork.testcontainers.postgres.GeorchestraDatabaseContainer;
import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base integration tests class to set up and tear down testcontainers for
 * {@link GeorchestraLdapContainer ldap} and {@link GeorchestraDatabaseContainer
 * database}.
 * 
 */
public class ConsoleIntegrationTest {

    public static GeorchestraLdapContainer ldap;
    public static GeorchestraDatabaseContainer database;

    public static @BeforeClass void setUpTestContainers() {
        ldap = new GeorchestraLdapContainer();
        database = new GeorchestraDatabaseContainer();
        ldap.start();
        database.start();
        int dbport = database.getMappedDatabasePort();
        System.setProperty("pgsqlPort", String.valueOf(dbport));
    }

    public static @AfterClass void tearDownTestContainers() {
        System.clearProperty("pgsqlPort");
        database.stop();
        ldap.stop();
    }
}
