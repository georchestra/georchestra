package org.georchestra.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

public class TrustedProxyRequestHeaderProvider extends HeaderProvider {

    private static final Log logger = LogFactory.getLog(ProxyTrustAnotherProxy.class.getPackage().getName());

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest) {
        if (session.getAttribute("pre-auth") != null) {
            Collection<Header> headers = new ArrayList<Header>();
            Enumeration<String> e = originalRequest.getHeaderNames();
            while (e.hasMoreElements()) {
                String headerName = e.nextElement();
                originalRequest.getHeader(headerName);
                logger.debug("Adding header: " + headerName + ", value: " + originalRequest.getHeader(headerName));
                headers.add(new BasicHeader(headerName, originalRequest.getHeader(headerName)));
            }
            return headers;
        } else {
            return Collections.emptyList();
        }
    }
}
