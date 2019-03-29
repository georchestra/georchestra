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

    private @Value("${ldap_port}") int ldapPort;

    private @Value("${psql_port}") int psqlPort;
    private @Value("${pgsqlUser}") String psqlUser;
    private @Value("${pgsqlPassword}") String psqlPassword;

    private TestName testName = new TestName();

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    public @Override Statement apply(Statement base, Description description) {
        testName.apply(base, description);
        return super.apply(base, description);
    }

    protected @Override void before() {
        LOGGER.debug(String.format("############# %s: psql_port: %s, ldap_port: %s\n", testName.getMethodName(),
                psqlPort, ldapPort));
        // pre-flight sanity check
        assertNotNull(ldapTemplate.lookup("cn=admin"));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    protected @Override void after() {

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
}
