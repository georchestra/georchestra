package mapfishapp.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mapfishapp.ws.classif.ClassifierCommand;
import mapfishapp.ws.classif.SLDClassifier;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * This controller represents the entry point to access RESTful document services.
 * This REST service provides an indirection point to save a file server side generated on the client side and a solution to get it back. <br />
 * Avalaible services: <br />
 * - POST: ws/wmc/ GET: ws/wmc/{filename} <br />
 * - POST: ws/csv/ GET: ws/csv/{filename} <br />
 * - POST: ws/sld/ GET: ws/sld/{filename} <br />
 * <br />
 * File can be sent via POST or by upload (max one file at a time)
 * <br />
 * <br />
 * The implementation should be modified when Spring 3.0 is released. It provides RESTful tools such as URI templates. <br />
 * It could also make easier to create a DocService Factory that create Doc Services with the service name found in the URL. <br />
 * 
 * @author yoann.buch@gmail.com
 *
 */

// TODO: Implements REST Style controllers when Spring framework 3.0 is released
  
@Controller
public class DocController {
    /**
     * Time (in minutes) before files are purged automatically from DIR
     */
    private int maxDocAgeInMinutes = 60 * 24;
	public int getMaxDocAgeInMinutes() {return maxDocAgeInMinutes;}
	public void setMaxDocAgeInMinutes(int maxDocAgeInMinutes) {this.maxDocAgeInMinutes = maxDocAgeInMinutes;}

	/**
	 * mapping from hostname -> credentials
	 */
	private Map<String, UsernamePasswordCredentials> credentials = new HashMap<String, UsernamePasswordCredentials>();
	public Map<String, UsernamePasswordCredentials> getCredentials() {return credentials;}
	public void setCredentials(Map<String, UsernamePasswordCredentials> credentials) {this.credentials = credentials;}
	
    /**
     * variable name that has to be used on client side
     */
    public static final String FILEPATH_VARNAME = "filepath";
    
    /**
     * Absolute (from domain name) URL path where doc services can be called
     */
    public static final String DOC_URL = "ws/";
    
    /**
     * Absolute (from domain name) URL path where the wmc service can be called
     */
    public static final String WMC_URL = DOC_URL + "wmc/";
    
    /**
     * Absolute (from domain name) URL path where the csv service can be called
     */
    public static final String CSV_URL = DOC_URL + "csv/";
    
    /**
     * Absolute (from domain name) URL path where the sld service can be called
     */
    public static final String SLD_URL = DOC_URL + "sld/";

    
    /*=======================Services entry points==========================================================================*/
    
    /*======================= WMC ======================================================================*/
    
    /**
     * POST WMC entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the wmc file
     * @param response contains the url path to get back the file: WMC_URL/{filename}
     */
    @RequestMapping(value="/wmc/", method=RequestMethod.POST)
    public void storeWMCFile(HttpServletRequest request, HttpServletResponse response) {   
        storeFile(new WMCDocService(maxDocAgeInMinutes), WMC_URL, request, response);   
    }
    
    /**
     * GET WMC entry point. Retrieve the right file previously stored corresponding to the REST argument. <br />
     * @param request no parameter. The parameter has to be provided REST style: WMC_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/wmc/*", method=RequestMethod.GET)
    public void getWMCFile(HttpServletRequest request, HttpServletResponse response) { 
        getFile(new WMCDocService(maxDocAgeInMinutes), request, response);
    }

    /*======================= JSON to CSV =====================================================================*/
    /**
     * POST CSV entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the file in the JSON format
     * @param response contains the url path to get back the file in CSV: CSV_URL/{filename}
     */
    @RequestMapping(value="/csv/", method=RequestMethod.POST)
    public void storeCSVFile(HttpServletRequest request, HttpServletResponse response) {   
        storeFile(new CSVDocService(maxDocAgeInMinutes), CSV_URL, request, response);   
    }
    
