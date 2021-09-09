/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import static org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.ogcservstatistics.log4j.OGCServiceMessageFormatter;
import org.georchestra.ogcservstatistics.log4j.OGCServicesAppender;
import org.georchestra.security.permissions.Permissions;
import org.georchestra.security.permissions.UriMatcher;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * This proxy provides an indirect access to a remote host to retrieve data.
 * Useful to overcome security constraints on client side.
 * <p>
 * There are two primary ways that the paths can be encoded:
 * <ul>
 * <li>The full url to forward to is encoded in a parameter called "url"</li>
 * <li>The url is encoded as part of the path. Then the target should be defined
 * (either in the targets-mapping.properties file of the datadir or in the
 * targets map property of the proxyservlet.xml file)</li>
 * </ul>
 * Examples:
 * <p>
 * Assume the default target is http://xyz.com and the targets are:
 * x:http://x.com, y:https://y.com
 * </p>
 * <ul>
 * <li>http://this.com/context/path -- gives -- http://xyz.com/path</li>
 * <li>http://this.com/context/x/path -- gives -- http://x.com/path</li>
 * <li>http://this.com/context/y/path -- gives -- https://y.com/path</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * @author yoann.buch@gmail.com
 * @author jesse.eichar@camptocamp.com
 */
@Controller
public class Proxy {

    private final static String SET_COOKIE_HEADER = "Set-Cookie";

    protected static final Log logger = LogFactory.getLog(Proxy.class.getPackage().getName());
    protected static final Log statsLogger = LogFactory.getLog(Proxy.class.getPackage().getName() + ".statistics");
    private static final org.apache.http.client.RedirectStrategy NO_REDIRECT_STRATEGY = new org.apache.http.client.RedirectStrategy() {
        @Override
        public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
                throws ProtocolException {
            return false;
        }

        @Override
        public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
                throws ProtocolException {
            return null;
        }
    };

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    /**
     * Data source to set on {@link OGCServicesAppender#setDataSource}
     */
    private @Autowired @Qualifier("ogcStatsDataSource") DataSource ogcStatsDataSource;

    private String defaultTarget;
    private String publicUrl = "https://georchestra.mydomain.org";

    private Map<String, String> targets = Collections.emptyMap();
    private HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
    private FilterRequestsStrategy strategyForFilteringRequests = new AcceptAllRequests();
    private List<String> requireCharsetContentTypes = Collections.emptyList();

    @SuppressWarnings("unused")
    private String defaultCharset = "UTF-8";

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private Permissions proxyPermissions = null;
    private Permissions sameDomainPermissions;
    private String proxyPermissionsFile;

    /**
     * This variable controls the socketTimeout parameter passed to the httpclient
     * configuration, used for proxified queries. Relying on HttpClient's
     * documentation
     * (https://hc.apache.org/httpcomponents-client-4.2.x/tutorial/html/connmgmt.html),
     * it corresponds to the max time between two consecutive packets, and is
     * expressed in milliseconds.
     *
     */
    private int httpClientTimeoutMillis = 300000;

    public void setHttpClientTimeout(int timeout) {
        this.httpClientTimeoutMillis = timeout;
    }

    /**
     * This variable holds the timeout in minutes before the proxified HTTP requests
     * will be discarded, throwing a java.util.concurrent.TimeoutException, see
     * return of executeHttpRequest() method below.
     */
    private int entityEnclosedOrEmptyResponseTimeout = 20;

    public void setEntityEnclosedOrEmptyResponseTimeout(int entityEnclosedOrEmptyResponseTimeout) {
        this.entityEnclosedOrEmptyResponseTimeout = entityEnclosedOrEmptyResponseTimeout;
    }

