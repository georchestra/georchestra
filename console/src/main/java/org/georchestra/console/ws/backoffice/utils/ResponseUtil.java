/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ws.backoffice.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class which contains useful method to prepare the http response.
 *
 *
 * @author Mauricio Pazos
 *
 */
final public class ResponseUtil {

    private ResponseUtil() {
        // utility class pattern
    }

    public static Response success() {
        return success(null);
    }

    public static Response success(@Nullable Object payload) {
        Response response = new Response();
        response.setSuccess(true);
        response.setResponse(payload);
        return response;
    }

    public static Response failure() {
        return failure(null);
    }

    public static Response failure(@Nullable String errorMessage) {
        Response response = new Response();
        response.setSuccess(false);
        response.setError(errorMessage);
        return response;
    }

    /**
     * Build the success message
     *
     * @return success message
     */
    public static String buildSuccessMessage() {
        return buildResponseMessage(Boolean.TRUE, null);
    }

    public static String buildResponseMessage(Boolean status) {
        return buildResponseMessage(status, null);
    }

    public static String buildResponseMessage(Boolean status, String errorMessage) {

        JSONObject res = new JSONObject();
        try {
            res.put("success", status);
            if (errorMessage != null) {
                res.put("error", errorMessage);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return res.toString();
    }

    public static void buildResponse(HttpServletResponse response, String jsonData, int sc) throws IOException {

        response.setContentType("application/json");
        response.setStatus(sc);

        PrintWriter out = response.getWriter();
        try {
            out.println(jsonData);

        } finally {
            out.close();
        }
    }

    public static void writeSuccess(HttpServletResponse response) throws IOException {

        buildResponse(response, ResponseUtil.buildSuccessMessage(), HttpServletResponse.SC_OK);
    }

    public static void writeError(HttpServletResponse response, String message) throws IOException {

        buildResponse(response, ResponseUtil.buildResponseMessage(Boolean.FALSE, message),
                HttpServletResponse.SC_NOT_FOUND);
    }

}
