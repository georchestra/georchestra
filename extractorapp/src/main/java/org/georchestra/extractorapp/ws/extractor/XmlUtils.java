package org.georchestra.extractorapp.ws.extractor;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class XmlUtils {
    private XmlUtils() {
        // do not allow instantiation
    }

    public static final OgcNamespaceContext WMS_NAMESPACE_CONTEXT = new OgcNamespaceContext(NameSpaces.wms);
    public static final OgcNamespaceContext WFS_NAMESPACE_CONTEXT = new OgcNamespaceContext(NameSpaces.wfs);
    public static final OgcNamespaceContext WCS_NAMESPACE_CONTEXT = new OgcNamespaceContext(NameSpaces.wcs);

    public static NodeList select(String xpathExpression, String data, OgcNamespaceContext nsContext)
            throws ProtocolException, MalformedURLException, IOException, AssertionError {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(nsContext);
            XPathExpression expression = xPath.compile(xpathExpression);
            InputSource source = new InputSource(new StringReader(data));
            return (NodeList) expression.evaluate(source, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(xpathExpression + " is not a legal xpath", e);
        }
    }

    public static NodeList selectWFSCabalitilies(String xpathExpression, String data)
            throws ProtocolException, MalformedURLException, IOException, AssertionError {
        return select(xpathExpression, data, WFS_NAMESPACE_CONTEXT);
    }

    public static NodeList selectWCSCabalitilies(String xpathExpression, String data)
            throws ProtocolException, MalformedURLException, IOException, AssertionError {
        return select(xpathExpression, data, WCS_NAMESPACE_CONTEXT);
    }

    public static NodeList selectWMSCabalitilies(String xpathExpression, String data)
            throws ProtocolException, MalformedURLException, IOException, AssertionError {
        return select(xpathExpression, data, WMS_NAMESPACE_CONTEXT);
    }

    static enum NameSpaces {
        gml("http://www.opengis.net/gml"), ogc("http://www.opengis.net/ogc"), ows("http://www.opengis.net/ows"),
        wms("http://www.opengis.net/wms"), wfs("http://www.opengis.net/wfs"), wcs("http://www.opengis.net/wcs");

        private final String uri;

        private NameSpaces(String uri) {
            this.uri = uri;
        }
    }

    /**
     * Namespace so that the xpath can get the data out of the wcs documents
     * 
     * @author jeichar
     */
    static class OgcNamespaceContext implements NamespaceContext {
        private NameSpaces defaultNS;

        public OgcNamespaceContext(NameSpaces defaultNS) {
            this.defaultNS = defaultNS;
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null)
                throw new NullPointerException("Null prefix");

            try {
                NameSpaces ns = NameSpaces.valueOf(prefix);
                if (ns != null) {
                    return ns.uri;
                } else {
                    return defaultNS.uri;
                }
            } catch (IllegalArgumentException e) {
                // no match
                return defaultNS.uri;
            }
        }

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        @SuppressWarnings("rawtypes")
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }

}
