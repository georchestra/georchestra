/**
 * 
 */
package org.georchestra.ldapadmin.dto;

/**
 * @author Mauricio Pazos
 *
 */
public class GroupFactory {

	public static Group create() {
		
		return new GroupImpl();
	}

}
