package mapfishapp.ws.upload;

import java.io.File;
import java.security.InvalidParameterException;

/**
 * Maintain useful file information about the uploaded file
 *  
 * @author Mauricio Pazos
 */
public class FileDescriptor {
	public String originalFileName;
	public String ext;
	public File savedFile;
	
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
