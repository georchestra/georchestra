/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

import static org.georchestra.commons.security.SecurityHeaders.SEC_PROXY;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.georchestra.security.HeaderNames.ACCEPT_ENCODING;
import static org.georchestra.security.HeaderNames.BASIC_AUTH_HEADER;
import static org.georchestra.security.HeaderNames.CONTENT_LENGTH;
import static org.georchestra.security.HeaderNames.COOKIE_ID;
import static org.georchestra.security.HeaderNames.HOST;
import static org.georchestra.security.HeaderNames.PROTECTED_HEADER_PREFIX;
import static org.georchestra.security.HeaderNames.REFERER_HEADER_NAME;
import static org.georchestra.security.HeaderNames.TRANSFER_ENCODING;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;

/**
 * A strategy for copying headers from the request to the proxied request and
 * the same for the response headers.
 *
 * @author jeichar
 */
public class HeadersManagementStrategy {
    protected static final Logger logger = LoggerFactory.getLogger(Proxy.class.getPackage().getName());

    /**
     * If true (default is false) AcceptEncoding headers are removed from request
     * headers
     */
    private boolean noAcceptEncoding = false;
    private List<HeaderProvider> headerProviders = Collections.emptyList();
    private List<HeaderFilter> filters = new ArrayList<HeaderFilter>(1);
    private String forcedReferer = null;

    public HeadersManagementStrategy() {
        filters.add(new SecurityRequestHeaderFilter());
    }

    /**
     * Copies the request headers from the original request to the proxy request. It
     * may modify the headers slightly
     * 
     * @param localProxy        true if the request targets a security-proxyfied
     *                          webapp (e.g. mapfishapp, ...), false otherwise
     * @param targetServiceName
     */
    public synchronized void configureRequestHeaders(HttpServletRequest originalRequest, HttpRequestBase proxyRequest,
            boolean localProxy, String targetServiceName) {

        final StringBuilder headersLog = logger.isTraceEnabled()
                ? new StringBuilder("Request Headers:\n==========================================================\n")
                : null;

        // see https://github.com/georchestra/georchestra/issues/509:
        addHeaderToRequestAndLog(proxyRequest, headersLog, SEC_PROXY, "true");
        if (!HeaderProvider.isPreAuthorized(originalRequest)) {
            addAllowedIncomingHeaders(originalRequest, proxyRequest, localProxy, headersLog);
        }

        if (localProxy) {
            handleRequestCookies(originalRequest, proxyRequest, headersLog);
            applyHeaderProviders(originalRequest, proxyRequest, headersLog, targetServiceName);
        } else if (forcedReferer != null) {
            addHeaderToRequestAndLog(proxyRequest, headersLog, REFERER_HEADER_NAME, this.forcedReferer);
        }

        if (logger.isTraceEnabled()) {
            headersLog.append("==========================================================");
            logger.trace(headersLog.toString());
        }
    }

    private void applyHeaderProviders(HttpServletRequest originalRequest, HttpRequestBase proxyRequest,
            StringBuilder headersLog, String targetServiceName) {

        final boolean preAuthorized = HeaderProvider.isPreAuthorized(originalRequest);
        Map<String, String> collectedHeaders = new HashMap<>();
        for (HeaderProvider provider : headerProviders) {

            // Don't include headers from security framework for request coming from trusted
            // proxy
            if (preAuthorized && !(provider instanceof TrustedProxyRequestHeaderProvider)) {
                logger.debug("Bypassing header provider : {}", provider.getClass().toString());
                continue;
            }
            Map<String, String> providerHeaders = provider.getCustomRequestHeaders(originalRequest, targetServiceName);
            collectedHeaders.putAll(providerHeaders);
        }

        collectedHeaders.forEach((headerName, headerValue) -> {
            // ignore Host and Content-Length header
            if (isOneOf(headerName, HOST, CONTENT_LENGTH)) {
                return;
            }

            if (isOneOf(headerName, SEC_USERNAME, SEC_ROLES) && proxyRequest.getFirstHeader(headerName) != null) {
                Header[] originalHeaders = proxyRequest.getHeaders(headerName);
                for (Header originalHeader : originalHeaders) {
                    addHeaderLog(headersLog, originalHeader.getName(), originalHeader.getValue());
                }
                return;
            }

            logger.debug("Adding header to proxied request: {} = {}", headerName, headerValue);
            proxyRequest.addHeader(headerName, headerValue);
            addHeaderLog(headersLog, headerName, headerValue);
        });
    }

