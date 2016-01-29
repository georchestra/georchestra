package org.georchestra.ldapadmin.dto;

/**
 * Defines the name of the user fields. They are consistent with the LDAP specification.
 * 
 * @author Mauricio Pazos
 *
 */
public interface UserSchema {

	// Internal unique identifier
	public static final String UUID_KEY = "entryUUID";

	public static final String UID_KEY = "uid";
	
	public static final String COMMON_NAME_KEY = "cn"; // full name (givenName) + Surname
	public static final String SURNAME_KEY = "sn";
	public static final String GIVEN_NAME_KEY = "givenName"; // first name
	
	public static final String STREET_KEY = "street";
	public static final String HOME_POSTAL_ADDRESS_KEY = "homePostalAddress";
	public static final String TITLE_KEY = "title";
	public static final String FACSIMILE_KEY = "facsimileTelephoneNumber";
	public static final String POSTAL_CODE_KEY = "postalCode";
	public static final String MAIL_KEY = "mail";
	public static final String POSTAL_ADDRESS_KEY = "postalAddress";
	public static final String POST_OFFICE_BOX_KEY = "postOfficeBox";
	public static final String DESCRIPTION_KEY = "description";
	public static final String TELEPHONE_KEY = "telephoneNumber";
	public static final String PHYSICAL_DELIVERY_OFFICE_NAME_KEY = "physicalDeliveryOfficeName";
	public static final String MOBILE_KEY = "mobile";
	public static final String ROOM_NUMBER_KEY = "roomNumber";
	public static final String LOCALITY_KEY = "l";
	public static final String ORG_KEY = "o";
	public static final String STATE_OR_PROVINCE_KEY = "st";
	public static final String ORG_UNIT_KEY = "ou";

	public static final String USER_PASSWORD_KEY = "userPassword";

	public static final String REGISTERED_ADDRESS_KEY =  "registeredAddress";
	public static final String SHADOW_EXPIRE = "shadowExpire";
	public static final String MANAGER = "manager";
	public static final String CONTEXT = "preferredLanguage";

	public static final String[] ATTR_TO_RETRIEVE = {UUID_KEY, UID_KEY, COMMON_NAME_KEY, SURNAME_KEY,
			GIVEN_NAME_KEY, STREET_KEY, HOME_POSTAL_ADDRESS_KEY, TITLE_KEY, FACSIMILE_KEY, POSTAL_CODE_KEY,
			MAIL_KEY, POSTAL_ADDRESS_KEY, POST_OFFICE_BOX_KEY, DESCRIPTION_KEY, TELEPHONE_KEY,
			PHYSICAL_DELIVERY_OFFICE_NAME_KEY, MOBILE_KEY, ROOM_NUMBER_KEY, LOCALITY_KEY, ORG_KEY,
			STATE_OR_PROVINCE_KEY, ORG_UNIT_KEY, USER_PASSWORD_KEY, REGISTERED_ADDRESS_KEY, SHADOW_EXPIRE, MANAGER, CONTEXT};

}