/**
 * This package brings a testsuite to test the whole lifecycle
 * of managing the users (Create, Read, Update, Delete).
 *
 * @author Pierre Mauduit
 */
package org.georchestra.ldapadmin.ws.ldap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;



public class UserLDAPCrudTest {
	private static LdapContextSource lctxs = null;
	private static LdapTemplate ltmpl = null;

	private static String ldapUrl = "ldap://localhost:389/";
	private static String baseDn = "dc=test,dc=org";
	private static String bindDn = "cn=admin,dc=test,dc=org";
	private static String bindPassword = "secret";

	@Before
	public void setUp() throws Exception {
		lctxs = new LdapContextSource();
		lctxs.setBase(baseDn);
		lctxs.setUrl(ldapUrl);
		lctxs.setUserDn(bindDn);
		lctxs.setPassword(bindPassword);
		lctxs.afterPropertiesSet();
		ltmpl = new LdapTemplate();
		ltmpl.setContextSource(lctxs);
		ltmpl.afterPropertiesSet();

		// removing users
		try {
			ltmpl.unbind("ou=users", true);
		} catch (NamingException e) {}

		// removing roles
		try {
			ltmpl.unbind("ou=roles", true);
		} catch (NamingException e) {}

		// recreating ou=users
		try {
			DirContextOperations ctx = new DirContextAdapter("ou=users");
			ctx.addAttributeValue("objectClass", "top");
			ctx.addAttributeValue("objectClass", "organizationalUnit");
			ctx.addAttributeValue("ou", "users");
			ltmpl.bind(ctx);
		} catch (Exception e) {};

		// recreating ou=roles
		try {
			DirContextOperations ctx = new DirContextAdapter("ou=roles");
			ctx.addAttributeValue("objectClass", "top");
			ctx.addAttributeValue("objectClass", "organizationalUnit");
			ctx.addAttributeValue("ou", "roles");
			ltmpl.bind(ctx);
		} catch (Exception e) {};

	}

	@After
	public void tearDown() {
		try {
		ltmpl.unbind("ou=users", true);
		} catch (NamingException e) {}
		try {
		ltmpl.unbind("ou=roles", true);
		} catch (NamingException e) {}
	}

	@BeforeClass
	public static void setUpC() {

	}

	@AfterClass
	public static void tearDownC() {

	}

	/**
	 * tests the user creation.
	 *
	 * It should attach a membership to the default group.
	 */
	@Test
	public void createUserTest() {
		assert true;
	}

	/**
	 * tests the user retrieval.
	 *
	 * It should be able to retrieve the previously created user.
	 */
	@Test
	public void readUserTest() {

	}

	/**
	 * tests the user updating.
	 *
	 * It should be able to modify user's informations.
	 */
	@Test
	public void updateUserTest() {

	}

	/**
	 * tests the user deletion.
	 * - it should be able to remove the user from the LDAP directory.
	 * - it should leave the membership of the group.
	 */
	@Test
	public void deleteUserTest() {

	}
}
