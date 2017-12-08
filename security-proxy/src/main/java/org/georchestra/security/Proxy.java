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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.ogcservstatistics.log4j.OGCServiceMessageFormatter;
import org.georchestra.security.permissions.Permissions;
import org.georchestra.security.permissions.UriMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;

/**
 * This proxy provides an indirect access to a remote host to retrieve data.
 * Useful to overcome security constraints on client side.
 * <p>
 * There are two primary ways that the paths can be encoded:
 * <ul>
 * <li>The full url to forward to is encoded in a parameter called "url"</li>
 * <li>The url is encoded as part of the path. Then the target should be
 * defined (either in the targets-mapping.properties file of the datadir or in
 * the targets map property of the proxyservlet.xml file)</li>
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
@RequestMapping("/*")
public class Proxy {
    protected static final Log logger = LogFactory.getLog(Proxy.class.getPackage().getName());
    protected static final Log statsLogger = LogFactory.getLog(Proxy.class.getPackage().getName() + ".statistics");
    protected static final Log commonLogger = LogFactory.getLog(Proxy.class.getPackage().getName() + ".statistics-common");


    protected enum RequestType {
        GET, POST, DELETE, PUT, TRACE, OPTIONS, HEAD
    }

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    /**
     * must be defined
     */
    private String defaultTarget;
    private Map<String, String> targets = Collections.emptyMap();
    /**
     * must be defined
     */
    private HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
    private FilterRequestsStrategy strategyForFilteringRequests = new AcceptAllRequests();
    private List<String> requireCharsetContentTypes = Collections.emptyList();
    private String defaultCharset = "UTF-8";

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private Permissions proxyPermissions = new Permissions();
    private String proxyPermissionsFile;


    private Integer httpClientTimeout = 300000;

    public void setHttpClientTimeout(Integer timeout) {
        this.httpClientTimeout = timeout;
    }

    public Integer getHttpClientTimeout() {
        return httpClientTimeout;
    }

    public void init() throws Exception {
        if (targets != null) {
            for (String url : targets.values()) {
                new URL(url); // test that it is a valid URL
            }
        }
        if (proxyPermissionsFile != null) {
            Closer closer = Closer.create();
            try {
                final ClassLoader classLoader = Proxy.class.getClassLoader();
                InputStream inStream = closer.register(classLoader.getResourceAsStream(proxyPermissionsFile));
                Map<String, Class<?>> aliases = Maps.newHashMap();
                aliases.put(Permissions.class.getSimpleName().toLowerCase(), Permissions.class);
                aliases.put(UriMatcher.class.getSimpleName().toLowerCase(), UriMatcher.class);
                XStreamMarshaller unmarshaller = new XStreamMarshaller();
                unmarshaller.setAliasesByType(aliases);
                setProxyPermissions((Permissions) unmarshaller.unmarshal(new StreamSource(inStream)));
            } finally {
                closer.close();
            }
        }
        // georchestra datadir autoconfiguration
        // dependency injection / properties setter() are made by Spring before
        // init() call
        if ((georchestraConfiguration != null) && (georchestraConfiguration.activated())) {
            logger.info("geOrchestra configuration detected, reconfiguration in progress ...");

            Properties pTargets = georchestraConfiguration.loadCustomPropertiesFile("targets-mapping");

            targets.clear();
            for (String target : pTargets.stringPropertyNames()) {
                targets.put(target, pTargets.getProperty(target));
            }
            logger.info("Done.");
        }
    }

    /* ---------- start work around for no gateway option -------------- */
    private Gateway gateway = new Gateway();

    @RequestMapping(value = "/gateway", method = { GET, POST })
    public void gateway(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        gateway.loadCredentialsPage(request, response);
    }

    @RequestMapping(value = "/testPage", method = { GET })
    public void testPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        gateway.testPage(response);
    }

    /* ---------- end work around for no gateway option -------------- */

    @RequestMapping(params = "login", method = { GET, POST })
    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, URISyntaxException {
        String uri = request.getRequestURI();
        if (uri.startsWith("sec")) {
            uri = uri.substring(3);
        } else if (uri.startsWith("/sec")) {
            uri = uri.substring(4);
        }

        URIBuilder uriBuilder = new URIBuilder(uri);
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if (!"login".equals(paramName)) {
                String[] paramValues = request.getParameterValues(paramName);
                for (int i = 0; i < paramValues.length; i++) {
                    uriBuilder.setParameter(paramName, paramValues[i]);
                }
            }
        }

        redirectStrategy.sendRedirect(request, response, uriBuilder.build().toString());
    }

    @RequestMapping("/services_monitoring")
    public void servicesMonitoring(HttpServletRequest request, HttpServletResponse response) throws IOException {
        (new ServicesMonitoring(this.georchestraConfiguration.loadCustomPropertiesFile("targets-mapping"))).checkServices(request, response);
    }

    @RequestMapping(params = { "login", "url" }, method = { GET, POST })
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws ServletException, IOException {
        redirectStrategy.sendRedirect(request, response, sURL);
    }

    // ----------------- Method calls where request is encoded in a url
    // parameter of request ----------------- //
    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.POST)
    public void handleUrlPOSTRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.POST, sURL);
    }

    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.GET)
    public void handleUrlGETRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.GET, sURL);
    }

    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.DELETE)
    public void handleUrlDELETERequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.DELETE, sURL);
    }

    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.HEAD)
    public void handleUrlHEADRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.HEAD, sURL);
    }

    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.OPTIONS)
    public void handleUrlOPTIONSRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.OPTIONS, sURL);
    }

    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.PUT)
    public void handleUrlPUTRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.PUT, sURL);
    }

    @RequestMapping(params = { "url", "!login" }, method = RequestMethod.TRACE)
    public void handleUrlTRACERequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.TRACE, sURL);
    }

    private void handleUrlParamRequest(HttpServletRequest request, HttpServletResponse response, RequestType type, String sURL) throws IOException {
        if (request.getRequestURI().startsWith("/sec/proxy/")) {
            testLegalContentType(request);
            URL url;
            try {
                url = new URL(sURL);
            } catch (MalformedURLException e) { // not an url
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
            if (proxyPermissions.isDenied(url) || urlIsProtected(request, url)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "URL is not allowed.");
                return;
            }
            handleRequest(request, response, type, sURL, false);
        } else {
            handlePathEncodedRequests(request, response, type);
        }
    }

    /**
     * Indicates whether the requested URL is a one protected by the
     * Security-proxy or not, e.g. urlIsProtected(mapfishapp) will generally
     * return true (unless if mapfishapp is not configured on this geOrchestra
     * instance, which is probably unlikely).
     *
     * @param request
     *            the HttpServletRequest
     * @param url
     *            the requested url
     * @return true if the url is protected by the SP, false otherwise.
     *
     * @throws IOException
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

    private boolean isSameServer(HttpServletRequest request, URL url) {
        try {
            return InetAddress.getByName(request.getServerName()).equals(InetAddress.getByName(url.getHost()));
        } catch (UnknownHostException e) {
            logger.error("Unknown host: " + request.getServerName());
            return false;
        }
    }

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
        String[] requestSegments;
        if (requestURI.charAt(0) == '/') {
            requestSegments = StringUtils.split(requestURI.substring(1), '/');
        } else {
            requestSegments = StringUtils.split(requestURI, '/');
        }
        return requestSegments;
    }

    /**
     * Since the URL param can access any url we need to control what it can
     * request so it is not used for nefarious purposes. We are basing the
     * control on contentType because it is supposed to be able to access any
     * server.
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

    // ----------------- Method calls where request is encoded in path of
    // request ----------------- //
    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.GET)
    public void handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.GET);
    }

    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.POST)
    public void handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.POST);
    }

    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.DELETE)
    public void handleDELETERequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.DELETE);
    }

    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.HEAD)
    public void handleHEADRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.HEAD);
    }

    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.OPTIONS)
    public void handleOPTIONSRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.OPTIONS);
    }

    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.PUT)
    public void handlePUTRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.PUT);
    }

    /**
     * Default redirection to defaultTarget. By default returns a 302 redirect to '/header/'. The
     * parameter can be customized in the security-proxy.properties file.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/", params = { "!url", "!login" })
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(defaultTarget);
        return;
    }

    @RequestMapping(params = { "!url", "!login" }, method = RequestMethod.TRACE)
    public void handleTRACERequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.TRACE);
    }

    // ----------------- Implementation methods ----------------- //

    private String buildForwardRequestURL(HttpServletRequest request) {
        String forwardRequestURI = request.getRequestURI();
        // Makes sure the URL is decoded because some servlet containers
        // (e.g. tomcat) provides the URL in an encoded manner, whereas
        // jetty does not.
        // Also we consider the whole geOrchestra stack to be full utf-8.
        try {
            forwardRequestURI = URLDecoder.decode(forwardRequestURI, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode the URL, using the encoded version", e);
        }
        String contextPath = request.getServletPath() + request.getContextPath();
        if (forwardRequestURI.length() <= contextPath.length()) {
            forwardRequestURI = "/";
        } else {
            forwardRequestURI = forwardRequestURI.substring(contextPath.length());
        }

        forwardRequestURI = forwardRequestURI.replaceAll("//", "/");

        return forwardRequestURI;
    }

    /**
     * Main entry point for methods where the request path is encoded in the
     * path of the URL
     */
    private void handlePathEncodedRequests(HttpServletRequest request, HttpServletResponse response, RequestType requestType) {
        try {
            String contextPath = request.getServletPath() + request.getContextPath();
            String forwardRequestURI = buildForwardRequestURL(request);

            logger.debug("handlePathEncodedRequests: -- Handling Request: " + requestType + ":" + forwardRequestURI + " from: " + request.getRemoteAddr());

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

            if (sameHostAndPort && (isRecursiveCallToProxy(forwardRequestURI, contextPath) || isRecursiveCallToProxy(url.getPath(), contextPath))) {
                response.sendError(403, forwardRequestURI + " is a recursive call to this service.  That is not a legal request");
            }

            if (request.getQueryString() != null) {
                StringBuilder query = new StringBuilder("?");
                Enumeration paramNames = request.getParameterNames();
                boolean needCasValidation = false;
                while (paramNames.hasMoreElements()) {
                    String name = (String) paramNames.nextElement();
                    String[] values = request.getParameterValues(name);
                    for (String string : values) {
                        if (query.length() > 1) {
                            query.append('&');
                        }
                        // special case: if we have a ticket parameter and no
                        // authentication principal, we need to validate/open
                        // the session against CAS server
                        if ((request.getUserPrincipal() == null)
                                && (name.equals(ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER))) {
                           needCasValidation = true;
                        } else {
                            query.append(name);
                            query.append('=');
                            query.append(URLEncoder.encode(string, "UTF-8"));
                        }
                    }
                }
                sURL += query;
                if ((needCasValidation) && (urlIsProtected(request, new URL(sURL)))) {
                    // loginUrl: sends a redirect to the client with a ?login (or &login if other arguments)
                    // since .*login patterns are protected by the SP, this would trigger an authentication
                    // onto CAS (which should succeed if the user is already connected onto the platform).
                    String loginUrl = String.format("%s%s%s", request.getPathInfo(), query, "login");
                    redirectStrategy.sendRedirect(request, response, loginUrl);
                    return;
                }
            }

            handleRequest(request, response, requestType, sURL, true);
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
        return findMatchingTarget(requestURI);
    }

    private String findMatchingTarget(String requestURI) {
        String[] segments = splitRequestPath(requestURI);

        if (segments.length == 0) {
            return null;
        }

        if (targets.containsKey(segments[0])) {
            return segments[0];
        } else {
            return null;
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse finalResponse, RequestType requestType, String sURL, boolean localProxy) {
        HttpClientBuilder htb = HttpClients.custom().disableRedirectHandling();

        RequestConfig config = RequestConfig.custom().setSocketTimeout(this.httpClientTimeout).build();
        htb.setDefaultRequestConfig(config);

        //
        // Handle http proxy for external request.
        // Proxy must be configured by system variables (e.g.: -Dhttp.proxyHost=proxy -Dhttp.proxyPort=3128)
        htb.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        HttpClient httpclient = htb.build();

        HttpResponse proxiedResponse = null;
        int statusCode = 500;

        try {
            URL url = null;
            try {
                url = new URL(sURL);
            } catch (MalformedURLException e) { // not an url
                finalResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            // HTTP protocol is required
            if (!"http".equalsIgnoreCase(url.getProtocol()) && !"https".equalsIgnoreCase(url.getProtocol())) {
                finalResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP protocol expected. \"" + url.getProtocol() + "\" used.");
                return;
            }

            // check if proxy must filter on final host
            if (!strategyForFilteringRequests.allowRequest(url)) {
                finalResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Host \"" + url.getHost() + "\" is not allowed to be requested");
                return;
            }

            logger.debug("Final request -- " + sURL);

            HttpRequestBase proxyingRequest = makeRequest(request, requestType, sURL);
            headerManagement.configureRequestHeaders(request, proxyingRequest);

            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Header[] originalHeaders = proxyingRequest.getHeaders("sec-orgname");
                String org = "";
                for (Header originalHeader : originalHeaders) {
                    org = originalHeader.getValue();
                }
                // no OGC SERVICE log if request going through /proxy/?url=
                if (!request.getRequestURI().startsWith("/sec/proxy/")) {
                    String [] roles = new String[] {""};
                    try {
                        Header[] rolesHeaders = proxyingRequest.getHeaders("sec-roles");
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

            if (localProxy) {
                //
                // Hack for geoserver
                // Should not be here. We must use a ProxyTarget class and
                // define
                // if Host header should be forwarded or not.
                //
                request.getHeader("Host");
                proxyingRequest.setHeader("Host", request.getHeader("Host"));

                if (logger.isDebugEnabled()) {
                    logger.debug("Host header set to: " + proxyingRequest.getFirstHeader("Host").getValue() + " for proxy request.");
                }
            }
            proxiedResponse = executeHttpRequest(httpclient, proxyingRequest);
            StatusLine statusLine = proxiedResponse.getStatusLine();
            statusCode = statusLine.getStatusCode();
            String reasonPhrase = statusLine.getReasonPhrase();

            if (reasonPhrase != null && statusCode > 399) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Error occurred. statuscode: " + statusCode + ", reason: " + reasonPhrase);
                }

                if (statusCode == 401) {
                    //
                    // Handle case of basic authentication.
                    //
                    Header authHeader = proxiedResponse.getFirstHeader("WWW-Authenticate");
                    finalResponse.setHeader("WWW-Authenticate", (authHeader == null) ? "Basic realm=\"Authentication required\"" : authHeader.getValue());
                }

                // 403 and 404 are handled by specific JSP files provided by the
                // security-proxy webapp
                if ((statusCode == 404) || (statusCode == 403)) {
                    finalResponse.sendError(statusCode);
                    return;
                }
            }

            headerManagement.copyResponseHeaders(request, request.getRequestURI(), proxiedResponse, finalResponse, this.targets);

            if (statusCode == 302 || statusCode == 301) {
                adjustLocation(request, proxiedResponse, finalResponse);
            }
            // get content type

            String contentType = null;
            if (proxiedResponse.getEntity() != null && proxiedResponse.getEntity().getContentType() != null) {
                contentType = proxiedResponse.getEntity().getContentType().getValue();
                logger.debug("content-type detected: " + contentType);
            }

            // content type has to be valid
            if (isCharsetRequiredForContentType(contentType)) {
                doHandleRequestCharsetRequired(request, finalResponse, requestType, proxiedResponse, contentType);
            } else {
                logger.debug("charset not required for contentType: " + contentType);
                doHandleRequest(request, finalResponse, requestType, proxiedResponse);
            }
        } catch (IOException e) {
            // connection problem with the host
            logger.error("Exception occured when trying to connect to the remote host: ", e);
            try {
                finalResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (IOException e2) {
                // error occured while trying to return the
                // "service unavailable status"
                finalResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    @VisibleForTesting
    protected HttpResponse executeHttpRequest(HttpClient httpclient, HttpRequestBase proxyingRequest) throws IOException {
        return httpclient.execute(proxyingRequest);
    }

    private void copyLocationHeaders(HttpResponse proxiedResponse, HttpServletResponse finalResponse) {
        for (Header locationHeader : proxiedResponse.getHeaders("Location")) {
            finalResponse.addHeader(locationHeader.getName(), locationHeader.getValue());
        }
    }

    private void adjustLocation(HttpServletRequest request, HttpResponse proxiedResponse, HttpServletResponse finalResponse) {
        if (logger.isDebugEnabled()) {
            logger.debug("adjustLocation called for request: " + request.getRequestURI());
        }
        String target = findMatchingTarget(request);

        if (logger.isDebugEnabled()) {
            logger.debug("adjustLocation found target: " + target + " for request: " + request.getRequestURI());
        }

        if (target == null) {
            copyLocationHeaders(proxiedResponse, finalResponse);
            return;
        }

        String baseURL = targets.get(target);
        URI baseURI = null;

        try {
            baseURI = new URI(baseURL);
        } catch (URISyntaxException e) {
            copyLocationHeaders(proxiedResponse, finalResponse);
            return;
        }

        for (Header locationHeader : proxiedResponse.getHeaders("Location")) {
            if (logger.isDebugEnabled()) {
                logger.debug("adjustLocation process header: " + locationHeader.getValue());
            }
            try {
                URI locationURI = new URI(locationHeader.getValue());
                URI resolvedURI = baseURI.resolve(locationURI);

                if (logger.isDebugEnabled()) {
                    logger.debug("Test location header: " + resolvedURI.toString() + " against: " + baseURI.toString());
                }
                if (resolvedURI.toString().startsWith(baseURI.toString())) {
                    // proxiedResponse.removeHeader(locationHeader);
                    String newLocation = "/" + target + "/" + resolvedURI.toString().substring(baseURI.toString().length());
                    finalResponse.addHeader("Location", newLocation);
                    // Header newLocationHeader = new BasicHeader("Location",
                    // newLocation);
                    if (logger.isDebugEnabled()) {
                        logger.debug("adjustLocation from: " + locationHeader.getValue() + " to " + newLocation);
                    }
                    // proxiedResponse.addHeader(newLocationHeader);
                } else {
                    finalResponse.addHeader(locationHeader.getName(), locationHeader.getValue());
                }
            } catch (URISyntaxException e) {
                finalResponse.addHeader(locationHeader.getName(), locationHeader.getValue());
            }
        }

    }

    /**
     * Direct copy of response
     */
    private void doHandleRequest(HttpServletRequest request, HttpServletResponse finalResponse, RequestType requestType, HttpResponse proxiedResponse)
            throws IOException {

        org.apache.http.StatusLine statusLine = proxiedResponse.getStatusLine();

        int statusCode = statusLine.getStatusCode();
        finalResponse.setStatus(statusCode);
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
        URI uri = new URI(url.getProtocol(),
                url.getUserInfo(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                null, // Don't use query part because URI constructor will try to double encode it
                // (query part is already encoded in sURL)
                url.getRef());

        // Reconstruct URL with encoded path from URI class and others parameters from URL class
        StringBuilder rawUrl = new StringBuilder(url.getProtocol() + "://" + url.getHost());

        if(url.getPort() != -1)
            rawUrl.append(":" + String.valueOf(url.getPort()));

        rawUrl.append(uri.getRawPath()); // Use encoded version from URI class

        if(url.getQuery() != null)
            rawUrl.append("?" + url.getQuery()); // Use already encoded query part

        return new URI(rawUrl.toString());
    }

    private HttpRequestBase makeRequest(HttpServletRequest request, RequestType requestType, String sURL) throws IOException {
        HttpRequestBase targetRequest;
        try {
            // Split URL
            URL url = new URL(sURL);
            URI uri = buildUri(url);

            switch (requestType) {
            case GET: {
                logger.debug("New request is: " + sURL + "\nRequest is GET");

                HttpGet get = new HttpGet(uri);
                targetRequest = get;
                break;
            }
            case POST: {
                logger.debug("New request is: " + sURL + "\nRequest is POST");

                HttpPost post = new HttpPost(uri);
                HttpEntity entity;
                request.setCharacterEncoding("UTF8");
                logger.debug("Post is recognized as a form post.");
                List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                    String name = (String) e.nextElement();
                    String[] v = request.getParameterValues(name);
                    for (String value : v) {
                        NameValuePair nv = new BasicNameValuePair(name, value);
                        parameters.add(nv);
                    }
                }
                String charset = request.getCharacterEncoding();
                try {
                    Charset.forName(charset);
                } catch (Throwable t) {
                    charset = null;
                }
                if (charset == null) {
                    charset = defaultCharset;
                }
                entity = new UrlEncodedFormEntity(parameters, charset);
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
            default: {
                String msg = requestType + " not yet supported";
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

    /**
     * For certain requests (OGC Web services mainly), the charset is absolutely
     * required. So for certain content types (xml-based normally) this method
     * is called to detect the charset of the data. This method is a slow way of
     * transferring data, so data of any significant size should not enter this
     * method.
     */
    private void doHandleRequestCharsetRequired(HttpServletRequest orignalRequest, HttpServletResponse finalResponse, RequestType requestType,
            HttpResponse proxiedResponse, String contentType) {

        InputStream streamFromServer = null;
        OutputStream streamToClient = null;

        try {

            /*
             * Here comes the tricky part because some host send files without
             * the charset in the header, therefore we do not know how they are
             * text encoded. It can result in serious issues on IE browsers when
             * parsing those files. There is a workaround which consists to read
             * the encoding within the file. It is made possible because this
             * proxy mainly forwards xml files. They all have the encoding
             * attribute in the first xml node.
             *
             * This is implemented as follows:
             *
             * A. The content type provides a charset: Nothing special, just
             * send back the stream to the client B. There is no charset
             * provided: The encoding has to be extracted from the file. The
             * file is read in ASCII, which is common to many charsets, like
             * that the encoding located in the first not can be retrieved. Once
             * the charset is found, the content-type header is overridden and
             * the charset is appended.
             *
             * /!\ Special case: whenever data are compressed in gzip/deflate
             * the stream has to be uncompressed and re-compressed
             */

            boolean isCharsetKnown = proxiedResponse.getEntity().getContentType().getValue().toLowerCase().contains("charset");
            // String contentEncoding =
            // getContentEncoding(proxiedResponse.getAllHeaders());
            String contentEncoding = getContentEncoding(proxiedResponse.getHeaders("Content-Encoding"));

            if (logger.isDebugEnabled()) {

                String cskString = "\tisCharSetKnown=" + isCharsetKnown;
                String cEString = "\tcontentEncoding=" + contentEncoding;
                logger.debug("Charset is required so verifying that it has been added to the headers\n" + cskString + "\n" + cEString);
            }

            if (contentEncoding == null || isCharsetKnown) {
                // A simple stream can do the job for data that is not in
                // content encoded
                // but also for data content encoded with a known charset
                streamFromServer = proxiedResponse.getEntity().getContent();
                streamToClient = finalResponse.getOutputStream();
            } else if (!isCharsetKnown && ("gzip".equalsIgnoreCase(contentEncoding) || "x-gzip".equalsIgnoreCase(contentEncoding))) {
                // the charset is unknown and the data are compressed in gzip
                // we add the gzip wrapper to be able to read/write the stream
                // content
                streamFromServer = new GZIPInputStream(proxiedResponse.getEntity().getContent());
                streamToClient = new GZIPOutputStream(finalResponse.getOutputStream());
            } else if ("deflate".equalsIgnoreCase(contentEncoding) && !isCharsetKnown) {
                // same but with deflate
                streamFromServer = new DeflaterInputStream(proxiedResponse.getEntity().getContent());
                streamToClient = new DeflaterOutputStream(finalResponse.getOutputStream());
            } else {
                doHandleRequest(orignalRequest, finalResponse, requestType, proxiedResponse);
                return;
            }

            byte[] buf = new byte[1024]; // read maximum 1024 bytes
            int len; // number of bytes read from the stream
            boolean first = true; // helps to find the encoding once and only
                                  // once
            String s = ""; // piece of file that should contain the encoding
            while ((len = streamFromServer.read(buf)) > 0) {

                if (first && !isCharsetKnown) {
                    // charset is unknown try to find it in the file content
                    for (int i = 0; i < len; i++) {
                        s += (char) buf[i]; // get the beginning of the file as
                                            // ASCII
                    }
                    // s has to be long enough to contain the encoding
                    if (s.length() > 200) {

                        if (logger.isTraceEnabled()) {
                            logger.trace("attempting to read charset from: " + s);
                        }
                        String charset = getCharset(s); // extract charset

                        if (charset == null) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("unable to find charset from raw ASCII data.  Trying to unzip it");
                            }

                            // the charset cannot be found, IE users must be
                            // warned
                            // that the request cannot be fulfilled, nothing
                            // good would happen otherwise
                        }
                        if (charset == null) {
                            String guessedCharset = null;
                            if (logger.isDebugEnabled()) {
                                logger.debug("unable to find charset so using the first one from the accept-charset request header");
                            }
                            String calculateDefaultCharset = calculateDefaultCharset(orignalRequest);
                            if (calculateDefaultCharset != null) {
                                guessedCharset = calculateDefaultCharset;
                                if (logger.isDebugEnabled()) {
                                    logger.debug("hopefully the server responded with this charset: " + calculateDefaultCharset);
                                }
                            } else {
                                guessedCharset = defaultCharset;
                                if (logger.isDebugEnabled()) {
                                    logger.debug("unable to find charset, so using default:" + defaultCharset);
                                }
                            }
                            String adjustedContentType = proxiedResponse.getEntity().getContentType().getValue() + ";charset=" + guessedCharset;
                            finalResponse.setHeader("Content-Type", adjustedContentType);
                            first = false; // we found the encoding, don't try
                                           // to do it again
                            finalResponse.setCharacterEncoding(guessedCharset);

                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("found charset: " + charset);
                            }
                            String adjustedContentType = proxiedResponse.getEntity().getContentType().getValue() + ";charset=" + charset;
                            finalResponse.setHeader("Content-Type", adjustedContentType);
                            first = false; // we found the encoding, don't try
                                           // to do it again
                            finalResponse.setCharacterEncoding(charset);
                        }
                    }
                }

                // for everyone, the stream is just forwarded to the client
                streamToClient.write(buf, 0, len);
            }

        } catch (IOException e) {
            // connection problem with the host
            e.printStackTrace();
        } finally {
            IOException exc = close(streamFromServer);
            exc = close(streamToClient, exc);
            if (exc != null) {
                logger.error("Error closing streams", exc);
            }
        }
    }

    private String calculateDefaultCharset(HttpServletRequest originalRequest) {
        String acceptCharset = originalRequest.getHeader("accept-charset");

        String calculatedCharset = null;

        if (acceptCharset != null) {
            calculatedCharset = acceptCharset.split(",")[0];
        }

        return calculatedCharset;
    }

    private IOException close(Closeable stream, IOException... previousExceptions) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            if (previousExceptions.length > 0) {
                return previousExceptions[0];
            }
            return e;
        }
        if (previousExceptions.length > 0) {
            return previousExceptions[0];
        }
        return null;
    }

    /**
     * Extract the encoding from a string which is the header node of an xml
     * file
     *
     * @param header
     *            String that should contain the encoding attribute and its
     *            value
     * @return the charset. null if not found
     */
    private String getCharset(String header) {
        Pattern pattern = null;
        String charset = null;
        try {
            // use a regexp but we could also use string functions such as
            // indexOf...
            pattern = Pattern.compile("encoding=(['\"])([A-Za-z]([A-Za-z0-9._]|-)*)");
        } catch (Exception e) {
            throw new RuntimeException("expression syntax invalid");
        }

        Matcher matcher = pattern.matcher(header);
        if (matcher.find()) {
            String encoding = matcher.group();
            charset = encoding.split("['\"]")[1];
        }

        return charset;
    }

    /**
     * Gets the encoding of the content sent by the remote host: extracts the
     * content-encoding header
     *
     * @param headers
     *            headers of the HttpURLConnection
     * @return null if not exists otherwise name of the encoding (gzip,
     *         deflate...)
     */
    private String getContentEncoding(Header[] headers) {
        if (headers == null || headers.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No content-encoding header for this request.");
            }
            return null;
        }
        for (Header header : headers) {
            // Header header = headers[i];
            String headerName = header.getName();
            if (logger.isDebugEnabled()) {
                logger.debug("Check content-encoding against header: " + headerName + " : " + header.getValue());
            }
            if (headerName != null && "Content-Encoding".equalsIgnoreCase(headerName)) {
                return header.getValue();
            }
        }

        return null;
    }

    /**
     * Check if the content type is accepted by the proxy
     *
     * @param contentType
     * @return true: valid; false: not valid
     */
    protected boolean isCharsetRequiredForContentType(final String contentType) {
        if (contentType == null) {
            return false;
        }
        // focus only on type, not on the text encoding
        String type = contentType.split(";")[0];
        for (String validTypeContent : requireCharsetContentTypes) {
            logger.debug(contentType + " vs " + validTypeContent + "=" + (validTypeContent.equalsIgnoreCase(type)));
            if (validTypeContent.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private String[] filter(String[] one) {
        ArrayList<String> result = new ArrayList<String>();

        for (String string : one) {
            if (string.length() > 0) {
                result.add(string);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Check to see if the call is recursive based on forwardRequestURI
     * startsWith contextPath
     */
    private boolean isRecursiveCallToProxy(String forwardRequestURI, String contextPath) {
        String[] one = forwardRequestURI.split("/");
        String[] two = contextPath.split("/");

        one = filter(one);
        two = filter(two);

        if (one.length < two.length) {
            return false;
        }

        boolean match = true;
        for (int i = 0; i < two.length && i < one.length; i++) {
            String s2 = two[i];
            String s1 = one[i];

            match &= s2.equalsIgnoreCase(s1);
        }
        return match;
    }

    public void setDefaultTarget(String defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public void setTargets(Map<String, String> targets) {
        this.targets = targets;
    }

    public void setContextpath(String contextpath) {
        // this.contextpath = contextpath;
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
            throw new IllegalArgumentException(defaultCharset + " is not supporte by current JVM");
        }
        this.defaultCharset = defaultCharset;
    }

    /**
     *
     * @param redirectStrategy
     */
    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    public void setProxyPermissionsFile(String proxyPermissionsFile) {
        this.proxyPermissionsFile = proxyPermissionsFile;
    }

    public void setProxyPermissions(Permissions proxyPermissions) throws UnknownHostException {
        this.proxyPermissions = proxyPermissions;
        this.proxyPermissions.init();
    }

    public Permissions getProxyPermissions() {
        return proxyPermissions;
    }

}
