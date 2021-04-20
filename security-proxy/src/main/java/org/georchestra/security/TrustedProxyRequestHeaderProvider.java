package org.georchestra.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class TrustedProxyRequestHeaderProvider extends HeaderProvider {

    @PostConstruct
    public void init() {
        logger.info("Will forward incoming headers for pre-authorized requests from trusted proxies");
    }

    @Override
    public Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest,
            String targetServiceName) {
        if (!isPreAuthorized(session)) {
            return Collections.emptyList();
        }
        Collection<Header> headers = new ArrayList<Header>();
        Enumeration<String> e = originalRequest.getHeaderNames();
        while (e.hasMoreElements()) {
            String headerName = e.nextElement();
            originalRequest.getHeader(headerName);
            logger.debug("Adding header: " + headerName + ", value: " + originalRequest.getHeader(headerName));
            headers.add(new BasicHeader(headerName, originalRequest.getHeader(headerName)));
        }
        return headers;
    }
}
