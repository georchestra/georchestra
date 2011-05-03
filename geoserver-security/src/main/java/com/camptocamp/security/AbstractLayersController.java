package com.camptocamp.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AbstractLayersController {

    protected File location;
    protected File regionsLocation;

    public AbstractLayersController() {
        super();
    }
    
    protected List<String> restrictedLayers(String region) throws IOException {
        List<String> restrictedLayers = new ArrayList<String>();
        
        File[] regions = regionsLocation.listFiles();
        
        for (File file : regions) {
            if(!file.getName().equals(getRegionsFile(region).getName())) {
                for(String line : read(file)) {
                    String[] parts = line.split("\\.");
                    restrictedLayers.add(parts[0] + "." + parts[1]);
                }
            }
        }
        return restrictedLayers;
    }

    protected boolean checkAccess(HttpServletRequest request, HttpServletResponse response, String region) throws IOException {
        if (!Arrays.asList(request.getHeader("sec-roles").split(",")).contains((Regions.PREFIX+region).toUpperCase())) {
            response.sendError(403, "La configuration de la region" + region + "n'est pas accessible a l'utilisateur courant");
            return false;
        }
        return true;
    }

    protected List<String> read(File region) throws IOException {
        if(!region.exists()) return Collections.emptyList();
        
        
        BufferedReader reader = new BufferedReader(new FileReader(region));
        List<String> lines = new ArrayList<String>();
        
        String line = reader.readLine();
        while(line != null) {
            line = line.trim();
            if(!line.startsWith("#") && line.length()>0) {
                lines.add(line);
            }
            line = reader.readLine();
        }
        return lines;
    }

    protected String read(BufferedReader reader) throws IOException {
        StringBuilder read = new StringBuilder();
        
        String line = reader.readLine();
        while(line != null) {
            line = line.trim();
            if(!line.startsWith("#") && line.length()>0) {
                read.append(line);
                read.append("\n");
            }
            line = reader.readLine();
        }
        return read.toString();
    }

    protected File getRegionsFile(String region) {
        File regionFile = new File(regionsLocation,(region+".properties").toLowerCase());
    
        if(!regionFile.getParentFile().exists()) {
            regionFile.getParentFile().mkdirs();
        }
        return regionFile;
    }

    public void setLocation(File location) {
        this.location = location;
        this.regionsLocation = new File(location, "regions");
    }

}