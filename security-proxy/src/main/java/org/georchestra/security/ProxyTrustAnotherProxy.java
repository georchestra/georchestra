package org.georchestra.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class ProxyTrustAnotherProxy extends AbstractPreAuthenticatedProcessingFilter {

    private static String AUTH_HEADER = "sec-username";
    private static String CONFIG_KEY = "trustedProxy";
    private static final Log logger = LogFactory.getLog(ProxyTrustAnotherProxy.class.getPackage().getName());

    private GeorchestraConfiguration georchestraConfiguration;
    private Set<InetAddress> trustedProxies = new HashSet<InetAddress>();

    public void init() throws UnknownHostException {
        if (! georchestraConfiguration.activated()) {
            logger.info("trusting security-proxies only works in datadir mode. Skipping bean configuration");
            return;
        }
        String rawProxyValue = georchestraConfiguration.getProperty(CONFIG_KEY);
        if (rawProxyValue == null) {
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
        //this.setAuthenticationDetailsSource(new ProxyTrustAnotherProxy.MyAuthenticationDetailsSource());
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        try {
            if(this.trustedProxies.contains(InetAddress.getByName(request.getRemoteAddr()))){
                String username = request.getHeader(AUTH_HEADER);
                logger.debug("Request from a trusted proxy, so log in user : " + username);
                request.getSession().setAttribute("pre-auth", true);
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

    /*public static class MyAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, User> {

        @Override
        public User buildDetails(HttpServletRequest req) {

            req.getSession().setAttribute("pre-auth", true);

            String username = "pmauduit";
            Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

            authorities.add(this.createGrantedAuthority("ROLE_USER"));
            authorities.add(this.createGrantedAuthority("ROLE_ADMINISTRATOR"));

            return new User(username, "N/A", authorities);
        }

        private GrantedAuthority createGrantedAuthority(final String role){
            return new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return role;
                }
            };
        }
    }*/

    public void setGeorchestraConfiguration(GeorchestraConfiguration georchestraConfiguration) {
        this.georchestraConfiguration = georchestraConfiguration;
    }

    public GeorchestraConfiguration getGeorchestraConfiguration() {
        return georchestraConfiguration;
    }
}
