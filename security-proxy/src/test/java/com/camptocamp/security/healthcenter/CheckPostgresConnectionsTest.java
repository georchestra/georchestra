/**
 * 
 */
package com.camptocamp.security.healthcenter;

import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import com.camptocamp.security.healthcenter.CheckPostgresConnections;

/**
 * @author Mauricio Pazos
 *
 */
public final class CheckPostgresConnectionsTest {
	
	private final static Log LOGGER = LogFactory.getLog(DatabaseHealthCenterTest.class.getPackage().getName());
	static{
		BasicConfigurator.configure();
	}
	
	
    @Ignore
    public static void setUpContext() throws Exception {

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		
		InitialContext ic = new InitialContext();

		ic.createSubcontext("java:");
		ic.createSubcontext("java:/comp");
		ic.createSubcontext("java:/comp/env");
		ic.createSubcontext("java:/comp/env/jdbc");

		// Construct DataSource
		PGSimpleDataSource ds = new PGSimpleDataSource();
		ds.setDatabaseName("testdb");
		ds.setPortNumber(5432);
		ds.setUser("postgres");
		ds.setPassword("admin");

		ic.bind("java:/comp/env/jdbc/postgres", ds);
        
    }	
	/**
	 * Test method for {@link com.camptocamp.security.healthcenter.CheckPostgresConnections#countConnection()}.
	 */
	@Test
	public void testCountConnection() throws Exception{
		Integer connections = CheckPostgresConnections.countConnection();
		
		LOGGER.info(connections);

		Assert.assertTrue(connections > 0);
		
	}
	
	@Test
	public void testConnectionsData() throws Exception{
		List<Map<String, Object>> findConnections = CheckPostgresConnections.findConnections();
		for (Map<String, Object> conn : findConnections) {
			LOGGER.info(conn);
		}
		
	}
}
