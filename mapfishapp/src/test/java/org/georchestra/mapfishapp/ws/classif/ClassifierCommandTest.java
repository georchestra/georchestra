package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.net.URL;


import org.georchestra.mapfishapp.ws.DocServiceException;
import org.georchestra.mapfishapp.ws.classif.ClassifierCommand;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;


/**
 * Test ClassifierCommand.
 * @see org.georchestra.mapfishapp.ws.classif.ClassifierCommand
 * @author yoann buch - yoann.buch@gmail.com
 */

public class ClassifierCommandTest {
    
    /**
     * Verifies correctness  of the construction step - CHOROPLETHS type
     * @throws Exception
     */
    @Test
    public void testChoropleths() throws Exception {
        
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        String firstColor = "#342534";
        String lastColor = "#121212";
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
        
        // test that json was parsed
        assertEquals(new URL(wfsUrl), command.getWFSUrl());
        assertEquals(featureTypeName, command.getFeatureTypeName());
        assertEquals(propertyName, command.getPropertyName());
        assertEquals(Color.decode(firstColor), command.getFirstColor());
        assertEquals(Color.decode(lastColor), command.getLastColor());
        
    }
    
    /**
     * Verifies correctness  of the construction step - UNIQUE VALUES type
     * @throws Exception
     */
    @Test
    public void testUniqueValues() throws Exception {
        
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        int paletteID = 1;
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"UNIQUE_VALUES\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("palette:\"" + paletteID + "\"");
        jsonRequest.append("}");
        
        // create command
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        ClassifierCommand command = new ClassifierCommand(jObj);
        
        // test that json was parsed
        assertEquals(new URL(wfsUrl), command.getWFSUrl());
        assertEquals(featureTypeName, command.getFeatureTypeName());
        assertEquals(propertyName, command.getPropertyName());
        assertEquals(paletteID, command.getPaletteID());
        
    }
    
    /**
     * Verifies correctness  of the construction step - PROP SYMBOLS type
     * @throws Exception
     */
    @Test
    public void testPropSymbols() throws Exception {
        
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        int minSize = 2;
        int maxSize = 14;
        int classCount = 3;
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"PROP_SYMBOLS\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("class_count:\"" + classCount + "\",");
        jsonRequest.append("min_size:\"" + minSize + "\",");
        jsonRequest.append("max_size:\"" + maxSize + "\"");
        jsonRequest.append("}");
        
        // create command
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        ClassifierCommand command = new ClassifierCommand(jObj);
        
        // test that json was parsed
        assertEquals(new URL(wfsUrl), command.getWFSUrl());
        assertEquals(featureTypeName, command.getFeatureTypeName());
        assertEquals(propertyName, command.getPropertyName());
        assertEquals(minSize, command.getMinSize());
        assertEquals(maxSize, command.getMaxSize());
        assertEquals(classCount, command.getClassCount());
        
    }
    
    /**
     * Checks exception when no parameters are provided
     * @throws Exception 
     */
    @Test(expected=DocServiceException.class)
    public void testNoParam() throws Exception {
        JSONTokener tokener = new JSONTokener("{}");
        JSONObject jObj = new JSONObject(tokener);
        @SuppressWarnings("unused")
        ClassifierCommand command = new ClassifierCommand(jObj);
    }
    
    /**
     * Checks exception when an unknown classification type is provided
     * @throws Exception 
     */
    @Test(expected=DocServiceException.class)
    public void testUnknownClassificationType() throws Exception {
        
        // build JSON request
        String wfsUrl = "http://sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities";
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"WRONGTYPE\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("}");
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        @SuppressWarnings("unused")
        ClassifierCommand command = new ClassifierCommand(jObj);
    }
    
    /**
     * Checks exception malformed URL
     * @throws Exception 
     */
    @Test(expected=DocServiceException.class)
    public void testMalformedURL() throws Exception {
        
        // build JSON request
        String wfsUrl = "sigma.openplans.org/geoserver/wfs?service=WFS&request=GetCapabilities"; // no protocol
        String featureTypeName = "topp:states";
        String propertyName = "PERSONS";
        
        StringBuilder jsonRequest = new StringBuilder();
        jsonRequest.append("{");
        jsonRequest.append("type:\"PROP_SYMBOLS\",");
        jsonRequest.append("wfs_url:\"" + wfsUrl + "\",");
        jsonRequest.append("layer_name:\"" + featureTypeName + "\",");
        jsonRequest.append("attribute_name:\"" + propertyName + "\",");
        jsonRequest.append("}");
        JSONTokener tokener = new JSONTokener(jsonRequest.toString());
        JSONObject jObj = new JSONObject(tokener);
        @SuppressWarnings("unused")
        ClassifierCommand command = new ClassifierCommand(jObj);
    }
    
    
}
