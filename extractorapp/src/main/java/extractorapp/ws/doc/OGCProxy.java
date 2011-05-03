package extractorapp.ws.doc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletInputStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 
 * 
 * This proxy provides an indirect access to a remote host to retrieve data. Useful to overcome security constraints on client side.
 * It expects HTTP requests (GET for now) with a "url" parameter containing the real HTTP GET request. <br />
 * Can be accessed via .../ogcproxy/* depending on the url mapping defined  in web.xml and ws-servlet.xml <br />
 * OGCProxy is a Spring Controller. <br /> 
 * 
 * @author yoann.buch@gmail.com
 */
    

@Controller
@RequestMapping("/ogcproxy/*")
public class OGCProxy {

 
    /**
     * List of allowed hosts.
     * If empty, means everyone's allowed
     */
    public static final String[] _allowedHosts = 
                                    {
                                    };

    /**
     * List of valid content types
     */
    public static final String[] _validContentTypes = 
                                    {
                                        "application/xml", "text/xml",
                                        "application/vnd.ogc.se_xml",           // OGC Service Exception 
                                        "application/vnd.ogc.se+xml",           // OGC Service Exception
                                        "application/vnd.ogc.success+xml",      // OGC Success (SLD Put)
                                        "application/vnd.ogc.wms_xml",          // WMS Capabilities
                                        "application/vnd.ogc.context+xml",      // WMC
                                        "application/vnd.ogc.gml",              // GML
                                        "application/vnd.ogc.sld+xml",          // SLD
                                        "application/vnd.google-earth.kml+xml", // KML
                                    };

