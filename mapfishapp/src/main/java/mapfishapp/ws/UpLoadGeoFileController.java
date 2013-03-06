/**
 * 
 */
package mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mapfishapp.ws.upload.FileDescriptor;
import mapfishapp.ws.upload.UpLoadFileManegement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;


/**
 * This controller is responsible for uploading a Zip file which contains a set of geofiles (shp, mid, mif). 
 * 
 * <pre>
 * In case of success, returns
 * 		{"success":"true","geojson":"{"type":"FeatureCollection","features":[...]}"} 
 * with 
 * 		Content-Type: text/html (cf introductory paragraph regarding file uploads 
 *      in http://docs.sencha.com/ext-js/3-4/?print=/api/Ext.form.BasicForm)
 * </pre> 
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
@RequestMapping("/togeojson/*")
public final class UpLoadGeoFileController {
	
	private static final Log LOG = LogFactory.getLog(UpLoadGeoFileController.class.getPackage().getName());
	
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
				return "{\"success\": \"true\", \"geojson\":" + jsonFeatures+"}"; 
			}
			
		},
		unsupportedFormat{
			@Override
			public String getMessage( final String detail){return "{\"success\":false, \"msg\": \"unsupported file type\"}"; }
		}, 
		sizeError{
			@Override
			public String getMessage(String detail) {
				return "{\"success\": \"false\", \"msg\": \"file exceeds the limit\" "	+ detail + "}";
			}
		},
		multiplefiles{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"msg\": \"multiple files\"}"; }
		}, 
		incompleteMIF{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"msg\": \"incomplete MIF/MID\"}"; }
		}, 
		incompleteSHP{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"msg\": \"incomplete shapefile\"}"; }
		}, 
		incompleteTAB{
			@Override
			public String getMessage( final String detail){return "{\"success\": \"false\", \"msg\": \"incomplete TAB file\"}"; }
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
		public abstract String getMessage( final String detail);
		
		public  String getMessage(){ return getMessage("");};
		
	}	

	// constants configured in the ws-servlet.xml file
	private String responseCharset;
	private String tempDirectory;

	private long zipSizeLimit;
	private long kmlSizeLimit;
	private long gpxSizeLimit;
	private long gmlSizeLimit;

	
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
	@RequestMapping(method = RequestMethod.POST)
	public void toGeoJson(HttpServletRequest request, HttpServletResponse response) throws IOException {

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
			MultipartFile upLoadFile =multipartRequest.getFile(fileName);
			FileDescriptor currentFile = createFileDescriptor(upLoadFile.getOriginalFilename());
			// process the uploaded file
			Status st = Status.ready;

			workDirectory = makeDirectoryForRequest(this.tempDirectory);
			
			// validate the format
			if( ! currentFile.isValidFormat() ) {
				writeResponse(response, Status.unsupportedFormat);
				return;
			}
			// validate the size
			long limit = getSizeLimit(currentFile.originalFileExt);
			if(  upLoadFile.getSize()  > limit ){
				
				long size = limit / 1048576; // converts to Mb
				final String msg = Status.sizeError.getMessage( size + "MB");
				
				writeResponse(response, Status.sizeError, msg);
				return;
			}
			// save the file in the temporal directory
			UpLoadFileManegement fileManagement = new UpLoadFileManegement(currentFile, workDirectory);

			fileManagement.save(upLoadFile);
				
			// if the uploaded file is a zip file then checks its content
			if(fileManagement.containsZipFile()){
				
				fileManagement.unzip();

				st  = checkGeoFiles(fileManagement);
				if( st != Status.ok ){
					writeResponse(response, st);
					return;
				}
			}
			
			// create a CRS object from the srs parameter
			CoordinateReferenceSystem crs = null;
			try {
				String srsParam = request.getParameter("srs");
				if(!"".equals(srsParam)){
					crs = CRS.decode(srsParam);
				}
			} catch (NoSuchAuthorityCodeException e) {
				LOG.error(e.getMessage());
				throw new IllegalArgumentException(e);
			} catch (FactoryException e) {
				LOG.error(e.getMessage());
				throw new IOException(e);
			}
			
			// encode the feature collection as json string
			String jsonFeatureCollection = (crs != null) 
						?fileManagement.getFeatureCollectionAsJSON(crs)
						:fileManagement.getFeatureCollectionAsJSON();

			writeResponse(response, Status.ok, jsonFeatureCollection);
		
		} catch (IOException e) {
			LOG.error(e);
			throw new IOException(e);
		} finally{
			if(workDirectory!= null) cleanTemporalDirectory(workDirectory);
		}
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
		
		if("zip".equalsIgnoreCase(fileExtension)){
			
			return this.zipSizeLimit;
			
		} else if("kml".equalsIgnoreCase(fileExtension) ){
			
			return this.kmlSizeLimit;
			
		} else if("gpx".equalsIgnoreCase(fileExtension) ){
			
			return this.gpxSizeLimit;
			
		} else if("gml".equalsIgnoreCase(fileExtension) ){
			
			return this.gmlSizeLimit;
			
		} else {
			throw new IllegalArgumentException("Unsupported format");
		}
	}


	/**
	 * Writes in the response object the message taking into account the process {@link Status}.
	 * Additionally the working directory is removed.
	 * 
	 * @param response
	 * @param st
	 * @param errorDetail 
	 * 
	 * @throws IOException
	 */
	private void writeResponse( HttpServletResponse response, final Status st, final String errorDetail) throws IOException {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			response.setCharacterEncoding(responseCharset);
			response.setContentType("text/html");

			String statusMsg;
			if("".equals(errorDetail)){
				statusMsg = st.getMessage();
			} else {
				statusMsg = st.getMessage(errorDetail);
			}
			out.println(statusMsg);
			if(LOG.isDebugEnabled()){
				LOG.debug("RESPONSE:" + statusMsg);
			} 

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
	private void writeResponse( HttpServletResponse response, final Status st) throws IOException {
		
		writeResponse(response, st, "");
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
	private Status checkGeoFiles(UpLoadFileManegement fileManagement) {
		//a zip file is unzipped to a temporary place and *.SHP, *.shp, *.MIF, *.MID, *.mif, *.mid files are looked for at the root of the archive. If several SHP files are found or several MIF or several MID, the error message is "multiple files"
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
