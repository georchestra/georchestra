package mapfishapp.ws.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mapfishapp.ws.upload.OGRFeatureReader.FileFormat;

import org.apache.commons.io.FilenameUtils;

/**
 * Maintains useful file information about the uploaded file.
 *  
 * @author Mauricio Pazos
 */
public class FileDescriptor {

	/** file name of upload file */
	public String originalFileName;
	
	/** upload file's extension */
	public String ext;
	
	/** upload file */
	public File savedFile; 
	
	/** the geofile format */
	public FileFormat geoFileType;
	
	/** list of the geo file extensions */
	public List<String> listOfExtensions = new ArrayList<String>();

	/** list of files contained in the zip file*/
	public List<String> listOfFiles = new LinkedList<String>();
	
	public FileDescriptor(final String fileName) {

		assert fileName != null;
		
		originalFileName = fileName;
		ext = FilenameUtils.getExtension(fileName);
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
