package org.georchestra.console.integration;

import org.apache.commons.lang3.RandomStringUtils;
import org.georchestra.console.integration.instruments.WithMockRandomUidUser;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @WithMockRandomUidUser
    public @Test
    void profile() throws Exception {
        String userName = ((User)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername();
        createUser(userName);

        getProfile().andExpect(jsonPath("$.roles[0]").value("USER"));

        String role1Name = createRole();
        String role2Name = createRole();
        setRole(userName, role1Name, role2Name);

        getProfile()
                .andExpect(jsonPath("$.roles[1]").value(role1Name))
                .andExpect(jsonPath("$.roles[2]").value(role2Name));

    }

    @WithMockRandomUidUser
    public @Test
    void changeOrgAndUid() throws Exception {
        String userName = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();
        String newUserName = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();
        createUser(userName);

        mockMvc.perform(put("/private/users/" + userName).content(readRessourceToString("/testData/createUserPayload.json")
                .replace("{uuid}", newUserName)
                .replace("psc", "cra")));


        mockMvc.perform(get("/private/users/" + newUserName))
                .andExpect(jsonPath("$.org").value("cra"))
                .andExpect(jsonPath("$.uid").value(newUserName));
    }

    @WithMockRandomUidUser
    public @Test
    void userDetail() throws Exception {
        String userName = ((User)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername();
        createUser(userName);

        mockMvc.perform(get("/account/userdetails").header("sec-username", userName)).andExpect(status().isOk());
    }

    @WithMockRandomUidUser
    public @Test
    void users() throws Exception {
        String userName = ((User)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername();
        createUser(userName);

        mockMvc.perform(get("/private/users"))
                .andExpect(jsonPath("$[?(@.pending in [false])].['pending']").exists())
                .andExpect(jsonPath("$[?(@.pending in [true])].['pending']").exists());
    }

    private void createUser(String userName) throws Exception {
        mockMvc.perform(post("/private/users").content(readRessourceToString("/testData/createUserPayload.json").replace("{uuid}", userName)));
    }

    private ResultActions getProfile() throws Exception {
        return this.mockMvc.perform(get("/private/users/profile"));
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
