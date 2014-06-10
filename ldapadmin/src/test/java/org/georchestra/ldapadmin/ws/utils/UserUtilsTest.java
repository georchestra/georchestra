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
		boolean result = UserUtils.isUidValid("123456");
		assertEquals(false, result);
	}

	@Test
	public void testIsUidValid2() {
		boolean result = UserUtils.isUidValid("a1234.asd56");
		assertEquals(true, result);
	}

	@Test
	public void testIsUidValid3() {
		boolean result = UserUtils.isUidValid("a_1234.asd56");
		assertEquals(false, result);
	}

	@Test
	public void testIsUidValid4() {
		assertEquals("[A-Za-z]+[A-Za-z0-9.]*", UserUtils.getUidRegExp());
	}
}
