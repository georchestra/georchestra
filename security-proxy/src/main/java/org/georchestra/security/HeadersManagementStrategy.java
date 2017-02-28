/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

import static org.georchestra.security.HeaderNames.ACCEPT_ENCODING;
import static org.georchestra.security.HeaderNames.CONTENT_LENGTH;
import static org.georchestra.security.HeaderNames.COOKIE_ID;
import static org.georchestra.security.HeaderNames.HOST;
import static org.georchestra.security.HeaderNames.LOCATION;
import static org.georchestra.security.HeaderNames.REFERER_HEADER_NAME;
import static org.georchestra.security.HeaderNames.SEC_PROXY;
import static org.georchestra.security.HeaderNames.SEC_ROLES;
import static org.georchestra.security.HeaderNames.SEC_USERNAME;
import static org.georchestra.security.HeaderNames.TRANSFER_ENCODING;
import static org.georchestra.security.HeaderNames.PROTECTED_HEADER_PREFIX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * A strategy for copying headers from the request to the proxied request and
 * the same for the response headers.
 *
 * @author jeichar
 */
public class HeadersManagementStrategy {
    protected static final Log logger = LogFactory.getLog(Proxy.class.getPackage().getName());

    /**
     * If true (default is false) AcceptEncoding headers are removed from request headers
     */
    private boolean noAcceptEncoding = false;
    private List<HeaderProvider> headerProviders = Collections.emptyList();
    private List<HeaderFilter> filters = new ArrayList<HeaderFilter>(1);
    private String referer = null;

    @Autowired
    private GeorchestraConfiguration georchestraConfiguration;

    public HeadersManagementStrategy() {
        filters.add(new SecurityRequestHeaderFilter());
    }

    public void init() {
        if ((georchestraConfiguration != null) && (georchestraConfiguration.activated())) {
            referer = georchestraConfiguration.getProperty("public.host");
        }
    }

    /**
     * Copies the request headers from the original request to the proxy request.  It may modify the
     * headers slightly
     */
    @SuppressWarnings("unchecked")
    public synchronized void configureRequestHeaders(HttpServletRequest originalRequest, HttpRequestBase proxyRequest) {
        Enumeration<String> headerNames = originalRequest.getHeaderNames();
        String headerName = null;

        StringBuilder headersLog = new StringBuilder("Request Headers:\n");
        headersLog
                .append("==========================================================\n");
        if (referer != null) {
            addHeaderToRequestAndLog(proxyRequest, headersLog, REFERER_HEADER_NAME, this.referer);
        }
        while (headerNames.hasMoreElements()) {
            headerName = headerNames.nextElement();
            if (headerName.compareToIgnoreCase(CONTENT_LENGTH) == 0) {
                continue;
            }
            if (headerName.equalsIgnoreCase(COOKIE_ID)) {
                continue;
            }
            if (filter(originalRequest, headerName, proxyRequest)) {
                continue;
            }
            if (noAcceptEncoding && headerName.equalsIgnoreCase(ACCEPT_ENCODING)) {
                continue;
            }
            if (headerName.equalsIgnoreCase(HOST)) {
                continue;
            }
            // Don't forward 'sec-*' headers, those headers must be managed by security-proxy
            if(headerName.toLowerCase().startsWith(PROTECTED_HEADER_PREFIX)){
                continue;
            }
            if (referer != null && headerName.equalsIgnoreCase(REFERER_HEADER_NAME)) {
                continue;
            }
            String value = originalRequest.getHeader(headerName);
            addHeaderToRequestAndLog(proxyRequest, headersLog, headerName, value);
        }
        // see https://github.com/georchestra/georchestra/issues/509:
        addHeaderToRequestAndLog(proxyRequest, headersLog, SEC_PROXY, "true");

        handleRequestCookies(originalRequest, proxyRequest, headersLog);
        HttpSession session = originalRequest.getSession();

        for (HeaderProvider provider : headerProviders) {
            for (Header header : provider.getCustomRequestHeaders(session, originalRequest)) {

//                if(session.getAttribute("pre-auth") != null && provider instanceof SecurityRequestHeaderProvider)
//                    continue;
                logger.debug("Processing  header : " + header.getName() + " from " + provider.getClass().toString());


                if ((header.getName().equalsIgnoreCase(SEC_USERNAME) ||
                        header.getName().equalsIgnoreCase(SEC_ROLES)) &&
                        proxyRequest.getHeaders(header.getName()) != null &&
                        proxyRequest.getHeaders(header.getName()).length > 0 &&
                        (session.getAttribute("pre-auth") == null)) {
                    Header[] originalHeaders = proxyRequest.getHeaders(header.getName());
                    for (Header originalHeader : originalHeaders) {
                        headersLog.append("\t" + originalHeader.getName());
                        headersLog.append("=");
                        headersLog.append(originalHeader.getValue());
                        headersLog.append("\n");
                    }
                } else {
                    logger.debug("Adding header to proxyed request : " + header.getName() + "=" + header.getValue());
                    proxyRequest.removeHeaders(header.getName());
                    proxyRequest.addHeader(header);
                    headersLog.append("\t" + header.getName());
                    headersLog.append("=");
                    headersLog.append(header.getValue());
                    headersLog.append("\n");
                }
            }
        }

        headersLog
                .append("==========================================================");

        logger.trace(headersLog.toString());
    }

