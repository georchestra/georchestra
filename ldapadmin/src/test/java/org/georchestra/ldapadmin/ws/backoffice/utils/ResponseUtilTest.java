package org.georchestra.console.ws.backoffice.utils;

import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class ResponseUtilTest {

    @Test
    public void testBuildSuccessMessage() throws JSONException {
        String ret = ResponseUtil.buildSuccessMessage();

        JSONObject js = new JSONObject(ret);
        assertTrue(js.get("success").equals(true));
    }

    @Test
    public void testBuildResponseMessageSuccess() throws JSONException {
        String ret = ResponseUtil.buildResponseMessage(true);

        JSONObject js = new JSONObject(ret);
        assertTrue(js.get("success").equals(true));
    }

    @Test
    public void testBuildResponseMessageError() throws JSONException {
        String ret = ResponseUtil.buildResponseMessage(false, "an error occured");

        JSONObject js = new JSONObject(ret);
        assertTrue(js.get("success").equals(false));
        assertTrue(js.get("error").equals("an error occured"));
    }


    @Test
    public void testWriteSuccess() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseUtil.writeSuccess(response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.get("success").equals(true));
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
    }

    @Test
    public void testWriteError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseUtil.writeError(response, "error occured");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.get("success").equals(false));
        assertTrue(ret.get("error").equals("error occured"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
    }
}
