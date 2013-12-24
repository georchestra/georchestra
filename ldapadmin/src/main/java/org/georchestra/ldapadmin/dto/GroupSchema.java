/**
 * 
 */
package org.georchestra.ldapadmin.dto;

/**
 * Defines the name of the group's fields. They are consistent with the LDAP specification.
 * 
 * @author Mauricio Pazos
 *
 */
public interface GroupSchema {
	
	public static final String COMMON_NAME_KEY = "cn"; // group name
	public static final String DESCRIPTION_KEY = "description";
	public static final String MEMBER_UID_KEY = "memberUid";
	public static final String GID_NUMBER_KEY = "gidNumber";


}
