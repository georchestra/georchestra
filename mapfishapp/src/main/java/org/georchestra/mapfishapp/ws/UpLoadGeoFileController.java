/**
 *
 */
package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.mapfishapp.ws.upload.FileDescriptor;
import org.georchestra.mapfishapp.ws.upload.FileFormat;
import org.georchestra.mapfishapp.ws.upload.UpLoadFileManagement;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;


/**
 * This controller is responsible for uploading a geofiles and transform their features to json syntax.
 * <pre>
 * In case of success, returns
 * 		{"success":"true","geojson":"{"type":"FeatureCollection","features":[...]}"}
 * with
 * 		Content-Type: text/html (cf introductory paragraph regarding file uploads
 *      in http://docs.sencha.com/ext-js/3-4/?print=/api/Ext.form.BasicForm)
 * </pre>
 *
 * One of the following implementation can be set:
 *
 * <p>
 * <br>OGR Implementation</br> accepts the following files:
 * <lu>
 * <li>ESRI Shape in zip:  shp, shx, prj file are expected </li>
 * <li>MapInfo MIF in zip:  mif, mid file are expected </li>
 * <li>MapInfo TAB in zip: tab, id, map, dat are expected </li>
 * <li>kml</li>
 * <li>gpx</li>
 * <li>gml</li>
 * </lu>
 * </p>
 * <p>
 * <br>Geotools Implementation </br> expects the following files
 * <lu>
 * <li>ESRI Shape in zip:  shp, shx, prj file are expected </li>
 * <li>MapInfo in zip:  mif, mid file are expected </li>
 * <li>kml</li>
 * <li>gml</li>
 * </lu>
 * </p>
 *
 *
 * @author Mauricio Pazos
 *
 */
@Controller
public final class UpLoadGeoFileController implements HandlerExceptionResolver {

	private static final Log LOG = LogFactory.getLog(UpLoadGeoFileController.class.getPackage().getName());

	private static final int MEGABYTE = 1048576;

