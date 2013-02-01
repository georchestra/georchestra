/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is responsible to maintain the uploaded file. It includes the method to save, unzip, and check the geofiles.
 * 
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManegement {
	
	private static final Log LOG = LogFactory.getLog(UpLoadFileManegement.class.getPackage().getName());
	
	private static List<String> VALID_EXTENSIONS;
	static{
		
		VALID_EXTENSIONS = new ArrayList<String>();
		// SHP
		VALID_EXTENSIONS.add("SHP");
		VALID_EXTENSIONS.add("DBF");
		VALID_EXTENSIONS.add("PRJ");
		VALID_EXTENSIONS.add("SHX");
		VALID_EXTENSIONS.add("QIX");
		
		// TAB
		VALID_EXTENSIONS.add("TAB");
		VALID_EXTENSIONS.add("ID");
		VALID_EXTENSIONS.add("MAP");
		VALID_EXTENSIONS.add("DAT");
		
		// MIF
		VALID_EXTENSIONS.add("MIF");
		VALID_EXTENSIONS.add("MID");
	}

	
	private FileDescriptor fileDescriptor;
	private String workDirectory;

	public UpLoadFileManegement(FileDescriptor currentFile, String workDirectory) {

		this.fileDescriptor = currentFile;
		this.workDirectory = workDirectory;
	}


	public void unzip() throws IOException {

		ZipFile zipFile = new ZipFile(fileDescriptor.savedFile.getAbsolutePath());

		// creates the directories
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			
			ZipEntry entry = (ZipEntry) entries.nextElement();

			String path = workDirectory+ File.separator+  entry.getName();

			File outFile = new File(path);
			makeDirectory(outFile.getParent());

			extractFile(zipFile, entry, outFile);
		}

		zipFile.close();
	}

	/**
	 * Create the directory structura taking into account the directory path
	 * @param path
	 * @throws IOException
	 */
	private void makeDirectory(String path) throws IOException{

		File newDirectory = new File(path);
		if(!newDirectory.exists() ){
			makeDirectory(newDirectory.getParent());
			newDirectory.mkdir();
		}		
	}
	

	/**
	 * Extract the file entry from the zip file. 
	 * @param zipFile
	 * @param entry
	 * @param outFile
	 * @throws IOException
	 */
	private void extractFile(final ZipFile zipFile, final ZipEntry entry, final File outFile) throws IOException {

		InputStream is = zipFile.getInputStream(entry);
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
		
		byte[] buffer = new byte[1024];
	    int len;

	    while((len = is.read(buffer)) >= 0){
	      os.write(buffer, 0, len);
	    }

	    is.close();
	    os.close();

	    // save the extension in the content extensions list
		String extension = FilenameUtils.getExtension(outFile.getName()).toUpperCase();
		this.fileDescriptor.listOfExtensions.add(extension);
		this.fileDescriptor.listOfFiles.add(outFile.getAbsolutePath());

	}


	/**
	 * Saves the upload file in the temporal directory.
	 * 
	 * 
	 * @param uploadFile
	 * @param downloadDirectory
	 * @return {@link File} the saved file
	 * 
	 * @throws IOException
	 * 
	 */
	public File save(MultipartFile uploadFile) throws IOException{

		try {
			final String originalFileName = uploadFile.getOriginalFilename();
			File outFile = new File(this.workDirectory+"/"+originalFileName);
			uploadFile.transferTo(outFile);

			this.fileDescriptor.savedFile = outFile;

			return outFile;
			
		} catch (IOException e) {
			LOG.fatal(e.getMessage());
			throw e;
		}
	}

	public boolean containsZipFile() {
		return this.fileDescriptor.isZipFile();		
	}


	/**
	 * Moves the upload file from the work directory (temporal) to download directory
	 * 
	 * @param downloadDirectory
	 * @throws IOException 
	 */
	public void moveTo(String downloadDirectory) throws IOException {
		
		File source = new File(this.workDirectory);
		File target = new File(downloadDirectory);
		if( !target.exists()) {
			boolean succeed = target.mkdir();
			if(! succeed ) {
				String message = "cannot create the download directory";
				LOG.fatal(message);
				throw new IOException(message);
			}
		}
		try{
			FileUtils.copyDirectory(source, target);
		} catch (IOException e){
			LOG.fatal(e.getMessage());
			throw e;
		}
	}

	/**
	 * Checks if the work directory contains files with valid extensions.
	 * 
	 * @return true if the extensions are OK
	 */
	public boolean checkGeoFileExtension(){

		for (String fileName : this.fileDescriptor.listOfFiles) {
			
			String ext = FilenameUtils.getExtension(fileName).toUpperCase();
			
			if( !VALID_EXTENSIONS.contains(ext)){
				return false;
			} 
			
		}
		return true;
		
	}

	/**
	 * a zip file is unzipped to a temporary place and *.shp, *.mid, *.tab 
	 * files are looked for at the root of the archive. If several SHP or several MIF or several TAB files are found, the error message is "multiple files"
	 * 
	 * @return true if the work directory contain only a one shp or mid o tab
	 */
	public boolean checkSingleGeoFile() {

		List<String> foundExtensions = new ArrayList<String>();
		for (String fileName : this.fileDescriptor.listOfFiles) {
			
			String ext = FilenameUtils.getExtension(fileName).toUpperCase();
			
			if( foundExtensions.contains(ext)){
				return false;
			} else {
				foundExtensions.add(ext);
			}
		}
		return true;
	}

	public boolean isMIF() {
		return this.fileDescriptor.listOfExtensions.contains("MIF");
	}

	public boolean isSHP() {
		return this.fileDescriptor.listOfExtensions.contains("SHP");
	}

	public boolean isTAB() {
		return this.fileDescriptor.listOfExtensions.contains("TAB");
	}

	
	/**
	 * if filename.mif is found, it is assumed that filename.mid exists too.
	 * 
	 * @return false if fid file doesn't exist.
	 */
	public boolean checkMIFCompletness() {
		
		return		this.fileDescriptor.listOfExtensions.contains("MIF") 
				&& 	this.fileDescriptor.listOfExtensions.contains("MID"); 
	}


	/**
	 * if filename.shp is found, it is assumed that filename.shx and filename.prj are also present (the DBF is not mandatory).
	 * @return true if shx and prj are found
	 */
	public boolean checkSHPCompletness() {
		return		this.fileDescriptor.listOfExtensions.contains("SHP") 
				&&	this.fileDescriptor.listOfExtensions.contains("SHX")
				&&	this.fileDescriptor.listOfExtensions.contains("PRJ"); 
	}


	public boolean checkTABCompletness() {
		return		this.fileDescriptor.listOfExtensions.contains("TAB") 
				&&	this.fileDescriptor.listOfExtensions.contains("ID")
				&&	this.fileDescriptor.listOfExtensions.contains("MAP") 
				&&	this.fileDescriptor.listOfExtensions.contains("DAT"); 
	}
	

}
