package mapfishapp.ws.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.io.FilenameUtils;

/**
 * Maintains useful file information about the uploaded file
 *  
 * @author Mauricio Pazos
 */
public class FileDescriptor {

	/** file name of upload file */
	public String originalFileName;
	
	public String ext;
	
	/** upload file */
	public File savedFile;
	
	/** list of the file extensions contained in the zip file */
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
