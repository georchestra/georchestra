package org.georchestra.console.integration;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Integration tests for {@code /private/roles} role management API.
 */
@RunWith(SpringRunner.class)
@EnableWebMvc
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@PropertySource("classpath:console-it.properties")
@WebAppConfiguration
public class RolesIT {
	private static Logger LOGGER = Logger.getLogger(RolesIT.class);

	private @Autowired LdapTemplate ldapTemplateSanityCheck;

	private @Value("${ldap_port}") int ldapPort;

	private @Value("${psql_port}") int psqlPort;

	public @Rule TestName testName = new TestName();

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	private String roleName;

	public static @BeforeClass void init() {
	}

	public @Before void before() {
		System.err.printf("############# %s: psql_port: %s, ldap_port: %s\n", testName.getMethodName(), psqlPort,
				ldapPort);
		// pre-flight sanity check
		assertNotNull(ldapTemplateSanityCheck.lookup("cn=admin"));
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	private void deleteQuiet() {
		try {
			delete();
		} catch (Exception e) {
			LOGGER.info(String.format("Error deleting role %s at %s", roleName, testName.getMethodName()), e);
		}
	}

	private MvcResult delete() throws Exception {
		return delete(roleName).andReturn();
	}

	private ResultActions delete(String roleName) throws Exception {
		return this.mockMvc.perform(MockMvcRequestBuilders.delete("/private/roles/{cn}", roleName));
	}

	private ResultActions create() throws Exception {
		roleName = "IT_ROLE_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
		return create(roleName);
	}

	private ResultActions create(String name) throws Exception {
		String body = "{ \"cn\": \"" + name + "\", \"description\": \"Role Description\", \"isFavorite\": false }";
		return this.mockMvc.perform(post("/private/roles").content(body));
	}

	private ResultActions update(String name, String description, boolean isFavorite) throws Exception {
		String body = "{ \"cn\": \"" + name + "\", \"description\": \"" + description + "\", \"isFavorite\": "
				+ isFavorite + " }";
		return this.mockMvc.perform(put("/private/roles/{cn}", name).content(body));
	}

	private ResultActions get(String name) throws Exception {
		return this.mockMvc.perform(MockMvcRequestBuilders.get("/private/roles/{cn}", name));
	}

	@WithMockUser(username = "user", roles = "USER")
	public @Test void testCreateBadUser() throws Exception {
		create().andExpect(status().isForbidden());
	}

	@WithMockUser(username = "admin", roles = "SUPERUSER")
	public @Test void testCreate() throws Exception {
		try {
			create()//
					.andExpect(status().isOk())// note: should return 201:CREATED instead?
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.cn").value(roleName));
		} finally {
			deleteQuiet();
		}
	}

	@WithMockUser(username = "admin", roles = "SUPERUSER")
	public @Test void testUpdateIsFavoriteNoOp() throws Exception {
		try {
			create().andExpect(status().isOk());

			update(roleName, "", false)//
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));

			get(roleName)// update says it was updated, but what does get say?
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));
		} finally {
			deleteQuiet();
		}
	}

	@WithMockUser(username = "admin", roles = "SUPERUSER")
	public @Test void testUpdateIsFavorite() throws Exception {
		try {
			create().andExpect(status().isOk());

			update(roleName, "", true)//
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(true));

			get(roleName)// update says it was updated, but what does get say?
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(true));

			update(roleName, "", false)//
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));

			get(roleName)// update says it was updated, but what does get say?
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));
		} finally {
			deleteQuiet();
		}
	}
}
