/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.security;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * This is a work around to the fact that spring does not yet support the gateway parameter.
 * 
 * <p>In summary the way this works is proxy will call use this class if the requested url is 
 * <em>gateway</em>.  When this class gets called it returns the contents of 
 * WEB-INF/gateway.html to the user.  The gateway.html page has an iframe that attempts to load
 * testPage?login.  The testPage is restricted to logged in users so it forces a redirect to cas.  If the user has
 * logged in then the proxy will get the login information and the user is forwarded to the final destination (as provided by url parameter) 
 * </p>
 * 
 * @author jeichar
 */
public class Gateway {
    private final static byte[] TEST_PAGE_BYTES;
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private byte[] loadPageBytes = null;
            
    static {
        try {
            TEST_PAGE_BYTES = ("<html><body></body></html>").getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Major programming error.  UTF-8 really should be supported", e);
        }
    }
    
    public void testPage(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        IOUtils.write(TEST_PAGE_BYTES, response.getOutputStream());
    }

    public synchronized void loadCredentialsPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(loadPageBytes == null) {
            File loadhtml = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/gateway.html"));
            loadPageBytes = FileUtils.readFileToString(loadhtml, "UTF-8").getBytes("UTF-8");
        }
        response.setContentType(CONTENT_TYPE);
        IOUtils.write(loadPageBytes, response.getOutputStream());
    }
}
