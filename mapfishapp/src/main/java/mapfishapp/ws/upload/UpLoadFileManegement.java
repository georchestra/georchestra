/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
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
	

}
