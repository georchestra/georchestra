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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;


/**
 * This controller is responsible to upload a Zip file which contains a set of geofiles (shp, mid, mif). 
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
	private enum Status{
		ok,
		unsupportedFormat, 
		sizeError, 
		ready, 
		multiplefiles, 
		incompleteMIF, 
		incompleteSHP, 
		incompleteTAB;
		
		/**
		 * convenient method
		 * @see getMessage
		 * @param st
		 * @return JSON string
		 */
		public static String getMessage(Status st){
			return getMessage(st, "");
		}
		/**
		 * Returns the message associated to the status using JSON syntax.
		 * 
		 * @param st process status descriptor
		 * @param detail detail to be added in the standard message
		 * 
		 * @return JSON string
		 */
		public static String getMessage(final Status st, final String detail){
			
			String msg = "";
			switch (st) {
			case ok:
				msg = "{\"success\":true}";
				break;
			case unsupportedFormat:
				msg = "{\"success\":false}";
				break;
			case sizeError:
				msg = "{ \"success\": false, \"msg\": \"file exceeds the limit\" "
						+ detail + "}";
				break;
			case multiplefiles:
				msg = "{ \"success\": false, \"msg\": \"multiple files\" }";
				break;
			case incompleteMIF:
				msg = "{ \"success\": false, \"msg\": \"incomplete MIF/MID\" }";
				break;
			case incompleteSHP:
				msg = "{ \"success\": false, \"msg\": \"incomplete shapefile\" }";
				break;
			case incompleteTAB:
				msg = "{ \"success\": false, \"msg\": \"incomplete TAB file\" }";
				break;
				
			}
			
			return msg;
		}
	}
	

	// controller properties 
	private FileDescriptor currentFile;
	
	// constants configured in the ws-servlet.xml file
	private String responseCharset;
	private String downloadDirectory; 
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
	
	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
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

	@RequestMapping(method = RequestMethod.POST)
	public void handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

    	LOG.info("Request: " + request.getRequestURL() ); 

    	LOG.info("InputStream: " +request.getInputStream());
		
		
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
			if( ! currentFile.isValidFormat()	 ) {
				writeResponse(response, Status.unsupportedFormat);
				return;
			}
			// validate the size
			long limit = getSizeLimit(currentFile.ext);
			if(  upLoadFile.getSize()  > limit ){
				
				long size = limit / 1048576; // converts to Mb
				final String msg = Status.getMessage(Status.sizeError, size + "MB");
				
				writeResponse(response, Status.sizeError, msg);
				return;
			}
			// save the file in the temporal directory
			UpLoadFileManegement fileManagement = new UpLoadFileManegement(currentFile, workDirectory);

			fileManagement.save(upLoadFile);
				
			// it the uploaded file is a zip file then checks its content
			if(fileManagement.containsZipFile()){
				
				fileManagement.unzip();

				st  = checkGeoFiles(fileManagement);
				if( st != Status.ok ){
					writeResponse(response, st);
					return;
				}
			}
			// the uploaded file is OK, it is moved from the temporal directory to the download directory
			fileManagement.moveTo( downloadDirectory );
			
			String jsonFeatureCollection = createFeatureCollection( downloadDirectory );

			// TODO add th feature collection the ok message
			writeResponse(response, st.ok);
		
		} finally{
			if(workDirectory!= null) cleanTemporalDirectory(workDirectory);

		}
	}

	private String createFeatureCollection(String downloadDirectory2) {
		// TODO Auto-generated method stub
		
		//the file SRS is obtained :
		//		from the prj file for shapefiles
		//		directly from the mif/mid files
		//		directly from the GML features
		//		assumed EPSG:4326 for all kml files
		//		assumed EPSG:4326 for all gpx files
		
		
		// In case of success, the web service sends {"success":true,"geojson":"{\"type\":\"FeatureCollection\",\"features\":[...]}"} with Content-Type:application/json; charset=utf-8
		
		// the geojson SRS is set by the "srs" form field (example value: "EPSG:2154")
		// encoding warning:
		//			dbf files may have specific encodings - if possible, autodetect & convert to UTF-8
		//			GML : check for encoding in XML prologue
		//			<?xml version="1.0" encoding="ISO-8859-1"?>
	
		return "";
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
			response.setContentType("application/json");

			String statusMsg;
			if("".equals(errorDetail)){
				statusMsg = Status.getMessage(st );
			} else {
				statusMsg = Status.getMessage(st , errorDetail);
			}
			out.println(statusMsg);
			
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

	private void cleanTemporalDirectory(String tempDirectory) throws IOException{
		FileUtils.cleanDirectory(new File(tempDirectory));
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
