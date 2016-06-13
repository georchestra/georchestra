package org.georchestra.atlas;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AtlasMailComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private VelocityEngine velocityEngine;
    
    private String successTemplatePath;
    
    private String georBaseUrl = "http://localhost:8080";

    
    @Autowired
    private GeorchestraConfiguration georConfiguration ;

    public void init() {
        if ((georConfiguration != null) && (georConfiguration.activated())) {
            successTemplatePath = georConfiguration.getContextDataDir() +
                    File.separator + "emails" + File.separator;
        }
        else {
            successTemplatePath = "emails/finished.vm";
        }
        Velocity.init();
        this.velocityEngine = new VelocityEngine(); 
    }

    private String toString(InputStreamCache property) throws IOException {

        property.reset();
        BufferedReader reader = new BufferedReader(new InputStreamReader(property));

        StringBuilder rawString = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
            rawString.append(line);
        return rawString.toString();
    }
    
    @Handler
    public void prepareMail(Exchange ex) throws JSONException {
        log.debug("into AtlasMailComponent");
        Velocity.getTemplate(successTemplatePath, "UTF-8");
        AtlasJob j = ex.getIn().getBody(AtlasJob.class);
        Long jobId = j.getId();
        JSONObject query = new JSONObject(j.getQuery());
        String email = query.getString("email");
        
        
    }
}
