/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.util.Date;
import java.util.List;

import javax.naming.Name;

import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	public void insert(final Account account, final String groupID) throws DataServiceException, DuplicatedEmailException{
	
		assert account != null;
		
		checkMandatoryFields(account);

		// checks unique email
		try {

			findByEmail(account.getEmail());
			
			throw new DuplicatedEmailException("there is a user with this email" + account.getEmail());
			
		} catch (NotFoundException e1) {
			// if not exist an account with this e-mail the new account can be added. 
		} 

		// insert the new user account
		try {
			Name dn = buildDn( account.getUid() );

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

		mapDetailsToContext(account, context);
		
		ldapTemplate.modifyAttributes(context);
	}


	/**
	 * @see {@link AccountDao#delete(Account)}
	 */
	@Override
	public void delete(final Account account) throws DataServiceException, NotFoundException{
		ldapTemplate.unbind(buildDn(account.getUid()));
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

		DistinguishedName dn = buildDn(uid);
		Account a = (Account) ldapTemplate.lookup(dn, new AccountContextMapper());
		
		if(a == null){
			throw new NotFoundException("There is not a user with this email: " + uid);
		}
		
		return  a;
		
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
	

	/**
	 * Create an ldap entry for the user 
	 * 
	 * @param uid user id
	 * @return
	 */
	private DistinguishedName buildDn(String  uid) {
		DistinguishedName dn = new DistinguishedName();
				
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
		if( a.getPassword().length() <= 0){
			throw new IllegalArgumentException("password is requird");
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
		context.setAttributeValue("sn", account.getSurname());

		context.setAttributeValue("cn", account.getCommonName());
		
		setAccountField(context, "description", account.getDetails());

		setAccountField(context, "telephoneNumber", account.getPhone());

		context.setAttributeValue("userPassword", account.getPassword());

		// organizationalPerson attributes
		// any attribute is set right now (when the account is created)
		
		// inetOrgPerson attributes
		setAccountField(context, "givenName", account.getGivenName());
		
		context.setAttributeValue("uid", account.getUid());

		context.setAttributeValue("mail", account.getEmail());
		
		// additional
		setAccountField(context, "o", account.getOrg());

		setAccountField(context, "title", account.getTitle());

		setAccountField(context, "postalAddress", account.getPostalAddress());

		setAccountField(context, "postalCode", account.getPostalCode());

		setAccountField(context, "registeredAddress", account.getRegisteredAddress());

		setAccountField(context, "postOfficeBox", account.getPostOfficeBox());

		setAccountField(context, "physicalDeliveryOfficeName", account.getPhysicalDeliveryOfficeName());
	}
	
	private void mapDetailsToContext(Account account, DirContextOperations context){
		
		context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalPerson", "inetOrgPerson" });

		// person attributes
		context.setAttributeValue("sn", account.getSurname());

		context.setAttributeValue("cn", account.getCommonName() );
		
		setAccountField(context, "description", account.getDetails());

		setAccountField(context, "telephoneNumber", account.getPhone());

		
		// organizationalPerson attributes
		// any attribute is set right now (when the account is created)
		
		// inetOrgPerson attributes
		setAccountField(context, "givenName", account.getGivenName());
		
		// additional
		setAccountField(context, "o", account.getOrg());

		setAccountField(context, "title", account.getTitle());

		setAccountField(context, "postalAddress", account.getPostalAddress());

		setAccountField(context, "postalCode", account.getPostalCode());

		setAccountField(context, "registeredAddress", account.getRegisteredAddress());
		
		setAccountField(context, "postOfficeBox", account.getPostOfficeBox());
		
		setAccountField(context, "physicalDeliveryOfficeName", account.getPhysicalDeliveryOfficeName());
	}
	
	private void setAccountField(DirContextOperations context,  String fieldName, String value) {

		if( !isNullValue(value) ){
			context.setAttributeValue(fieldName, value);
		}
	}
	
	
	private static class AccountContextMapper implements ContextMapper {

		@Override
		public Object mapFromContext(Object ctx) {
			
			DirContextAdapter context = (DirContextAdapter) ctx;
			
			Account account = AccountFactory.createFull(
					context.getStringAttribute("uid"),
					context.getStringAttribute("cn"),
					context.getStringAttribute("sn"),
					context.getStringAttribute("givenName"),
					context.getStringAttribute("mail"),
					
					context.getStringAttribute("o"),
					context.getStringAttribute("title"),

					context.getStringAttribute("telephoneNumber"),
					context.getStringAttribute("description"),

					context.getStringAttribute("postalAddress"),
					context.getStringAttribute("postalCode"),
					context.getStringAttribute("registeredAddress"),
					context.getStringAttribute("postOfficeBox"),
					context.getStringAttribute("physicalDeliveryOfficeName") );

			return account;
		}
	}
	
	private boolean isNullValue(String str) {

		if(str == null) return true;
		
		if(str.length() == 0) return true;
		
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
		
		// the followint acction remove the old password. It there are two passowrd (old and new passowrd) they will 
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
		
		// TODO this logic requires set the update date (is there any ldap field to support this???)
		
		ldapTemplate.modifyAttributes(context);
	}



	
}
