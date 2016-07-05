package org.georchestra.atlas;


import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import org.apache.camel.converter.stream.InputStreamCache;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CamelPopulatePropertiesComponent {

    /**
     * The base URL where the webapp can be reached.
     *
     */
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    /**
     * Generate following key in exchange properties :
     *  * layers : List of all layers in print request (feature layer and base layers)
     *  * legendURL : URL to download legend
     */
    @Handler
    public void merge(Exchange ex) throws JSONException, IOException {

        String rawJson = ex.getProperty("rawJson", String.class);
        JSONObject jobSpec = new JSONObject(new JSONTokener(rawJson));

        JSONObject featureLayer = (JSONObject) jobSpec.get("featureLayer");
        JSONArray baseLayers = (JSONArray) jobSpec.get("baseLayers");

        JSONArray layers = new JSONArray();
        layers.put(featureLayer);

        for(int i = 0; i < baseLayers.length();i++)
            layers.put(baseLayers.get(i));

        ex.setProperty("layers",layers.toString());
        ex.setProperty("baseUrl",this.getBaseUrl());

        String legendURL = featureLayer.getString("baseURL");
        legendURL += "?SERVICE=WMS";
        legendURL += "&VERSION=" + featureLayer.getString("version");
        legendURL += "&REQUEST=GetLegendGraphic&FORMAT=image/png&TRANSPARENT=true";
        legendURL += "&LAYER=" + featureLayer.getJSONArray("layers").getString(0);

        ex.setProperty("legendURL", legendURL);

    }
}
