package org.georchestra.console.integration;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.georchestra.commons.WaitForDb;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This class defines test against a real LDAP server, from the official
 * geOrchestra LDAP image.
 *
 * @author pmauduit
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class ConsoleIT {

	@ClassRule
	public static GenericContainer georchestraLdap = new GenericContainer("georchestra/ldap").withExposedPorts(389)
			.withEnv("SLAPD_ORGANISATION", "georchestra").withEnv("SLAPD_DOMAIN", "georchestra.org")
			.withEnv("SLAPD_PASSWORD", "secret").withEnv("SLAPD_ADDITIONAL_MODULES", "groupofmembers");

	@ClassRule
	public static GenericContainer georchestraDatabase = new GenericContainer("georchestra/database")
			.withExposedPorts(5432).withEnv("POSTGRES_USER", "georchestra").withEnv("POSTGRES_PASSWORD", "georchestra");

	// <bean id="waitForDb" class="org.georchestra.commons.WaitForDb"
	// init-method="test">
	// <property name="url" value="${psql.url}"/>
	// <property name="username" value="${psql.user}"/>
	// <property name="password" value="${psql.pass}"/>
	// <property name="driverClassName" value="org.postgresql.Driver"/>
	// </bean>
	public @Bean(name = "waitForDb") @Lazy(false) WaitForDb tcpIpPropertySource() {
		Integer dbport = georchestraDatabase.getMappedPort(5432);
		String psqlurl = String.format("jdbc:postgresql://localhost:%d/georchestra", dbport.intValue());

		WaitForDb bean = new WaitForDb();
		bean.setDriverClassName("org.postgresql.Driver");
		bean.setUsername("georchestra");
		bean.setPassword("georchestra");
		bean.setUrl(psqlurl);
		bean.test();
		return bean;
	}

	// <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource"
	// depends-on="waitForDb">
	// <property name="jdbcUrl" value="${psql.url}"/>
	// <property name="user" value="${psql.user}"/>
	// <property name="password" value="${psql.pass}"/>
	// <property name="driverClass" value="org.postgresql.Driver"/>
	// <property name="maxPoolSize" value="10"/>
	// <property name="minPoolSize" value="1"/>
	// <property name="automaticTestTable" value="cpds_connection_test"/>
	// </bean>
	public @Bean(name = "dataSource") @Autowired DataSource psqlDataSource(WaitForDb wd) throws PropertyVetoException {

		ComboPooledDataSource pool = new ComboPooledDataSource();
		pool.setJdbcUrl(wd.getUrl());
		pool.setUser(wd.getUsername());
		pool.setPassword(wd.getPassword());
		pool.setDriverClass(wd.getDriverClassName());
		pool.setMaxPoolSize(10);
		pool.setMinPoolSize(1);
		pool.setAutomaticTestTable("cpds_connection_test");
		return pool;
	}

	private @Autowired LdapTemplate ldapTemplate;

	public @BeforeClass static void setUp() {
		System.out.println("pg mapped port: " + georchestraDatabase.getMappedPort(5432));
	}

	public @Before void before() {
	}

	@Test
	public void testTheTest() throws InterruptedException {
		System.out.println(ldapTemplate.lookup("cn=admin"));
	}

}
