/**
 * 
 */
package com.camptocamp.security.healthcenter;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import com.camptocamp.security.healthcenter.DatabaseHealthCenter;

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
	 * Test method for {@link com.camptocamp.security.healthcenter.DatabaseHealthCenter#checkConnections(int)}.
	 */
	@Test
	public void testCheckConnectionsUnstable() {
		
		DatabaseHealthCenter hc = DatabaseHealthCenter.getInstance("postgres", "postgres", "admin", "testCase");
		boolean healthy = hc.checkConnections(2);
		
		// TODO assertFalse(healty);
	}
	@Test
	public void testCheckConnectionsOnLimits() {
		
		DatabaseHealthCenter hc = DatabaseHealthCenter.getInstance("postgres","postgres", "admin", "testCase");
		boolean healthy = hc.checkConnections(6);

		// TODO assertTrue(healthy);
	}

	@Test
	public void testCheckConnectionsOK() {
		
		DatabaseHealthCenter hc = DatabaseHealthCenter.getInstance("postgres","postgres", "admin", "testCase");
		boolean healthy = hc.checkConnections(10);

		// TODO assertTrue(healthy);
	}

}
