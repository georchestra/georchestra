package org.georchestra.atlas;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.velocity.app.VelocityEngine;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.google.common.annotations.VisibleForTesting;

public class AtlasMailComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private VelocityEngine velocityEngine;
    
    private String successTemplatePath;
    
    private String georBaseUrl = "http://localhost:8080";
    
    private String emailFrom = "noreply+atlas@georchestra.org";

    private String emailSubject = "[geOrchestra] Your Atlas request";


    @Autowired
    private GeorchestraConfiguration georConfiguration ;

    public void init() {
        Properties vProp = new Properties();
        if ((georConfiguration != null) && (georConfiguration.activated())) {
            vProp.setProperty("resource.loader", "file");
            vProp.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            vProp.setProperty("file.resource.loader.path", georConfiguration.getContextDataDir() +
                    File.separator + "emails");
            successTemplatePath = "finished.vm";
        }
        else {
            // templates are considered nested in the webapp,
            // using a classpath resource loader
            vProp.setProperty("resource.loader", "class");            
            vProp.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            successTemplatePath = "emails/finished.vm";
        }
        this.velocityEngine = new VelocityEngine(vProp);
    }

    @Handler
    public void prepareMail(Exchange ex) throws JSONException {
        AtlasJob j = ex.getIn().getBody(AtlasJob.class);
        Long jobId = j.getId();
        JSONObject query = new JSONObject(j.getQuery());
        String email = query.getString("email");

        String mailBody = this.formatMail(jobId);
        ex.getOut().setHeader("from", this.emailFrom);
        ex.getOut().setHeader("subject", this.emailSubject);
        ex.getOut().setHeader("to", email);
        ex.getOut().setHeader("Content-Type", "text/plain; charset=utf-8");
        

        ex.getOut().setBody(mailBody);
    }
    
    /**
     * TODO: see:
     * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mail.html
     * "velocity-based example"
     *
     * @param jobId
     * @return
     */
    @VisibleForTesting
    public String formatMail(Long jobId) {
        String mail = null;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("jobId",jobId);
        model.put("baseUrl", this.georBaseUrl);
        
        mail = VelocityEngineUtils.mergeTemplateIntoString(this.velocityEngine,
                this.successTemplatePath, "UTF-8", model);
        
        return mail;
    }
    public void setGeorBaseUrl(String georBaseUrl) {
        this.georBaseUrl = georBaseUrl;
    }
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
}
