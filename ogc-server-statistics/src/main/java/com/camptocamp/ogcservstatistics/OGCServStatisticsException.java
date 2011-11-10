/**
 * 
 */
package com.camptocamp.ogcservstatistics;

/**
 * @author Mauricio Pazos
 *
 */
public class OGCServStatisticsException extends Exception {

	/**
	 * serialization
	 */
	private static final long serialVersionUID = -5109217524588114531L;

	/**
	 * 
	 */
	public OGCServStatisticsException() {
		super();
	}

	/**
	 * @param message
	 */
	public OGCServStatisticsException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public OGCServStatisticsException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OGCServStatisticsException(String message, Throwable cause) {
		super(message, cause);
	}

}
