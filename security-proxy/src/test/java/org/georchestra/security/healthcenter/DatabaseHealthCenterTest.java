/**
 * 
 */
package org.georchestra.security.healthcenter;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.georchestra.security.healthcenter.DatabaseHealthCenter;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * TODO Work in progress
 * 
 * @author Mauricio Pazos
 *
 */
public class DatabaseHealthCenterTest {

	private final static Log LOGGER = LogFactory.getLog(DatabaseHealthCenterTest.class.getPackage().getName());

	@BeforeClass
	public static void settingConnections(){
		BasicConfigurator.configure();

		// TODO set 5 database connections
	}
	/**
	 * Test method for {@link org.georchestra.security.healthcenter.DatabaseHealthCenter#checkConnections(int)}.
	 */
	@Test
	public void testCheckConnectionsUnstable() {
		
		DatabaseHealthCenter hc = DatabaseHealthCenter.getInstance("localhost", 5432, "postgres", "postgres", "admin", "testCase");
		boolean healthy = hc.checkConnections(2);
		
		// TODO assertFalse(healty);
	}
	@Test
	public void testCheckConnectionsOnLimits() {
		
		DatabaseHealthCenter hc = DatabaseHealthCenter.getInstance("localhost", 5432, "postgres","postgres", "admin", "testCase");
		boolean healthy = hc.checkConnections(6);

		// TODO assertTrue(healthy);
	}

	@Test
	public void testCheckConnectionsOK() {
		
		DatabaseHealthCenter hc = DatabaseHealthCenter.getInstance("localhost", 5432, "postgres","postgres", "admin", "testCase");
		boolean healthy = hc.checkConnections(10);

		// TODO assertTrue(healthy);
	}

}
