package org.georchestra.extractorapp.ws;

/**
 * The exception that represents something bad occurred in system but should be
 * handled at a higher level
 * 
 * @author jeichar
 */
public class ExtractorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExtractorException(Exception cause) {
		super(cause);
	}

	public ExtractorException(String message) {
        super(message);
    }

    @Override
	public StackTraceElement[] getStackTrace() {
        if(getCause() == null) {
            return super.getStackTrace();
        }
		return getCause().getStackTrace();
	}
}
