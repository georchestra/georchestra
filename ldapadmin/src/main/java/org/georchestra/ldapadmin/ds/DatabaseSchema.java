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
	final static String SCHEMA_NAME = "ldapadmin";

	// columns
	final static String UID_COLUMN = "uid";
	final static String TOKEN_COLUMN = "token";
	final static String CREATION_DATE_COLUMN = "creation_date";
	
}
