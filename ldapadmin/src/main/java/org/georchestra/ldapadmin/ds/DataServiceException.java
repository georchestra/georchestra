/**
 *
 */
package org.georchestra.ldapadmin.ds;


/**
 *
 * @author Mauricio Pazos
 *
 */
public class DataServiceException extends Exception {


	/**
	 *
	 */
	private static final long serialVersionUID = 7285966167139584662L;

	public DataServiceException(Exception e) {
		super(e);
	}

	public DataServiceException(String  msg) {
		super(msg);
	}

}