    private HttpAsyncClientBuilder httpAsyncClientBuilder;

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    @PostConstruct
    public void init() throws Exception {
        OGCServicesAppender.setDataSource(ogcStatsDataSource);

        // georchestra datadir autoconfiguration
        // dependency injection / properties setter() are made by Spring before
        // init() call
        final boolean loadExternalConfig = (georchestraConfiguration != null) && (georchestraConfiguration.activated());
        if (loadExternalConfig) {
            logger.info("geOrchestra configuration detected, reconfiguration in progress ...");

            Properties pTargets = georchestraConfiguration.loadCustomPropertiesFile("targets-mapping");
            this.targets = Maps.fromProperties(pTargets);

            // Configure proxy permissions based on proxy-permissions.xml file in datadir
            String datadirContext = georchestraConfiguration.getContextDataDir();
            File datadirPermissionsFile = new File(
                    String.format("%s%s%s", datadirContext, File.separator, "proxy-permissions.xml"));
            if (datadirPermissionsFile.exists()) {
                logger.info("reading proxy permissions from " + datadirPermissionsFile.getAbsolutePath());

                try (FileInputStream fis = new FileInputStream(datadirPermissionsFile)) {
                    setProxyPermissions(Permissions.parse(fis));
                } catch (Exception ex) {
                    logger.error("Error during proxy permissions configuration from "
                            + datadirPermissionsFile.getAbsolutePath(), ex);
                }
            }

            logger.info("Done.");
        }

        targets.forEach((name, url) -> {
            final String mapping = name + "=" + url;
            logger.trace("verifying target mapping " + mapping);
            try {
                URL target = new URL(url);
                logger.trace("target mapping: " + name + "=" + target);
            } catch (MalformedURLException e) {
                throw new BeanInitializationException("Invalid target mapping: " + mapping, e);
            }
        });

        // Create a deny permission for URL with same domain
        String publicDomain = new URL(this.publicUrl).getHost();
        this.sameDomainPermissions = new Permissions();
        this.sameDomainPermissions.setDenied(Collections.singletonList(new UriMatcher(publicDomain)));
        this.sameDomainPermissions.setAllowByDefault(true);
        this.sameDomainPermissions.init();

        // Proxy permissions not set by datadir
        if (proxyPermissionsFile != null && proxyPermissions == null) {
            final ClassLoader classLoader = Proxy.class.getClassLoader();
            InputStream inStream = classLoader.getResourceAsStream(proxyPermissionsFile);
            if (inStream == null) {
                throw new RuntimeException("ProxyPermissionsFile not found");
            }
            setProxyPermissions(Permissions.parse(inStream));
        }
        httpAsyncClientBuilder = createHttpAsyncClientBuilder();
    }

    /* ---------- start work around for no gateway option -------------- */
    private Gateway gateway = new Gateway();

    @RequestMapping(value = "/gateway", method = { GET, POST })
    public void gateway(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        gateway.loadCredentialsPage(request, response);
    }

