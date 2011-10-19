package extractorapp.ws.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.json.JSONException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.web.util.WebUtils;

import extractorapp.ws.CompleteEmailParams;
import extractorapp.ws.extractor.wcs.WcsFormat;

/**
 * Thread responsible for downloading all the data for a single request and
 * emailing the link for obtaining the data.
 * 
 * @author jeichar
 */
public class ExtractorThread extends Thread {
    private static final Log       LOG = LogFactory.getLog(ExtractorThread.class.getPackage().getName());

    private static final int                  EXTRACTION_ATTEMPTS = 3;
    private final List<ExtractorLayerRequest> _requests;
    private final UUID                        _requestUuid;
    private final CompleteEmailParams         _emailParams;
    private final ServletContext              _servletContext;
    private final boolean                     _testing;
    private final String _username;
    private final String _roles;
    private final UsernamePasswordCredentials _adminCredentials;
    private final String _secureHost;
    private final long maxCoverageExtractionSize;

    public ExtractorThread(
            boolean testing, List<ExtractorLayerRequest> requests, ServletContext servletContext, UUID requestUuid,
            CompleteEmailParams emailParams, String username, String roles, UsernamePasswordCredentials adminCredentials,
            String secureHost, long maxCoverageExtractionSize)
            throws NoSuchAuthorityCodeException, MalformedURLException, JSONException,
            FactoryException {
        _requests = requests;
        this._servletContext = servletContext;
        this._requestUuid = requestUuid;
        this._emailParams = emailParams;
        this._testing = testing;
        this._username = username;
        this._roles = roles;
        this._adminCredentials = adminCredentials;
        this._secureHost = secureHost;
        this.maxCoverageExtractionSize = maxCoverageExtractionSize;

        setName(getClass().getSimpleName()+"--"+requestUuid);
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        final File tmpExtractionBundle = mkTmpBundleDir(_requestUuid.toString());
        LOG.info("Starting extraction into directory: "+tmpExtractionBundle);
        
        final File failureFile = new File(tmpExtractionBundle,"failures.html");
        final List<String> successes = new ArrayList<String>();
        final List<String> failures = new ArrayList<String>();
        final List<String> oversized = new ArrayList<String>();
        for (ExtractorLayerRequest request : _requests) {

            int tries = 0;
            while (tries < EXTRACTION_ATTEMPTS) {
                tries ++;
                String name = String.format("%s__%s", request._url.getHost(), request._layerName);
                File layerTmpDir = mkTmpBundleDir(name);
                LOG.info("Attempt "+tries+" for extracting layer: "+request._url+" -- "+request._layerName);

                try {
                    switch (request._owsType) {
                    case WCS:
                        extractWcsLayer(request, layerTmpDir);
                        break;
                    case WFS:
                        extractWfsLayer(request, layerTmpDir);
                        break;
                    default:
                        throw new IllegalArgumentException(request._owsType + " not supported");
                    }
                    for (File from : layerTmpDir.listFiles()) {
                        File to = new File(tmpExtractionBundle,from.getName());
                        FileUtils.moveFile(from, to);
                    }
                    LOG.info("Finished extracting layer: "+request._url+" -- "+request._layerName);
                    tries = EXTRACTION_ATTEMPTS+1;
                    successes.add(name);
                } catch (OversizedCoverageRequestException e) {
                    tries = EXTRACTION_ATTEMPTS+1;  // don't re-try
                    oversized.add(name);
                    handleExtractionException(request, e, failureFile);
                } catch (SecurityException e) {
                    tries = EXTRACTION_ATTEMPTS+1;  // don't re-try

                    try{
                        FileUtils.delete(layerTmpDir);
                    } catch (Throwable t) { /* ignore */ }
                
                    failures.add(name);
                    handleExtractionException(request, e, failureFile);
                } catch (Throwable e) {
                    try{
                        FileUtils.delete(layerTmpDir);
                    } catch (Throwable t) { /* ignore */ }
                    
                    if(tries >= EXTRACTION_ATTEMPTS) {
                        failures.add(name);
                        handleExtractionException(request, e, failureFile);
                    }
                }
            }
        }

        closeFailuresFile(failureFile);
        
        File archive = archiveExtraction(tmpExtractionBundle);
        long end = System.currentTimeMillis();
        
        String msg = String.format("Finished extraction into directory: %s achive is: %s \nExtraction took %s", tmpExtractionBundle, archive, time(start,end));
        LOG.info(msg);
        
        FileUtils.delete(tmpExtractionBundle);
        if(!_testing){
            try {
                emailNotice(successes,failures,oversized);
            } catch (Throwable e) {
                handleException(e);
            }
        }
    }

