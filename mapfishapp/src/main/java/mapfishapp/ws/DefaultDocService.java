package mapfishapp.ws;

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
    @SuppressWarnings("unused")
    private DefaultDocService(int maxDocAgeInMinutes) {
        super(maxDocAgeInMinutes, "", "");
    }
    
    /**
     * Creates a new Doc Service using all the features of its abstract base class 
     * {@link A_DocService}
     * @param fileExtension file extension
     * @param MIMEType mime type
     */
    public DefaultDocService(int maxDocAgeInMinutes, String fileExtension, String MIMEType) {
        super(maxDocAgeInMinutes, fileExtension, MIMEType);
    }

}
