/*
 * Copyright (C) 2019 by the geOrchestra PSC
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

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * A junit {@link Rule} that can be {@code @Autowired} into integration tests,
 * validates the ldap and database external resources and sets up a mock MVC.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * <code>
 * {@literal @}RunWith(SpringRunner.class)
 * {@literal @}WebAppConfiguration
 * {@literal @}ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
 * public class ExampleIT {
 *      public {@literal @}Rule {@literal @}Autowired IntegrationTestSupport support;
 *
 *      public {@literal @}Test void testFoo(){
 *          support.perform(MockMvcRequestBuilders.get("/foo"))
 *                 .andExpect(status().isOk())//
 *                 .andExpect(content().contentTypeCompatibleWith("application/json"))//
 *                 .andExpect(jsonPath("$.bar").value("expected value"));
 *      }
 *  }
 * </code>
 * </pre>
 */
public @Service class IntegrationTestSupport extends ExternalResource {
    private static Logger LOGGER = Logger.getLogger(IntegrationTestSupport.class);

    private @Autowired LdapTemplate ldapTemplate;

    private @Value("${ldapPort}") int ldapPort;

    private @Value("${pgsqlPort}") int psqlPort;
    private @Value("${pgsqlUser}") String psqlUser;
    private @Value("${pgsqlPassword}") String psqlPassword;

    private TestName testName = new TestName();

    private Set<String> createdUsers;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    public @Override Statement apply(Statement base, Description description) {
        testName.apply(base, description);
        return super.apply(base, description);
    }

    public @Override void before() {
        this.createdUsers = new HashSet<>();
        LOGGER.debug(String.format("############# %s: pgsqlPort: %s, ldapPort: %s\n", testName.getMethodName(),
                psqlPort, ldapPort));
        // pre-flight sanity check
        assertNotNull(ldapTemplate.lookup("ou=users"));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    public @Override void after() {
        for (String user : new HashSet<>(createdUsers)) {
            try {
                deleteUser(user);
            } catch (Exception e) {
                LogFactory.getLog(getClass()).error("Error deleting " + user, e);
            }
        }
    }

    public int ldapPort() {
        return ldapPort;
    }

    public int psqlPort() {
        return psqlPort;
    }

    public String psqlUser() {
        return psqlUser;
    }

    public String psqlPassword() {
        return psqlPassword;
    }

    public ResultActions perform(RequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder);
    }

    public String testName() {
        return testName.getMethodName();
    }

    public String readResourceToString(String name) throws URISyntaxException, IOException {
        java.net.URL url = this.getClass().getResource(name);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }

    public ResultActions createUser(String userName) throws Exception {
        ResultActions perform = perform(post("/private/users")
                .content(readResourceToString("/testData/createUserPayload.json").replace("{uuid}", userName)));
        this.createdUsers.add(userName);
        return perform;
    }

    public ResultActions createUser(String userName, boolean expired) throws Exception {
        GregorianCalendar date = new GregorianCalendar();
        if (expired) {
            date.add(Calendar.YEAR, -10);
        } else {
            date.add(Calendar.YEAR, 10);
        }
        String dateAsString = new SimpleDateFormat("yyyy-MM-dd").format(date.getTime());
        return perform(post("/private/users")
                .content(readResourceToString("/testData/createUserPayload.json").replace("{uuid}", userName)
                        .replaceAll("\"shadowExpire\": null,", String.format("\"shadowExpire\": %s,", dateAsString))));
    }

    public ResultActions deleteUser(String userName) throws Exception {
        this.createdUsers.remove(userName);
        return perform(delete("/private/users/" + userName));
    }
}