    /**
     * GET CSV entry point. Retrieve the right file previously stored corresponding to the REST argument. 
     * @param request no parameter. The parameter has to be provided REST style: CSV_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/csv/*", method=RequestMethod.GET)
    public void getCSVFile(HttpServletRequest request, HttpServletResponse response) { 
        getFile(new CSVDocService(maxDocAgeInMinutes), request, response);
    }
    
    /*======================= SLD =====================================================================*/
    
    /**
     * POST SLD entry point. Create and store a SLD file. 
     * @param request Can contains an SLD file (POST body or upload) or a JSON request to build a classification SLD 
     * (this request must fulfill the ClassifierCommand syntax).
     * @param response contains the url path to get back the file in SLD: SLD_URL/{filename}
     */
    @RequestMapping(value="/sld/", method=RequestMethod.POST)
    public void doSLDPost(HttpServletRequest request, HttpServletResponse response) {   
        
    	/*
    	 * We need anonymous users to be able to use service
    	 * but there is a potential for users to fill up harddrive
    	 * We should address that at some point
        String roles = request.getHeader("sec-roles");
        if((roles == null) || roles.equals("ROLE_ANONYMOUS")) {
            sendErrorToClient(response, HttpServletResponse.SC_UNAUTHORIZED, "Only non anonymous users are allowed to post SLD");
            return;
        }*/

        if(request.getContentType().contains("application/vnd.ogc.sld+xml")) {
            // sld to store
            storeFile(new SLDDocService(maxDocAgeInMinutes), SLD_URL, request, response);   
        }
        else if(request.getContentType().contains("application/json") || request.getContentType().contains("text/json")) {
            // classification based on client request
            doClassification(request, response);
        }
        else {
            sendErrorToClient(response, HttpServletResponse.SC_BAD_REQUEST, "Expected content types: application/vnd.ogc.sld+xml or application/json (classification)");
        }
    }
    
    /**
     * GET SLD entry point. Retrieve the right file previously stored corresponding to the REST argument.
     * @param request o parameter. The parameter has to be provided REST style: SLD_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/sld/*", method=RequestMethod.GET)
    public void getSLDFile(HttpServletRequest request, HttpServletResponse response) { 
        getFile(new SLDDocService(maxDocAgeInMinutes), request, response);
    }
    
    /*=======================Private Methods==========================================================================*/
    