    private void addHeaderToRequestAndLog(HttpRequestBase proxyRequest, StringBuilder headersLog, String headerName, String value) {
        proxyRequest.addHeader(new BasicHeader(headerName, value));
        headersLog.append("\t" + headerName);
        headersLog.append("=");
        headersLog.append(value);
        headersLog.append("\n");
    }

    private void handleRequestCookies(HttpServletRequest originalRequest, HttpRequestBase proxyRequest,
                                      StringBuilder headersLog) {

        Enumeration<String> headers = originalRequest.getHeaders(COOKIE_ID);
        StringBuilder cookies = new StringBuilder();
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            for(String requestCookies : value.split(";")) {
                String trimmed = requestCookies.trim();
                if(trimmed.length() > 0) {
                    if(!trimmed.startsWith(HeaderNames.JSESSION_ID)) {
                        if(cookies.length() > 0) cookies.append("; ");
                        cookies.append(trimmed);
                    }
                }
            }
        }
        HttpSession session = originalRequest.getSession();
        String requestPath = proxyRequest.getURI().getPath();
        if(session != null && session.getAttribute(HeaderNames.JSESSION_ID)!=null) {
            Map<String,String> jessionIds = (Map) session.getAttribute(HeaderNames.JSESSION_ID);
            String currentPath = null;
            String currentId = null;
            for (String path : jessionIds.keySet()) {
                // see https://www.owasp.org/index.php/HttpOnly
                // removing extra suffixes for JSESSIONID cookie ("; HttpOnly")
                // This is related to some issues with newer versions of tomcat
                // and session loss, e.g.:
                // https://github.com/georchestra/georchestra/pull/913
                String actualPath  = path.split(";")[0].trim();

                // the cookie we will use is the cookie with the longest matching path
                if(requestPath.startsWith(actualPath)) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Found possible matching JSessionId: Path = "+actualPath+" id="+jessionIds.get(path)+" for "+requestPath+" of uri "+proxyRequest.getURI());
                    }
                    if(currentPath==null || currentPath.length()<actualPath.length()) {
                        currentPath=actualPath;
                        currentId = jessionIds.get(path);
                    }
                }
            }
            if(currentPath!=null) {
                if(cookies.length() > 0) cookies.append("; ");
                cookies.append(currentId);
            }
        }

        headersLog.append("\t" + COOKIE_ID);
        headersLog.append("=");
        headersLog.append(cookies);
        headersLog.append("\n");

        proxyRequest.addHeader(new BasicHeader(COOKIE_ID, cookies.toString()));

    }

    private boolean filter(HttpServletRequest originalRequest, String headerName, HttpRequestBase proxyRequest) {
        for (HeaderFilter filter : filters) {
            if(filter.filter(headerName, originalRequest, proxyRequest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copy headers from the proxy response to the final response
     */
    public synchronized void copyResponseHeaders(HttpServletRequest originalRequest, String originalRequestURI, HttpResponse proxyResponse, HttpServletResponse finalResponse, Map<String,String> proxyTargets) {
        HttpSession session = originalRequest.getSession(true);

        StringBuilder headersLog = new StringBuilder("Response Headers:\n");
        headersLog
                .append("==========================================================\n");

        // Set Response headers
        for (Header header : proxyResponse.getAllHeaders()) {
            headersLog.append("\t");
            if (header.getName().equalsIgnoreCase(HeaderNames.SET_COOKIE_ID)) {
                continue;
            } else if (LOCATION.equalsIgnoreCase(header.getName())) {
//            	DO NOTHING
//            	Handle in Proxy.java
//            	if (logger.isDebugEnabled()) {
//            		logger.debug("handle location header: " + header.getValue());
//            	}
//            	Header locationHeader = handleLocation(originalRequest, header, proxyTargets);
//            	finalResponse.addHeader(locationHeader.getName(), locationHeader.getValue());
			} else if (defaultIgnores(header)){
                headersLog.append("-- IGNORING -- ");
            } else {
                finalResponse.addHeader(header.getName(), header.getValue());
            }
            headersLog.append(header.getName());
            headersLog.append("=");
            headersLog.append(header.getValue());
            headersLog.append("\n");
        }

        for(HeaderProvider provider : headerProviders) {
            for (Header header : provider.getCustomResponseHeaders()) {
                finalResponse.addHeader(header.getName(), header.getValue());
                headersLog.append("\t" + header.getName());
                headersLog.append("=");
                headersLog.append(header.getValue());
                headersLog.append("\n");
            }
        }

        Header[] cookieHeaders = proxyResponse.getHeaders(HeaderNames.SET_COOKIE_ID);
        if(cookieHeaders!=null) {
            handleResponseCookies(originalRequestURI, finalResponse, cookieHeaders, session,headersLog);
        }

        headersLog
                .append("==========================================================\n");

        if (logger.isTraceEnabled()) {
        	logger.trace(headersLog.toString());
        }
    }

    private Header handleLocation(HttpServletRequest request, Header locationHeader, Map<String,String> proxyTargets) {
    	String locationValue = null;
    	for (String proxyTargetKey : proxyTargets.keySet()) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("Test proxyTarget: " + proxyTargets.get(proxyTargetKey) + " against: " + locationHeader.getValue());
    		}
    		if (locationHeader.getValue().startsWith(proxyTargets.get(proxyTargetKey))) {
    			locationValue = "/" + proxyTargetKey + "/" + locationHeader.getValue().substring(proxyTargets.get(proxyTargetKey).length());
    			if (logger.isDebugEnabled()) {
    				logger.debug("Adjust location header on redirection from: " + locationHeader.getValue() + " to: " + locationValue);
    			}
    			Header newLocationHeader = new BasicHeader(locationHeader.getName(), locationValue);
    			return newLocationHeader;
    		}
    		else {
    			String newLocation = sanitizeLocation(request, locationHeader.getValue(), proxyTargets);
    			if (!locationHeader.getValue().equals(newLocation)) {
    				if (logger.isDebugEnabled()) {
        				logger.debug("Adjust location header on redirection from: " + locationHeader.getValue() + " to: " + newLocation);
        			}

    				Header newLocationHeader = new BasicHeader(locationHeader.getName(), newLocation);
        			return newLocationHeader;
    			}
    		}
    	}

    	return locationHeader;
    }

    private String sanitizeLocation(HttpServletRequest request, String location, Map<String,String> targets) {
    	if (location.startsWith("/")) {
    		String [] requestPath = StringUtils.split(location.substring(1), "/");
    		if (logger.isDebugEnabled()) {
    			if (requestPath.length > 0)
    				logger.debug("Sanitize location: " + requestPath[0]);
    		}
    		if (requestPath.length > 0 && targets.containsKey(requestPath[0])) {
    			requestPath[0] = targets.get(requestPath[0]);
    			return StringUtils.arrayToDelimitedString(requestPath, "/");
    		}
    	}

    	return location;
    }


    private void handleResponseCookies(String originalRequestURI, HttpServletResponse finalResponse, Header[] headers, HttpSession session, StringBuilder headersLog) {
        String originalPath = originalRequestURI.substring("/sec/".length()).split("/")[0];
        for (Header header : headers) {
            String[] parts = header.getValue().split("(?i)Path=",2);

            StringBuilder cookies = new StringBuilder();
            for (String cookie : parts[0].split(";")) {
                if(cookie.trim().length() == 0) {
                    continue;
                }
                if(cookie.trim().startsWith(HeaderNames.JSESSION_ID)) {
                    String path = "";
                    if(parts.length == 2) {
                        path = parts[1];
                    }
                    storeJsessionHeader(session, path.trim(), cookie,headersLog);
                } else {
                    if(cookies.length()>0) cookies.append("; ");
                    cookies.append(cookie);
                }
            }

            if(cookies.length() > 0) {
                cookies.append("; Path= /" + originalPath);
                finalResponse.addHeader(HeaderNames.SET_COOKIE_ID, cookies.toString());
                headersLog.append("\t" + HeaderNames.SET_COOKIE_ID);
                headersLog.append("=");
                headersLog.append(cookies);
                headersLog.append("\n");
            }

        }
    }

    private void storeJsessionHeader(HttpSession session, String path, String cookie, StringBuilder headersLog) {
        Map<String,String> map = (Map<String, String>) session.getAttribute(HeaderNames.JSESSION_ID);
        if(map==null) {
            map = new HashMap<String,String>();
            session.setAttribute(HeaderNames.JSESSION_ID, map);
        }
        if(path.length() > 0) {
            // clean out session IDs with longer path since this should supercede them
            for (String key : new HashMap<String,String>(map).keySet()) {
                if(key.startsWith(path)) {
                    map.remove(key);
                }
            }

        }
        map.put(path,cookie);

        headersLog.append("\tStoring JSESSION cookie ");
        headersLog.append(cookie);
        headersLog.append(" for path ");
        headersLog.append(path);
        headersLog.append("\n");

    }

    private boolean defaultIgnores(Header header) {
        boolean transferEncoding = header.getName().equalsIgnoreCase(TRANSFER_ENCODING) && header.getValue().equalsIgnoreCase(HeaderNames.CHUNKED);

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

    public void setReferer(String referer){
        this.referer = referer;
    }}
