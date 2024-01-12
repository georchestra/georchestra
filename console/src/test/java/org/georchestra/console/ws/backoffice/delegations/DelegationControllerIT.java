/*
 * Copyright (C) 2024 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.delegations;

import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.console.integration.IntegrationTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableWebMvc
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@PropertySource("classpath:console-it.properties")
@WebAppConfiguration
public class DelegationControllerIT extends ConsoleIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    public @Rule @Autowired IntegrationTestSupport support;

    @WithMockUser(roles = { "SUPERUSER" })
    public @Test void testDelegationControllersUidWithDots() throws Exception {
        String payload = "{\"uid\":\"flup.top@georchestra.com\",\"roles\":[\"GN_ADMIN\"],\"orgs\":[\"psc\"]}";

        support.createUser("flup.top@georchestra.com");
        support.perform(post("/private/delegation/flup.top@georchestra.com").content(payload))
                .andExpect(status().isOk());

        support.perform(get("/private/delegation/flup.top@georchestra.com")).andExpect(content().string(payload));
    }

}
