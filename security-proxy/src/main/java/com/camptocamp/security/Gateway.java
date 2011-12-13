package com.camptocamp.security;

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
 * WEB-INF/gateway/load.html to the user.  The load.html page has an iframe that attempts to load
 * testPage (which has a single div with id <em>userLoggedIn</em>.  if the javascript in load.html 
 * can find that div then it knows the user is logged in and the user is forwarded to the final 
 * URL (provided in the url parameter of gateway).</p>  
 * 
 * @author jeichar
 *
 */
public class Gateway {
    private final static String DIVID = "userLoggedIn";
    private final static byte[] TEST_PAGE_BYTES;
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private byte[] loadPageBytes = null;
            
    static {
        try {
            TEST_PAGE_BYTES = ("<html><body><div id=\""+DIVID+"\"/></body></html>").getBytes("UTF-8");
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
            File loadhtml = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/gateway/load.html"));
            File jquery = new File(request.getSession().getServletContext().getRealPath("/WEB-INF/gateway/jquery-min.js"));
            loadPageBytes = FileUtils.readFileToString(loadhtml, "UTF-8").replace("@jquery@", FileUtils.readFileToString(jquery, "UTF-8")).getBytes("UTF-8");
        }
        response.setContentType(CONTENT_TYPE);
        IOUtils.write(loadPageBytes, response.getOutputStream());
    }
}