    /**
     * Proxy entry point. Accessible via POST.
     * @param request. Must contains a "url" parameter pointing to the remote host
     * @param response. Contains in its content response from remote host
     * @param sURL. Automatically filled by the Spring url mapping
     */
    @RequestMapping(method=RequestMethod.POST)
    public void handlePOSTRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) {   
        handleRequest(request, response, sURL);
    }
   
    /**
     * Proxy entry point. Accessible via GET.
     * @param request. Must contains a "url" parameter pointing to the remote host
     * @param response. Contains in its content response from remote host
     * @param sURL. Automatically filled by the Spring url mapping
     */
    @RequestMapping(method=RequestMethod.GET)
    public void handleGETRequest(HttpServletRequest request, HttpServletResponse response, @RequestParam("url") String sURL) {   
        handleRequest(request, response, sURL);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, String sURL) {
        try {
            
            URL url = null;
            try {
                url = new URL(sURL);
            }
            catch (MalformedURLException e) { // not an url
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
            
            // HTTP protocol is required 
            if (!"http".equalsIgnoreCase(url.getProtocol())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                        "HTTP protocol expected. \"" + url.getProtocol() + "\" used.");
                return;
            }
        
            // check if proxy must filter on final host
            if (isFilteringOnFinalHost()) {
                // requested host has to be among the list (_allowedHosts)
                if (!isHostAllowed(url.getHost())) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                                        "Host \"" + url.getHost() + "\" is not allowed to be requested");
                    return;
                }
            }

            String requestMethod = request.getMethod();

            // open communication between proxy and final host
            // all actions before the connection can be taken now
            HttpURLConnection connectionWithFinalHost = (HttpURLConnection) url.openConnection();

            // set request method
            connectionWithFinalHost.setRequestMethod(requestMethod);

            // set doOutput to true if we are POSTing
            if (requestMethod.equalsIgnoreCase("POST")) {
                connectionWithFinalHost.setDoOutput(true);
            }
            
            // copy headers from client's request to request that will be send to the final host
            copyHeadersToConnection(request, connectionWithFinalHost);
            connectionWithFinalHost.setRequestProperty("Accept-Encoding", "");
            
            // connect to remote host
            // interactions with the resource are enabled now
            connectionWithFinalHost.connect();

            if (requestMethod.equalsIgnoreCase("POST")) {
                ServletInputStream in = request.getInputStream();
                OutputStream out = connectionWithFinalHost.getOutputStream();
                byte[] buf = new byte[1024]; // read maximum 1024 bytes
                int len;                     // number of bytes read from the stream
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            
            // get content type
            String contentType = connectionWithFinalHost.getContentType();
            if (contentType == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "Host url has been validated by proxy but content type given by remote host is null");
                return;
            }
            
            // content type has to be valid
            if (!isContentTypeValid(contentType)) {
                
                if (connectionWithFinalHost.getResponseMessage() != null) {
                    if (connectionWithFinalHost.getResponseMessage().equalsIgnoreCase("Not Found")) {
                        // content type was not valid because it was a not found page (text/html)
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Remote host not found"); 
                        return;
                    }
                }
                
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                        "The content type of the remote host's response \"" + contentType 
                        + "\" is not allowed by the proxy rules");
                return;
            }
            
            // send remote host's response to client
            
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
            
            boolean isCharsetKnown = connectionWithFinalHost.getContentType().toLowerCase().contains("charset");
            String contentEncoding = getContentEncoding(connectionWithFinalHost.getHeaderFields());
             
            // copy headers from the remote server's response to the response to send to the client
            if (isCharsetKnown) {
                copyHeadersFromConnectionToResponse(response, connectionWithFinalHost);
            } else {
                // copy everything except Content-Type header
                // because we need to concatenate the charset later
                copyHeadersFromConnectionToResponse(response, connectionWithFinalHost, new String[] {"Content-Type"});
            }
                
            InputStream streamFromServer = null;
            OutputStream streamToClient = null;
            if(contentEncoding == null || isCharsetKnown) {
                // A simple stream can do the job for data that is not in content encoded
                // but also for data content encoded with a known charset
                streamFromServer = connectionWithFinalHost.getInputStream();  
                streamToClient = response.getOutputStream();
            }
            else if ("gzip".equalsIgnoreCase(contentEncoding) && !isCharsetKnown) {
                // the charset is unknown and the data are compressed in gzip
                // we add the gzip wrapper to be able to read/write the stream content
                streamFromServer = new GZIPInputStream(connectionWithFinalHost.getInputStream());
                streamToClient = new GZIPOutputStream(response.getOutputStream());
            }
            else if("deflate".equalsIgnoreCase(contentEncoding) && !isCharsetKnown) {
                // same but with deflate
                streamFromServer = new DeflaterInputStream(connectionWithFinalHost.getInputStream());
                streamToClient = new DeflaterOutputStream(response.getOutputStream());
            } else {
                throw new UnsupportedOperationException("Please handle the stream when it is encoded in " + contentEncoding);
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
                        String charset = getCharset(s); // extract charset
                        
                        if (charset == null) {
                            // the charset cannot be found, IE users must be warned
                            // that the request cannot be fulfilled, nothing good would happen otherwise
                            if(request.getHeader("User-Agent").toLowerCase().contains("msie")) {
                                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                                    "Charset of the response is unknown");
                                streamFromServer.close();
                                connectionWithFinalHost.disconnect();
                                streamToClient.close();
                                return;
                            }
                        } else {
                            // override content-type header and add the charset found
                            response.addHeader("Content-Type", 
                                    connectionWithFinalHost.getContentType() 
                                    + ";charset=" + charset);
                            first = false; // we found the encoding, don't try to do it again
                        }  
                    }
                }
                
                // for everyone, the stream is just forwarded to the client
                streamToClient.write(buf, 0, len);
            }
            
            streamFromServer.close();
            connectionWithFinalHost.disconnect();
            streamToClient.close();
        }
        catch (IOException e) {
            // connection problem with the host 
            e.printStackTrace();
        }
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
     * @param headerFields headers of the HttpURLConnection
     * @return null if not exists otherwise name of the encoding (gzip, deflate...)
     */
    private String getContentEncoding(Map<String, List<String>> headerFields) {
        for (Iterator<String> i = headerFields.keySet().iterator() ; i.hasNext() ; ) {
            String headerName = i.next(); 
            if(headerName != null) {
                if("Content-Encoding".equalsIgnoreCase(headerName)) {
                    List<String> valuesList = headerFields.get(headerName);
                    StringBuilder sBuilder = new StringBuilder();
                    for(String value : valuesList) {
                        sBuilder.append(value);
                    }
                    
                    return sBuilder.toString().toLowerCase();
                }
            }
        }
        return null;
    }

    /**
     * Copy headers from the connection to the response
     * @param response to copy headers in
     * @param uc contains headers to copy
     * @param ignoreList list of headers that mustn't be copied
     */
    private void copyHeadersFromConnectionToResponse(HttpServletResponse response, HttpURLConnection uc, String... ignoreList) {
        Map<String, List<String>> map =  uc.getHeaderFields();
        for (Iterator<String> i = map.keySet().iterator() ; i.hasNext() ; ) {
            
            String headerName = i.next(); 
            
            if(! isInIgnoreList(headerName, ignoreList)) {
                
                // concatenate all values from the header
                List<String> valuesList = map.get(headerName);
                StringBuilder sBuilder = new StringBuilder();
                for(String value : valuesList) {
                    sBuilder.append(value);
                }

                // add header to HttpServletResponse object
                if (headerName != null) {
                    if("Transfer-Encoding".equalsIgnoreCase(headerName) && "chunked".equalsIgnoreCase(sBuilder.toString())) {
                        // do not write this header because Tomcat already assembled the chunks itself
                        continue;
                    }
                    response.addHeader(headerName, sBuilder.toString());
                } 
            }
        }
    }
    
    /**
     * Helper function to detect if a specific header is in a given ignore list
     * @param headerName
     * @param ignoreList
     * @return true: in, false: not in
     */
    private boolean isInIgnoreList(String headerName, String[] ignoreList) {
        if (headerName == null) return false;
        
        for (String headerToIgnore : ignoreList) {
            if (headerName.equalsIgnoreCase(headerToIgnore))
                return true;
        }   
        return false;
    }

    /**
     * Copy client's headers in the request to send to the final host
     * Trick the host by hiding the proxy indirection and keep useful headers information
     * @param request
     * @param uc. Contains now headers from client request except Host
     */
    protected void copyHeadersToConnection(HttpServletRequest request, HttpURLConnection uc) {
        
        for (@SuppressWarnings("rawtypes")
        Enumeration enumHeader=request.getHeaderNames(); enumHeader.hasMoreElements();) {
            String headerName = (String)enumHeader.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // copy every header except host
            if (!"host".equalsIgnoreCase(headerName)) {
                uc.setRequestProperty(headerName, headerValue);
            }       
        }         
    }
    
    /**
     * Check if a particular host can be requested
     * @param host - host name
     * @return true: allowed; false: not allowed
     */
    protected boolean isHostAllowed(final String host) {
        
        if (!isFilteringOnFinalHost()) {
            // Proxy doesn't filter anyone
            return true;
        }
        for (String allowedHost : OGCProxy._allowedHosts) {
            if (allowedHost.equals(host)) {
                return true;
            }
        }
        return false;       
    }
    
    /**
     * Determine if Proxy filters on final host according to the list of allowed ones
     */
    protected boolean isFilteringOnFinalHost() {
        
        return (OGCProxy._allowedHosts.length == 0)?(false):(true);
    }
    
    /**
     * Check if the content type is accepted by the proxy
     * @param contentType
     * @return true: valid; false: not valid
     */
    protected boolean isContentTypeValid(final String contentType) {
        
        // focus only on type, not on the text encoding
        String type = contentType.split(";")[0];
        for (String validTypeContent : OGCProxy._validContentTypes) {
            if (validTypeContent.equals(type)) {
                return true;
            }
        }
        return false;       
    }
}
