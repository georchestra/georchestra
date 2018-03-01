package org.georchestra.console.ws.newaccount;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author mauro
 *
 */
public class UidGeneratorTest {

	@Test
	public void testWithoutNumer() {

		String result = UidGenerator.next( "hsimpson");

		assertEquals("hsimpson1", result);
	}

	@Test
	public void testNext1() {

		String result = UidGenerator.next( "hsimpson1");

		assertEquals("hsimpson2", result);
	}
	@Test
	public void testNext2() {

		String result = UidGenerator.next( "hsimpson2");

		assertEquals("hsimpson3", result);
	}
}
