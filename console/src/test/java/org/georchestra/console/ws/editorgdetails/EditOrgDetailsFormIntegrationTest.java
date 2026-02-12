package org.georchestra.console.ws.editorgdetails;

import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class EditOrgDetailsFormIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private static GeorchestraLdapContainer ldapContainer = new GeorchestraLdapContainer();

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @DynamicPropertySource
    static void ldapProperties(DynamicPropertyRegistry registry) {
        ldapContainer.start();
        int ldapPort = ldapContainer.getMappedLdapPort();
        registry.add("ldapPort", () -> ldapPort);
    }

    public @Test @WithMockUser(username = "admin", roles = "SUPERUSER") void testEditOrgDetailsFormView()
            throws Exception {
        mockMvc.perform(get("/account/orgdetails").header("sec-org", "PSC").characterEncoding("utf-8"))
                .andExpect(status().isOk());
    }
}
