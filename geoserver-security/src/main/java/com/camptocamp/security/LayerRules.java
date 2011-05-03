package com.camptocamp.security;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Allows access to geoserver layers configuration
 * @author jesse.eichar@camptocamp.com
 */
@Controller
@RequestMapping("/rules/layers")
public class LayerRules extends AbstractLayersController {
    private static final String NAME = "name";
    private static final String PERMISSION = "permission";
    private static final String ROLE = "role";
    protected static final Log logger = LogFactory.getLog(LayerRules.class.getPackage().getName());
    long id = 0;
    
    @RequestMapping(method=RequestMethod.GET)
    public synchronized void get(HttpServletRequest request, HttpServletResponse response, @RequestParam("region") String region) throws IOException {

        if(!checkAccess(request, response, region)) return;
        
        response.setContentType("application/json; charset=UTF-8");
        
        File regionFile = getRegionsFile(region);

        List<String> lines = read(regionFile);
        
        PrintWriter writer = response.getWriter();
        id += 1;
        writer.printf("{ \"gen_id\": %s, \"rules\": [",id);
        
        boolean comma = false;
        for (String line : lines) {
            if(comma) writer.print(",");
            comma=true;
            
            String[] parts = line.split("\\.");
            String template = "{\"" + NAME + "\": \"%s\",\"" + PERMISSION
                    + "\": \"%s\",\"" + ROLE + "\": \"%s\"}";
            String name = parts[0] + "." + parts[1];

            String[] lastParts = parts[2].split("=");
            String permission = "w".equalsIgnoreCase(lastParts[0]) ? "write" : "read";
            String role = lastParts[1];
            writer.printf(template,name, permission, role);
        }
        
        writer.println("]}");
    }

    @RequestMapping(method=RequestMethod.POST)
    public synchronized void post(HttpServletRequest request, HttpServletResponse response, @RequestParam("region") String region) throws IOException, JSONException {

        if(!checkAccess(request, response, region)) return;

        if(!writeRegionFile(request, response, region)) return;
        
        combineRegions();
    }

    private void combineRegions() throws FileNotFoundException, IOException {
        File[] regions = regionsLocation.listFiles();
        StringBuilder fullConfig = new StringBuilder();
        
        for (File file : regions) {
            BufferedReader in = new BufferedReader(new FileReader(file));
            try {
                fullConfig.append(read(in));
            } finally {
                in.close();
            }
        }
        
        fullConfig.append("\nmode=mixed\n");

        write(new File(location, "layers.properties"), fullConfig);
    }

    private boolean writeRegionFile(HttpServletRequest request, HttpServletResponse response, String region)
            throws JSONException, IOException {

        JSONObject jsonData = new JSONObject(read(request.getReader()));
        
        if(jsonData.getLong("gen_id") != id) {
            response.sendError(503, "Concurrent update: Another user updated the settings during your edit session");
            return false;
        }

        List<String> restrictedLayers = restrictedLayers(region);
        
        JSONArray layers = jsonData.getJSONArray("rules");
        StringBuilder config = new StringBuilder();
        
        StringBuilder errors = new StringBuilder();
        
        for (int i = 0; i < layers.length(); i++) {
            JSONObject jsonLayer = layers.getJSONObject(i);
            String layerName = jsonLayer.getString(NAME);
            
            if(restrictedLayers.contains(layerName)) {
                if(errors.length() == 0) {
                    errors.append(", ");
                }
                errors.append(layerName);
            }
            
            config.append(layerName);
            config.append('.');
            config.append("write".equalsIgnoreCase(jsonLayer.getString(PERMISSION))?"w":"r");
            config.append('=');
            config.append(jsonLayer.getString(ROLE));
            config.append('\n');
        }
        
        if(errors.length() > 0) {
            response.sendError(403, errors.toString());
            return false;
        }
        
        File regionFile = getRegionsFile(region);

        write(regionFile, config);
        
        return true;
    }

    private void write(File regionFile, StringBuilder config) throws IOException {
        File tmpFile = File.createTempFile("regionsFile", "properties");
        FileWriter writer = new FileWriter(tmpFile);
        try {
            writer.write(config.toString());
        }finally {
            writer.close();
        }
        
        regionFile.delete();
        if(!tmpFile.renameTo(regionFile)) {
            FileChannel to = new RandomAccessFile(regionFile,"rws").getChannel();
            FileChannel from = new RandomAccessFile(tmpFile,"rs").getChannel();
            from.transferTo(0, from.size(), to);
        }
    }
}
