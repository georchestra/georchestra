package org.georchestra.mapfishapp.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.mapfishapp.ws.upload.AbstractFeatureGeoFileReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AbstractController implements ApplicationContextAware {

    public AbstractController() {
        super();
    }
    protected ApplicationContext context;


    protected Map<String, Object> createModelFromString(HttpServletRequest request, String str) {
        Map<String, Object> model;
        try {
            if (str != null) {
                model = createModel(request, str);
            } else {
                model = defaultModel(request);
            }
        } catch (Throwable e) {
            model = defaultModel(request);
        }
        return model;
    }

    Map<String, Object> defaultModel(HttpServletRequest request) {
        Map<String, Object> model;
        model = new HashMap<String, Object>();
        model.put("data", "null");
        model.put("bbox", request.getParameter("bbox"));
        model.put("lat", request.getParameter("lat"));
        model.put("lon", request.getParameter("lon"));
        Integer radius;
        if (request.getParameter("radius") != null) {
            radius = Integer.parseInt(request.getParameter("radius"));
        } else {
            radius = null;
        }
        model.put("radius", radius);
        model.put("fileFormatList", new AbstractFeatureGeoFileReader().getFormatListAsJSON());
        model.put("debug", Boolean.parseBoolean(request.getParameter("debug")));

        addContextsToModel(model);
        addAddonsToModel(model);

        return model;
    }

    private void addContextsToModel(Map model) {
        try {
            ContextController cc = context.getBean(ContextController.class);
            model.put("contexts", cc.getContexts());
        } catch (Exception e) {
            model.put("contexts", new JSONArray());
        }
    }

    private void addAddonsToModel(Map model) {
        try {
            AddonController ac = context.getBean(AddonController.class);
            model.put("addons", ac.constructAddonsSpec());
        } catch (Exception e) {
            model.put("addons", new JSONArray());
        }
    }


    Map<String, Object> createModel(HttpServletRequest request, String str) {
        JSONObject jsonData;
        String data, search;
        try {
            jsonData = new JSONObject(str);
        } catch (JSONException e) {
            throw new RuntimeException("Cannot parse the json post data", e);
        }

        boolean debug;
        if (request.getParameter("debug") == null) {
            try {
                debug = jsonData.getBoolean("debug");
            } catch (JSONException e) {
                debug = false;
            }
        } else {
            debug = Boolean.parseBoolean(request.getParameter("debug"));
        }

        try {
            JSONArray jsonLayers, jsonServices, jsonSearch;

            jsonLayers = jsonData.optJSONArray("layers");

            jsonServices = jsonData.optJSONArray("services");
            if (jsonLayers == null) {
                jsonLayers = jsonServices;
            } else {
                jsonLayers = new JSONArray(jsonLayers.toString(1).replaceAll("layername", "name").replaceAll("\"WMS\"", "\"WMSLayer\"").replaceAll("\"WFS\"", "\"WFSLayer\""));

                if(jsonServices!=null) {
                    jsonServices = new JSONArray(jsonServices.toString(1).replaceAll("text", "name"));
                    for (int i = 0; i < jsonServices.length(); i++) {
                        jsonLayers.put(jsonServices.get(i));
                    }
                }
            }
            if (jsonLayers != null) {
                data = jsonLayers.toString(1).replaceAll("owstype", "type").replaceAll("owsurl", "url");
            } else {
                data = "[]";
            }
            
            jsonSearch = jsonData.optJSONArray("search");
            if (jsonSearch != null) {
                search = jsonLayers.toString(1);
            } else {
                search = "[]";
            }
        } catch (JSONException e) {
            data = "[]";
            search = "[]";
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("debug", debug);
        model.put("bbox", request.getParameter("bbox"));
        model.put("lat", request.getParameter("lat"));
        model.put("lon", request.getParameter("lon"));
        Integer radius;
        if (request.getParameter("radius") != null) {
            radius = Integer.parseInt(request.getParameter("radius"));
        } else {
            radius = null;
        }
        model.put("radius", radius);

        model.put("fileFormatList", new AbstractFeatureGeoFileReader().getFormatListAsJSON());
        model.put("data", data);
        model.put("search", search);

        addContextsToModel(model);
        addAddonsToModel(model);

        return model;
    }

    protected String getPostData(HttpServletRequest request) throws IOException {
        String str = request.getParameter("data");

        if (str == null) {
            // there is no "data" param: we should parse raw post data
            BufferedReader postData = request.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String cur;
            while ((cur = postData.readLine()) != null) {
                stringBuilder.append(cur).append("\n");
            }
            if (stringBuilder.length() > 0) {
                str = stringBuilder.toString();
            }
        }
        return str;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
