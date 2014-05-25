package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.net.URL;

import org.georchestra.mapfishapp.ws.DocServiceException;
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

        JSONObject jsReq = new JSONObject().put("type", "CHOROPLETHS")
                .put("wfs_url", wfsUrl)
                .put("layer_name", featureTypeName)
                .put("attribute_name", propertyName)
                .put("class_count", classCount)
                .put("first_color", firstColor)
                .put("symbol_type", "point")
                .put("last_color", lastColor);

        // create command
        ClassifierCommand command = new ClassifierCommand(jsReq);

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

        JSONObject jsReq = new JSONObject().put("type", "UNIQUE_VALUES")
                .put("wfs_url", wfsUrl)
                .put("layer_name", featureTypeName)
                .put("attribute_name", propertyName)
                .put("palette", paletteID)
                .put("symbol_type", "point");

        // create command
        ClassifierCommand command = new ClassifierCommand(jsReq);

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

        JSONObject jsReq = new JSONObject().put("type", "PROP_SYMBOLS")
                .put("wfs_url", wfsUrl)
                .put("layer_name", featureTypeName)
                .put("attribute_name", propertyName)
                .put("class_count", classCount)
                .put("min_size", minSize)
                .put("symbol_type", "point")
                .put("max_size", maxSize);

        // create command
        ClassifierCommand command = new ClassifierCommand(jsReq);

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

        JSONObject jsReq = new JSONObject().put("type", "WRONGTYPE")
                .put("wfs_url", wfsUrl)
                .put("layer_name", featureTypeName)
                .put("attribute_name", propertyName);

        @SuppressWarnings("unused")
        ClassifierCommand command = new ClassifierCommand(jsReq);
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

        JSONObject jsReq = new JSONObject().put("type", "PROP_SYMBOLS")
                .put("wfs_url", wfsUrl)
                .put("layer_name", featureTypeName)
                .put("attribute_name", propertyName);

        @SuppressWarnings("unused")
        ClassifierCommand command = new ClassifierCommand(jsReq);
    }


}
