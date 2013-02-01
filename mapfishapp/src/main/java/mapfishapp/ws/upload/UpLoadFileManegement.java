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

import mapfishapp.ws.UpLoadGeoFileController;
import mapfishapp.ws.UpLoadGeoFileController.FileDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManegement {
	
	private static final Log LOG = LogFactory.getLog(UpLoadGeoFileController.class.getPackage().getName());
	
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

			File entryFile = new File(path);
			
			(new File(entryFile.getParent())).mkdir();

			extractFile(zipFile, entry, entryFile);
		}

		zipFile.close();
	}
	

	
	private void extractFile(final ZipFile zipFile, final ZipEntry entry, final File newFile) throws IOException {

		InputStream in = zipFile.getInputStream(entry);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
		
		byte[] buffer = new byte[1024];
	    int len;

	    while((len = in.read(buffer)) >= 0){
	      out.write(buffer, 0, len);
	    }

	    in.close();
	    out.close();
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
	

}