    private String time(long start, long end) {
        long seconds = (end - start) / 1000;
        if(seconds > 60) {
            long minutes = seconds / 60;
            seconds = seconds - (minutes*60);
            if(minutes > 24) {
                long hours = minutes / 24;
                minutes = minutes - (hours*24);
                return (hours + " hour "+minutes +" min");
            }
            return minutes+" min "+seconds+" sec";
        }
        
        
        return seconds + " seconds";
    }

    // ----------------- support methods ----------------- //
    /**
     * Protected to allow unit test to override
     */
    protected File mkTmpBundleDir(String name) {
        File tmpDir = WebUtils.getTempDir(_servletContext);

        File tmpExtractionBundle = new File(tmpDir, name);
        tmpExtractionBundle.mkdirs();
        return tmpExtractionBundle;
    }
    /**
     * Protected to allow unit test to override
     * @return 
     */
    protected File archiveExtraction(File tmpExtractionBundle) {
        String filename = _requestUuid.toString()+ExtractorController.EXTRACTION_ZIP_EXT;
        File storageFile = FileUtils.storageFile(filename);
        if(!storageFile.getParentFile().exists()) {
            storageFile.getParentFile().mkdirs();
        }
        try {
            FileUtils.archiveToZip(tmpExtractionBundle, storageFile);
        } catch (IOException e1) {
            handleException(e1);
        }
        return storageFile;
    }

    private void handleExtractionException(ExtractorLayerRequest request, Throwable e, File failureFile) {
        if (!failureFile.getParentFile().exists()) {
            throw new AssertionError("The temporary extraction bundle directory: " + failureFile.getParentFile()
                    + " does not exist");
        }
        
        
        String msg = "Exception occurred while extracting data";
        LOG.error(msg , e);
        
	    openFailuresFile(failureFile);
	    String message = String.format("<li>Erreur d'accès à la couche: %s \n" +
	    		"  <ul>\n" +
	    		"    <li>Serveur: %s</li>\n" +
	            "    <li>Couche: %s</li>\n" +
	            "    <li>Exception (à destination de l'administrateur): %s</li>\n" +
	    		"  </ul>\n" +
	    		"</li>\n",
	            request._layerName, request._url, request._layerName, e);
	    writeToFile(failureFile, message, true);
	}

	private void openFailuresFile(File failureFile) {
	    if (!failureFile.exists()) {
	        String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	        		"<html>\n" +
	        		"<head>\n" +
	        		"<title>" +
	        		"Erreurs lors de l'extraction" +
	        		"</title>\n" +
	        		"</head><body>\n" +
	        		"L'extraction a échoué pour certaines données.  " +
	        		"Contacter l'administrateur concernant les serveurs/couches suivants\n" +
	        		"\n\nToutes les couches ont été contactées "+EXTRACTION_ATTEMPTS+" fois afin d'extraire les données.\n\n"+ 
	        		"<ul>";
	        writeToFile(failureFile, msg, false);
	    }
	}

    private void closeFailuresFile(File failureFile) {
        if (failureFile.exists()) {
            writeToFile(failureFile, "</ul></body></html>", true);
        }
    }

