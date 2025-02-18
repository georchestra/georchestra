package org.georchestra.ds.users;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author mauro
 *
 */
public class UidGeneratorTest {

    @Test
    public void testWithoutNumer() {

        String result = UidGenerator.next("hsimpson");

        assertEquals("hsimpson1", result);
    }

    @Test
    public void testNext1() {

        String result = UidGenerator.next("hsimpson1");

        assertEquals("hsimpson2", result);
    }

    @Test
    public void testNext2() {

        String result = UidGenerator.next("hsimpson2");

        assertEquals("hsimpson3", result);
    }
}