    private boolean isOneOf(String header, String... names) {
        for (String name : names) {
            if (header.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void addAllowedIncomingHeaders(HttpServletRequest originalRequest, HttpRequestBase proxyRequest,
            boolean localProxy, StringBuilder headersLog) {
        for (String headerName : Collections.list(originalRequest.getHeaderNames())) {
            if (!ignoreIncomingHeader(headerName, originalRequest, proxyRequest, localProxy)) {
                String value = originalRequest.getHeader(headerName);
                addHeaderToRequestAndLog(proxyRequest, headersLog, headerName, value);
            }
        }
    }

    private boolean ignoreIncomingHeader(String headerName, HttpServletRequest originalRequest,
            HttpRequestBase proxyRequest, boolean localProxy) {

        if (headerName.compareToIgnoreCase(CONTENT_LENGTH) == 0) {
            return true;
        }
        if (headerName.equalsIgnoreCase(COOKIE_ID)) {
            return true;
        }
        if (filter(originalRequest, headerName, proxyRequest)) {
            return true;
        }
        if (noAcceptEncoding && headerName.equalsIgnoreCase(ACCEPT_ENCODING)) {
            return true;
        }
        // It is the HttpClient's lib duty to add this header accordingly.
        if (headerName.equalsIgnoreCase(TRANSFER_ENCODING)) {
            return true;
        }
        if (headerName.equalsIgnoreCase(HOST)) {
            return true;
        }
        // Don't forward basic auth
        if (headerName.equalsIgnoreCase(BASIC_AUTH_HEADER)) {
            return true;
        }
        // Don't forward 'sec-*' headers, those headers must be managed by
        // security-proxy
        if (headerName.toLowerCase().startsWith(PROTECTED_HEADER_PREFIX)) {
            return true;
        }
        if (!localProxy && forcedReferer != null && headerName.equalsIgnoreCase(REFERER_HEADER_NAME)) {
            return true;
        }
        return false;
    }

    private void addHeaderToRequestAndLog(HttpRequestBase proxyRequest, @Nullable StringBuilder headersLog,
            String headerName, String value) {
        logger.debug("Add Header: {} = {}", headerName, value);
        proxyRequest.addHeader(new BasicHeader(headerName, value));
        addHeaderLog(headersLog, headerName, value);
    }

    private void addHeaderLog(StringBuilder headersLog, String headerName, CharSequence value) {
        if (headersLog != null) {
            headersLog.append("\t" + headerName);
            headersLog.append("=");
            headersLog.append(value);
            headersLog.append("\n");
        }
    }

    /**
     * Handles the 'Cookie' header coming from the client.
     * 
     * This code gets all the possible values for the cookie sent by the browser,
     * and reconstructs a new one composed of every key=value except the JSESSIONID.
     * The reason is that the JSESSIONID sent by the browser could be different from
     * the one the SP is using to reach the proxified webapp. So we will inject the
     * expected JSESSIONID later on.
     * 
     * Note: we shall usually receive only one cookie header, but the HTTP specs
     * allows to send the same header key with different values multiple times,
     * hence the 'while' loop.
     *
     * @param originalRequest the request as it comes from the client
     * @param proxyRequest    the proxified request
     * @param headersLog      a stringbuilder used in case of debugging / tracing
     *                        loglevel.
     */
    @VisibleForTesting
    public void handleRequestCookies(HttpServletRequest originalRequest, HttpRequestBase proxyRequest,
            @Nullable StringBuilder headersLog) {

        Enumeration<String> headers = originalRequest.getHeaders(COOKIE_ID);
        StringBuilder cookies = new StringBuilder();

        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            for (String requestCookies : value.split(";")) {
                String trimmed = requestCookies.trim();
                if (trimmed.length() > 0) {
                    if (!trimmed.startsWith(HeaderNames.JSESSION_ID)) {
                        if (cookies.length() > 0)
                            cookies.append("; ");
                        cookies.append(trimmed);
                    }
                }
            }
        }
        HttpSession session = originalRequest.getSession();
        String requestPath = proxyRequest.getURI().getPath();
        if (session != null && session.getAttribute(HeaderNames.JSESSION_ID) != null) {
            Map<String, String> jessionIds = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
            String currentPath = null;
            String currentId = null;
            for (String path : jessionIds.keySet()) {
                // the cookie we will use is the cookie with the longest matching path
                if (requestPath.startsWith(path)) {
                    logger.debug("Found possible matching JSessionId: Path={} id={} for {} of uri {}", path,
                            jessionIds.get(path), requestPath, proxyRequest.getURI());
                    if (currentPath == null || currentPath.length() < path.length()) {
                        currentPath = path;
                        currentId = jessionIds.get(path);
                    }
                }
            }
            if (currentPath != null) {
                if (cookies.length() > 0)
                    cookies.append("; ");
                cookies.append(currentId);
            }
        }

        addHeaderLog(headersLog, COOKIE_ID, cookies);

        proxyRequest.addHeader(new BasicHeader(COOKIE_ID, cookies.toString()));

    }

    private boolean filter(HttpServletRequest originalRequest, String headerName, HttpRequestBase proxyRequest) {
        for (HeaderFilter filter : filters) {
            if (filter.filter(headerName, originalRequest, proxyRequest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copy headers from the proxy response to the final response
     */
    public synchronized void copyResponseHeaders(HttpServletRequest originalRequest, String originalRequestURI,
            HttpResponse proxyResponse, HttpServletResponse finalResponse, Map<String, String> proxyTargets) {
        HttpSession session = originalRequest.getSession(true);
        Collection<String> protectedHeaders = finalResponse.getHeaderNames();

        // Set Response headers
        for (Header header : proxyResponse.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase(HeaderNames.SET_COOKIE_ID)) {
                continue;
            } else if (defaultIgnores(header)) {
                continue;
            } else {
                // if the header is not in the servlet response, add it
                // else (header is already present in the servlet response),
                // make sure the value is erased by the one sent by the downstream webapp.
                //
                // The code voluntary misses the corner case where the header is already present
                // in the serlvet response but appears twice or more in the webapp response.
                if (!protectedHeaders.contains(header.getName())) {
                    finalResponse.addHeader(header.getName(), header.getValue());
                } else {
                    finalResponse.setHeader(header.getName(), header.getValue());
                }
            }
        }

        for (HeaderProvider provider : headerProviders) {
            for (Header header : provider.getCustomResponseHeaders()) {
                finalResponse.addHeader(header.getName(), header.getValue());
            }
        }

        Header[] cookieHeaders = proxyResponse.getHeaders(HeaderNames.SET_COOKIE_ID);
        if (cookieHeaders.length > 0) {
            handleResponseCookies(originalRequestURI, finalResponse, cookieHeaders, session);
        }
    }

    /**
     * Manages the "Set-Cookie" headers coming from security-proxified webapps.
     * 
     * If the cookie is a JSESSIONID, then we need to keep it into a map stored into
     * the current user's session, but not transmit it to the client, as there is
     * probably already a sessionid between the client and the SP. It will be reused
     * for later requests to the same proxified webapp.
     * 
     * In the other cases, it is probably safe to transmit it to the client, but we
     * force the path to the currently proxified webapp.
     *
     * If the cookie is transmitted to the client, the Path can be mangled to limit
     * its scope to the proxified webapp.
     *
     * @param originalRequestURI the request URI being made. We need it to force the
     *                           path of the cookies
     * @param finalResponse      the actual response sent to the client
     * @param headers            the array of "Set-Cookie" headers
     * @param session            the current user's session
     */
    @VisibleForTesting
    public void handleResponseCookies(String originalRequestURI, HttpServletResponse finalResponse, Header[] headers,
            HttpSession session) {
        String overridenPath = null;
        try {
            overridenPath = originalRequestURI.split("/")[1];
        } catch (Exception e) {
        }
        for (Header header : headers) {
            logger.debug("Parsing header: \"{}\"", header.toString());
            List<HttpCookie> currentCookies = HttpCookie.parse(header.getValue());
            // Normally, we should be only interested in the first element of the list.
            if (currentCookies.isEmpty()) {
                continue;
            }
            HttpCookie currentCookie = currentCookies.get(0);
            // if the cookie is a JSESSIONID, we need to store it in the session map,
            // but do not transmit it to the client, as it could disrupt the current
            // session between the user and the SP (if no path set, for instance).
            if (currentCookie.getName().equalsIgnoreCase(HeaderNames.JSESSION_ID)) {
                // Do not store the JSESSIONID in session if no path is provided
                // Note: by default, Java webapps seem to limit to their own scope.
                if (currentCookie.getPath() != null) {
                    logger.debug("Storing the JSESSIONID into session for path {}", currentCookie.getPath());
                    storeJsessionHeader(session, currentCookie.getPath(), currentCookie.toString());
                }
            }
            // Else, it has to be transmitted to the client
            else {
                String actualHeaderValue = header.getValue().trim();
                // if path is not set on the cookie and the overridden path does not point to /
                if (currentCookie.getPath() == null) {
                    if (overridenPath != null) {
                        logger.debug("Current cookie has not path set, forcing it to {}", overridenPath);
                        if (actualHeaderValue.endsWith(";")) {
                            actualHeaderValue.concat("Path=/" + overridenPath + ";");
                        } else {
                            actualHeaderValue.concat(";Path=/" + overridenPath + ";");
                        }
                    }
                }
                // a path is already set on the cookie, but we make sure to limit its scope
                // to the current proxified webapp
                else {
                    logger.debug("Current cookie {} has a path set to {}, forcing it to {}", currentCookie.getName(),
                            currentCookie.getPath(), overridenPath);
                    actualHeaderValue = actualHeaderValue.replaceAll("Path=[^;$]+", "Path=/" + overridenPath);
                }
                finalResponse.addHeader(HeaderNames.SET_COOKIE_ID, actualHeaderValue);
            }
        }
    }

    /**
     * Stores the JSESSION hashes in a Map, and saves it to the current user's
     * session. If the Map does not exist, it is initialized.
     * 
     * The map is saved under the key "JSESSIONID" of the session's attributes, and
     * the cookies are indexed with the path to the proxified webapps (as seen by
     * the SP, not by the client).
     *
     * If a session id for the given path already exists, or is longer than the one
     * provided as argument, it is removed from the map.
     *
     * @param session the current user's session
     * @param path    the cookie path
     * @param cookie  the cookie value, as JSESSIONID=session_id
     */
    private void storeJsessionHeader(HttpSession session, String path, String cookie) {
        Map<String, String> map = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
        if (map == null) {
            map = new HashMap<String, String>();
        }
        if (!StringUtils.isEmpty(path)) {
            // clean out session IDs with longer path since this should supersede them
            for (String key : new HashMap<String, String>(map).keySet()) {
                if (key.startsWith(path)) {
                    map.remove(key);
                }
            }
        }
        map.put(path, cookie);
        session.setAttribute(HeaderNames.JSESSION_ID, map);
    }

    private boolean defaultIgnores(Header header) {
        boolean transferEncoding = header.getName().equalsIgnoreCase(TRANSFER_ENCODING)
                && header.getValue().equalsIgnoreCase(HeaderNames.CHUNKED);

        return transferEncoding;
    }

    public void setNoAcceptEncoding(boolean noAcceptEncoding) {
        this.noAcceptEncoding = noAcceptEncoding;
    }

    public void setHeaderProviders(List<HeaderProvider> headerProviders) {
        this.headerProviders = headerProviders;
    }

    public void setFilters(List<HeaderFilter> filters) {
        this.filters = filters;
    }

    public void setReferer(String referer) {
        this.forcedReferer = referer;
    }
}
