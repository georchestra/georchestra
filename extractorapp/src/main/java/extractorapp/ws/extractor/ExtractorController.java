package extractorapp.ws.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletContextAware;

import extractorapp.ws.CompleteEmailParams;
import extractorapp.ws.EmailDefaultParams;
import extractorapp.ws.SharedConstants;
import extractorapp.ws.acceptance.CheckFormAcceptance;
import extractorapp.ws.extractor.task.ExtractionManager;
import extractorapp.ws.extractor.task.ExtractionTask;

/**
 * Controller for the Extractor
 * 
 * @author jeichar
 */
@Controller
public class ExtractorController implements ServletContextAware {
    public static final String EXTRACTION_ZIP_EXT = "-extraction.zip";

    private static final Log      LOG = LogFactory.getLog(ExtractorController.class.getPackage().getName());

    private static final String   BASE_MAPPING      = "/extractor/";
    private static final String   EXTRACTOR_MAPPING = BASE_MAPPING + "initiate";
    private static final String   TEST_EXTRACTOR_MAPPING = BASE_MAPPING + "test/initiate";
    private static final String   RESULTS_MAPPING   = BASE_MAPPING + "package";
    private static final String   UUID_PARAM = "uuid";

    private String                      responseTemplateFile;
    private String                      reponseMimeType;
    private String                      responseCharset;
    private EmailDefaultParams          emailDefaults;
    private String                      emailAckTemplateFile;
    private String                      emailTemplateFile;
    private String                      emailSubject;
    private ServletContext              servletContext;
    private String                      servletUrl;
    private String                      extractionFolderPrefix;
    private boolean                     remoteReproject = true;
    private boolean                     useCommandLineGDAL = false;

    private UsernamePasswordCredentials adminCredentials;
    private String secureHost;
    private long maxCoverageExtractionSize = Long.MAX_VALUE;     
    private CheckFormAcceptance checkFormAcceptance;
    
    private ExtractionManager extractionManager;
    
    public void validateConfig() {
    	if(extractionManager==null) {
            throw new AssertionError("A extractionManager needs to be defined in spring configuration");    		
    	}
        if(SharedConstants.inProduction()){
            File storageFile = FileUtils.storageFile("");
            if(!storageFile.exists()){
                if(!storageFile.mkdirs()){
                    throw new AssertionError("extractorapp does not have access to "+storageFile+" and cannot create it");
                }
            }
        }
    }
    
    @RequestMapping(value = RESULTS_MAPPING, method = RequestMethod.GET)
    public void results(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uuid = request.getParameter(UUID_PARAM);
        File file = FileUtils.storageFile(uuid+EXTRACTION_ZIP_EXT);
        
        if(file.exists()) {
            LOG.info("request for extraction archive: "+file+" requested by "+request.getRemoteAddr());
            FileInputStream in = new FileInputStream(file);
            ServletOutputStream out = response.getOutputStream();
            try {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition","attachment; filename="+extractionFolderPrefix+uuid+".zip");
                in.getChannel().transferTo(0, file.length(), Channels.newChannel(out));
            }finally{
                try { 
                    in.close();
                } finally {
                    out.close();
                }
            }
        } else {
            LOG.warn("request for a non-existing extraction archive: "+file+" requested by "+request.getRemoteAddr());
            response.sendError(404, "Requested file not found");
        }
    }

    @RequestMapping(value = EXTRACTOR_MAPPING, method = RequestMethod.POST)
    public void extract(HttpServletRequest request, HttpServletResponse response) throws Exception {
        doExtraction(false, request, response);
    }

    @RequestMapping(value = TEST_EXTRACTOR_MAPPING, method = RequestMethod.POST)
    public void testExtract(HttpServletRequest request, HttpServletResponse response) throws Exception {
        doExtraction(true, request, response);
    }

    // ----------------- implementation of extraction ----------------- //

