package org.georchestra.ldapadmin.ws.backoffice.utils;

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
    public void testGetKeyFromPathVariable() throws JSONException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String ret = RequestUtil.getKeyFromPathVariable(request);

        assertTrue(ret.equals(""));

        request.setRequestURI("/ldapadmin/with/a/more/complex/path");
        ret = RequestUtil.getKeyFromPathVariable(request);

        assertTrue(ret.equals("path"));

    }

    @Test
    public void testGetKeyFromPathVariableRsrc() throws JSONException {
        MockHttpServletRequest request = new MockHttpServletRequest();

        try {
            RequestUtil.getKeyFromPathVariable(request, "test");
        } catch (Throwable e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        request.setRequestURI("/ldapadmin/with/a/more/complex/path");
        String ret = RequestUtil.getKeyFromPathVariable(request, "complex");

        assertTrue(ret.equals("path"));

        try {
            RequestUtil.getKeyFromPathVariable(request, "path");
        } catch (Throwable e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }



}
