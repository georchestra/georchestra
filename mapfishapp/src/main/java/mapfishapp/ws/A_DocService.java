package mapfishapp.ws;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * This service is the basic template to handle the storage and the loading of a file on a temporary directory.
 * Some methods can be override to provide treatments specific to a file extension.
 * 
 * @author yoann buch  - yoann.buch@gmail.com
 *
 */

public abstract class A_DocService {

    protected static final Log LOG = LogFactory.getLog(A_DocService.class.getPackage().getName());

    /**
     * Document prefix helping to differentiate documents among others OS tmp files
     */
    protected static final String DOC_PREFIX = "geodoc";


    /**
     * File extension. 
     */
    protected String _fileExtension;
    
    /**
     * MIME type.
     */
    private String _MIMEType;
    
    /**
     * File content. Can be altered
     */
    protected String _content;
    
    /**
     * File name. Can be altered otherwise default name is kept (the one generated by OS)
     */
    protected String _name;

    /**
     * files are stored in the configured directory 
     */
    private String _tempDirectory;  
	
	/**
	 * Creates the temporal directory if it doesn't exist and set the path
	 */
	private void setTempDirectory(final String tempDirectory) {
		
		File t = new File(tempDirectory);
		if(!t.exists()){
			boolean succeed = t.mkdirs();
			
			if(!succeed){
				LOG.error("cannot create the dirctory: " + tempDirectory);
			}
		}
		_tempDirectory = tempDirectory;
		
	}
    
    
    /*========================Public Methods====================================================*/

    /**
     * Subclasses have to provide their file extension name and MIME type
     * 
     * @param maxDocAgeInMinutes
     * @param fileExtension
     * @param MIMEType
     * @param docTempDirectory
     */
    public A_DocService(int maxDocAgeInMinutes, final String fileExtension, final String MIMEType,  final String docTempDirectory) {
        _fileExtension = fileExtension;
        _MIMEType = MIMEType;
        
        setTempDirectory(docTempDirectory);
        
        Runnable purgeDocsTask = new PurgeDocsRunnable(maxDocAgeInMinutes, _tempDirectory);
        PurgeDocsTimer.startPurgeDocsTimer(purgeDocsTask, maxDocAgeInMinutes);
    }
    
    /**
     * Store the given data
     * @param data raw data to be stored
     * @return file name
     * @throws DocServiceException
     */
    public String saveData(final String data) throws DocServiceException {
        // purge doc directory from old files
        // FIXME do not purge for now, this will need to be revisited once
        // we have authentication in place
        //purgeDocDir();
        
        _content = data;
        
        // actions to take before saving data
        preSave();
        
        // store file under a file and get its name
        String fileName = saveDataIntoFile(_content);
        
        return fileName;   
    }
    
    /**
     * Load the file corresponding to the file name in the service.
     * Content can be accessed via getContent, name via getName, and MIME type via getMIMEType
     * @param fileName file name
     * @throws DocServiceException
     */
    public void loadFile(final String fileName) throws DocServiceException {
        // check first if file exists
        if(!isFileExist(fileName)) {
            throw new DocServiceException("Requested file  does not exist.", HttpServletResponse.SC_NOT_FOUND);
        }
        
        // default, file name will the one generated by OS
        _name = fileName;
        
        // load file content
        _content = loadContent(fileName);
        
        // actions to take after loading the content
        postLoad();
    }
    
    /*========================Accessor Methods====================================================*/
    
    /**
     * Get the MIME type
     * @return String MIME type
     */
    public String getMIMEType() {
        return _MIMEType;
    }
    
    /**
     * Get the file content. Should be called once loadFile has been called.
     * @return String file content
     */
    public String getContent() {
        if(_content == null) {
            throw new RuntimeException("_content is null. Should be called after loadFile");
        }
        return _content;
    }
    
    /**
     * Get the file name (contains file extension). Should be called once loadFile has been called.
     * @return String file name
     */
    public String getName() {
        if(_name == null) {
            throw new RuntimeException("_name is null. Should be called after loadFile");
        }
        return _name;
    }
    
