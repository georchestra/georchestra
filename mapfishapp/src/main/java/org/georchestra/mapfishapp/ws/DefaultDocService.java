package org.georchestra.mapfishapp.ws;

/**
 * This is a convenient class that provides a basic DocService.
 * It means that the features of the abstract base class {@link A_DocService} is enough to do the job.
 * It is useful for creating new doc services that do not need specific behaviors
 * and it also prevent the multiplication of classes that inherits from {@link A_DocService}.
 * @author yoann.buch@gmail.com
 */

public class DefaultDocService extends A_DocService {


	/**
     * This constructor is set private. 
     * It forces user to use {@link DefaultDocService#DefaultDocService(String, String)}
     * 
	 * @param tempDir
	 */
	@SuppressWarnings("unused")
    private DefaultDocService(final String tempDir) {
        super("", "", tempDir);
    }
    
    /**
     * Creates a new Doc Service using all the features of its abstract base class 
     * {@link A_DocService}
     * 
     * @param fileExtension
     * @param MIMEType
     * @param tempDir
     */
    public DefaultDocService(final String fileExtension, final String MIMEType, final String tempDir ) {
        super(fileExtension, MIMEType, tempDir);
    }

}
