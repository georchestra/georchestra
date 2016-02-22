package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;


/**
 * Test SLDClassifier
 * @author yoann buch - yoann.buch@gmail.com
 *
 */

public class SLDClassifierTest {

    private static final Map<String, UsernamePasswordCredentials> EMPTY_MAP = Collections.<String,UsernamePasswordCredentials>emptyMap();

    @Test
    @Ignore("Moving to wfs-ng, tests are known to be broken")
    public void testChoropleths() throws Exception {

        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        String firstColor = "#0000ff";
        String lastColor = "#ff0000";
        int classCount = 3;

        JSONObject jsReq = new JSONObject().put("type", "CHOROPLETHS")
                .put("wfs_url", wfsUrl).put("layer_name", featureTypeName)
                .put("attribute_name", propertyName)
                .put("class_count", classCount).put("first_color", firstColor)
                .put("symbol_type", "polygon").put("last_color", lastColor);


        // create command
        ClassifierCommand command = new ClassifierCommand(jsReq);
        SLDClassifier classifier = new SLDClassifier(EMPTY_MAP, command, new MockWFSDataStoreFactory());

        Document doc = createDomDocument(classifier.getSLD());

        // need as many rules, filters and symbolizers as classes
        assertEquals(classCount, doc.getElementsByTagName("sld:Rule").getLength());
        assertEquals(classCount, doc.getElementsByTagName("ogc:Filter").getLength());
        assertEquals(classCount, doc.getElementsByTagName("sld:PolygonSymbolizer").getLength());

    }

    @Test
    @Ignore("Moving to wfs-ng, tests are known to be broken")
    public void testSymbols() throws Exception {

        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        int minSize = 4;
        int lastSize = 24;
        int classCount = 3;

        JSONObject jsReq = new JSONObject().put("type", "PROP_SYMBOLS")
                    .put("wfs_url", wfsUrl)
                    .put("layer_name", featureTypeName)
                    .put("attribute_name", propertyName)
                    .put("class_count", classCount)
                    .put("min_size", minSize)
                    .put("symbol_type", "point")
                    .put("max_size", lastSize);

        // create command
        ClassifierCommand command = new ClassifierCommand(jsReq);
        SLDClassifier classifier = new SLDClassifier(EMPTY_MAP, command, new MockWFSDataStoreFactory());
        Document doc = createDomDocument(classifier.getSLD());

        // need as many rules, filters and symbolizers as classes
        assertEquals(classCount, doc.getElementsByTagName("sld:Rule").getLength());
        assertEquals(classCount, doc.getElementsByTagName("ogc:Filter").getLength());
        assertEquals(classCount, doc.getElementsByTagName("sld:PointSymbolizer").getLength());
    }

    @Test
    @Ignore("Moving to wfs-ng, tests are known to be broken")
    public void testUniqueValues() throws Exception {

        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "STATE_NAME";
        int paletteID = 1;

        JSONObject jsReq = new JSONObject().put("type", "unique_values")
                    .put("wfs_url", wfsUrl)
                    .put("layer_name", featureTypeName)
                    .put("attribute_name", propertyName)
                    .put("symbol_type", "polygon")
                    .put("palette", paletteID);

        // create command
        ClassifierCommand command = new ClassifierCommand(jsReq);
        SLDClassifier classifier = new SLDClassifier(EMPTY_MAP, command, new MockWFSDataStoreFactory());

        Document doc = createDomDocument(classifier.getSLD());

        // should retrieve expected tags
        assertEquals(true, doc.getElementsByTagName("sld:UserLayer").getLength() == 0);
        assertEquals(true, doc.getElementsByTagName("sld:NamedLayer").getLength() != 0);
        assertEquals(true, doc.getElementsByTagName("sld:Rule").getLength() != 0);
        assertEquals(true, doc.getElementsByTagName("ogc:Filter").getLength() != 0);
        assertEquals(true, doc.getElementsByTagName("sld:PolygonSymbolizer").getLength() != 0);
    }

    private Document createDomDocument(final String content) throws Exception {
        // create xml doc
        final DocumentBuilderFactory lDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder lDocumentBuilder = lDocumentBuilderFactory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        Document document = lDocumentBuilder.parse(stream);
        return document;
    }
}
