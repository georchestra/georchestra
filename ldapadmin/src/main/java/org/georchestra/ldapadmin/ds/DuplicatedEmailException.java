/**
 * 
 */
package org.georchestra.ldapadmin.ds;

/**
 * Throws this exception if the e-mail field is present in an existent ldap account.
 * 
 * @author Mauricio Pazos
 */
public class DuplicatedEmailException extends Exception {
	private static final long serialVersionUID = -3664679290591393089L;
	
	public DuplicatedEmailException(String msg) {
		
		super(msg);
	}
	
	

}