	private void doExtraction(boolean testing, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String postData = FileUtils.asString(request.getInputStream());
		String reponseData = "";
		String sessionId = request.getSession() != null ? request.getSession().getId() : "";
		
		if (checkFormAcceptance.isFormAccepted(sessionId,
				request.getHeader("sec-username"), postData)) {
			UUID requestUuid = UUID.randomUUID();

			StringBuilder url = new StringBuilder(servletUrl);
			url.append(RESULTS_MAPPING);
			url.append("?");
			url.append(UUID_PARAM);
			url.append("=");
			url.append(requestUuid);

			List<ExtractorLayerRequest> requests = Collections
					.unmodifiableList(ExtractorLayerRequest.parseJson(postData));
			if (requests.size() > 0) {
	
				String[] recipients = requests.get(0)._emails;
				String message = replace(readFile(emailTemplateFile),
						url.toString(), recipients);
				CompleteEmailParams emailParams = new CompleteEmailParams(
						emailDefaults, recipients, emailSubject, message);
				String username = request.getHeader("sec-username");
				String roles = request.getHeader("sec-roles");
				RequestConfiguration requestConfig = new RequestConfiguration(requests, requestUuid, emailParams, 
		                servletContext, testing, username, roles, adminCredentials, secureHost, extractionFolderPrefix, maxCoverageExtractionSize, remoteReproject,
		                useCommandLineGDAL);
				ExtractionTask extractor = new ExtractionTask(requestConfig);
				
				LOG.info("Sending mail to user");
				try {
					extractor.emailNotice(readFile(emailAckTemplateFile));
				} catch (Throwable e) {
					LOG.error("Error while sending the notification to the user.", e);
				}
		        LOG.info("Extraction request submitted, request uuid = "+extractor.executionMetadata.getUuid());

				if (testing) {
					extractor.run();
				} else {
					extractionManager.submit(extractor);
				}

				reponseData = replace(readFile(responseTemplateFile),
						url.toString(), recipients);

				response.setCharacterEncoding(responseCharset);
				response.setContentType(reponseMimeType);
			}
		} else {
			reponseData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<response>\n"
					+ "  <message>form not accepted</message>\n"
					+ "  <success>false</success>\n" + "</response>";
		}
		PrintWriter out = response.getWriter();

		try {
			out.println(reponseData);
		} finally {
			out.close();
		}
	}

    // ----------------- JavaBean methods ----------------- //

    
    /**
     * Sets the template used to respond to the user. Each instance of {link}
     * will be replaced with the URL
     */
    public void setResponseTemplateFile(String responseTemplate) throws IOException {
        this.responseTemplateFile = responseTemplate;
    }

    public void setReponseMimeType(String reponseMimeType) {
        this.reponseMimeType = reponseMimeType;
    }

    public void setResponseCharset(String charset) {
        this.responseCharset = charset;
    }

    public void setEmailDefaults(EmailDefaultParams emailDefaults) {
        this.emailDefaults = emailDefaults;
        emailDefaults.freeze();
    }

    /**
     * Sets the template used to will be emailed to the user when extraction is
     * complete.
     * 
     * Each instance of {link} will be replaced with the extraction bundle URL
     */
    public void setEmailTemplateFile(String emailTemplate) throws IOException {
        this.emailTemplateFile = emailTemplate;
    }

    public void setEmailAckTemplateFile(String emailTemplate) throws IOException {
        this.emailAckTemplateFile = emailTemplate;
    }
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
    
    public void setServletUrl(String servletUrl) {
        this.servletUrl = servletUrl;
    }

    public void setAdminCredentials(UsernamePasswordCredentials adminCredentials) {
        this.adminCredentials = adminCredentials;
    }
    
    public void setSecureHost(String secureHost) {
        this.secureHost = secureHost;
    }
    public void setCheckFormAcceptance(CheckFormAcceptance a) {
    	this.checkFormAcceptance = a;
    }

    private String replace(String template, String url, String[] emails) {
        String t = template.replace("{link}", url);
        t = t.replace("{emails}", Arrays.toString(emails));
        return t;
    }

    private String readFile(String responseTemplate) throws IOException {
        String realPath = servletContext.getRealPath(responseTemplate);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(realPath), "UTF-8") );
        StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    public void setMaxCoverageExtractionSize(long maxCoverageExtractionSize) {
        this.maxCoverageExtractionSize = maxCoverageExtractionSize;
    }

    public void setExtractionManager(ExtractionManager extractionManager) {
		this.extractionManager = extractionManager;
	}
    
    public void setRemoteReproject(boolean remoteReproject) {
        this.remoteReproject = remoteReproject;
    }    

	public void setUseCommandLineGDAL(boolean useCommandLineGDAL) {
        this.useCommandLineGDAL = useCommandLineGDAL;
    }
	
	public void setExtractionFolderPrefix(String extractionFolderPrefix) {
	    if(extractionFolderPrefix==null) {
	        this.extractionFolderPrefix = "extraction-";
	    } else {
	        this.extractionFolderPrefix = extractionFolderPrefix;
	    }
    }

    // ----------------- Methods for accessing servlet context ----------------- //
    // ServletContext is required for determining where files are within the 
    // webapp
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}

