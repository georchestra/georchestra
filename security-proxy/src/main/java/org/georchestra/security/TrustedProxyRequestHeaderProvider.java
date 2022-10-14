package org.georchestra.security;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @see ProxyTrustAnotherProxy
 */
public class TrustedProxyRequestHeaderProvider extends HeaderProvider {

    @PostConstruct
    public void init() {
        logger.info("Will forward incoming headers for pre-authorized requests from trusted proxies");
    }

    @Override
    public Map<String, String> getCustomRequestHeaders(HttpServletRequest originalRequest, String targetServiceName) {
        if (!isPreAuthorized(originalRequest)) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> e = originalRequest.getHeaderNames();
        while (e.hasMoreElements()) {
            String headerName = e.nextElement();
            String value = originalRequest.getHeader(headerName);
            if (logger.isDebugEnabled()) {
                logger.debug("Adding header: " + headerName + ", value: " + value);
            }
            headers.put(headerName, value);
        }
        return headers;
    }
}
