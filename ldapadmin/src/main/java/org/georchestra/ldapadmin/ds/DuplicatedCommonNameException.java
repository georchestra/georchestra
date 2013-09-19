/**
 * 
 */
package org.georchestra.ldapadmin.ds;

/**
 * Throws if the common name is present in the LDAP.
 *  
 * @author Mauricio Pazos
 *
 */
public class DuplicatedCommonNameException extends Exception {

	private static final long serialVersionUID = 6399369489493455851L;
	
	public DuplicatedCommonNameException(String msg) {
		
		super(msg);
	}
	
}
