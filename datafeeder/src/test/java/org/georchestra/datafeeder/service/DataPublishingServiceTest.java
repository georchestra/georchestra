/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

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
