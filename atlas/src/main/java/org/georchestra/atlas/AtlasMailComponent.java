package org.georchestra.atlas;

import com.google.common.annotations.VisibleForTesting;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.velocity.app.VelocityEngine;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        vProp.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        vProp.put("runtime.log.logsystem.log4j.category", "velocity");
        vProp.put("runtime.log.logsystem.log4j.logger", "velocity");
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
        JSONObject query = new JSONObject(j.getQuery());
        String email = query.getString("email");

        String mailBody = this.formatMail(j);
        ex.getOut().setHeader("from", this.emailFrom);
        ex.getOut().setHeader("subject", this.emailSubject);
        ex.getOut().setHeader("to", email);
        ex.getOut().setHeader("Content-Type", "text/plain; charset=utf-8");

        ex.getOut().setBody(mailBody);
    }
    
    /**
     * Loads the default template e-mail for finished jobs and replaces
     * the variables nested into it with the appropriate values.
     * 
     * The e-mail is supposed to be UTF-8 encoded.
     *
     * @param job AltlasJob to format.
     * @return string of the formatted e-mail
     */
    @VisibleForTesting
    public String formatMail(AtlasJob job) {
        String mail = null;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("jobId", job.getId());
        model.put("baseUrl", this.georBaseUrl);
        model.put("token", job.getToken());
        String outputFormat = "pdf";

        try {
            outputFormat = job.getOutputFormat();
        } catch (JSONException e) {
            log.error("unable to parse the outputformat of the Job " + job, e);
        }

        model.put("extension", outputFormat);
        
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
