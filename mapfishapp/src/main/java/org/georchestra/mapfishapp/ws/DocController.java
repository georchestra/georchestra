/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.mapfishapp.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.mapfishapp.ws.classif.ClassifierCommand;
import org.georchestra.mapfishapp.ws.classif.SLDClassifier;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.common.annotations.VisibleForTesting;

// TODO: KML, GML, CSV services: do not store files, this is useless
// instead, implement a generic "echo" service with custom mime type, as specified by client.

/**
 * This controller represents the entry point to access RESTful document services.
 * This REST service provides an indirection point to save a file server side generated on the client side and a solution to get it back. <br />
 * Avalaible services: <br />
 * - POST: ws/wmc/ GET: ws/wmc/{filename} <br />
 * - POST: ws/csv/ GET: ws/csv/{filename} <br />
 * - POST: ws/sld/ GET: ws/sld/{filename} <br />
 * - POST: ws/kml/ GET: ws/kml/{filename} <br />
 * - POST: ws/gml/ GET: ws/gml/{filename} <br />
 * - POST: ws/fe/  GET: ws/fe/{filename}  <br />
 * - POST: ws/wkt/  GET: ws/wkt/{filename}  <br />
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

	/** the temporary directory used by the document services*/
    private String docTempDir;

    @Autowired
    public GeorchestraConfiguration georchestraConfiguration;

    /** the connection pool used by the document services*/
    @Autowired
    private DataSource connectionPool;

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

    /**
     * Absolute (from domain name) URL path where the kml service can be called
     */
    public static final String KML_URL = DOC_URL + "kml/";

    /**
     * Absolute (from domain name) URL path where the gml service can be called
     */
    public static final String GML_URL = DOC_URL + "gml/";

    /**
     * Absolute (from domain name) URL path where the fe service can be called
     */
    public static final String FE_URL = DOC_URL + "fe/";

    /**
     * Absolute (from domain name) URL path where the fe service can be called
     */
    public static final String WKT_URL = DOC_URL + "wkt/";


    public void init() throws IOException {
        if (georchestraConfiguration.activated()) {
            docTempDir = georchestraConfiguration.getProperty("docTempDir");

            Properties userPasswordCreds = georchestraConfiguration.loadCustomPropertiesFile("credentials");
            credentials.clear();
            for (String key : userPasswordCreds.stringPropertyNames()) {
                credentials.put(key, new UsernamePasswordCredentials(userPasswordCreds.getProperty(key)));
            }
        }
    }

    public String getDocTempDir() {
        return docTempDir;
    }

	public void setDocTempDir(String docTempDir) {
	    this.docTempDir = docTempDir;
	}


	// needed for tests
	public @VisibleForTesting void setConnectionPool(DataSource cp) {
	    connectionPool = cp;
    }

	private WFSDataStoreFactory factory = new WFSDataStoreFactory();
	public void setWFSDataStoreFactory(WFSDataStoreFactory fac) { factory = fac; }

	/**
	 * mapping from hostname -> credentials
	 */
	private Map<String, UsernamePasswordCredentials> credentials = new HashMap<String, UsernamePasswordCredentials>();
	public Map<String, UsernamePasswordCredentials> getCredentials() {
	    return credentials;
	}
	public void setCredentials(Map<String, UsernamePasswordCredentials> credentials) {
	    this.credentials = credentials;
	}
    
    /*=======================Services entry points==========================================================================*/

    /*======================= WMC ======================================================================*/

    /**
     * POST WMC entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the wmc file
     * @param response contains the url path to get back the file: WMC_URL/{filename}
     */
    @RequestMapping(value="/wmc/", method=RequestMethod.POST)
    public void storeWMCFile(HttpServletRequest request, HttpServletResponse response) {
        storeFile(new WMCDocService(this.docTempDir, this.connectionPool), WMC_URL, request, response);
    }

    /**
     * GET WMC entry point. Retrieve the right file previously stored corresponding to the REST argument. <br />
     * @param request no parameter. The parameter has to be provided REST style: WMC_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/wmc/*", method=RequestMethod.GET)
    public void getWMCFile(HttpServletRequest request, HttpServletResponse response) {
        getFile(new WMCDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /**
     * Get a list of WMC stored by connected user
     * @param request used to retrieve connected user
     * @param response contains a list of WMC doc created by current user
     */
    @RequestMapping(value="/wmcs.json", method=RequestMethod.GET)
    public void getAllWMC(HttpServletRequest request, HttpServletResponse response){
        getFilesList(new WMCDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /**
     * DELETE WMC entry point. Delete wmc from DB corresponding to the REST argument. <br />
     * @param request no parameter. The parameter has to be provided REST style: WMC_URL/{filename}
     * @param response empty
     */
    @RequestMapping(value="/wmc/*", method=RequestMethod.DELETE)
    public void deleteWMCFile(HttpServletRequest request, HttpServletResponse response) {
        deleteFile(new WMCDocService(this.docTempDir, this.connectionPool), request, response);
    }
    /*======================= KML =====================================================================*/
    /**
     * POST KML entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the file in the JSON format
     * @param response contains the url path to get back the file in CSV: KML_URL/{filename}
     */
    @RequestMapping(value="/kml/", method=RequestMethod.POST)
    public void storeKMLFile(HttpServletRequest request, HttpServletResponse response) {
        storeFile(new KMLDocService(this.docTempDir, this.connectionPool), KML_URL, request, response);
    }

    /**
     * GET KML entry point. Retrieve the right file previously stored corresponding to the REST argument.
     * @param request no parameter. The parameter has to be provided REST style: KML_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/kml/*", method=RequestMethod.GET)
    public void getKMLFile(HttpServletRequest request, HttpServletResponse response) {
        getFile(new KMLDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /*======================= GML =====================================================================*/
    /**
     * POST GML entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the file in the JSON format
     * @param response contains the url path to get back the file in CSV: GML_URL/{filename}
     */
    @RequestMapping(value="/gml/", method=RequestMethod.POST)
    public void storeGMLFile(HttpServletRequest request, HttpServletResponse response) {
        storeFile(new GMLDocService(this.docTempDir, this.connectionPool), GML_URL, request, response);
    }

    /**
     * GET GML entry point. Retrieve the right file previously stored corresponding to the REST argument.
     * @param request no parameter. The parameter has to be provided REST style: GML_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/gml/*", method=RequestMethod.GET)
    public void getGMLFile(HttpServletRequest request, HttpServletResponse response) {
        getFile(new GMLDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /*======================= FE ======================================================================*/
    /**
     * POST FE entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the file in the JSON format
     * @param response contains the url path to get back the file in CSV: FE_URL/{filename}
     */
    @RequestMapping(value="/fe/", method=RequestMethod.POST)
    public void storeFEFile(HttpServletRequest request, HttpServletResponse response) {
        storeFile(new FEDocService(this.docTempDir, this.connectionPool), FE_URL, request, response);
    }

    /**
     * GET FE entry point. Retrieve the right file previously stored corresponding to the REST argument.
     * @param request no parameter. The parameter has to be provided REST style: FE_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/fe/*", method=RequestMethod.GET)
    public void getFEFile(HttpServletRequest request, HttpServletResponse response) {
        getFile(new FEDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /*======================= WKT ======================================================================*/
    /**
     * POST WKT entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the file in the JSON format
     * @param response contains the url path to get back the file in CSV: WKT_URL/{filename}
     */
    @RequestMapping(value="/wkt/", method=RequestMethod.POST)
    public void storeWKTFile(HttpServletRequest request, HttpServletResponse response) {
        storeFile(new WKTDocService(this.docTempDir, this.connectionPool), WKT_URL, request, response);
    }

    /**
     * GET WKT entry point. Retrieve the right file previously stored corresponding to the REST argument.
     * @param request no parameter. The parameter has to be provided REST style: WKT_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/wkt/*", method=RequestMethod.GET)
    public void getWKTFile(HttpServletRequest request, HttpServletResponse response) {
        getFile(new WKTDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /*======================= JSON to CSV =====================================================================*/
    /**
     * POST CSV entry point. Store the body of the request POST (or file by upload) in a temporary file.
     * @param request contains in its body the file in the JSON format
     * @param response contains the url path to get back the file in CSV: CSV_URL/{filename}
     */
    @RequestMapping(value="/csv/", method=RequestMethod.POST)
    public void storeCSVFile(HttpServletRequest request, HttpServletResponse response) {
        storeFile(new CSVDocService(this.docTempDir, this.connectionPool), CSV_URL, request, response);
    }

    /**
     * GET CSV entry point. Retrieve the right file previously stored corresponding to the REST argument.
     * @param request no parameter. The parameter has to be provided REST style: CSV_URL/{filename}
     * @param response contains the file content
     */
    @RequestMapping(value="/csv/*", method=RequestMethod.GET)
    public void getCSVFile(HttpServletRequest request, HttpServletResponse response) {
        getFile(new CSVDocService(this.docTempDir, this.connectionPool), request, response);
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

        if(request.getContentType().contains("application/vnd.ogc.sld+xml")) {
            // sld to store
            storeFile(new SLDDocService(this.docTempDir, this.connectionPool), SLD_URL, request, response);
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
        getFile(new SLDDocService(this.docTempDir, this.connectionPool), request, response);
    }

    /*=======================Private Methods==========================================================================*/

    /**
     * Create SLD file. Let handle the generation by a classifier object.
     */
    private void doClassification(HttpServletRequest request, HttpServletResponse response) {
        try {
            // classification based on client request in json
            SLDClassifier c = new SLDClassifier(credentials, new ClassifierCommand(getBodyFromRequest(request)), factory);

            // save SLD content under a file
            SLDDocService service = new SLDDocService(this.docTempDir, this.connectionPool);
            String fileName = service.saveData(c.getSLD(), request.getHeader("sec-username"));

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
     * - file can be sent by upload. Take into account only one file
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
                Iterator<String> it = multipartRequest.getFileNames();
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
            String fileName = docService.saveData(fileContent, request.getHeader("sec-username"));

            // send back to client the url path to retrieve this file later on
            response.setStatus(HttpServletResponse.SC_CREATED); // 201 created, new resource created
            // Note: "text/html" required for http://docs.sencha.com/extjs/3.4.0/#!/api/Ext.form.BasicForm
            response.setContentType("text/html");
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

            // let the specific service retrieve the file stored on the server
            docService.loadFile(fileName);

            // send back the file content
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(docService.getMIMEType()); // MIME type of the file
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + docService.getName() + "\"");
            response.setHeader("Cache-Control", "public, max-age=31536000"); // 1 year
            // see http://stackoverflow.com/questions/3339859/what-is-the-risk-of-having-http-header-cache-control-public
            // there is a tradeoff between privacy and performance here ...
            // Documents like CSV may contain sensitive information => private
            // but we want it to be be fast => cached by proxies => public
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
     * Generate a list of geodoc filtered by standard
     * @param docService Any service implementing A_DocService
     * @param request used to retrieve current username
     * @param response contains a list of docs description in JSON. Format depends on docs type.
     */
    private void getFilesList(A_DocService docService, HttpServletRequest request, HttpServletResponse response) {

        try {
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print(docService.listFiles(request.getHeader("sec-username")).toString(4));
        } catch (Exception e) {
            sendErrorToClient(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

     /**
     * Delete a geodoc based on filename. This method also check that current user is owner of geodoc.
     * @param docService Any service implementing A_DocService
     * @param request used to retrieve current username and filename
     * @param response {"success" : true} if successfully deleted
     */
    private void deleteFile(A_DocService docService, HttpServletRequest request, HttpServletResponse response) {
        String fileName = getFileNameFromURI(request.getRequestURI());
        if(fileName == null) {
            sendErrorToClient(response, HttpServletResponse.SC_BAD_REQUEST, "Could not find the file name in the URL");
            return;
        }

        // remove doc prefix 'geodoc' from filename
        fileName = fileName.replaceAll(A_DocService.DOC_PREFIX, "");

        try {
            docService.deleteFile(fileName, request.getHeader("sec-username"));
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            JSONObject res = new JSONObject();
            res.put("success", true);
            out.println(res.toString(4));
        } catch (Exception e) {
            sendErrorToClient(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
