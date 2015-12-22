package org.georchestra.extractorapp.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.extractorapp.ws.extractor.ExpiredArchiveDaemon;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class AbstractEmailFactory {

	protected String smtpHost;
	protected int smtpPort = -1;
	protected String emailHtml;
	protected String replyTo;
	protected String from;
	protected String bodyEncoding;
	protected String subjectEncoding;
	protected String[] languages;
	protected ExpiredArchiveDaemon expireDeamon;
	protected String  emailAckTemplateFile;
	protected String  emailTemplateFile;
	protected String  extraKeywordsFile;
	protected String  emailSubject;

    private boolean frozen = false;

    protected Log LOG = LogFactory.getLog(this.getClass().getPackage().getName());

    @Autowired
    protected GeorchestraConfiguration georConfig;

    public void init() {
        if ((georConfig != null) && (georConfig.activated())) {
            LOG.info("geOrchestra datadir: reconfiguring bean " + this.getClass());
            smtpHost = georConfig.getProperty("smtpHost");
            smtpPort = Integer.parseInt(georConfig.getProperty("smtpPort"));
            emailHtml = georConfig.getProperty("emailHtml");
            replyTo = georConfig.getProperty("replyTo");
            from = georConfig.getProperty("from");
            languages = georConfig.getProperty("language").split(",");
            extraKeywordsFile = String.format("i18n/extra_keywords_%s", georConfig.getProperty("language"));
            emailSubject = georConfig.getProperty("emailsubject");
            emailAckTemplateFile = String.format("%s/templates/extractor-email-ack-template.tpl", georConfig.getContextDataDir());
            emailTemplateFile = String.format("%s/templates/extractor-email-template.tpl", georConfig.getContextDataDir());
            LOG.info("geOrchestra datadir: done.");
        }
    }

    public AbstractEmailFactory() {
        // this is the default constructor for use by spring
    }

	public abstract Email createEmail(HttpServletRequest request,
			final String[] recipients, final String url) throws IOException;

	// -------------- Not public API -------------- //
    /**
     * Signals that the values for this object are set and may not
     * be changed.  This is to ensure that when the defaults are set
     * by Spring that later no one will change them programatically.
     *
     * The defaults should only be set via spring configuration.
     *
     * Freeze will be called by the class that has the parameter
     * set by spring.
     */
    public void freeze() {
        this.frozen = true;
        try {
            if (!InetAddress.getByName(smtpHost).isReachable(3000)) {
                throw new IllegalStateException(smtpHost + " is not a reachable address");
            }
        } catch (IOException e) {
            throw new IllegalStateException(smtpHost + " is not a reachable address");
        }
        if (smtpPort < 0) {
            throw new IllegalStateException(smtpPort + " is not a legal port make sure it is correctly configured");
        }
        if (replyTo == null || from == null) {
            if (replyTo == null && from == null) {
                throw new IllegalStateException("Either or both from or replyTo must have a valid value");
            }
            if (replyTo == null) {
                replyTo = from;
            } else {
                from = replyTo;
            }
        }
        if (bodyEncoding == null) {
            bodyEncoding = "UTF-8";
        }
        if (subjectEncoding == null) {
            subjectEncoding = bodyEncoding;
        }
    }

    protected String readFile(HttpServletRequest request, final String path) throws IOException {
        String realPath = null;
        // If georConfig is activated, then the given path is already the one
        // pointing to the correct template file. Else, we fall back on the
        // original behaviour (nested into the webapp).
        if ((this.georConfig != null) && (this.georConfig.activated())) {
            if (! new File(path).exists()) {
                throw new RuntimeException("Template file \""+ realPath +"\" does not exist. Please check your configuration.");
            }
            realPath = path;
        } else {
            realPath = request.getSession().getServletContext().getRealPath(path);
        }
    	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(realPath), "UTF-8") );
        StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }
    protected HashMap<String,String> readExtraKeywords(String path) throws IOException {
        HashMap<String,String> ret = new HashMap<String,String>();
        if ((this.georConfig != null) && (georConfig.activated())) {
            Properties p = georConfig.loadCustomPropertiesFile(extraKeywordsFile);
            for (String key : p.stringPropertyNames()) {
                ret.put(key, p.getProperty(key));
            }
            return ret;
        }
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(path);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            Properties extraProps = new Properties();
            extraProps.load(isr);

            for (String key : extraProps.stringPropertyNames()) {
                ret.put(key, extraProps.getProperty(key));
            }
            return ret;
        } finally {
            if (is != null) is.close();
        }
    }
    private void checkState() {
        if (frozen) {
            throw new IllegalStateException("EmailDefaultParams have already been frozen");
        }
    }

    // -------------- Bean setters/getters -------------- //
    public String getSmtpHost() {
        return smtpHost;
    }
    public void setSmtpHost(String smtpHost) {
        checkState();
        this.smtpHost = smtpHost;
    }
    public int getSmtpPort() {
        return smtpPort;
    }
    public void setSmtpPort(int port) {
        checkState();
        this.smtpPort = port;
    }
    public String getEmailHtml() {
        return emailHtml;
    }
    public void setEmailHtml(String emailHtml) {
        checkState();
        this.emailHtml = emailHtml;
    }
    public String getReplyTo() {
        return replyTo;
    }
    public void setReplyTo(String replyTo) {
        checkState();
        this.replyTo = replyTo;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        checkState();
        this.from = from;
    }
    public String getBodyEncoding() {
        return bodyEncoding;
    }
    public void setBodyEncoding(String bodyEncoding) {
        checkState();
        this.bodyEncoding = bodyEncoding;
    }
    public String getSubjectEncoding() {
        return subjectEncoding;
    }
    public void setSubjectEncoding(String subjectEncoding) {
        checkState();
        this.subjectEncoding = subjectEncoding;
    }
    public String[] getLanguages() {
        return languages;
    }
    public void setLanguages(String[] languages) {
        checkState();
        this.languages = languages;
    }
    public ExpiredArchiveDaemon getExpireDeamon() {
    	return this.expireDeamon;
    }
    public void setExpireDeamon(ExpiredArchiveDaemon deamon){
    	checkState();
    	this.expireDeamon = deamon;
    }
    public void setEmailAckTemplateFile(String emailAckTemplateFile) {
		this.emailAckTemplateFile = emailAckTemplateFile;
	}

	public void setEmailTemplateFile(String emailTemplateFile) {
		this.emailTemplateFile = emailTemplateFile;
	}

	public void setExtraKeywordsFile(String extraKeywordsFile) {
	    this.extraKeywordsFile = extraKeywordsFile;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
}
