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

package org.georchestra.console.integration.ws.backoffice.users;

import static org.junit.Assert.assertNotNull;

import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.console.ws.backoffice.users.UserInfoExporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class UserInfoExporterIT extends ConsoleIntegrationTest {

    private @Autowired UserInfoExporter ldifExporter;

    public @Test void test() throws Exception {
        String ldifContents = ldifExporter.exportAsLdif("testuser");
        assertNotNull(ldifContents);
        System.err.println(ldifContents);
    }

}
