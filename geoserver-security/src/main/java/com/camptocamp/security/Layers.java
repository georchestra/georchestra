package com.camptocamp.security;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * Allows access to geoserver layers configuration
 * @author jesse.eichar@camptocamp.com
 */
@Controller
@RequestMapping("/layers")
public class Layers extends AbstractLayersController {

    protected static final Log logger = LogFactory.getLog(Layers.class.getPackage().getName());
    
    private static final WmsNamespaceContext WMS_NAMESPACE_CONTEXT = new WmsNamespaceContext ();
    private static final String XML_ERROR_TYPE = "application/vnd.ogc.se_xml";

    private String capabilitiesURL;
    private String privelegedAdminUser;
    private String privelegedAdminPass;

    @RequestMapping(method = RequestMethod.GET)
    public void layers(HttpServletRequest request, HttpServletResponse response, @RequestParam("region") String region) throws IOException {
        
        if(!checkAccess(request, response, region)) return;
        
        response.setContentType("application/json; charset=UTF-8");

        List<String> allLayers = layers();
        

        List<String> restrictedLayers = restrictedLayers(region);
        
        PrintWriter writer = response.getWriter();
        try {
            writer.write("{\"layers\": [");
            boolean comma = false;
            for (String name : allLayers) {
                if(!restrictedLayers.contains(name)) {
                    if (comma) {
                        writer.write(",");
                    }
                    comma = true;
                    writer.write(String.format("{\"cn\": \"%s\"}", name));
                }
            }
            writer.write("]}");
        } finally {
            writer.close();
        }
    }


    private String toString (InputStream stream) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream ();
    
            byte[] bytes = new byte[8192];
    
            int read = stream.read (bytes);
            while (read != -1) {
                out.write (bytes, 0, read);
                read = stream.read (bytes);
            }
            return new String (out.toByteArray ());
        } finally {
            stream.close();
        }
    }

    /**
     * 
     * @param resolveFormat
     *            Currently all parameters are sent in all requests. (Format
     *            parameter is sent for a describeLayer request which is
     *            unnecessary, but should ignored). Because of this I had an
     *            infinite loop. The describeLayer request required for getting
     *            the supported formats was trying to resolve the aliases in
     *            order to make the request. 
     */
    private InputStream makeRequest (String request, int timeout) throws IOException, ProtocolException,
            MalformedURLException {
        InputStream in;
            URL getURL = new URL (capabilitiesURL);

            HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
        
        connection.setReadTimeout(timeout);
        
        // HACK  I want unrestricted access to layers. 
        // Security check takes place in ExtractorController
        String userPassword = privelegedAdminUser+ ":" + privelegedAdminPass;
        String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
        
        connection.setRequestProperty ("Authorization", "Basic " + encoding);
        
        // check for an error response from the server
        if(connection.getContentType().contains(XML_ERROR_TYPE)){
            String error = toString(connection.getInputStream());
            throw new RuntimeException("Error from server while fetching coverage:"+error);
        } else {
            in = connection.getInputStream();
            return in;            
        }
    }

    private List<String> layers () throws ProtocolException, MalformedURLException, IOException,
            AssertionError {
        String data = toString(makeRequest(capabilitiesURL, 3000));
        String xpathExpression = "//Layer/Name";
        try {
            XPath xPath = XPathFactory.newInstance ().newXPath ();
            xPath.setNamespaceContext (WMS_NAMESPACE_CONTEXT);
            XPathExpression expression = xPath.compile (xpathExpression);
            InputSource source = new InputSource (new StringReader (data));
            NodeList nodes = (NodeList) expression.evaluate (source, XPathConstants.NODESET);
            ArrayList<String> results = new ArrayList<String>();
            for (int i = 0; i < nodes.getLength(); i++) {
                results.add(nodes.item(i).getTextContent().trim().replaceAll(":","."));
            }
            return results;
        } catch (XPathExpressionException e) {
            throw new RuntimeException (xpathExpression + " is not a legal xpath", e);
        }
    }


    /**
     * Namespace so that the xpath can get the data out of the wcs documents
     * 
     * @author jeichar
     */
    private static class WmsNamespaceContext implements NamespaceContext {

        public String getNamespaceURI (String prefix) {
            if (prefix == null)
                throw new NullPointerException ("Null prefix");
            else if ("gml".equals (prefix))
                return "http://www.opengis.net/gml";
            else if ("ogc".equals (prefix))
                return "http://www.opengis.net/ogc";
            else if ("wms".equals (prefix))
                return "http://www.opengis.net/wms";
            return "";
        }

        // This method isn't necessary for XPath processing.
        public String getPrefix (String uri) {
            throw new UnsupportedOperationException ();
        }

        // This method isn't necessary for XPath processing either.
        @SuppressWarnings("unchecked")
        public Iterator getPrefixes (String uri) {
            throw new UnsupportedOperationException ();
        }
    }
    
    public void setCapabilitiesURL(String capabilitiesURL) {
        this.capabilitiesURL = capabilitiesURL;
    }
    public void setPrivelegedAdminUser(String privelegedAdminUser) {
        this.privelegedAdminUser = privelegedAdminUser;
    }
    public void setPrivelegedAdminPass(String privelegedAdminPass) {
        this.privelegedAdminPass = privelegedAdminPass;
    }
}
