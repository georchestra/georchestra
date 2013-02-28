package org.georchestra.extractorapp.ws.doc;

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
     */
    private DefaultDocService() {
        super("", "");
    }
    
    /**
     * Creates a new Doc Service using all the features of its abstract base class 
     * {@link A_DocService}
     * @param fileExtension file extension
     * @param MIMEType mime type
     */
    public DefaultDocService(String fileExtension, String MIMEType) {
        super(fileExtension, MIMEType);
    }

}
