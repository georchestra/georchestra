package org.georchestra.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class ProxyTrustAnotherProxy extends AbstractPreAuthenticatedProcessingFilter {

    private static String AUTH_HEADER = "sec-username";
    private static final Log logger = LogFactory.getLog(ProxyTrustAnotherProxy.class.getPackage().getName());

    /* The default value is an empty list of trusted proxies */
    private String rawProxyValue = "";
    private Set<InetAddress> trustedProxies = new HashSet<InetAddress>();

    public void init() throws UnknownHostException {
        if (rawProxyValue == "") {
            logger.info("\"trustedProxy\" property is not defined. Skipping bean configuration");
            return;
        }
        rawProxyValue = rawProxyValue.trim();

        String[] rawProxyList;
        if(rawProxyValue.length() != 0){
            rawProxyList = rawProxyValue.split("\\s*,\\s*");
        } else {
            rawProxyList = new String[0];
        }

        for(String proxy : rawProxyList) {
            InetAddress trustedProxyAddress; 
            try {
                trustedProxyAddress = InetAddress.getByName(proxy);
            } catch (UnknownHostException e) {
                logger.error("Unable to lookup " + proxy + ". skipping.");
                continue;
            }
            this.trustedProxies.add(trustedProxyAddress);
            logger.info("Add trusted proxy : " + trustedProxyAddress);
        }
        if(this.trustedProxies.size() == 0){
            logger.info("No trusted proxy loaded");
        } else {
            logger.info("Successful loading of " + this.trustedProxies.size() + " trusted proxy");
        }

        this.setContinueFilterChainOnUnsuccessfulAuthentication(true);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        try {
            if(this.trustedProxies.contains(InetAddress.getByName(request.getRemoteAddr()))){
                String username = request.getHeader(AUTH_HEADER);
                if(username != null){
                    logger.debug("Request from a trusted proxy, so log in user : " + username);
                    request.getSession().setAttribute("pre-auth", true);
                } else {
                    logger.debug("Request from a trusted proxy, but no sec-username header found");
                }
                return username;
            } else {
                logger.debug("Request from a NON trusted proxy, bypassing log in");
                return null;
            }
        } catch (UnknownHostException e) {
            logger.error("Unable to resolve remote address : " + request.getRemoteAddr());
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    public void setRawProxyValue(String rawProxyValue) {
        this.rawProxyValue = rawProxyValue;
    }
}
