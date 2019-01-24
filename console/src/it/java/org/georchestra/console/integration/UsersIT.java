package org.georchestra.console.integration;

import org.apache.commons.lang3.RandomStringUtils;
import org.georchestra.console.integration.instruments.ModifiableUsernameToken;
import org.georchestra.console.integration.instruments.WithMockCustomUser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@EnableWebMvc
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@PropertySource("classpath:console-it.properties")
@WebAppConfiguration
public class UsersIT {

    private @Autowired
    LdapTemplate ldapTemplateSanityCheck;

    private @Value("${ldap_port}") int ldapPort;

    private @Value("${psql_port}") int psqlPort;

    public @Rule
    TestName testName = new TestName();

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    String userAdminName;

    public static @BeforeClass
    void init() {
    }

    public @Before
    void before() {
        System.err.printf("############# %s: psql_port: %s, ldap_port: %s\n", testName.getMethodName(), psqlPort, ldapPort);
        // pre-flight sanity check
        assertNotNull(ldapTemplateSanityCheck.lookup("cn=admin"));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

    }

    @WithMockCustomUser
    public @Test
    void testCreate() throws Exception {
        userAdminName = "IT_USER_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        ((ModifiableUsernameToken)SecurityContextHolder.getContext().getAuthentication()).setUserName(userAdminName);
        createUser(userAdminName);
        String role1Name = createRole();
        String role2Name = createRole();
        setRole(userAdminName, role1Name, role2Name);
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        getProfile(userAdminName)
                .andExpect(jsonPath("$.roles[1]").value(role1Name))
                .andExpect(jsonPath("$.roles[2]").value(role2Name));

    }

    private void createUser(String userName) throws Exception {
        mockMvc.perform(post("/private/users").content(readRessourceToString("/testData/createUserPayload.json").replace("{uuid}", userName)));
    }

    private ResultActions getProfile(String name) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get("/private/users/profile"));
    }

    private String createRole() throws Exception {
        String roleName = "IT_ROLE_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        String body = "{ \"cn\": \"" + roleName + "\", \"description\": \"Role Description\", \"isFavorite\": false }";
        this.mockMvc.perform(post("/private/roles").content(body));
        return roleName;
    }

    private void setRole(String userName, String role1Name, String role2Name) throws Exception {
        String body = "{ \"users\":[\"" + userName + "\"],\"PUT\":[\""+ role1Name + "\", \"" + role2Name + "\"],\"DELETE\":[]}";
        this.mockMvc.perform(post("/private/roles_users").content(body));
    }

    private String readRessourceToString(String name) throws URISyntaxException, IOException {
        java.net.URL url = this.getClass().getResource(name);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }
}
