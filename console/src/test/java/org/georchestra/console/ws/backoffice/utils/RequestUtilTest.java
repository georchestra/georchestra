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

package org.georchestra.console.ws.backoffice.utils;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestUtilTest {

    @Test
    public void testGetFieldValue() throws JSONException {
        JSONObject testObj = new JSONObject("{ \"number\": 5 }");

        String ret = RequestUtil.getFieldValue(testObj, "number");

        assertTrue(ret.equals("5"));

        // Testing with a more complex object
        testObj = new JSONObject().put("anotherObj", new MockHttpServletRequest());

        ret = RequestUtil.getFieldValue(testObj, "anotherObj");

        // Actually getting a string with fully qualified class
        assertTrue(ret.contains("MockHttpServletRequest"));
    }

    @Test
    public void getFieldValueReturnNullWhenNoFound() throws JSONException {
        JSONObject testObj = new JSONObject("{ \"number\": 5 }");

        String ret = RequestUtil.getFieldValue(testObj, "i_am_not_defined_in_the_json");

        assertNull(ret);
    }

    @Test
    public void getFieldValueReturnBlankWhenFoundBlank() throws JSONException {
        JSONObject testObj = new JSONObject("{ \"number\": \"\" }");

        String ret = RequestUtil.getFieldValue(testObj, "number");

        assertTrue(ret.equals(""));
    }

}
