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
