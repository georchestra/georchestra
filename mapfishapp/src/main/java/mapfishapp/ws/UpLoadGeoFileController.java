/**
 * 
 */
package mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.Iterator;

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
		ready;
		
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
		 * Returns a message using JSON syntax
		 * 
		 * @param st
		 * @param detail detail to be added in the standard message
		 * 
		 * @return JSON string
		 */
		public static String getMessage(Status st, String detail){
			
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
	public FileDescriptor createFileDescriptor(final String fileName){
			
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

			workDirectory = makeDirectoryForSession(this.tempDirectory);
			
			if( ! currentFile.isValidFormat()	 ) {
				writeResponse(response, Status.unsupportedFormat, workDirectory);
				return;
			}
			long limit = getSizeLimit(currentFile.ext);
			if(  upLoadFile.getSize()  > limit ){
				
				long size = limit / 1048576; // converts to Mb
				final String msg = Status.getMessage(Status.sizeError, size + "MB");
				
				writeResponse(response, Status.sizeError, msg, workDirectory);
				return;
			}
			
			UpLoadFileManegement fileManagement = new UpLoadFileManegement(currentFile, workDirectory);

			fileManagement.save(upLoadFile);
				
			if(fileManagement.containsZipFile()){
				
				fileManagement.unzip();

				st  = checkGeoFiles(fileManagement);
				if( st != Status.ok ){
					writeResponse(response, st, workDirectory);
					return;
				}
			}
			
			fileManagement.moveTo( downloadDirectory );

			writeResponse(response, st.ok, workDirectory);
		
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
	 * @param workDirectory 
	 * 
	 * @throws IOException
	 */
	private void writeResponse( HttpServletResponse response, final Status st, final String errorDetail, final String workDirectory) throws IOException {
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

			cleanTemporalDirectory(workDirectory);

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
	private void writeResponse( HttpServletResponse response, final Status st, final String workDirectory) throws IOException {
		
		writeResponse(response, st, "", workDirectory);
	}

	private String makeDirectoryForSession(String tempDirectory) throws IOException{

		// create a temporal root directory if it doesn't exist
		File f = new File(tempDirectory);
		if(!f.exists()){
			f.mkdirs();
		}
		// TODO add a user directory
		
		String workDirectory = f.getAbsolutePath();
		
		return workDirectory;
	}

	private void cleanTemporalDirectory(String tempDirectory) throws IOException{
		FileUtils.cleanDirectory(new File(tempDirectory));
	}

	private Status checkGeoFiles(UpLoadFileManegement fileManagement) {
		// TODO Auto-generated method stub
		
		
		// accepts only zip, kml, gpx, gml files or throws {"success": false, "msg": "unsupported file type"}
		
		//file size limitation is configurable per file-type. Error message: {"success": false, "msg": "file exceeds the limit (8Mb)"}
		
		
		//a zip file is unzipped to a temporary place and *.SHP, *.shp, *.MIF, *.MID, *.mif, *.mid files are looked for at the root of the archive. If several SHP files are found or several MIF or several MID, the error message is "multiple files
		
		// if filename.mif is found, it is assumed that filename.mid exists too. If not: msg = "incomplete mif/mid
		
		// if filename.shp is found, it is assumed that filename.shx and filename.prj are also present (the DBF is not mandatory). If not: msg = "incomplete shapefile"
		
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
		
		return Status.ok;
	}
}