    /**
     * Create SLD file. Let handle the generation by a classifier object. 
     */
    private void doClassification(HttpServletRequest request, HttpServletResponse response) {
        try {
            // classification based on client request in json
            SLDClassifier c = new SLDClassifier(credentials, new ClassifierCommand(getBodyFromRequest(request)));
            
            // save SLD content under a file
            SLDDocService service = new SLDDocService(maxDocAgeInMinutes);
            String fileName = service.saveData(c.getSLD());
            
            PrintWriter out = response.getWriter(); 
            out.println("{\"success\":true,\"" + FILEPATH_VARNAME + "\":\"" + SLD_URL + fileName + "\"}"); 
        } 
        catch (DocServiceException e) {
            sendErrorToClient(response, e.getErrorCode() , e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Store a file sent by the client. 2 modes: <br />
     * - content is stored in POST Body
     * - file can be sent by upload. Take into account only on file
     * @param docService Any service implementing A_DocService
     * @param request request contains in its body the file
     * @param response response contains the url path to get back the file: SERVICE_URL/{filename}
     */
    @SuppressWarnings("unchecked")
    private void storeFile(A_DocService docService, String docUrl, HttpServletRequest request, HttpServletResponse response) {  
        try {
            
            String fileContent;
            
            if(request instanceof MultipartHttpServletRequest)
            {
                // the request is a MultipartHttpServletRequest => one upload occurred
                // execution order => DispatcherServlet => CommonsMultipartResolver => DocController
                // CommonsMultipartResolver wrapped HttpServletRequest into MultipartHttpServletRequest    
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                
                // get file name
                Iterator<String> it = (Iterator<String>) multipartRequest.getFileNames();
                if(!it.hasNext()) {
                    sendErrorToClient(response, HttpServletResponse.SC_BAD_REQUEST, "No file has been sent");
                    return;
                }  
                String fileName = it.next();  
                    
                // get file
                MultipartFile file = multipartRequest.getFile(fileName);
                if(file.isEmpty()) {
                    sendErrorToClient(response, HttpServletResponse.SC_BAD_REQUEST, "Uploaded file is empty");
                    return;
                }

                // get file content
                fileContent = new String(file.getBytes());

            }
            else {
                // service has been called normally: RESTful style 
                
                // extract body from the client request, it should contain the file content
                fileContent = getBodyFromRequest(request);
            }
               
            // let the specific service handles the storage on the server
            // get back the file name under which it is saved
            String fileName = docService.saveData(fileContent);

            // send back to client the url path to retrieve this file later on
            response.setStatus(HttpServletResponse.SC_CREATED); // 201 created, new resource created
            //response.setContentType("application/json; charset=utf-8"); // does not work as expected ... FIXME     
            response.setContentType("text/html; charset=utf-8");
            response.setHeader("Cache-Control", "max-age=0"); // both Cache-Control and Expires are required
            response.setHeader("Expires", "Fri, 19 Jun 1970 14:23:31 GMT"); // which is in the past
            
            PrintWriter out = response.getWriter(); 
            out.println("{\"success\":true,\"" + FILEPATH_VARNAME + "\":\"" + docUrl + fileName + "\"}"); 
        } 
        catch (DocServiceException e) {
            sendErrorToClient(response, e.getErrorCode() , e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param docService Any service implementing A_DocService
     * @param request no parameter. The parameter has to be provided REST style: SERVICE_URL/{filename}
     * @param response contains the file content
     */
    private void getFile(A_DocService docService, HttpServletRequest request, HttpServletResponse response) { 
        try {
            
            // extract file name from the request URI
            // will be simpler and safer with Spring 3.0 - REST features
            String fileName = getFileNameFromURI(request.getRequestURI());
            if(fileName == null) {
                sendErrorToClient(response, HttpServletResponse.SC_BAD_REQUEST, "Could not find the file name in the URL");
                return;
            }
            
            // let the specific service retrieves the file stored on the server
            docService.loadFile(fileName);
            
            // send back the file content
            response.setStatus(HttpServletResponse.SC_OK); 
            response.setContentType(docService.getMIMEType()); // MIME type of the file
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + docService.getName() + "\""); 
            PrintWriter out = response.getWriter(); 
            out.println(docService.getContent());      
        } 
        catch (DocServiceException docExc) {
            sendErrorToClient(response, docExc.getErrorCode() , docExc.toString());
            docExc.printStackTrace();
        }
        catch (Exception e) {
            sendErrorToClient(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "");
            e.printStackTrace();
        }
    }
    
    /**
     * Try to get the filename argument from the URI. Method will be simplified with Spring 3.0
     * @param uri URI of the request
     * @return null: no filename found. Otherwise file name.
     */
    private String getFileNameFromURI(final String uri) {
        
        // get last argument from uri
        String[] splittedUri = uri.split("/");
        String lastArgument = splittedUri[splittedUri.length - 1].trim();

        // last argument has to end with the doc prefix
        if(lastArgument.contains(A_DocService.DOC_PREFIX)) {
            return lastArgument;
        }
        else {
            return null;
        }
    }
    
    /**
     * Extract the content of a POST request
     * @param request POST request
     * @return content of request
     */
    private String getBodyFromRequest(final HttpServletRequest request) {
        StringBuilder strBuilder = new StringBuilder();
        
        try {
            // use stream from the request
            InputStream  response = request.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            
            String sLine;
            while ((sLine = reader.readLine()) != null) {
                strBuilder.append(sLine);
            }
        }
        catch(IOException ioExc) {
            ioExc.printStackTrace();
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }
        
        return strBuilder.toString();
    }
    
    /**
     * Send HTTP error to the given client represented by the response object
     * @param response represent the client output point
     * @param status HTTP error code
     * @param message optional message to be sent to client
     */
    private void sendErrorToClient(final HttpServletResponse response, int status, final String message) {
        if(!response.isCommitted()) {
            try {
                response.sendError(status, message);
            } catch (IOException ioExc) {
                ioExc.printStackTrace();
            }
        }
    }
}
