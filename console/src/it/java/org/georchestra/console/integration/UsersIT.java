package org.georchestra.console.integration;

import org.apache.commons.lang3.RandomStringUtils;
import org.georchestra.console.integration.instruments.WithMockRandomUidUser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.net.URISyntaxException;

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

    public @Rule @Autowired IntegrationTestSupport support;

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

        support.perform(put("/private/users/" + userName).content(readRessourceToString("/testData/createUserPayload.json")
                .replace("{uuid}", newUserName)
                .replace("psc", "cra")));


        support.perform(get("/private/users/" + newUserName))
                .andExpect(jsonPath("$.org").value("cra"))
                .andExpect(jsonPath("$.uid").value(newUserName));
    }

    @WithMockRandomUidUser
    public @Test
    void userDetail() throws Exception {
        String userName = ((User)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername();
        createUser(userName);

        support.perform(get("/account/userdetails").header("sec-username", userName)).andExpect(status().isOk());
    }

    @WithMockRandomUidUser
    public @Test
    void users() throws Exception {
        String userName = ((User)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername();
        createUser(userName);

        support.perform(get("/private/users"))
                .andExpect(jsonPath("$[?(@.pending in [false])].['pending']").exists())
                .andExpect(jsonPath("$[?(@.pending in [true])].['pending']").exists());
    }

    @WithMockRandomUidUser
    public @Test
    void updateUsers() throws Exception {
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

        String body = String.format("{ \"users\":[\"%s\",\"%s\"],\"PUT\":[\"%s\",\"%s\"],\"DELETE\":[\"%s\",\"%s\"]}", userName1, userName2, role1Name, role3Name, role2Name, role4Name);
        support.perform(post("/private/roles_users").content(body));

        support.perform(get("/private/roles/" + role1Name)).andExpect(jsonPath("$.users[0]").value(userName1));
        support.perform(get("/private/roles/" + role1Name)).andExpect(jsonPath("$.users[1]").value(userName2));
        support.perform(get("/private/roles/" + role3Name)).andExpect(jsonPath("$.users[0]").value(userName1));
        support.perform(get("/private/roles/" + role3Name)).andExpect(jsonPath("$.users[1]").value(userName2));
        support.perform(get("/private/roles/" + role2Name)).andExpect(jsonPath("$.users").isEmpty());
        support.perform(get("/private/roles/" + role4Name)).andExpect(jsonPath("$.users").isEmpty());
    }

    private void createUser(String userName) throws Exception {
        support.perform(post("/private/users").content(readRessourceToString("/testData/createUserPayload.json").replace("{uuid}", userName)));
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
        String body = "{ \"users\":[\"" + userName + "\"],\"PUT\":[\""+ role1Name + "\", \"" + role2Name + "\"],\"DELETE\":[]}";
        support.perform(post("/private/roles_users").content(body));
    }

    private String readRessourceToString(String name) throws URISyntaxException, IOException {
        java.net.URL url = this.getClass().getResource(name);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }
}
