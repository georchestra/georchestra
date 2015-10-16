package org.georchestra.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.georchestra.ogcservstatistics.log4j.OGCServiceMessageFormatter;
import org.georchestra.security.healthcenter.DatabaseHealthCenter;
import org.georchestra.security.permissions.Permissions;
import org.georchestra.security.permissions.UriMatcher;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * This proxy provides an indirect access to a remote host to retrieve data.
 * Useful to overcome security constraints on client side.
 * <p>
 * There are two primary ways that the paths can be encoded:
 * <ul>
 * <li>The full url to forward to is encoded in a parameter called "url"</li>
 * <li>The url is encoded as part of the path<ul>
 *     <li>The first way is to forward to the default target server.  The fragment of the path after the context
 *         will be appended to the defaultTarget</li>
 *     <li>The second way is to define the targets. The segment after the context of this service will be the key for
 *         looking up the target server and the rest of the path will be appended to the target</li>
 *     </ul>
 *     Examples:
 *     <p>Assume the default target is http://xyz.com and the targets are: x:http://x.com, y:https://y.com</p>
 *     <ul>
 *        <li>http://this.com/context/path -- gives -- http://xyz.com/path</li>
 *        <li>http://this.com/context/x/path -- gives -- http://x.com/path</li>
 *        <li>http://this.com/context/y/path -- gives -- https://y.com/path</li>
 *     </ul>
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

    protected enum RequestType {
        GET, POST, DELETE, PUT, TRACE, OPTIONS, HEAD
    }

    /**
     * must be defined
     */
    private String                    defaultTarget;
    private Map<String, String>       targets = Collections.emptyMap();
    /**
     * must be defined
     */
    private HeadersManagementStrategy headerManagement             = new HeadersManagementStrategy();
    private FilterRequestsStrategy    strategyForFilteringRequests = new AcceptAllRequests();
    private List<String>              requireCharsetContentTypes   = Collections.emptyList();
    private String defaultCharset = "UTF-8";

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private Permissions proxyPermissions = new Permissions();
    private String proxyPermissionsFile;

    
    /*  ----------  Required for  DatabaseHealthCenter -------------------- */
    
    private static Boolean checkHealth = false;
    private String database;
    private String user;
    private String password;
    private Integer maxDatabaseConnections;

    public void init() throws IOException, ClassNotFoundException {
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
    }

    /*  ----------  start work around for no gateway option  -------------- */
    private Gateway gateway = new Gateway();

    @RequestMapping(value="/gateway", method={GET,POST} )
    public void gateway(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        gateway.loadCredentialsPage(request, response);
    }

    @RequestMapping(value="/testPage", method={GET} )
    public void testPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        gateway.testPage(response);
    }
    /*  ----------  end work around for no gateway option  -------------- */


    @RequestMapping(params="login", method={GET,POST} )
    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, URISyntaxException {
        String uri = request.getRequestURI();
        if(uri.startsWith("sec")) {
            uri=uri.substring(3);
        } else if(uri.startsWith("/sec")) {
            uri=uri.substring(4);
        }

        URIBuilder uriBuilder = new URIBuilder(uri);
        Enumeration parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements())
        {
            String paramName = (String)parameterNames.nextElement();
            if (!"login".equals(paramName)) {
                String[] paramValues = request.getParameterValues(paramName);
                for (int i = 0; i < paramValues.length; i++) {
                    uriBuilder.setParameter(paramName, paramValues[i]);
                }
            }
        }

        redirectStrategy.sendRedirect(request, response, uriBuilder.build().toString());
    }

    @RequestMapping(params={"login","url"}, method={GET,POST})
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws ServletException, IOException {
        redirectStrategy.sendRedirect(request, response, sURL);
    }

    // ----------------- Method calls where request is encoded in a url parameter of request ----------------- //
    @RequestMapping(params={"url","!login"}, method=RequestMethod.POST)
    public void handleUrlPOSTRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws
            IOException {
        handleUrlParamRequest(request, response, RequestType.POST, sURL);
    }

    @RequestMapping(params={"url","!login"}, method=RequestMethod.GET)
    public void handleUrlGETRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws
            IOException {
        handleUrlParamRequest(request, response, RequestType.GET, sURL);
    }
    @RequestMapping(params={"url","!login"}, method=RequestMethod.DELETE)
    public void handleUrlDELETERequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.DELETE, sURL);
    }
    @RequestMapping(params={"url","!login"}, method=RequestMethod.HEAD)
    public void handleUrlHEADRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws
            IOException {
        handleUrlParamRequest(request, response, RequestType.HEAD, sURL);
    }
    @RequestMapping(params={"url","!login"}, method=RequestMethod.OPTIONS)
    public void handleUrlOPTIONSRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.OPTIONS, sURL);
    }
    @RequestMapping(params={"url","!login"}, method=RequestMethod.PUT)
    public void handleUrlPUTRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws
            IOException {
        handleUrlParamRequest(request, response, RequestType.PUT, sURL);
    }
    @RequestMapping(params={"url","!login"}, method=RequestMethod.TRACE)
    public void handleUrlTRACERequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) throws IOException {
        handleUrlParamRequest(request, response, RequestType.TRACE, sURL);
    }

    private void handleUrlParamRequest(HttpServletRequest request, HttpServletResponse response, RequestType type, String sURL) throws
            IOException {
        if(request.getRequestURI().startsWith("/sec/proxy/")){
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
    private boolean isSameServer(HttpServletRequest request, URL url) throws UnknownHostException {
        return InetAddress.getByName(request.getServerName()).equals(InetAddress.getByName(url.getHost()));
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
        if(requestURI.charAt(0) == '/') {
            requestSegments = StringUtils.split(requestURI.substring(1), '/');
        } else {
            requestSegments = StringUtils.split(requestURI, '/');
        }
        return requestSegments;
    }

    /**
     * Since the URL param can access any url we need to control what it can request
     * so it is not used for nefarious purposes.  We are basing the control on contentType
     * because it is supposed to be able to access any server.
     */
    private void testLegalContentType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if(contentType==null){
            return ;
        }
                // focus only on type, not on the text encoding
        String type = contentType.split(";")[0];
        for (String validTypeContent : requireCharsetContentTypes) {
            if (!validTypeContent.equals(type)) {
                return ;
            }
        }
        throw new IllegalArgumentException("ContentType " + contentType + " is not permitted to be requested when the request is made through the URL parameter form.");
    }

    // ----------------- Method calls where request is encoded in path of request ----------------- //
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.GET)
    public void handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.GET);
    }
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.POST)
    public void handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.POST);
    }
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.DELETE)
    public void handleDELETERequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.DELETE);
    }
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.HEAD)
    public void handleHEADRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.HEAD);
    }
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.OPTIONS)
    public void handleOPTIONSRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.OPTIONS);
    }
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.PUT)
    public void handlePUTRequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.PUT);
    }
    @RequestMapping(params={"!url","!login"}, method=RequestMethod.TRACE)
    public void handleTRACERequest(HttpServletRequest request, HttpServletResponse response) {
        handlePathEncodedRequests(request, response, RequestType.TRACE);
    }

    // ----------------- Implementation methods ----------------- //

    private String buildForwardRequestURL(HttpServletRequest request) {
    	String forwardRequestURI = request.getRequestURI();

        String contextPath = request.getServletPath() + request.getContextPath();
        if (forwardRequestURI.length() <= contextPath.length()) {
            forwardRequestURI = "/";
        } else {
            forwardRequestURI = forwardRequestURI.substring(contextPath
                    .length());
        }

        forwardRequestURI = forwardRequestURI.replaceAll("//", "/");

        return forwardRequestURI;
    }

    /**
     * Main entry point for methods where the request path is encoded in the path of the URL
     */
    private void handlePathEncodedRequests(HttpServletRequest request, HttpServletResponse response, RequestType requestType) {
        try {
        	String contextPath = request.getServletPath() + request.getContextPath();
            String forwardRequestURI = buildForwardRequestURL(request);

            logger.debug("handlePathEncodedRequests: -- Handling Request: "+requestType+":"+forwardRequestURI+" from: "+request.getRemoteAddr());

            String sURL = findTarget(forwardRequestURI);

            if(sURL == null){
                response.sendError(404);
            }

            URL url;
            try {
                url = new URL(sURL);
            }catch (MalformedURLException e) {
                throw new MalformedURLException(sURL +" is not a valid URL");
            }

            if (isSameHostAndPort(request, url) && (isRecursiveCallToProxy(forwardRequestURI, contextPath)
                    || isRecursiveCallToProxy(url.getPath(), contextPath))) {
                response.sendError(403, forwardRequestURI + " is a recursive call to this service.  That is not a legal request");
            }

            if (request.getQueryString() != null && !isFormContentType(request)) {
                StringBuilder query = new StringBuilder("?");
                Enumeration paramNames = request.getParameterNames();
                while(paramNames.hasMoreElements()) {
                    String name = (String) paramNames.nextElement();
                    String[] values = request.getParameterValues(name);
                    for (String string : values) {
                        if(query.length() > 1) {
                            query.append('&');
                        }
                        query.append(name);
                        query.append('=');
                        query.append(URLEncoder.encode(string, "UTF-8"));
                    }
                }
                sURL += query;
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
        if(requestURI.charAt(0) == '/') {
            segments = requestURI.substring(1).split("/");
        } else {
            segments = requestURI.split("/");
        }

        if(segments.length == 0){
            return concat(defaultTarget, new StringBuilder(requestURI));
        }
        String target = targets.get(segments[0]);
        if(target==null){
            target=defaultTarget;
            return concat(defaultTarget, new StringBuilder(requestURI));
        } else {
            StringBuilder builder = new StringBuilder("/");
            for (int i = 1; i < segments.length; i++) {
                String segment = segments[i];
                builder.append(segment);
                if(i+1 < segments.length)
                    builder.append("/");
            }

            if (requestURI.endsWith("/") && builder.charAt(builder.length()-1) != '/') {
                builder.append('/');
            }

            return concat(target,builder);
        }
    }

    private String concat(String target, StringBuilder builder) {
        if(target == null){
            return null;
        }
        String target2 = target;
        if(target.endsWith("/")){
            target2=target.substring(0, target.length()-1);
        }
        if(builder.charAt(0) != '/'){
            builder.insert(0, '/');
        }
        return target2+builder;

    }

    private String findMatchingTarget(HttpServletRequest request) {
    	String requestURI = buildForwardRequestURL(request);
    	return findMatchingTarget(requestURI);
    }

    private String findMatchingTarget(String requestURI) {
        String[] segments = splitRequestPath(requestURI);
        
        if(segments.length == 0){
        	return null;
        }

        if (targets.containsKey(segments[0])){
        	return segments[0];
        } else {
        	return null;
        }
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse finalResponse, RequestType requestType, String sURL, boolean localProxy) {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter("http.socket.timeout", new Integer(300000));
        httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

        if( isCheckHealth() ){
            DatabaseHealthCenter.getInstance(this.database, this.user, this.password, Proxy.class.getSimpleName())
				.checkConnections(this.maxDatabaseConnections);
        }

        //
        // Handle http proxy for external request.
        // Proxy must be configured by system variables (e.g.: -Dhttp.proxyHost=proxy -Dhttp.proxyPort=3128)
        //
        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                     httpclient.getConnectionManager().getSchemeRegistry(),
                     ProxySelector.getDefault());

        ((DefaultHttpClient)httpclient).setRoutePlanner(routePlanner);

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

            HttpRequestBase proxyingRequest = makeRequest(request, requestType, sURL);
            headerManagement.configureRequestHeaders(request, proxyingRequest);

            try {
            	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            	Header [] originalHeaders = proxyingRequest.getHeaders("sec-org");
            	String org = "";
        		for (Header originalHeader : originalHeaders) {
        			org = originalHeader.getValue();
        		}
            	statsLogger.info(OGCServiceMessageFormatter.format(authentication.getName(), sURL, org));
            } catch (Exception e) {
            	logger.error("Unable to log the request into the statistics logger", e);
            }

            if (localProxy) {
		        //
		        // Hack for geoserver
		        // Should not be here. We must use a ProxyTarget class and define
		        // if Host header should be formwared or not.
		        //
		        request.getHeader("Host");
		        proxyingRequest.setHeader("Host", request.getHeader("Host"));

		        if (logger.isDebugEnabled()) {
		        	logger.debug("Host header set to: " + proxyingRequest.getFirstHeader("Host").getValue() + " for proxy request.");
		        }
            }

            HttpResponse proxiedResponse = executeHttpRequest(httpclient, proxyingRequest);

            org.apache.http.StatusLine statusLine = proxiedResponse.getStatusLine();

            int statusCode = statusLine.getStatusCode();

            String reasonPhrase = statusLine.getReasonPhrase();

            if (reasonPhrase != null && statusCode > 399) {
            	if (logger.isWarnEnabled()) {
            		logger.warn("Error occurred. statuscode: "+statusCode+", reason: "+reasonPhrase);
            	}

            	if (statusCode == 401) {
            		//
            		// Handle case of basic authentication.
            		//
            		Header authHeader = proxiedResponse.getFirstHeader("WWW-Authenticate");
            		finalResponse.setHeader("WWW-Authenticate", (authHeader == null) ? "Basic realm=\"Authentication required\"" : authHeader.getValue());
            	}

                // 403 and 404 are handled by specific JSP files provided by the security-proxy webapp
                if ((statusCode == 404) || (statusCode == 403)) {
                    finalResponse.sendError(statusCode);
                    return;
                }
            }

            headerManagement.copyResponseHeaders(request, request.getRequestURI(), proxiedResponse, finalResponse, this.targets);

            if (statusCode == 302 || statusCode == 301)
            	adjustLocation(request, proxiedResponse, finalResponse);

            // get content type

            String contentType = null;
            if (proxiedResponse.getEntity() != null && proxiedResponse.getEntity().getContentType() != null) {
                contentType = proxiedResponse.getEntity().getContentType().getValue();
                logger.debug("content-type detected: "+contentType);
            }

            // content type has to be valid
            if (isCharsetRequiredForContentType(contentType)) {
                doHandleRequestCharsetRequired(request, finalResponse, requestType, proxiedResponse, contentType);
            } else {
                logger.debug("charset not required for contentType: "+contentType);
                doHandleRequest(request, finalResponse, requestType, proxiedResponse);
            }
        } catch (IOException e) {
            // connection problem with the host
            e.printStackTrace();
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
	            	// Header newLocationHeader = new BasicHeader("Location", newLocation);
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("adjustLocation from: " + locationHeader.getValue() + " to " + newLocation);
	            	}
	            	// proxiedResponse.addHeader(newLocationHeader);
	            }
	            else {
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
    private void doHandleRequest(HttpServletRequest request, HttpServletResponse finalResponse, RequestType requestType,
            HttpResponse proxiedResponse) throws IOException {

        org.apache.http.StatusLine statusLine = proxiedResponse
                .getStatusLine();

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

    private HttpRequestBase makeRequest(HttpServletRequest request, RequestType requestType, String sURL) throws IOException {
        HttpRequestBase targetRequest;
        try {
            URI uri = new URI(sURL);
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
                if (isFormContentType(request)) {
                    logger.debug("Post is recognized as a form post.");
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                    for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
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
                    if(charset == null) {
                        charset = defaultCharset;
                    }
                    entity = new UrlEncodedFormEntity(parameters,charset);
                    post.setEntity(entity);

                } else {
                    logger.debug("Post is NOT recognized as a form post. (Not an error just a comment)");
                    int contentLength = request.getContentLength();
                    ServletInputStream inputStream = request.getInputStream();
                    entity = new InputStreamEntity(inputStream, contentLength);
                }
                post.setEntity(entity);
                targetRequest = post;
                break;
            }
            case TRACE: {
                logger.debug("New request is: " + sURL + "\nRequest is POST");

                HttpTrace post = new HttpTrace(uri);

                targetRequest = post;
                break;
            }
            case OPTIONS: {
                logger.debug("New request is: " + sURL + "\nRequest is POST");

                HttpOptions post = new HttpOptions(uri);

                targetRequest = post;
                break;
            }
            case HEAD: {
                logger.debug("New request is: " + sURL + "\nRequest is POST");

                HttpHead post = new HttpHead(uri);

                targetRequest = post;
                break;
            }
            case PUT: {
                logger.debug("New request is: " + sURL + "\nRequest is PUT");

                HttpPut put = new HttpPut(uri);

                put.setEntity(new InputStreamEntity(request.getInputStream(), request
                        .getContentLength()));

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

    private boolean isFormContentType(HttpServletRequest request) {
        if(request.getContentType() == null){
            return false;
        }
        String contentType = request.getContentType().split(";")[0].trim();

        boolean equalsIgnoreCase = "application/x-www-form-urlencoded".equalsIgnoreCase(contentType);
        return equalsIgnoreCase;
    }

    /**
     * For certain requests (OGC Web services mainly) the charset is absolutely
     * required. So for certain content types (xml based normally) this method
     * is called to detect the charset of the data. This method is a slow way of
     * transferring data so data of any significant size should not enter this
     * method.
     */
    private void doHandleRequestCharsetRequired(HttpServletRequest orignalRequest, HttpServletResponse finalResponse,
            RequestType requestType, HttpResponse proxiedResponse, String contentType) {

        InputStream streamFromServer = null;
        OutputStream streamToClient = null;

        try {

            /* Here comes the tricky part because some host send files without the charset
             * in the header, therefore we do not know how they are text encoded. It can result
             * in serious issues on IE browsers when parsing those files.
             * There is a workaround which consists to read the encoding within the file. It is made
             * possible because this proxy mainly forwards xml files. They all have the encoding
             * attribute in the first xml node.
             *
             * This is implemented as follows:
             *
             * A. The content type provides a charset:
             *     Nothing special, just send back the stream to the client
             * B. There is no charset provided:
             *     The encoding has to be extracted from the file.
             *     The file is read in ASCII, which is common to many charsets,
             *     like that the encoding located in the first not can be retrieved.
             *     Once the charset is found, the content-type header is overridden and the
             *     charset is appended.
             *
             *     /!\ Special case: whenever data are compressed in gzip/deflate the stream has to
             *     be uncompressed and compressed
             */

            boolean isCharsetKnown = proxiedResponse.getEntity().getContentType().getValue().toLowerCase().contains("charset");
            // String contentEncoding = getContentEncoding(proxiedResponse.getAllHeaders());
            String contentEncoding = getContentEncoding(proxiedResponse.getHeaders("Content-Encoding"));

            if(logger.isDebugEnabled()) {

                String cskString = "\tisCharSetKnown="+isCharsetKnown;
                String cEString = "\tcontentEncoding="+contentEncoding;
                logger.debug("Charset is required so verifying that it has been added to the headers\n"+cskString+"\n"+cEString);
            }

            if(contentEncoding == null || isCharsetKnown) {
                // A simple stream can do the job for data that is not in content encoded
                // but also for data content encoded with a known charset
                streamFromServer = proxiedResponse.getEntity().getContent();
                streamToClient = finalResponse.getOutputStream();
            }
            else if (!isCharsetKnown && ("gzip".equalsIgnoreCase(contentEncoding) || "x-gzip".equalsIgnoreCase(contentEncoding))) {
                // the charset is unknown and the data are compressed in gzip
                // we add the gzip wrapper to be able to read/write the stream content
                streamFromServer = new GZIPInputStream(proxiedResponse.getEntity().getContent());
                streamToClient = new GZIPOutputStream(finalResponse.getOutputStream());
            }
            else if("deflate".equalsIgnoreCase(contentEncoding) && !isCharsetKnown) {
                // same but with deflate
                streamFromServer = new DeflaterInputStream(proxiedResponse.getEntity().getContent());
                streamToClient = new DeflaterOutputStream(finalResponse.getOutputStream());
            } else {
                doHandleRequest(orignalRequest, finalResponse, requestType, proxiedResponse);
                return;
            }

            byte[] buf = new byte[1024]; // read maximum 1024 bytes
            int len;                     // number of bytes read from the stream
            boolean first = true;        // helps to find the encoding once and only once
            String s = "";               // piece of file that should contain the encoding
            while ((len = streamFromServer.read(buf)) > 0) {

                if (first && !isCharsetKnown) {
                    // charset is unknown try to find it in the file content
                    for(int i=0; i < len; i++) {
                        s += (char) buf[i]; // get the beginning of the file as ASCII
                    }
                    // s has to be long enough to contain the encoding
                    if(s.length() > 200) {

                       if(logger.isTraceEnabled()) {
                           logger.trace("attempting to read charset from: " + s);
                       }
                       String charset = getCharset(s); // extract charset

                        if (charset == null) {
                            if(logger.isTraceEnabled()) {
                               logger.trace("unable to find charset from raw ASCII data.  Trying to unzip it");
                            }

                            // the charset cannot be found, IE users must be warned
                            // that the request cannot be fulfilled, nothing good would happen otherwise
                        }
                        if(charset == null) {
                            String guessedCharset = null;
                            if(logger.isDebugEnabled()) {
                                logger.debug("unable to find charset so using the first one from the accept-charset request header");
                            }
                            String calculateDefaultCharset = calculateDefaultCharset(orignalRequest);
                            if (calculateDefaultCharset !=null ) {
                                guessedCharset = calculateDefaultCharset;
                                if(logger.isDebugEnabled()) {
                                    logger.debug("hopefully the server responded with this charset: "+calculateDefaultCharset);
                                }
                            } else {
                                guessedCharset = defaultCharset;
                                if(logger.isDebugEnabled()) {
                                    logger.debug("unable to find charset, so using default:"+defaultCharset);
                                }
                            }
                            String adjustedContentType = proxiedResponse.getEntity().getContentType().getValue() + ";charset=" + guessedCharset;
                            finalResponse.setHeader("Content-Type", adjustedContentType);
                            first = false; // we found the encoding, don't try to do it again
                            finalResponse.setCharacterEncoding(guessedCharset);

                        } else {
                            if(logger.isDebugEnabled()) {
                                logger.debug("found charset: "+charset);
                            }
                            String adjustedContentType = proxiedResponse.getEntity().getContentType().getValue() + ";charset=" + charset;
                            finalResponse.setHeader("Content-Type", adjustedContentType);
                            first = false; // we found the encoding, don't try to do it again
                            finalResponse.setCharacterEncoding(charset);
                        }
                    }
                }

                // for everyone, the stream is just forwarded to the client
                streamToClient.write(buf, 0, len);
            }

        }
        catch (IOException e) {
            // connection problem with the host
            e.printStackTrace();
        } finally {
            IOException exc = close(streamFromServer);
            exc = close(streamToClient, exc);
            if(exc!=null){
                logger.error("Error closing streams", exc);
            }
        }
    }

    private String calculateDefaultCharset(HttpServletRequest originalRequest) {
        String acceptCharset = originalRequest.getHeader("accept-charset");

        String calculatedCharset = null;

        if(acceptCharset !=null) {
            calculatedCharset = acceptCharset.split(",")[0];
        }

        return calculatedCharset;
    }

    private IOException close(Closeable stream, IOException... previousExceptions) {
        try {
            if(stream!=null){
                stream.close();
            }
        }catch (IOException e) {
            if( previousExceptions.length > 0) {
                return previousExceptions[0];
            }
            return e;
        }
        if( previousExceptions.length > 0) {
            return previousExceptions[0];
        }
        return null;
    }

    /**
     * Extract the encoding from a string which is the header node of an xml file
     * @param header String that should contain the encoding attribute and its value
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
     * @param headers headers of the HttpURLConnection
     * @return null if not exists otherwise name of the encoding (gzip, deflate...)
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
            if(headerName != null && "Content-Encoding".equalsIgnoreCase(headerName)) {
            	return header.getValue();
//                HeaderElement[] valuesList = header.getElements();
//                StringBuilder sBuilder = new StringBuilder();
//                for(HeaderElement headerElem : valuesList) {
//                    String headerVal = headerElem.getValue();
//                    if(headerVal != null)
//                        sBuilder.append(headerVal);
//                }
//
//                if(sBuilder.toString().trim().length() > 0) {
//                    return sBuilder.toString().toLowerCase();
//                } else {
//                    return null;
//                }
            }
        }

        return null;
    }

    /**
     * Check if the content type is accepted by the proxy
     * @param contentType
     * @return true: valid; false: not valid
     */
    protected boolean isCharsetRequiredForContentType(final String contentType) {
        if(contentType==null){
            return false;
        }
        // focus only on type, not on the text encoding
        String type = contentType.split(";")[0];
        for (String validTypeContent : requireCharsetContentTypes) {
            logger.debug(contentType+" vs "+validTypeContent+"="+(validTypeContent.equalsIgnoreCase(type)));
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
        return (String[]) result.toArray(new String[result.size()]);
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
    public void setTargets(Map<String,String> targets) {
        this.targets = targets;
    }
    public void setContextpath(String contextpath) {
//        this.contextpath = contextpath;
    }
    public void setHeaderManagement(HeadersManagementStrategy headerManagement) {
        this.headerManagement = headerManagement;
    }

    public void setDatabase(String database){
    	this.database = database;
    }

    public void setUser(String user){
        this.user = user;
    }


    public void setPassword(String password){
        this.password = password;
    }

    public void setMaxDatabaseConnections(Integer maxDatabaseConnections){
    	this.maxDatabaseConnections = maxDatabaseConnections;
    }

    public Boolean getCheckHealth() {
		return this.checkHealth;
	}

	public void setCheckHealth(boolean checkHealth) {
		this.checkHealth = checkHealth;
	}

    public boolean isCheckHealth() {
		return this.checkHealth.booleanValue();
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
            throw new IllegalArgumentException(defaultCharset+" is not supporte by current JVM");
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
