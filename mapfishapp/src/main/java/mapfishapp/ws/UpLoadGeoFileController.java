/**
 * 
 */
package mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	
	/**
	 * Maintain useful file information available in the 
	 * @author Mauricio Pazos
	 *
	 */
	private class FileDescriptor{

		public String originalFileName;
		public String ext;
		
		public FileDescriptor(final String fileName) {

			assert fileName != null;
			
			int dot= fileName.lastIndexOf(".");
			if(dot == -1 ){
				throw new InvalidParameterException("a file name extesion is expected");
			}
			
			ext = fileName.substring(dot + 1);
			originalFileName = fileName;
			
		}

		public String getOriginalFileName() {
			return originalFileName;
		}

		public String getExt() {
			return ext;
		}

		public boolean isValidFormat() {
			assert ext != null;
			return 
			    "zip".equalsIgnoreCase(ext) ||
				"kml".equalsIgnoreCase(ext) ||
				"gpx".equalsIgnoreCase(ext) ||
				"gml".equalsIgnoreCase(ext);			
		}

		public boolean isZipFile() {
			return "zip".equalsIgnoreCase(ext);
		}
		
	}

	private FileDescriptor currentFile = null;
	private String responseCharset;
	private String downloadDirectory; 
	private String tempDirectory; 

	
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

	@RequestMapping(method = RequestMethod.POST)
	public void handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if( !(request instanceof MultipartHttpServletRequest) ){
			final String msg = "MultipartHttpServletRequest is expected";
			LOG.fatal(msg);
			throw new IOException(msg);
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		
		Map<String, Object> fileDescriptor = multipartRequest.getFileMap();
		
		Iterator fileNames = multipartRequest.getFileNames();
		if(! fileNames.hasNext() ){
			final String msg = "a file is expected";
			LOG.error(msg);
			throw new IOException(msg);
		}
		String fileName = (String) fileNames.next();
		MultipartFile upLoadFile =multipartRequest.getFile(fileName);
		FileDescriptor currentFile = createFileDescriptor(upLoadFile.getOriginalFilename());

		Status st = Status.ready;

		String workDirectory = makeDirectoryForSession(this.tempDirectory);
		
		if( ! currentFile.isValidFormat()	 ) {
			writeResponse(response, Status.unsupportedFormat);
			return;
		}

		save(upLoadFile, workDirectory);

		if(currentFile.isZipFile()){
			unzip(upLoadFile, workDirectory );
		}

		st  = checkGeoFiles(workDirectory );
		if( st != Status.ok ){
			writeResponse(response, st);
		}
		
		move(workDirectory, downloadDirectory );
		
		try{
			writeResponse(response, st.ok);
		} finally{
			cleanTemporalDirectory(tempDirectory);
		}
	}

	/**
	 * Writes in the response object the message taking into account the process {@link Status}.
	 * 
	 * @param response
	 * @param st 
	 * @throws IOException
	 */
	private void writeResponse( HttpServletResponse response, final Status st) throws IOException {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			response.setCharacterEncoding(responseCharset);
			response.setContentType("application/json");
			
			String statusMsg = Status.getMessage(st);
			out.println(statusMsg);
		} finally {
			if(out != null) out.close();
		}
	}

	private String makeDirectoryForSession(String tempDirectory) throws IOException{
		// TODO Auto-generated method stub
		return tempDirectory;
	}

	private void cleanTemporalDirectory(String tempDirectory) throws IOException{
		// TODO Auto-generated method stub
		
	}

	private void move(String tempDirecory, String downloadDirectory) throws IOException{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Only zip, kml, gpx and gml file are accepted.
	 *  
	 * @return {@link Status}
	 * 
	 */
	private Status checkFileFormat(FileDescriptor currentFile){
		
		if( currentFile.isValidFormat()	 ) {
			return Status.unsupportedFormat;
		}

		return Status.ok;
		
		
	}
	/**
	 * Saves the upload file in the temporal directory.
	 * 
	 * 
	 * @param uploadFile
	 * @param downloadDirectory
	 * @return {@link Status}
	 * @throws IOException
	 */
	private void save(MultipartFile uploadFile, String downloadDirectory) throws IOException{

		try {
			final String originalFileName = uploadFile.getOriginalFilename();
			File outFile = new File(downloadDirectory+"/"+originalFileName);
			uploadFile.transferTo(outFile);
			
		} catch (IOException e) {
			LOG.fatal(e.getMessage());
			throw e;
		}
	}
	
	private Status checkGeoFiles(String downloadDirectory) {
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

	private void unzip(MultipartFile zipFile, String downloadDirectory) {
		// TODO Auto-generated method stub
		
	}
	
}
