package org.georchestra.console.ws.utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;


/**
 * This class defines test against a real LDAP server, from the official
 * geOrchestra LDAP image.
 *
 * @author pmauduit
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/webmvc-config-test.xml"})
public class ConsoleIntegrationTest {

	@ClassRule
	public static GenericContainer georchestraLdap = new GenericContainer("georchestra/ldap")
		.withExposedPorts(389)
		.withEnv("SLAPD_ORGANISATION", "georchestra")
		.withEnv("SLAPD_DOMAIN", "georchestra.org")
		.withEnv("SLAPD_PASSWORD", "secret")
		.withEnv("SLAPD_ADDITIONAL_MODULES", "groupofmembers");

	@ClassRule
	public static GenericContainer georchestraDatabase = new GenericContainer("georchestra/database")
		.withExposedPorts(5432)
		.withEnv("POSTGRES_USER", "georchestra")
		.withEnv("POSTGRES_PASSWORD", "georchestra");

	
	@Autowired
	private LdapTemplate ldapTemplate;

	@BeforeClass
	public static void setUp() {
		georchestraLdap.start();
		georchestraDatabase.start();
	}
	
	@AfterClass
	public static void tearDown() {
		georchestraLdap.stop();
		georchestraDatabase.stop();
	}
	
	@Test
	public void testTheTest() throws InterruptedException {
		System.out.println(ldapTemplate.lookup("cn=admin"));
	}
	
}
