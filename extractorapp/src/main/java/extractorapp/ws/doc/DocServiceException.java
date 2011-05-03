package extractorapp.ws.doc;

/**
 * This exception should be used to send specific HTTP error to client. <br />
 * It is strongly tied with HTTP codes from {@link javax.servlet.http.HttpServletResponse}
 * @author yoann.buch@gmail.com
 *
 */

@SuppressWarnings("serial")
public class DocServiceException extends Exception{

    private int _errorCode;
    
    /**
     * Constructor
     * @param message String Exception message 
     * @param code HTTP error code. Can be retrieved from HttpServletResponse
     */
    public DocServiceException(String message, int code) {
        super(message);
        _errorCode = code;
    }
    
    /**
     * Get error code
     * @return int error code
     */
    public int getErrorCode() {
        return _errorCode;
    }
}
