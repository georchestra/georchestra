package org.georchestra.console.ws.backoffice.utils;

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

}