	/**
	 * Status of the upload process
	 *
	 * @author Mauricio Pazos
	 *
	 */
	public enum Status{
		ok{
			@Override
			public String getMessage( final String jsonFeatures){
				return "{\"success\": \"true\", \"geojson\": }";
			}

		},
		outOfMemoryError{

			@Override
			public String getMessage( final String detail){return "{\"success\":false, \"error\":\"fileupload_error_outOfMemory\", \"msg\": \"out of memory - "+ detail + "\"}"; }
		},
		ioError{
			@Override
			public String getMessage( final String detail){return "{\"success\":false, \"error\":\"fileupload_error_ioError\", \"msg\": \"" + detail + "\"}"; }

		},
		unsupportedFormat{
			@Override
			public String getMessage( final String detail){return "{\"success\":false, \"error\":\"fileupload_error_unsupportedFormat\", \"msg\": \"unsupported file type\"}"; }
		},
		projectionError{
			@Override
			public String getMessage( final String detail){return "{\"success\":false, \"error\":\"fileupload_error_projectionError\", \"msg\": \"Error occured while parsing coordinates: "+ detail + "\"}"; }
		},
		sizeError{
			@Override
			public String getMessage(String detail) {
				return "{\"success\": \"false\", \"error\":\"fileupload_error_sizeError\", \"msg\": \"file exceeds the limit. "	+ detail + "\"}";
			}
		},
		multiplefiles{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"error\":\"fileupload_error_multipleFiles\", \"msg\": \"multiple files\"}"; }
		},
		incompleteMIF{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"error\":\"fileupload_error_incompleteMIF\", \"msg\": \"incomplete MIF/MID\"}"; }
		},
		incompleteSHP{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"error\":\"fileupload_error_incompleteSHP\", \"msg\": \"incomplete shapefile\"}"; }
		},
		incompleteTAB{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"error\":\"fileupload_error_incompleteTAB\", \"msg\": \"incomplete TAB file\"}"; }
		},
		ready{
			@Override
			public String getMessage( final String detail){throw new UnsupportedOperationException("no message is associated to this status");}
		};


		/**
		 * Returns the message associated to this status.
		 *
		 * @return JSON string
		 */
		public abstract String getMessage( final String detail );

		public  String getMessage(){ return getMessage("");};

	}

	// constants configured in the ws-servlet.xml file
	private String responseCharset;
	private String tempDirectory;

	private long zipSizeLimit;
	private long kmlSizeLimit;
	private long gpxSizeLimit;
	private long gmlSizeLimit;
	private long osmSizeLimit;


	private UpLoadFileManagement fileManagement = UpLoadFileManagement.create();


	/**
	 * The current file that was upload an is in processing
	 *
	 * @return {@link FileDescriptor}
	 */
	private FileDescriptor createFileDescriptor(final String fileName){

		return new FileDescriptor(fileName);
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}


	public void setResponseCharset(String responseCharset) {
		this.responseCharset = responseCharset;
	}

	public void setZipSizeLimit(long zipSizeLimit) {
		this.zipSizeLimit = zipSizeLimit;
	}

	public void setKmlSizeLimit(long kmlSizeLimit) {
		this.kmlSizeLimit = kmlSizeLimit;
	}

	public void setGpxSizeLimit(long gpxSizeLimit) {
		this.gpxSizeLimit = gpxSizeLimit;
	}

	public void setGmlSizeLimit(long gmlSizeLimit) {
		this.gmlSizeLimit = gmlSizeLimit;
	}

	public void setOsmSizeLimit(long osmSizeLimit) {
        this.osmSizeLimit = osmSizeLimit;
    }

	/**
	 * Returns the set of file formats which this service can manage.
	 *
	 * <pre>
	 * URL example
	 *
	 * http://localhost:8080/mapfishapp/ws/formats
	 * </pre>
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/formats", method = RequestMethod.GET)
	public void formats(HttpServletRequest request, HttpServletResponse response) throws IOException {

		FileFormat[] formatList = this.fileManagement.getFormatList();

		response.setCharacterEncoding(responseCharset);
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		try {
			out.println(fileFormatListToJSON( formatList));
		} finally {
			out.close();
		}
	}

	public String fileFormatListToJSON() {
		try {
			FileFormat[] formatList = this.fileManagement.getFormatList();
			return fileFormatListToJSON(formatList);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			return "";
		}
	}

	private String fileFormatListToJSON(FileFormat[] formatList) throws IOException {

		try {
			JSONArray jsonFormatArray = new JSONArray();
			for (int i=0; i < formatList.length; i++) {

				jsonFormatArray.put(i, formatList[i].toString());
			}
			return jsonFormatArray.toString();
		} catch (JSONException e) {

			LOG.error(e.getMessage());

			throw new IOException("The file formats aren't available");
		}
	}

	/**
	 * Load the file provide in the request. The content of this file is returned as a json object.
	 * If an CRS is provided the resultant features will be transformed to that CRS before.
	 * <p>
	 * The file is maintained in a temporal store that will be cleaned when the response has be done.
	 * </p>
	 *
	 * @param request The expected parameters are geofile and srs
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/togeojson/*", method = RequestMethod.POST)
	public void toGeoJson(HttpServletRequest request, HttpServletResponse response) throws Exception {

		if( !(request instanceof MultipartHttpServletRequest) ){
			final String msg = "MultipartHttpServletRequest is expected";
			LOG.fatal(msg);
			throw new IOException(msg);
		}

		String workDirectory = null;
		try{
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

			// create the file descriptor using the original file name which is in the multipartRequest object
			Iterator<?> fileNames = multipartRequest.getFileNames();
			if(! fileNames.hasNext() ){
				final String msg = "a file is expected";
				LOG.error(msg);
				throw new IOException(msg);
			}
			String fileName = (String) fileNames.next();
			MultipartFile upLoadFile = multipartRequest.getFile(fileName);
			FileDescriptor currentFile = createFileDescriptor(upLoadFile.getOriginalFilename());
			// process the uploaded file
			Status st = Status.ready;

			workDirectory = makeDirectoryForRequest(this.tempDirectory);

			// validate the format
			if( ! currentFile.isValidFormat() ) {
				writeErrorResponse(response, Status.unsupportedFormat);
				return;
			}
			// validate the size - it's a double-check, since normally
			// a MaxUploadSizeExceededException has already been
			// launched and handled
			long limit = getSizeLimit(currentFile.originalFileExt);
			if(  upLoadFile.getSize()  > limit ){

				long size = limit / 1048576; // converts to Mb
				final String msg = Status.sizeError.getMessage( size + "MB");

				writeErrorResponse(response, Status.sizeError, msg, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
				return;
			}

			// save the file in the temporal directory

			this.fileManagement.setWorkDirectory(workDirectory);
			this.fileManagement.setFileDescriptor(currentFile);

			this.fileManagement.save(upLoadFile);

			// if the uploaded file is a zip file then checks its content
			if (this.fileManagement.containsZipFile()) {
				this.fileManagement.unzip();

				st  = checkGeoFiles(this.fileManagement);
				if (st != Status.ok) {
					writeErrorResponse(response, st);
					return;
				}
			}

			// create a CRS object from the srs parameter
			CoordinateReferenceSystem crs = null;
			try {
				final String crsParam = request.getParameter("srs");
				if( (crsParam != null) && (crsParam.length() > 0) ){
					crs = CRS.decode(crsParam);
				}
			} catch (NoSuchAuthorityCodeException e) {
				LOG.error(e.getMessage());
				throw new IllegalArgumentException(e);
			} catch (FactoryException e) {
				LOG.error(e.getMessage());
				throw new IOException(e);
			}

			// retrieves the feature collection and write the response
			writeOKResponse(response, this.fileManagement, crs);

		} catch (IOException e) {
			LOG.error(e);
			throw new IOException(e);
		} catch (ProjectionException e) {
			LOG.error(e.getMessage());
			throw e;
		}

		finally{
			if(workDirectory!= null) cleanTemporalDirectory(workDirectory);
		}
	}

	/**
	 * Write the features in the response object.
	 * <p>
	 * The output to build is like to
	 *
	 * "{\"success\": \"true\", \"geojson\":" + jsonFeatures+"}"
	 * </p>
	 *
	 * @param response
	 * @param fileManagement
	 * @param crs
	 *
	 * @throws Exception
	 */
	private void writeOKResponse( final HttpServletResponse response, final UpLoadFileManagement fileManagement, final CoordinateReferenceSystem crs) throws Exception {

		StringWriter json_out = new StringWriter();

		response.setCharacterEncoding(responseCharset);
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		try {

			fileManagement.writeFeatureCollectionAsJSON(json_out, crs);

			// builds the following response: "{\"success\": \"true\", \"geojson\":" + jsonFeatures+"}");
			out.print("{\"success\": \"true\", \"geojson\":");
			out.print(json_out.toString());
			out.println("}");

			out.flush();

			if(LOG.isDebugEnabled()){
				LOG.debug("RESPONSE: OK");
			}
		} catch(OutOfMemoryError e){
			writeErrorResponse(response, Status.outOfMemoryError, buildOutOfMemoryErrorMessage(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		} catch (IOException e) {
			writeErrorResponse(response, Status.ioError, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ProjectionException e) {
			writeErrorResponse(response, Status.projectionError, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if(out != null) out.close();
		}
	}


	/**
	 * Writes in the response object the message taking into account the process {@link Status}.
	 * Additionally the working directory is removed.
	 *
	 * @param response
	 * @param st
	 * @param errorDetail
	 * @param responseStatusError
	 *
	 * @throws IOException
	 */
	private void writeErrorResponse( HttpServletResponse response, final Status st, final String errorDetail, final int responseStatusError)  {
		response.reset();
		PrintWriter out = null;
		try {
			out = response.getWriter();
			response.setCharacterEncoding(responseCharset);
			response.setContentType("text/html");
			response.setStatus(responseStatusError);

			String statusMsg;
			if("".equals(errorDetail)){
				statusMsg = st.getMessage();
			} else {
				statusMsg = st.getMessage(errorDetail);
			}
			out.println(statusMsg);
			out.flush();

			if(LOG.isDebugEnabled()){
				LOG.debug("RESPONSE:" + statusMsg);
			}

		} catch (IOException e) {

			LOG.error(e.getMessage());

        } finally {

			if(out != null) out.close();
		}
	}

	/**
	 * writes the response using the information provided as parameter
	 *
	 *
	 * @param response
	 * @param st
	 * @param workDirectory
	 * @throws IOException
	 */
	private void writeErrorResponse( HttpServletResponse response, final Status st) throws IOException {

		writeErrorResponse(response, st, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}


	/**
	 * Builds the out of memory Error
	 * @return out of memory error message
	 */
	private String buildOutOfMemoryErrorMessage() {

		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		final long max = memoryMXBean.getHeapMemoryUsage().getMax() / MEGABYTE;
		final long used = memoryMXBean.getHeapMemoryUsage().getUsed() / MEGABYTE;
		final String msg = Status.outOfMemoryError.getMessage("There is not enough memory. Maximum = " + max + "Mb, Used = " + used + " Mb.");

		LOG.error(msg);

	    return msg;
    }

	/**
	 * Handles the exception throws by the {@link CommonsMultipartResolver}. A response error will be made if the size of the uploaded file
	 * is greater than the configured maximum (see ws-servlet.xml for more details).
	 */
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {

			LOG.error(exception.getMessage());

			if (exception instanceof MaxUploadSizeExceededException) {

				MaxUploadSizeExceededException sizeException = (MaxUploadSizeExceededException) exception;
				long size = sizeException.getMaxUploadSize() / MEGABYTE; // converts to Mb
				writeErrorResponse(
				        response,
				        Status.sizeError,
				        "The configured maximum size is " + size + " MB. ("+sizeException.getMaxUploadSize()+" bytes)",
				        HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			} else {

				writeErrorResponse(response, Status.ioError, exception.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}

		return null ;
	}

	/**
	 * Returns the size limit (bytes) taking into account the file format.
	 *
	 * @param fileExtension
	 *
	 * @return the limit
	 */
	private long getSizeLimit(final String fileExtension) {

		if( this.zipSizeLimit <=  0 ) throw new IllegalStateException("zipSizeLimit was not set");
		if( this.kmlSizeLimit <=  0 ) throw new IllegalStateException("kmlSizeLimit was not set");
		if( this.gpxSizeLimit <=  0 ) throw new IllegalStateException("gpxSizeLimit was not set");
		if( this.gmlSizeLimit <=  0 ) throw new IllegalStateException("gmlSizeLimit was not set");
        if( this.osmSizeLimit <=  0 ) throw new IllegalStateException("osmSizeLimit was not set");

		if("zip".equalsIgnoreCase(fileExtension)){

			return this.zipSizeLimit;

		} else if("kml".equalsIgnoreCase(fileExtension) ){

			return this.kmlSizeLimit;

		} else if("gpx".equalsIgnoreCase(fileExtension) ){

			return this.gpxSizeLimit;

		} else if("gml".equalsIgnoreCase(fileExtension) ){

			return this.gmlSizeLimit;

		} else if ("osm".equalsIgnoreCase(fileExtension) ){

            return this.osmSizeLimit;

		}else {
			throw new IllegalArgumentException("Unsupported format");
		}
	}


	/**
	 * Creates a work directory for this request. An
	 * @param tempDirectory
	 * @return
	 * @throws IOException
	 */
	private String makeDirectoryForRequest(String tempDirectory) throws IOException{

		// create a temporal root directory if it doesn't exist
		File root = new File(tempDirectory );
		if(!root.exists()){
			root.mkdirs();
		}
		String pathname = tempDirectory + File.separator +  UUID.randomUUID();
		File requestDirectory = new File(pathname);
		Boolean succeed = requestDirectory.mkdir();
		if(!succeed ){
			throw new IOException("cannot create the directory " + pathname);
		}

		String workDirectory = requestDirectory.getAbsolutePath();

		return workDirectory;
	}


	private void cleanTemporalDirectory(String workDirectory) throws IOException{
		File file = new File(workDirectory);
		FileUtils.cleanDirectory(file);
		boolean removed = file.delete();
		if(!removed ) throw new IOException("cannot remove the directory: " + file.getAbsolutePath());
	}

	/**
	 * Checks the content of zip file.
	 *
	 * @param fileManagement
	 * @return
	 */
	private Status checkGeoFiles(UpLoadFileManagement fileManagement) {
		// a zip file is unzipped to a temporary place and *.SHP, *.shp, *.MIF, *.MID, *.mif, *.mid files are looked for at the root of the archive.
		// If several SHP files are found or several MIF or several MID, the error message is "multiple files"
		if( ! fileManagement.checkGeoFileExtension() ) {
			return Status.unsupportedFormat;
		}

		if( ! fileManagement.checkSingleGeoFile() ){
			return Status.multiplefiles;
		}

		if( fileManagement.isMIF() ){
			// if filename.mif is found, it is assumed that filename.mid exists too. If not: msg = "incomplete mif/mid
			if( !fileManagement.checkMIFCompletness() ){
				return Status.incompleteMIF;
			}

		} else if( fileManagement.isSHP() ){
			// if filename.shp is found, it is assumed that filename.shx and filename.prj are also present (the DBF is not mandatory). If not: msg = "incomplete shapefile"
			if( !fileManagement.checkSHPCompletness()){
				return Status.incompleteSHP;
			}
		} else if( fileManagement.isTAB() ){
			if( !fileManagement.checkTABCompletness() ){
				return Status.incompleteTAB;
			}
		}

		return Status.ok;
	}


}