    @RequestMapping(value = "/testPage", method = { GET })
    public void testPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        gateway.testPage(response);
    }

    /* ---------- end work around for no gateway option -------------- */

    @RequestMapping(value = "/**", params = "login", method = { GET, POST })
    public void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, URISyntaxException {
        String uri = request.getRequestURI();

        URIBuilder uriBuilder = new URIBuilder(uri);
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (!"login".equals(paramName)) {
                String[] paramValues = request.getParameterValues(paramName);
                for (int i = 0; i < paramValues.length; i++) {
                    uriBuilder.setParameter(paramName, paramValues[i]);
                }
            }
        }

        redirectStrategy.sendRedirect(request, response, uriBuilder.build().toString());
    }

    /**
     * Entrypoint used for login.
     */
    @RequestMapping(value = "/**", params = { "login", "url" }, method = { GET, POST })
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL)
            throws ServletException, IOException {
        redirectStrategy.sendRedirect(request, response, sURL);
    }

    /**
     * Entry point used mainly for XHR requests using a URL-encoded parameter named
     * url.
     */
    @RequestMapping(value = "/proxy/", params = { "url", "!login" })
    public void handleUrlParamRequest(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("url") String sURL) throws IOException {
        testLegalContentType(request);
        URL url;
        try {
            url = new URL(sURL);
        } catch (MalformedURLException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // deny request with same domain as publicUrl - deny based on
        // proxy-permissions.xml file
        if (this.sameDomainPermissions.isDenied(url) || proxyPermissions.isDenied(url)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "URL is not allowed.");
            return;
        }
        handleRequest(request, response, sURL, false);
    }

    /**
     * Entry point used for security-proxified webapps. Note: the url parameter is
     * sometimes used by the underlying webapps (e.g. mapfishapp and the mfprint
     * configuration). hence we need to allow it in the following "params" array.*
     */
    @RequestMapping(value = "/**", params = { "!login" })
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response);
    }

    /**
     * Default redirection to defaultTarget. By default returns a 302 redirect to
     * '/header/'. The parameter can be customized in the security-proxy.properties
     * file.
     */
    @RequestMapping(value = "/", params = { "!url", "!login" })
    public void handleDefaultRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(defaultTarget);
        return;
    }

    /**
     * Indicates whether the requested URL is a one protected by the Security-proxy
     * or not, e.g. urlIsProtected(mapfishapp) will generally return true (unless if
     * mapfishapp is not configured on this geOrchestra instance, which is probably
     * unlikely).
     *
     * @param request the HttpServletRequest
     * @param url     the requested url
     * @return true if the url is protected by the SP, false otherwise.
     *
     */
    private boolean urlIsProtected(HttpServletRequest request, URL url) throws IOException {
        if (isSameServer(request, url)) {
            String requestURI = url.getPath();
            String[] requestSegments = splitRequestPath(requestURI);
            for (String target : targets.values()) {
                if (samePathPrefix(requestSegments, target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the request targets a host (host header) which is the same as the
     * requested URL in parameter.
     *
     * @return true if the host in the request matches the host in the requested
     *         URL, false otherwise.
     */
    private boolean isSameServer(HttpServletRequest request, URL url) {
        try {
            return InetAddress.getByName(request.getServerName()).equals(InetAddress.getByName(url.getHost()));
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if the target url matches a security-proxified target.
     *
     * @return true if so, false otherwise.
     */
    private boolean samePathPrefix(String[] requestSegments, String target) throws MalformedURLException {
        String[] targetSegments = splitRequestPath(new URL(target).getPath());
        for (int i = 0; i < targetSegments.length; i++) {
            String targetSegment = targetSegments[i];
            if (!targetSegment.equals(requestSegments[i])) {
                return false;
            }
        }
        return true;
    }

    private String[] splitRequestPath(String requestURI) {
        return filter(requestURI.split("/"));
    }

    /**
     * Since the URL param can access any url we need to control what it can request
     * so it is not used for nefarious purposes. We are basing the control on
     * contentType because it is supposed to be able to access any server.
     */
    private void testLegalContentType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return;
        }
        // focus only on type, not on the text encoding
        String type = contentType.split(";")[0];
        for (String validTypeContent : requireCharsetContentTypes) {
            if (!validTypeContent.equals(type)) {
                return;
            }
        }
        throw new IllegalArgumentException("ContentType " + contentType
                + " is not permitted to be requested when the request is made through the URL parameter form.");
    }

    private String buildForwardRequestURL(HttpServletRequest request) {
        String forwardRequestURI = request.getRequestURI();
        forwardRequestURI = forwardRequestURI.replaceAll("//", "/");

        return forwardRequestURI;
    }

    /**
     * Main entry point for methods where the request path is encoded in the path of
     * the URL
     */
    private void handlePathEncodedRequests(HttpServletRequest request, HttpServletResponse response) {
        try {
            String contextPath = request.getServletPath() + request.getContextPath();
            String forwardRequestURI = buildForwardRequestURL(request);
            HttpMethod type = HttpMethod.resolve(request.getMethod());
            logger.debug("handlePathEncodedRequests: -- Handling Request: " + type + ":" + forwardRequestURI + " from: "
                    + request.getRemoteAddr());

            String sURL = findTarget(forwardRequestURI);

            if (sURL == null) {
                response.sendError(404);
                return;
            }

            URL url;
            try {
                url = new URL(sURL);
            } catch (MalformedURLException e) {
                throw new MalformedURLException(sURL + " is not a valid URL");
            }

            boolean sameHostAndPort = false;

            try {
                sameHostAndPort = isSameHostAndPort(request, url);
            } catch (UnknownHostException e) {
                logger.error("Unknown host in requested URL", e);
                response.sendError(503);
                return;
            }

            if (sameHostAndPort && (isRecursiveCallToProxy(forwardRequestURI, contextPath)
                    || isRecursiveCallToProxy(url.getPath(), contextPath))) {
                response.sendError(403,
                        forwardRequestURI + " is a recursive call to this service.  That is not a legal request");
                return;
            }

            final String query = request.getQueryString();
            boolean needCasValidation = (request.getParameter(ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER) != null)
                    && (request.getUserPrincipal() == null) && urlIsProtected(request, new URL(sURL));
            // special case: if we have a ticket parameter and no
            // authentication principal, we probably need to validate/open
            // the session against CAS server
            if (needCasValidation) {
                // loginUrl: sends a redirect to the client with a ?login (or &login if other
                // arguments)
                // since .*login patterns are protected by the SP, this would trigger an
                // authentication
                // onto CAS (which should succeed if the user is already connected onto the
                // platform).
                String loginUrl = String.format("%s%s%s", request.getPathInfo(),
                        StringUtils.isEmpty(query) ? "?" : query, "login");
                redirectStrategy.sendRedirect(request, response, loginUrl);
                return;
            }
            if (query != null)
                sURL += "?" + query;
            handleRequest(request, response, sURL, true);
        } catch (IOException e) {
            logger.error("Error connecting to client", e);
        }
    }

    private boolean isSameHostAndPort(HttpServletRequest request, URL url) throws IOException {
        return isSameServer(request, url) && url.getPort() == request.getServerPort();
    }

    private String findTarget(String requestURI) {
        String[] segments;
        if (requestURI.charAt(0) == '/') {
            segments = requestURI.substring(1).split("/");
        } else {
            segments = requestURI.split("/");
        }

        if (segments.length == 0) {
            return null;
        }
        String target = targets.get(segments[0]);
        if (target == null) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder("/");
            for (int i = 1; i < segments.length; i++) {
                String segment = segments[i];
                builder.append(segment);
                if (i + 1 < segments.length)
                    builder.append("/");
            }

            if (requestURI.endsWith("/") && builder.charAt(builder.length() - 1) != '/') {
                builder.append('/');
            }

            return concat(target, builder);
        }
    }

    private String concat(String target, StringBuilder builder) {
        if (target == null) {
            return null;
        }
        String target2 = target;
        if (target.endsWith("/")) {
            target2 = target.substring(0, target.length() - 1);
        }
        if (builder.charAt(0) != '/') {
            builder.insert(0, '/');
        }
        return target2 + builder;

    }

    private String findMatchingTarget(HttpServletRequest request) {
        String requestURI = buildForwardRequestURL(request);
        String[] segments = splitRequestPath(requestURI);

        if (segments.length == 0) {
            return null;
        }

        if (targets.containsKey(segments[0])) {
            return segments[0];
        }
        return null;
    }

    /**
     * Actually do the request to the proxified server.
     *
     * @param request       the original request
     * @param finalResponse the servlet response
     * @param sURL          the url to proxify onto
     * @param localProxy    true if the request targets a security-proxyfied webapp
     *                      (e.g. mapfishapp, ...), false otherwise
     */
    private void handleRequest(HttpServletRequest request, HttpServletResponse finalResponse, String sURL,
            boolean localProxy) {
        try (CloseableHttpAsyncClient httpclient = httpAsyncClientBuilder.build()) {
            httpclient.start();

            HttpResponse proxiedResponse = null;
            int statusCode = 500;

            URL url = null;
            try {
                url = new URL(sURL);
            } catch (MalformedURLException e) { // not an url
                finalResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            // HTTP protocol is required
            if (!"http".equalsIgnoreCase(url.getProtocol()) && !"https".equalsIgnoreCase(url.getProtocol())) {
                finalResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "HTTP protocol expected. \"" + url.getProtocol() + "\" used.");
                return;
            }

            // check if proxy must filter on final host
            if (!strategyForFilteringRequests.allowRequest(url)) {
                finalResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Host \"" + url.getHost() + "\" is not allowed to be requested");
                return;
            }

            logger.debug("Final request -- " + sURL);

            HttpRequestBase proxyingRequest = makeRequest(request, sURL);
            String targetServiceName = findMatchingTarget(request);
            logger.debug("Gathering headers for service " + targetServiceName);
            headerManagement.configureRequestHeaders(request, proxyingRequest, localProxy, targetServiceName);

            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Header[] originalHeaders = proxyingRequest.getHeaders(SEC_ORGNAME);
                String org = "";
                for (Header originalHeader : originalHeaders) {
                    org = originalHeader.getValue();
                }
                // no OGC SERVICE log if request going through /proxy/?url=
                if (!request.getRequestURI().startsWith("/proxy/")) {
                    String[] roles = new String[] { "" };
                    try {
                        Header[] rolesHeaders = proxyingRequest.getHeaders(SEC_ROLES);
                        if (rolesHeaders.length > 0) {
                            roles = rolesHeaders[0].getValue().split(";");
                        }
                    } catch (Exception e) {
                        logger.error("Unable to compute roles");
                    }
                    statsLogger.info(OGCServiceMessageFormatter.format(authentication.getName(), sURL, org, roles));

                }

            } catch (Exception e) {
                logger.error("Unable to log the request into the statistics logger", e);
            }

            proxiedResponse = executeHttpRequest(httpclient, proxyingRequest);
            StatusLine statusLine = proxiedResponse.getStatusLine();
            statusCode = statusLine.getStatusCode();
            String reasonPhrase = statusLine.getReasonPhrase();

            if (reasonPhrase != null && statusCode >= 400) {
                logger.warn("Downstream server returned a status code which could be an error. " + "Statuscode: "
                        + statusCode + ", reason: " + reasonPhrase);

                if (statusCode == 401) {
                    //
                    // Handle case of basic authentication.
                    //
                    Header authHeader = proxiedResponse.getFirstHeader("WWW-Authenticate");
                    finalResponse.setHeader("WWW-Authenticate",
                            (authHeader == null) ? "Basic realm=\"Authentication required\"" : authHeader.getValue());
                }

                // 403 and 404 are handled by specific JSP files provided by the security-proxy
                // webapp
                if ((statusCode == 404) || (statusCode == 403)) {
                    // Hack for GN3.4: to protect against CSRF attacks, a token is provided by the
                    // xml.info service.
                    // Even if the return code is a 403, we are interested in getting the Set-Cookie
                    // value.
                    if (sURL.contains("/geonetwork/")) {
                        Header setCookie = extractHeaderSetCookie(proxiedResponse);
                        if (setCookie != null) {
                            finalResponse.addHeader(setCookie.getName(), setCookie.getValue());
                        }
                    }
                    finalResponse.sendError(statusCode);
                    return;
                }
            }

            // process response headers before handling redirect or performing request
            headerManagement.copyResponseHeaders(request, request.getRequestURI(), proxiedResponse, finalResponse,
                    this.targets);

            // Handle redirects
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                Optional<String> adjustedLocation = adjustLocation(request, proxiedResponse);
                if (adjustedLocation.isPresent()) {
                    logger.debug("Handling redirect to " + adjustedLocation.get());
                    finalResponse.setStatus(statusCode);
                    finalResponse.setHeader("Location", adjustedLocation.get());
                } else {
                    finalResponse.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to proxify redirect URL");
                    return;
                }
            }

            doHandleRequest(finalResponse, proxiedResponse);
        } catch (TimeoutException e) {
            String errMsg = String.format("timeout on [%s] '%s'", request.getMethod(), sURL);
            logger.error(errMsg, e);
            try {
                finalResponse.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            } catch (IOException ex2) {
                // the least we can do is then to log the exception
                // and set an explicit status
                logger.error(ex2);
                finalResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        } catch (IOException | ExecutionException | InterruptedException e) {
            // connection problem with the host
            String errMsg = String.format("Exception occured when trying to connect to the remote url '%s'", sURL);
            logger.error(errMsg, e);
            try {
                if (e.getCause() instanceof java.net.UnknownHostException) {
                    // If the SP cannot resolve the remote host, it sounds more
                    // logical to return a 404.
                    finalResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                finalResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (IOException e2) {
                // error occured while trying to return the "service unavailable status"
                finalResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private HttpAsyncClientBuilder createHttpAsyncClientBuilder() {
        HttpAsyncClientBuilder htb = HttpAsyncClients.custom().setRedirectStrategy(NO_REDIRECT_STRATEGY);

        RequestConfig config = RequestConfig.custom().setSocketTimeout(this.httpClientTimeoutMillis).build();
        htb.setDefaultRequestConfig(config);

        // Handle http proxy for external request.
        // Proxy must be configured by system variables (e.g.: -Dhttp.proxyHost=proxy
        // -Dhttp.proxyPort=3128)
        htb.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        return htb;
    }

    /**
     * Extracts the set-cookie http header from the downstream response.
     *
     * @return the header if present, else null.
     */
    private Header extractHeaderSetCookie(HttpResponse proxiedResponse) {
        for (Header h : proxiedResponse.getAllHeaders()) {
            if (h.getName().equalsIgnoreCase(SET_COOKIE_HEADER)) {
                return h;
            }
        }
        return null;
    }

    @VisibleForTesting
    protected HttpResponse executeHttpRequest(CloseableHttpAsyncClient httpclient, HttpRequestBase proxyingRequest)
            throws IOException, TimeoutException, ExecutionException, InterruptedException {
        CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();

        HttpAsyncResponseConsumer<Boolean> consumer = new AbstractAsyncResponseConsumer<Boolean>() {

            private ByteBuffer bbuf = ByteBuffer.allocate(8192);
            private HttpResponse httpResponse;
            private PipedOutputStream pos = new PipedOutputStream();
            private PipedInputStream pis = new PipedInputStream(pos);
            private WritableByteChannel channel = Channels.newChannel(pos);

            @Override
            protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
                HttpEntity streamEntity = new InputStreamEntity(pis, contentType);
                httpResponse.setEntity(streamEntity);
                future.complete(httpResponse);
            }

            @Override
            protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
                int bytesRead = decoder.read(this.bbuf);
                while (bytesRead > 0) {
                    this.bbuf.flip();
                    while (this.bbuf.hasRemaining()) {
                        channel.write(this.bbuf);
                    }
                    this.bbuf.clear();
                    bytesRead = decoder.read(this.bbuf);
                }
            }

            @Override
            protected void releaseResources() {
                try {
                    channel.close();
                    pos.close();
                } catch (IOException e) {
                }
            }

            @Override
            protected void onResponseReceived(HttpResponse httpResponse) throws HttpException, IOException {
                this.httpResponse = httpResponse;
                if (httpResponse.getEntity() == null) {
                    future.complete(httpResponse);
                }
            }

            @Override
            protected Boolean buildResult(HttpContext httpContext) throws Exception {
                return Boolean.TRUE;
            }

        };

        HttpAsyncRequestProducer producer = new BasicAsyncRequestProducer(
                new HttpHost(proxyingRequest.getURI().getHost(), proxyingRequest.getURI().getPort(),
                        proxyingRequest.getURI().getScheme()),
                proxyingRequest) {
            @Override
            public void failed(Exception exc) {
                future.completeExceptionally(exc);
            }
        };

        httpclient.execute(producer, consumer, null);

        return future.get(this.entityEnclosedOrEmptyResponseTimeout, TimeUnit.MINUTES);
    }

    private @Nullable String extractLocationHeader(HttpResponse proxiedResponse) {
        Header location = proxiedResponse.getFirstHeader("Location");
        return location == null ? null : location.getValue();
    }

    private Optional<String> adjustLocation(HttpServletRequest request, HttpResponse proxiedResponse) {
        logger.debug("adjustLocation called for request: " + request.getRequestURI());

        final String target = findMatchingTarget(request);
        final String locationHeader = extractLocationHeader(proxiedResponse);

        String adjustedLocation = locationHeader;

        if (target != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("adjustLocation found target: " + target + " for request: " + request.getRequestURI());
            }

            final String baseURL = targets.get(target);
            final URI baseURI;
            try {
                baseURI = new URI(baseURL);
                logger.debug("adjustLocation process header: " + locationHeader);
                URI locationURI = new URI(locationHeader);
                URI resolvedURI = baseURI.resolve(locationURI);

                logger.debug("Test location header: " + resolvedURI.toString() + " against: " + baseURI.toString());
                if (resolvedURI.toString().startsWith(baseURI.toString())) {
                    String resolvedSuffix = resolvedURI.toString().substring(baseURI.toString().length());
                    String newLocation = "/" + target;
                    if (!resolvedSuffix.startsWith("/")) {
                        newLocation += "/";
                    }
                    newLocation += resolvedSuffix;
                    logger.debug("adjustLocation from: " + locationHeader + " to " + newLocation);
                    adjustedLocation = newLocation;
                }
            } catch (URISyntaxException e) {
                logger.info("Error creating baseURI from baseURL, leaving original Location header untouched", e);
            }
        }
        return Optional.ofNullable(adjustedLocation);

    }

    /**
     * Direct copy of response
     */
    private void doHandleRequest(HttpServletResponse finalResponse, HttpResponse proxiedResponse) throws IOException {

        finalResponse.setStatus(proxiedResponse.getStatusLine().getStatusCode());

        HttpEntity entity = proxiedResponse.getEntity();
        if (entity != null) {
            // Send the Response
            OutputStream outputStream = finalResponse.getOutputStream();
            try {
                entity.writeTo(outputStream);
            } finally {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    private URI buildUri(URL url) throws URISyntaxException {
        // Let URI constructor encode Path part
        // Don't use query part because URI constructor will try to double-encode it
        // (query part is already encoded in sURL)
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), null,
                url.getRef());

        // Reconstruct URL with encoded path from URI class and others parameters from
        // URL class
        StringBuilder rawUrl = new StringBuilder(url.getProtocol() + "://" + url.getHost());

        if (url.getPort() != -1)
            rawUrl.append(":" + String.valueOf(url.getPort()));

        rawUrl.append(uri.getPath()); // Do not URL-encode, and take the request
        // as it originally comes instead of doing extra-encoding.

        if (url.getQuery() != null)
            rawUrl.append("?" + url.getQuery()); // Use already encoded query part

        return new URI(rawUrl.toString());
    }

    private HttpRequestBase makeRequest(HttpServletRequest request, String sURL) throws IOException {
        HttpRequestBase targetRequest;
        try {
            URL url = new URL(sURL);
            URI uri = buildUri(url);
            String method = request.getMethod();

            // handles webdav specific verbs
            String[] webdavVerb = { "COPY", "LOCK", "UNLOCK", "MKCOL", "MOVE", "PROPFIND", "PROPPATCH", "UNLOCK",
                    "REPORT", "SEARCH" };
            boolean isWebdav = Arrays.stream(webdavVerb).anyMatch(x -> x.equalsIgnoreCase(method));
            if (isWebdav) {
                HttpEntityEnclosingRequestBase heerb = new HttpEntityEnclosingRequestBase() {
                    @Override
                    public String getMethod() {
                        return method.toUpperCase();
                    }
                };
                heerb.setURI(uri);
                int contentLength = request.getContentLength();
                ServletInputStream inputStream = request.getInputStream();
                HttpEntity entity = new InputStreamEntity(inputStream, contentLength);
                heerb.setEntity(entity);
                return heerb;
            }

//            if (PropFindMethod.METHOD_NAME.equalsIgnoreCase(method)) {
//                PropFindMethod pfm = new PropFindMethod(uri);
//                int contentLength = request.getContentLength();
//                ServletInputStream inputStream = request.getInputStream();
//                HttpEntity entity = new InputStreamEntity(inputStream, contentLength);
//                pfm.setEntity(entity);
//                return pfm;
//            } else if (SearchMethod.METHOD_NAME.equalsIgnoreCase(method)) {
//                SearchMethod sm = new SearchMethod(uri);
//                int contentLength = request.getContentLength();
//                ServletInputStream inputStream = request.getInputStream();
//                HttpEntity entity = new InputStreamEntity(inputStream, contentLength);
//                sm.setEntity(entity);
//                return sm;
//            }

            HttpMethod meth = HttpMethod.resolve(method);
            if (meth == null) {
                throw new IllegalArgumentException(method + " is not supported.");
            }

            switch (meth) {
            case GET: {
                logger.debug("New request is: " + sURL + "\nRequest is GET");

                HttpGet get = new HttpGet(uri);
                targetRequest = get;
                break;
            }
            case POST: {
                logger.debug("New request is: " + sURL + "\nRequest is POST");
                HttpPost post = new HttpPost(uri);

                int contentLength = request.getContentLength();
                ServletInputStream inputStream = request.getInputStream();
                HttpEntity entity = new InputStreamEntity(inputStream, contentLength);
                post.setEntity(entity);
                targetRequest = post;
                break;
            }
            case TRACE: {
                logger.debug("New request is: " + sURL + "\nRequest is TRACE");

                HttpTrace post = new HttpTrace(uri);

                targetRequest = post;
                break;
            }
            case OPTIONS: {
                logger.debug("New request is: " + sURL + "\nRequest is OPTIONS");

                HttpOptions post = new HttpOptions(uri);

                targetRequest = post;
                break;
            }
            case HEAD: {
                logger.debug("New request is: " + sURL + "\nRequest is HEAD");

                HttpHead post = new HttpHead(uri);

                targetRequest = post;
                break;
            }
            case PUT: {
                logger.debug("New request is: " + sURL + "\nRequest is PUT");

                HttpPut put = new HttpPut(uri);

                put.setEntity(new InputStreamEntity(request.getInputStream(), request.getContentLength()));

                targetRequest = put;
                break;
            }
            case DELETE: {
                logger.debug("New request is: " + sURL + "\nRequest is DELETE");

                HttpDelete delete = new HttpDelete(uri);

                targetRequest = delete;
                break;
            }
            case PATCH: {
                logger.debug("New request is: " + sURL + "\nRequest is PATCH");

                HttpPatch patch = new HttpPatch(uri);

                targetRequest = patch;
                break;
            }
            default: {
                String msg = meth + " not yet supported";
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }
            }
        } catch (URISyntaxException e) {
            logger.error("ERROR creating URI from " + sURL, e);
            throw new IOException(e);
        }

        return targetRequest;
    }

    protected String[] filter(String[] one) {
        return Arrays.stream(one).filter(x -> x.length() > 0).toArray(String[]::new);
    }

    /**
     * Check to see if the call is recursive based on forwardRequestURI startsWith
     * contextPath.
     */
    protected boolean isRecursiveCallToProxy(String forwardRequestURI, String contextPath) {
        String[] one = filter(forwardRequestURI.split("/"));
        String[] two = filter(contextPath.split("/"));

        if (one.length < two.length) {
            return false;
        }

        for (int i = 0; i < two.length && i < one.length; i++) {
            if (!(two[i].equalsIgnoreCase(one[i]))) {
                return false;
            }
        }
        return true;
    }

    public void setDefaultTarget(String defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public void setTargets(Map<String, String> targets) {
        this.targets = targets == null ? ImmutableMap.of() : ImmutableMap.copyOf(targets);
    }

    public void setHeaderManagement(HeadersManagementStrategy headerManagement) {
        this.headerManagement = headerManagement;
    }

    public void setRequireCharsetContentTypes(List<String> requireCharsetContentTypes) {
        this.requireCharsetContentTypes = requireCharsetContentTypes;
    }

    public void setStrategyForFilteringRequests(FilterRequestsStrategy strategyForFilteringRequests) {
        this.strategyForFilteringRequests = strategyForFilteringRequests;
    }

    public void setDefaultCharset(String defaultCharset) {
        try {
            Charset.forName(defaultCharset);
        } catch (Throwable t) {
            throw new IllegalArgumentException(defaultCharset + " is not supported by current JVM");
        }
        this.defaultCharset = defaultCharset;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    public void setProxyPermissionsFile(String proxyPermissionsFile) {
        this.proxyPermissionsFile = proxyPermissionsFile;
    }

    public void setProxyPermissions(Permissions proxyPermissions) throws UnknownHostException {
        this.proxyPermissions = proxyPermissions;
    }

    public Permissions getProxyPermissions() {
        return proxyPermissions;
    }

    public @VisibleForTesting void setOgcStatsDataSource(DataSource ogcStatsDataSource) {
        this.ogcStatsDataSource = ogcStatsDataSource;
    }

}
