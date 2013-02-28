package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.georchestra.mapfishapp.ws.classif.ClassifierCommand;
import org.georchestra.mapfishapp.ws.classif.SLDClassifier;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.w3c.dom.Document;


/**
 * Test SLDClassifier
 * @author yoann buch - yoann.buch@gmail.com
 *
 */

public class SLDClassifierTest {

    private static final Map<String, UsernamePasswordCredentials> EMPTY_MAP = Collections.<String,UsernamePasswordCredentials>emptyMap();

    @Test(timeout=10000)
    public void testChoropleths() throws Exception {
         
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        String firstColor = "#0000ff";
        String lastColor = "#ff0000";
        int classCount = 3;
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"CHOROPLETHS\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("class_count:\"" + classCount + "\",");
        jsonRequest.append("first_color:\"" + firstColor + "\",");
        jsonRequest.append("last_color:\"" + lastColor + "\"");
        jsonRequest.append("}");
        
        // create command
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        ClassifierCommand command = new ClassifierCommand(jObj);
        SLDClassifier classifier = new SLDClassifier(EMPTY_MAP, command);

        Document doc = createDomDocument(classifier.getSLD());

        // need as many rules, filters and symbolizers as classes
        assertEquals(classCount, doc.getElementsByTagName("sld:Rule").getLength());
        assertEquals(classCount, doc.getElementsByTagName("ogc:Filter").getLength());
        assertEquals(classCount, doc.getElementsByTagName("sld:PolygonSymbolizer").getLength());
        
    }
    
    @Test(timeout=10000)
    public void testSymbols() throws Exception {
        
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        int minSize = 4;
        int lastSize = 24;
        int classCount = 3;
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"PROP_SYMBOLS\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("class_count:\"" + classCount + "\",");
        jsonRequest.append("min_size:\"" + minSize + "\",");
        jsonRequest.append("max_size:\"" + lastSize + "\"");
        jsonRequest.append("}");
        
        // create command
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        ClassifierCommand command = new ClassifierCommand(jObj);
        SLDClassifier classifier = new SLDClassifier(EMPTY_MAP, command);
        
        Document doc = createDomDocument(classifier.getSLD());

        // need as many rules, filters and symbolizers as classes
        assertEquals(classCount, doc.getElementsByTagName("sld:Rule").getLength());
        assertEquals(classCount, doc.getElementsByTagName("ogc:Filter").getLength());
        assertEquals(classCount, doc.getElementsByTagName("sld:PointSymbolizer").getLength());
    }
    
    @Test(timeout=10000)
    public void testUniqueValues() throws Exception {
        
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "STATE_NAME";
        int paletteID = 1;
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"unique_values\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("palette:\"" + paletteID + "\"");
        jsonRequest.append("}");
        
        // create command
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        ClassifierCommand command = new ClassifierCommand(jObj);
        SLDClassifier classifier = new SLDClassifier(EMPTY_MAP, command);
        
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
