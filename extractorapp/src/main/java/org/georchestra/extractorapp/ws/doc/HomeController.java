package org.georchestra.extractorapp.ws.doc;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


/**
 * Main controller that display the extractor home page
 * 
 * @author bruno.binet@camptocamp.com
 */
    

@Controller
@RequestMapping("/")
public class HomeController {
    private static final String SESSION_POST_DATA_STORE = "SESSION_POST_DATA_STORE";

    /**
     * POST entry point.
     * @param request. Must contains information from the layers and services to be extracted
     * @param response.
     * @return 
     * @throws IOException 
     */
    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String str = request.getParameter("data");

        if (str == null) {
            // there is no "data" param: we should parse raw post data
            BufferedReader postData = request.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String cur;
            while ((cur = postData.readLine()) != null) {
                stringBuilder.append(cur).append("\n");
            }
            if(stringBuilder.length() > 0) {
                str = stringBuilder.toString();
            }
        }

        Map<String, Object> model = createModelFromStringOrSession(request, str, false);

        return new ModelAndView("index", "c", model);
    }

    /**
     * GET entry point.
     * @param request.
     * @param response.
     * @return 
     */
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        String str = request.getParameter("data");

        boolean allowFake = true;
        Map<String, Object> model = createModelFromStringOrSession(request, str, allowFake);

        return new ModelAndView("index", "c", model);
    }

    private Map<String, Object> createModelFromStringOrSession(HttpServletRequest request, String str, boolean allowFake) {
        HttpSession session = request.getSession();
        if( str == null || str.trim() == "") {
            str = (String) session.getAttribute(SESSION_POST_DATA_STORE);
        } else {
            session.setAttribute(SESSION_POST_DATA_STORE, str);
        }
        
        if(str == null || str.trim() == ""){
            session.setAttribute(SESSION_POST_DATA_STORE, null);
            str = null;
        }
        
        Map<String, Object> model;
        if(str != null) {
            model = createModelFromString(request, str);
        } else if(allowFake){
            model = new HashMap<String,Object>();
            model.put("fake", true);
        } else {
            model = new HashMap<String,Object>();
            model.put("fake", false);
        }
        return model;
    }

    private Map<String,Object> createModelFromString(HttpServletRequest request, String str) {
        JSONObject jsonData;
        JSONArray jsonLayers, jsonServices;
        String layers, services;
        try {
            jsonData = new JSONObject(str);
        } catch (JSONException e) {
            throw new RuntimeException("Cannot parse the JSON post data", e);
        }

        try {
            jsonLayers = jsonData.getJSONArray("layers");
            layers = jsonLayers.toString(1);
        } catch (JSONException e) {
            layers = "[]";
        }
        try {
            jsonServices = jsonData.getJSONArray("services");
            services = jsonServices.toString(1);
        } catch (JSONException e) {
            services = "[]";
        }

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("fake", false);
        model.put("layers", layers);
        model.put("services", services);
        return model;
    }
    
}