    private void writeToFile(File failureFile, String message, boolean append) {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(failureFile, append);
            writer.write(message.getBytes("UTF-8"));
        } catch (IOException e1) {
            handleException(e1);
        } finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e2) {
                    handleException(e2);
                }
            }
        }
    }
    
    private void handleException(Throwable e1) {
        // TODO handle failure. What am I supposed to do about it?
        e1.printStackTrace();
    }
    
    private void emailNotice(List<String> successes, List<String> failures, List<String> oversized) throws MessagingException {
        String[] languages = _emailParams.getLanguages();
        final Properties props = System.getProperties();
        props.put("mail.smtp.host", _emailParams.getSmtpHost());
        props.put("mail.protocol.port", _emailParams.getSmptPort());
        final Session session = Session.getInstance(props, null);
        final MimeMessage message = new MimeMessage(session);
        if (isValidEmailAddress(_emailParams.getFrom())) {
            message.setFrom(new InternetAddress(_emailParams.getFrom()));
        }
        String[] recipients = _emailParams.getRecipients();
        boolean validRecipients = false;
        for (String recipient : recipients) {
            if(isValidEmailAddress(recipient)){
                validRecipients = true;
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
        }
        
        if (!validRecipients) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(_emailParams.getFrom()));
            message.setSubject("[ERREUR] Message non délivré : " + _emailParams.getSubject(), _emailParams.getSubjectEncoding());
        } else {
            message.setSubject(_emailParams.getSubject(), _emailParams.getSubjectEncoding());
        }
        

        Multipart multipart = new MimeMultipart();

        if (_emailParams.getMessage() != null) {
            MimeBodyPart bodyPart = new MimeBodyPart();
            String msg = _emailParams.getMessage();
            msg = msg.replace("{successes}", format(successes));
            msg = msg.replace("{failures}", format(failures));
            msg = msg.replace("{oversized}", format(oversized));
            bodyPart.setText(msg, _emailParams.getBodyEncoding(), "html");
            bodyPart.setContentLanguage(languages);
            multipart.addBodyPart(bodyPart);
        }

        message.setContent(multipart);
        LOG.debug("preparing to send extraction email");
        Transport.send(message);
        LOG.debug("extraction email has been sent to:\n"+Arrays.toString(recipients));

    }

    private String format(List<String> list) {
        if(list.isEmpty()){
            return "<p>aucune</p>";
        }
        StringBuilder b = new StringBuilder("<ul>");
        for (String string : list) {
            b.append("<li>");
            b.append(string);
            b.append("</li>");
        }
        b.append("</ul>");

        return b.toString();
    }

    private static boolean isValidEmailAddress(String address) {
        if (address == null) {
            return false;
        }

        boolean hasCharacters = address.trim().length() > 0;
        boolean hasAt = address.contains("@");

        if (!hasCharacters || !hasAt)
            return false;

        String[] parts = address.trim().split("@", 2);

        boolean mainPartNotEmpty = parts[0].trim().length() > 0;
        boolean hostPartNotEmpty = parts[1].trim().length() > 0;
        return mainPartNotEmpty && hostPartNotEmpty;
    }


    private void extractWcsLayer(ExtractorLayerRequest request, File requestBaseDir) throws IOException,
            TransformException, FactoryException {
        WcsExtractor extractor = new WcsExtractor(requestBaseDir, new WcsFormat(maxCoverageExtractionSize), 
                _adminCredentials.getUserName(), _adminCredentials.getPassword(), _secureHost);
        extractor.checkPermission(request, _secureHost, _username, _roles);
        extractor.extract(request);
    }

    private void extractWfsLayer(ExtractorLayerRequest request, File requestBaseDir) throws IOException,
            TransformException, FactoryException {
        WfsExtractor extractor = new WfsExtractor(requestBaseDir, new WFSDataStoreFactory(), 
                _adminCredentials.getUserName(), _adminCredentials.getPassword(), _secureHost);
        
        extractor.checkPermission(request, _secureHost, _username, _roles);

        extractor.extract(request);
    }
    
}

