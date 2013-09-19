/**
 * 
 */
package org.georchestra.ldapadmin.ds;

/**
 * Provide information about the database schema
 * 
 * @author Mauricio Pazos
 *
 */
interface DatabaseSchema {
	
	final static String TABLE_USER_TOKEN = "user_token";

	// columns
	final static String UID_COLUMN = "uid";
	final static String TOKEN_COLUMN = "token";
	final static String CREATEION_DATE_COLUMN = "creation_date";
	
}