    /*========================Protected Methods - Variable algorithms==============================================*/
    
    /**
     * Must be override to take actions before the data are saved. <br />
     * Examples: valid data format or integrity, interpret or transform data.
     * @throws DocServiceException
     */
    protected void preSave() throws DocServiceException {
    }
    
    /**
     * Must be override to take actions once the file is load in memory <br />
     * Examples: parse the file to get the real file name
     * @throws DocServiceException
     */
    protected void postLoad() throws DocServiceException {
    }
    
    /**
     * Provide a method to its subclasses to determine if their content is valid based on a xsd schema
     * @param schemaURL
     * @return true: valid; false: not valid. No use to expect this return value. If the document is not valid a DocServiceException is thrown
     * @throws DocServiceException
     */
    protected boolean isDocumentValid(final String schemaURL) throws DocServiceException {
        try {
            
            InputStream dataToValid = new ByteArrayInputStream(getContent().getBytes("UTF-8"));
            
            // lookup a factory for the W3C XML Schema language
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        
            // get the schema online.
            Schema schema = factory.newSchema(new URL(schemaURL));
    
            // prepare source to valid by the validator based on the schema
            Source source = new StreamSource(dataToValid);  
            Validator validator = schema.newValidator();
            
            // check if doc is valid
            validator.validate(source);
            return true;
        }
        catch (SAXException ex) {
            // occurs when validation errors happen
            throw new DocServiceException("File is not valid. " + ex.getMessage(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return false;
    }
    
    /*=====================Private Methods - Common to every DocService=========================================*/
    
    /**
     * Save the given data under a specific name and location
     * @param data data to be stored
     * @return file name
     */
    private String saveDataIntoFile(final String data) {
        String fileName = "";
        try {
            // file saved under: DOC_PREFIX + ID generated by OS + _fileExtension in the DIR_PATH
            File file = File.createTempFile(DOC_PREFIX, _fileExtension, new File(_tempDirectory));  
            file.deleteOnExit(); // will be purged when JVM stops
          
            // write content file as bytes
            DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
            out.write(data.getBytes("UTF-8"));
            out.close();  
            
            fileName = file.getName();
            
        }
        catch(FileNotFoundException fnfExc) {
            fnfExc.printStackTrace();
        }
        catch(IOException ioExc) {
            ioExc.printStackTrace();
        }
      
        return fileName;
    }
    
    /**
     * Check that file exists in DIR_PATH
     * @param fileName
     * @return true: exists, false: not exists
     */
    private boolean isFileExist(final String fileName) {
        // file was stored previously in a known place
        File dir = new File(_tempDirectory);
        
        if(!dir.exists()) {
            throw new RuntimeException(_tempDirectory + " directory not found");
        } 
        
        // prepare filter to get the right file 
        FilenameFilter filter = 
            new FilenameFilter() {
                                    public boolean accept(File dir, String name) {
                                        
                                        return fileName.equals(name);
                                    }
                                }; 
                                
        // get file thanks to the previous filter
        String[] fileList = dir.list(filter);  
        
        return fileList.length == 1;
    }
    
    /**
     * Get file content of the given file stored in DIR_PATH
     * @param fileName file name
     * @return file content
     */
    private String loadContent(final String fileName) {
        File file = new File(_tempDirectory + File.separatorChar + fileName); 
        String content = "";
        
        FileInputStream  fis = null;
        try {
            fis = new FileInputStream(file);
            
            // get file size
            long fileSize = file.length();
            if (fileSize > Integer.MAX_VALUE) {
                throw new IOException("File is too big");
            }

            // allocate necessary memory to store content
            byte[] bytes = new byte[(int) fileSize];
            
            // read the content in the byte array
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=fis.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
            
            // Ensure all the bytes have been read 
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        
            // return the file content
            content = new String(bytes);
            
        } catch (FileNotFoundException fnfExc) {
            fnfExc.printStackTrace();
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
        } finally{
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return content;
    }

}
