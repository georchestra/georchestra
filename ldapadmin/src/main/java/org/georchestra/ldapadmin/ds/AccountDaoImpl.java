/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.util.List;

import javax.naming.Name;

import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.georchestra.ldapadmin.ws.newaccount.UidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * This class is responsible of maintaining the user accounts (CRUD operations). 
 * 
 * @author Mauricio Pazos
 */
public final class AccountDaoImpl implements AccountDao{
	
	private LdapTemplate ldapTemplate;
	private GroupDao groupDao;
	
	
	@Autowired
	public AccountDaoImpl( LdapTemplate ldapTemplate, GroupDao groupDao) {
	
		this.ldapTemplate =ldapTemplate;
		this.groupDao = groupDao;
	}	
	
	
	public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}
	

	/**
	 * @see {@link AccountDao#insert(Account, String)}
	 */
	@Override
	public void insert(final Account account, final String groupID) throws DataServiceException, DuplicatedUidException, DuplicatedEmailException{
	
		assert account != null;
		
		checkMandatoryFields(account);

		// checks unique uid
		
		String uid = account.getUid().toLowerCase();
		try{
			findByUID(uid);
			
			throw new DuplicatedUidException("there is a user with this user identifier (uid): " + account.getUid());

		} catch (NotFoundException e1) {
			// if not exist an account with this uid the new account can be added. 
		} 
		
		// checks unique email
		try {
			findByEmail(account.getEmail().trim());
			
			throw new DuplicatedEmailException("there is a user with this email: " + account.getEmail());
			
		} catch (NotFoundException e1) {
			// if not exist an account with this e-mail the new account can be added. 
		} 

		// insert the new user account
		try {
			Name dn = buildDn( uid );

			DirContextAdapter context = new DirContextAdapter(dn);
			mapToContext(account, context);

			this.ldapTemplate.bind(dn, context, null);

			this.groupDao.addUser( groupID, account.getUid() );

		} catch (NotFoundException e) {
			throw new DataServiceException(e);
		}
	}


	/**
	 * @see {@link AccountDao#update(Account)}
	 */
	@Override
	public void update(final Account account) throws DataServiceException, DuplicatedEmailException{

		// checks mandatory fields
		if( account.getUid().length() == 0) {
			throw new IllegalArgumentException("uid is required");
		}
		if( account.getSurname().length()== 0 ){
			throw new IllegalArgumentException("surname is required");
		}
		if( account.getCommonName().length()== 0 ){
			throw new IllegalArgumentException("common name is required");
		}
		if( account.getGivenName().length()== 0 ){
			throw new IllegalArgumentException("given name is required");
		}
		
		// checks unique email
		try {

			// if the email is found in other account different that this account, the new email cannot be used.
			Account foundAccount = findByEmail(account.getEmail());
			
			if( !foundAccount.getUid().equals(account.getUid()) ){
				throw new DuplicatedEmailException("there is a user with this email" + account.getEmail());
			}
			
		} catch (NotFoundException e1) {
			// if not exist an account with this e-mail the it can be part of the updated account. 
		} 
		
		// update the entry in the ldap tree
		Name dn = buildDn(account.getUid());
		DirContextOperations context = ldapTemplate.lookupContext(dn);

		mapToContext(account, context);
		
		ldapTemplate.modifyAttributes(context);
	}


	/**
	 * Removes the user account and the reference included in the group
	 * 
	 * @see {@link AccountDao#delete(Account)}
	 */
	@Override
	public void delete(final String uid) throws DataServiceException, NotFoundException{
		this.ldapTemplate.unbind(buildDn(uid), true);
		
		this.groupDao.deleteUser( uid );

	}
	
	/**
	 * @see {@link AccountDao#findAll()}
	 */
	@Override
	public List<Account> findAll() throws DataServiceException{
		
		EqualsFilter filter = new EqualsFilter("objectClass", "person");
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), new AccountContextMapper());
	}

	
	/**
	 * @see {@link AccountDao#findByUID(String)}
	 */
	@Override
	public Account findByUID(final String uid) throws DataServiceException, NotFoundException{

		try{
			DistinguishedName dn = buildDn(uid.toLowerCase());
			Account a = (Account) ldapTemplate.lookup(dn, new AccountContextMapper());
			
			return  a;
			
		} catch (NameNotFoundException e){

			throw new NotFoundException("There is not a user with this user identifier (uid): " + uid);
		}
		
	}

	/**
	 * @see {@link AccountDao#findByEmail(String)}
	 */
	@Override
	public Account findByEmail(final String email) throws DataServiceException, NotFoundException {

		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
		filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
		filter.and(new EqualsFilter("objectClass", "person"));
		filter.and(new EqualsFilter("mail", email));

		List<Account> accountList = ldapTemplate.search(
								DistinguishedName.EMPTY_PATH, 
								filter.encode(), 
								new AccountContextMapper());
		if(accountList.isEmpty()){
			throw new NotFoundException("There is not a user with this email: " + email);
		}
		Account account = accountList.get(0);
		
		return  account;
	}
	
	public boolean exist(final String uid) throws DataServiceException{

		try{
			DistinguishedName dn = buildDn(uid.toLowerCase());
			ldapTemplate.lookup(dn);
			return true;
		} catch (NameNotFoundException ex ){
			return false;
		}
	}

	/**
	 * Create an ldap entry for the user 
	 * 
	 * @param uid user id
	 * @return
	 */
	private DistinguishedName buildDn(String  uid) {
		DistinguishedName dn = new DistinguishedName();
				
//		dn.add("dc", "org");
//		dn.add("dc", "georchestra");
		dn.add("ou", "users");
		dn.add("uid", uid);
		
		return dn;
	}
	
	/**
	 * Checks that  mandatory fields are present in the {@link Account}
	 */
	private void checkMandatoryFields( Account a ) throws IllegalArgumentException{

		// required by the account entry
		if( a.getUid().length() <= 0 ){
			throw new  IllegalArgumentException("uid is requird");
		}
		
		// required field in Person object
		if( a.getGivenName().length() <= 0 ){
			throw new  IllegalArgumentException("Given name (cn) is requird");
		}
		if( a.getSurname().length() <= 0){
			throw new IllegalArgumentException("surname name (sn) is requird");
		}
		if( a.getEmail().length() <= 0){
			throw new IllegalArgumentException("email is requird");
		}
		
	}

	 		
	/**
	 * Maps the following the account object to the following LDAP entry schema:
	 *
	 * <pre>
	 * dn: uid=anUid,ou=users,dc=georchestra,dc=org
	 * sn: aSurname
	 * objectClass: organizationalPerson
	 * objectClass: person 
	 * objectClass: inetOrgPerson
	 * objectClass: top
	 * mail: aMail
	 * uid: anUid
	 * cn: aCommonName
	 * description: description
	 * userPassword: secret
	 * </pre>
	 * 
	 * 
	 * @param account
	 * @param context
	 * @param createEntry
	 */
	private void mapToContext(Account account, DirContextOperations context) {
		
		context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });

		// person attributes
		setAccountField(context, UserSchema.SURNAME_KEY, account.getSurname());

		setAccountField(context, UserSchema.COMMON_NAME_KEY, account.getCommonName());
		
		setAccountField(context, UserSchema.DESCRIPTION_KEY, account.getDescription());

		setAccountField(context, UserSchema.TELEPHONE_KEY, account.getPhone());

		setAccountField(context, UserSchema.USER_PASSWORD_KEY , account.getPassword());

		setAccountField(context, UserSchema.MOBILE_KEY , account.getMobile());

		// organizationalPerson attributes
		setAccountField(context, UserSchema.TITLE_KEY, account.getTitle());

		setAccountField(context, UserSchema.STREET_KEY, account.getStreet());
		
		setAccountField(context, UserSchema.LOCALITY_KEY, account.getLocality());
		
		setAccountField(context, UserSchema.FACSIMILE_KEY, account.getFacsimile());
		
		setAccountField(context, UserSchema.ROOM_NUMBER_KEY, account.getRoomNumber());
		
		// inetOrgPerson attributes
		setAccountField(context, UserSchema.GIVEN_NAME_KEY, account.getGivenName());
		
		setAccountField(context, UserSchema.UUID_KEY, account.getUid().toLowerCase());

		setAccountField(context, UserSchema.MAIL_KEY, account.getEmail());
		
		// additional
		setAccountField(context, UserSchema.ORG_KEY, account.getOrg());

		setAccountField(context, UserSchema.POSTAL_ADDRESS_KEY, account.getPostalAddress());

		setAccountField(context, UserSchema.POSTAL_CODE_KEY, account.getPostalCode());

		setAccountField(context, UserSchema.REGISTERED_ADDRESS_KEY, account.getRegisteredAddress());

		setAccountField(context, UserSchema.POST_OFFICE_BOX_KEY , account.getPostOfficeBox());

		setAccountField(context, UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY, account.getPhysicalDeliveryOfficeName());

		setAccountField(context, UserSchema.STATE_OR_PROVINCE_KEY, account.getStateOrProvince());

		setAccountField(context, UserSchema.ORG_UNIT_KEY, account.getOrganizationalUnit());
	}
	
	private void setAccountField(DirContextOperations context,  String fieldName, Object value) {

		if( !isNullValue(value) ){
			context.setAttributeValue(fieldName, value);
		}
	}
	
	
	private static class AccountContextMapper implements ContextMapper {

		@Override
		public Object mapFromContext(Object ctx) {
			
			DirContextAdapter context = (DirContextAdapter) ctx;
			
			Account account = AccountFactory.createFull(
					context.getStringAttribute(UserSchema.UUID_KEY),
					context.getStringAttribute(UserSchema.COMMON_NAME_KEY),
					context.getStringAttribute(UserSchema.SURNAME_KEY),
					context.getStringAttribute(UserSchema.GIVEN_NAME_KEY),
					context.getStringAttribute(UserSchema.MAIL_KEY),
					
					context.getStringAttribute(UserSchema.ORG_KEY),
					context.getStringAttribute(UserSchema.TITLE_KEY),

					context.getStringAttribute(UserSchema.TELEPHONE_KEY),
					context.getStringAttribute(UserSchema.DESCRIPTION_KEY),

					context.getStringAttribute(UserSchema.POSTAL_ADDRESS_KEY),
					context.getStringAttribute(UserSchema.POSTAL_CODE_KEY),
					context.getStringAttribute(UserSchema.REGISTERED_ADDRESS_KEY),
					context.getStringAttribute(UserSchema.POST_OFFICE_BOX_KEY),
					context.getStringAttribute(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY),

					context.getStringAttribute(UserSchema.STREET_KEY),
					context.getStringAttribute(UserSchema.LOCALITY_KEY),

					context.getStringAttribute(UserSchema.FACSIMILE_KEY),
					context.getStringAttribute(UserSchema.ORG_UNIT_KEY),
					
					context.getStringAttribute(UserSchema.HOME_POSTAL_ADDRESS_KEY),
					context.getStringAttribute(UserSchema.MOBILE_KEY),
					context.getStringAttribute(UserSchema.ROOM_NUMBER_KEY),
					context.getStringAttribute(UserSchema.STATE_OR_PROVINCE_KEY)
				);

			return account;
		}
	}
	
	private boolean isNullValue(Object value) {

		if(value == null) return true;
		
		if(value instanceof String){
			if(((String)value).length() == 0) return true;
		}
		
		return false;
	}


	@Override
	public void changePassword(final String uid, final String password) throws DataServiceException {
		
		if( uid.length() == 0) {
			throw new IllegalArgumentException("uid is required");
		}
		if( password.length()== 0 ){
			throw new IllegalArgumentException("password is required");
		}
		
		 // update the entry in the ldap tree
		Name dn = buildDn(uid);
		DirContextOperations context = ldapTemplate.lookupContext(dn);
		
		// the following action remove the old password. It there are two password (old and new password) they will 
		// be replaced by a single user password
		context.setAttributeValue("userPassword", password);
		
		ldapTemplate.modifyAttributes(context);
	}


	/**
	 * Adds the new password in the user password array. 
	 * The new password is maintained in array with two userPassword attributes.
	 * <pre>
	 * Format: 
	 * userPassword[0] : old password
	 * userPassword[1] : new password
	 * </pre>
	 * @see {@link AccountDao#addNewPassword(String, String)}
	 */
	@Override
	public void addNewPassword(String uid, String newPassword) {
		if( uid.length() == 0) {
			throw new IllegalArgumentException("uid is required");
		}
		if( newPassword.length()== 0 ){
			throw new IllegalArgumentException("new password is required");
		}
		
		 // update the entry in the ldap tree
		Name dn = buildDn(uid);
		DirContextOperations context = ldapTemplate.lookupContext(dn);
		
		final String pwd = "userPassword";
		Object[] pwdValues = context.getObjectAttributes(pwd);
		if(pwdValues.length < 2){
			// adds the new password
			context.addAttributeValue(pwd, newPassword, false);
		} else {
			// update the last password with the new password
			pwdValues[1] = newPassword;
			context.setAttributeValues(pwd, pwdValues);
		}
		
		ldapTemplate.modifyAttributes(context);
	}


	/**
	 * Generate a new uid based on the provided uid
	 *  
	 * @param
	 * 
	 * @return the proposed uid
	 */
	@Override
	public String generateUid(String uid) throws DataServiceException {

		String newUid = UidGenerator.next(uid);

		while (exist(newUid)) {

			newUid = UidGenerator.next(newUid);
		}
		
		return newUid;
	}


	
}
