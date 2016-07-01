package org.georchestra.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * If the user-agent of the client is one of the supported user-agents then send a basic authentication request.
 * <p/>
 * Normally the security proxy is configured to redirect to cas login if a user is not authenticated and tries to access a secured page.
 * This behaviour is fine for users visiting the site via browsers, however user with other clients usch as QGis or ARcGIS that use
 * basic authentication expect a basic authentication challenge in order to authenticate.
 * <p/>
 * This filter will check the user-agent and, if a match is found, send a basic auth challenge.
 * <p/>
 * User: Jesse
 * Date: 11/7/13
 * Time: 9:44 AM
 */
public class BasicAuthChallengeByUserAgent extends BasicAuthenticationFilter {

    private final List<Pattern> _userAgents = new ArrayList<Pattern>();
    private List<String> blacklistedHosts = new ArrayList<String>();
    private boolean ignoreHttps = false;
    private static final Log LOGGER = LogFactory.getLog(BasicAuthChallengeByUserAgent.class.getPackage().getName());
    private AuthenticationException _exception = new AuthenticationException("No basic authentication credentials provided") {};

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        if(!req.getScheme().equalsIgnoreCase("https") && ! ignoreHttps) {
            LOGGER.debug("not in HTTPS, skipping filter.");
            chain.doFilter(req, res);
            return;
        }
        try {

            // We have to rely on the HTTP header "x-forwarded-for", instead of
            // req.getRemoteAddr() (which will return the last proxy IP addr
            // instead).

            // Note: the field can be multivalued, comma-separated list, the
            // last value should be the most reliable one (added by the
            // geOrchestra front webserver).

            String[] xfwForHeaders = ((HttpServletRequest) req).getHeader("x-forwarded-for").split(",");
            String xfwForHeader = xfwForHeaders[xfwForHeaders.length - 1].trim();
            for (String blHost : blacklistedHosts) {
                if (blHost.equals(xfwForHeader)) {
                    LOGGER.debug(String.format("Host \"%s\" is blacklisted. Skipping filter.", blHost));
                    chain.doFilter(req, res);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to check if the host doing the request is blacklisted or not, skipping check: ", e);
        }

        final HttpServletRequest request = (HttpServletRequest) req;
        final String userAgent = request.getHeader("User-Agent");
        if (userAgentMatch(userAgent)) {
            String auth = request.getHeader("Authorization");

            if ((auth == null) || !auth.startsWith("Basic ")) {
                getAuthenticationEntryPoint().commence(request, (HttpServletResponse) res, _exception);
            } else {
                LOGGER.debug("Activating filter ...");
                super.doFilter(req, res, chain);
            }
        } else {
            LOGGER.debug("the user-agent does not match, skipping filter.");
            chain.doFilter(req, res);
        }
    }

    private boolean userAgentMatch(Object attribute) {
        if (attribute!=null) {
            for (Pattern userAgent : _userAgents) {
                if (userAgent.matcher(attribute.toString()).matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Set the user-agents the string is parsed as a Regex expression.
     */
    public void setChallengeUserAgents(List<String> userAgents) {
        _userAgents.clear();
        for (String userAgent : userAgents) {
            _userAgents.add(Pattern.compile(userAgent));
        }
    }

    /**
     * Sets the ignoreHttps flag.
     * if set to true the filter is active even on regular non-SSL HTTP requests.
     */
    public void setIgnoreHttps(boolean f) {
        ignoreHttps = f;
    }
    /**
     * Sets the lists of hosts which are blacklisted.
     * If the host doing the request is in this list, the filter is discarded.
     */
    public void setBlackListedHosts(List<String> hosts) {
        this.blacklistedHosts = hosts;
    }
}
