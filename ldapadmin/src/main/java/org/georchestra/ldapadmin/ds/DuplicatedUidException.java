/**
 * 
 */
package org.georchestra.ldapadmin.ds;

/**
 * Throws this exception when an existent account contains the user identifier of the new account.
 * 
 * @author Mauricio Pazos
 *
 */
public class DuplicatedUidException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -11146356841108209L;
	
	public DuplicatedUidException(String msg) {
		
		super(msg);
	}


}
