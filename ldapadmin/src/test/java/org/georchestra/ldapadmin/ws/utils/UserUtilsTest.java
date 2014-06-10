/**
 * 
 */
package org.georchestra.ldapadmin.ws.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Sylvain Lesage
 *
 */
public class UserUtilsTest {

	@Test
	public void testIsUidValid1() {
		Boolean result = UserUtils.isUidValid("123456");
		assertEquals(false, result);
	}

	@Test
	public void testIsUidValid2() {
		Boolean result = UserUtils.isUidValid("a1234.asd56");
		assertEquals(true, result);
	}

	@Test
	public void testIsUidValid3() {
		Boolean result = UserUtils.isUidValid("a_1234.asd56");
		assertEquals(false, result);
	}
}
