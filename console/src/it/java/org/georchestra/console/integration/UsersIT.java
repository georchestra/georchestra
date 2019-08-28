package org.georchestra.console.integration;

import static com.github.database.rider.core.api.dataset.SeedStrategy.CLEAN_INSERT;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.georchestra.console.ds.PostgresExtendedDataTypeFactory;
import org.georchestra.console.integration.instruments.WithMockRandomUidUser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;

@RunWith(SpringRunner.class)
@EnableWebMvc
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@PropertySource("classpath:console-it.properties")
@WebAppConfiguration
@DBRider
public class UsersIT {

    public @Rule @Autowired IntegrationTestSupport support;

    private @Autowired LdapTemplate ldapTemplateSanityCheck;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    public static @BeforeClass void init() {
    }

    public @Before void before() {
        // pre-flight sanity check
        assertNotNull(ldapTemplateSanityCheck.lookup("cn=admin"));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @WithMockRandomUidUser
    public @Test void profile() throws Exception {
        String userName = ((User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
                .getUsername();
        createUser(userName);

        getProfile().andExpect(jsonPath("$.roles[0]").value("USER"));

        String role1Name = createRole();
        String role2Name = createRole();
        setRole(userName, role1Name, role2Name);

        getProfile().andExpect(jsonPath("$.roles[1]").value(role1Name))
                .andExpect(jsonPath("$.roles[2]").value(role2Name));

    }

    @WithMockRandomUidUser
    public @Test void changeOrgAndUid() throws Exception {
        String userName = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();
        String newUserName = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();
        createUser(userName);

        support.perform(put("/private/users/" + userName)
                .content(support.readResourceToString("/testData/createUserPayload.json").replace("{uuid}", newUserName)
                        .replace("psc", "cra")));

        support.perform(get("/private/users/" + newUserName)).andExpect(jsonPath("$.org").value("cra"))
                .andExpect(jsonPath("$.uid").value(newUserName));
    }

    @WithMockRandomUidUser
    public @Test void userDetail() throws Exception {
        String userName = ((User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
                .getUsername();
        createUser(userName);

        support.perform(get("/account/userdetails").header("sec-username", userName)).andExpect(status().isOk());
    }

    @WithMockRandomUidUser
    public @Test void users() throws Exception {
        String userName = ((User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
                .getUsername();
        createUser(userName);

        support.perform(get("/private/users")).andExpect(jsonPath("$[?(@.pending in [false])].['pending']").exists())
                .andExpect(jsonPath("$[?(@.pending in [true])].['pending']").exists());
    }

    @WithMockRandomUidUser
    public @Test void updateUsers() throws Exception {
        String userName1 = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();
        String userName2 = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();
        createUser(userName1);
        createUser(userName2);
        String role1Name = createRole();
        String role2Name = createRole();
        String role3Name = createRole();
        String role4Name = createRole();
        setRole(userName1, role1Name, role2Name);
        setRole(userName1, role3Name, role4Name);

        String body = String.format("{ \"users\":[\"%s\",\"%s\"],\"PUT\":[\"%s\",\"%s\"],\"DELETE\":[\"%s\",\"%s\"]}",
                userName1, userName2, role1Name, role3Name, role2Name, role4Name);
        support.perform(post("/private/roles_users").content(body));

        support.perform(get("/private/roles/" + role1Name)).andExpect(jsonPath("$.users[0]").value(userName1));
        support.perform(get("/private/roles/" + role1Name)).andExpect(jsonPath("$.users[1]").value(userName2));
        support.perform(get("/private/roles/" + role3Name)).andExpect(jsonPath("$.users[0]").value(userName1));
        support.perform(get("/private/roles/" + role3Name)).andExpect(jsonPath("$.users[1]").value(userName2));
        support.perform(get("/private/roles/" + role2Name)).andExpect(jsonPath("$.users").isEmpty());
        support.perform(get("/private/roles/" + role4Name)).andExpect(jsonPath("$.users").isEmpty());
    }

    private void createUser(String userName) throws Exception {
        support.perform(post("/private/users")
                .content(support.readResourceToString("/testData/createUserPayload.json").replace("{uuid}", userName)));
    }

    private ResultActions getProfile() throws Exception {
        return support.perform(get("/private/users/profile"));
    }

    private String createRole() throws Exception {
        String roleName = "IT_ROLE_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        String body = "{ \"cn\": \"" + roleName + "\", \"description\": \"Role Description\", \"isFavorite\": false }";
        support.perform(post("/private/roles").content(body));
        return roleName;
    }

    private void setRole(String userName, String role1Name, String role2Name) throws Exception {
        String body = "{ \"users\":[\"" + userName + "\"],\"PUT\":[\"" + role1Name + "\", \"" + role2Name
                + "\"],\"DELETE\":[]}";
        support.perform(post("/private/roles_users").content(body));
    }

    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    @WithMockRandomUidUser
    @DBUnit(qualifiedTableNames = true, dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
    @DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql", strategy = CLEAN_INSERT, value = { "dbunit/all.csv" })
    public @Test void testDeleteAccountRecords() throws Exception {

        createUser("user1");
        this.mockMvc.perform(post("/private/users/gdpr/delete?uid=user1"))//
                .andExpect(status().isOk())//
                .andExpect(content().contentTypeCompatibleWith("application/json"))//
                // .andDo(print())//
                .andExpect(jsonPath("$.account").value("user1"))//
                .andExpect(jsonPath("$.metadata").value(2))//
                .andExpect(jsonPath("$.extractor").value(2))//
                .andExpect(jsonPath("$.geodocs").value(3))//
                .andExpect(jsonPath("$.ogcStats").value(3));

        this.mockMvc.perform(post("/private/users/gdpr/delete?uid=user1"))//
                .andExpect(status().isOk())//
                .andExpect(content().contentTypeCompatibleWith("application/json"))//
                .andExpect(jsonPath("$.account").value("user1"))//
                .andExpect(jsonPath("$.metadata").value(0))//
                .andExpect(jsonPath("$.extractor").value(0))//
                .andExpect(jsonPath("$.geodocs").value(0))//
                .andExpect(jsonPath("$.ogcStats").value(0));
    }

}
