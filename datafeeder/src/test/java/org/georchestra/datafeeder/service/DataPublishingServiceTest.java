package org.georchestra.datafeeder.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DataPublishingServiceTest {
    DataPublishingService toTest = new DataPublishingService();

    public @Test void testMorphOptions_null() {
        assertEquals("expected null", toTest.morphOptions(null), null);
    }

    public @Test void testMorphOptions() {
        Map<String, Object> map = Map.of("key1", "value1", //
                "key2", 4, //
                "key3", Arrays.asList("one", "two", "three")//
        );

        Map<String, String> ret = toTest.morphOptions(map);

        assertEquals("Expected list to be morphed into a string for map['key3']", ret.get("key3"), "[one, two, three]");
        assertEquals("Expected map['key2'] to be '4'", ret.get("key2"), "4");
    }

}
