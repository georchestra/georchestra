package org.georchestra.security;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.springframework.util.StringUtils;

/**
 * A strategy for copying headers from the request to the proxied request and
 * the same for the response headers
 * 
 * @author jeichar
 */
public class HeadersManagementStrategy {
    protected static final Log logger = LogFactory.getLog(Proxy.class.getPackage().getName());
    private static final String JSESSION_ID = "JSESSIONID";
    private static final String SET_COOKIE_ID ="Set-Cookie";
    private static final String COOKIE_ID ="Cookie";
    public static final String REFERER_HEADER_NAME = "referer";

    /**
     * If true (default is false) AcceptEncoding headers are removed from request headers
     */
    private boolean noAcceptEncoding = false;
    private List<HeaderProvider> headerProviders = Collections.emptyList(); 
    private List<HeaderFilter> filters = Collections.emptyList();
    private String referer = null;

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
            if (headerName.compareToIgnoreCase("content-length") == 0) {
                continue;
            }
            if (headerName.equalsIgnoreCase(COOKIE_ID)) {
                continue;
            }
            if (filter(originalRequest, headerName, proxyRequest)) {
                continue;
            }
            if (noAcceptEncoding && headerName.equalsIgnoreCase("Accept-Encoding")) {
                continue;
            }
            if (headerName.equalsIgnoreCase("host")) {
                continue;
            }
            if (referer != null && headerName.equalsIgnoreCase(REFERER_HEADER_NAME)) {
                continue;
            }
            if (headerName.equalsIgnoreCase("sec-username") ||
                headerName.equalsIgnoreCase("sec-roles")) {
                continue;
            }
            
            String value = originalRequest.getHeader(headerName);
            addHeaderToRequestAndLog(proxyRequest, headersLog, headerName, value);
        }
        // see https://github.com/georchestra/georchestra/issues/509:
        addHeaderToRequestAndLog(proxyRequest, headersLog, "sec-proxy", "true");

        handleRequestCookies(originalRequest, proxyRequest, headersLog);
        HttpSession session = originalRequest.getSession();

        for (HeaderProvider provider : headerProviders) {
            for (Header header : provider.getCustomRequestHeaders(session)) {
                if ((header.getName().equalsIgnoreCase("sec-username") ||
                     header.getName().equalsIgnoreCase("sec-roles")) &&
                    proxyRequest.getHeaders(header.getName()) != null &&
                    proxyRequest.getHeaders(header.getName()).length > 0) {
                    Header[] originalHeaders = proxyRequest.getHeaders(header.getName());
                    for (Header originalHeader : originalHeaders) {
                        headersLog.append("\t" + originalHeader.getName());
                        headersLog.append("=");
                        headersLog.append(originalHeader.getValue());
                        headersLog.append("\n");
                    }
                } else {
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
                    if(!trimmed.startsWith(JSESSION_ID)) {
                        if(cookies.length() > 0) cookies.append("; ");
                        cookies.append(trimmed);
                    }
                }
            }
        }
        HttpSession session = originalRequest.getSession();
        String requestPath = proxyRequest.getURI().getPath();
        if(session != null && session.getAttribute(JSESSION_ID)!=null) {
            Map<String,String> jessionIds = (Map) session.getAttribute(JSESSION_ID);
            String currentPath = null;
            String currentId = null;
            for (String path : jessionIds.keySet()) {
                // the cookie we will use is the cookie with the longest matching path
                if(requestPath.startsWith(path)) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Found possible matching JSessionId: Path = "+path+" id="+jessionIds.get(path)+" for "+requestPath+" of uri "+proxyRequest.getURI());
                    }
                    if(currentPath==null || currentPath.length()<path.length()) {
                        currentPath=path;
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
        session.setMaxInactiveInterval(Integer.MAX_VALUE);
        
        StringBuilder headersLog = new StringBuilder("Response Headers:\n");
        headersLog
                .append("==========================================================\n");

        // Set Response headers
        for (Header header : proxyResponse.getAllHeaders()) {
            headersLog.append("\t");
            if (header.getName().equalsIgnoreCase(SET_COOKIE_ID)) {
                continue;
            } else if ("location".equalsIgnoreCase(header.getName())) {
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

        Header[] cookieHeaders = proxyResponse.getHeaders(SET_COOKIE_ID);
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
    				logger.debug("Santize location: " + requestPath[0]);
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
                if(cookie.trim().startsWith(JSESSION_ID)) {
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
                finalResponse.addHeader(SET_COOKIE_ID, cookies.toString());
                headersLog.append("\t" + SET_COOKIE_ID);
                headersLog.append("=");
                headersLog.append(cookies);
                headersLog.append("\n");
            }

        }
    }

    private void storeJsessionHeader(HttpSession session, String path, String cookie, StringBuilder headersLog) {
        Map<String,String> map = (Map<String, String>) session.getAttribute(JSESSION_ID);
        if(map==null) {
            map = new HashMap<String,String>();
            session.setAttribute(JSESSION_ID, map);
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
        boolean transferEncoding = header.getName().equalsIgnoreCase("Transfer-Encoding") && header.getValue().equalsIgnoreCase("chunked");
        
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
