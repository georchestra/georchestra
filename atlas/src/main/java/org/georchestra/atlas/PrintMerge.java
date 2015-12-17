package org.georchestra.atlas;


import org.apache.camel.Exchange;
import org.apache.camel.Handler;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;


/**
 * Created by jacroute on 12/11/15.
 */
public class PrintMerge {


    @Handler
    public void merge(Exchange ex) throws JSONException {

        JSONObject page = new JSONObject(new JSONTokener(ex.getIn().getBody(String.class)));
        JSONArray layers = new JSONArray(new JSONTokener(ex.getProperty("layers", String.class)));




        String input = "{minScaleDenominator:266.5911979812228, maxScaleDenominator:5.59082264028718E8, baseURL: \"http://sdi.georchestra.org/geoserver/gwc/service/wms\", opacity:1.0}";

        JSONTokener tokener = new JSONTokener(input);
        JSONObject root = new JSONObject(tokener);
        String a = "<pre>Hello world ! " + root.toString(4) + "</pre>";



        // Parse original
//        Object rawJson = ex.getProperty("layers");
//        String classs = rawJson.getClass().getName();
//        JsonElement gson = new Gson().fromJson(classs, JsonElement.class);
//
//        JsonObject o = gson.getAsJsonObject();
//        JsonArray layers = o.getAsJsonArray("layers");

        JSONObject newLayer = new JSONObject();
        newLayer.put("minScaleDenominator", "mlkm");
        newLayer.put("maxScaleDenominator", "lhjlo");
        newLayer.put("baseURL", "lkj");
        //layers.add(newLayer);

        String out = newLayer.toString();

        ex.setProperty("layers", ex.getProperty("baseLayers").toString() );

/*

        JsonObject test = new JsonObject();
        test.addProperty("test", "ok");
        test.addProperty("test2", 666);
//        layers.add(newLayer);
        String out = test.getAsString();



        return out;*/
    }
}
