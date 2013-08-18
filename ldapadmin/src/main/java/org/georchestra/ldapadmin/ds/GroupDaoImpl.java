/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.util.List;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * Maintains the group of users in the ldap store.
 * 
 * @author Mauricio Pazos
 *
 */
public class GroupDaoImpl implements GroupDao {

	private static final Log LOG = LogFactory.getLog(GroupDaoImpl.class.getName());
	
	private LdapTemplate ldapTemplate;
	
	public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	/**
	 * Create an ldap entry for the user 
	 * 
	 * @param uid user id
	 * @return
	 */
	private DistinguishedName buildDn(String  id) {
		DistinguishedName dn = new DistinguishedName();
				
		dn.add("ou", "groups");
		dn.add("cn", id);
		
		return dn;
	}
	
	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.ds.GroupDao#addUser(java.lang.String, java.lang.String)
	 */
	@Override
	public void addUser(final String groupID, final String userId) throws NotFoundException, DataServiceException {
		
		Name dn = buildDn(groupID);
		DirContextOperations context = ldapTemplate.lookupContext(dn);

		context.setAttributeValues("objectclass", new String[] { "top", "posixGroup" });

		try {
			context.addAttributeValue("memberUid", userId, false);

			this.ldapTemplate.modifyAttributes(context);

		} catch (Exception e) {
			throw new DataServiceException(e);		
		}
		
	}

	/**
	 * Removes the uid from the group (PENDING_USERS or SV_USER)
	 * 
	 * @param uid
	 */
	@Override
	public void deleteUser(String uid) throws DataServiceException {
		
		deleteUser(Group.SV_USER, uid);
		
		deleteUser(Group.PENDING_USERS, uid);
	}
	
	public void deleteUser(String groupName, String uid) throws DataServiceException {
		
		Name dnSvUser = buildDn(groupName);
		
		DirContextOperations ctx = ldapTemplate.lookupContext(dnSvUser);
		ctx.setAttributeValues("objectclass", new String[] { "top", "posixGroup" });
		ctx.removeAttributeValue("memberUid", uid);

		this.ldapTemplate.modifyAttributes(ctx);
	}
	
	/**
	 * Returns all groups. Each groups will contains its list of users.
	 * 
	 * @return list of {@link Group}
	 */
	@Override
	public List<Group> findAll() throws DataServiceException {
		
		EqualsFilter filter = new EqualsFilter("objectClass", "posixGroup");
		List<Group> groupList = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), new GroupContextMapper());
		
		return groupList;
	}
	
	public List<String> findUsers(final String groupName) throws DataServiceException{
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "ou"));
		filter.and(new EqualsFilter("ou", "groups"));
		filter.and(new EqualsFilter("cn", groupName));

		List<String> memberList = ldapTemplate.search(
								DistinguishedName.EMPTY_PATH, 
								filter.encode(), 
								new GroupContextMapper());
		
		return  memberList;
	}
	

	/**
	 * Searches the group by common name (cn)
	 * 
	 * @param commonName
	 * @throws NotFoundException 
	 */
	public Group findByCN(String commonName) throws NotFoundException {

		try{
			DistinguishedName dn = buildDn(commonName);
			Group g = (Group) ldapTemplate.lookup(dn, new GroupContextMapper());
			
			return  g;
			
		} catch (NameNotFoundException e){

			throw new NotFoundException("There is not a group with this common name (cn): " + commonName);
		}
	}
	
//	TODO remove it
//	private Group findGroupByCN(String groupCN) throws NotFoundException {
//
//		DistinguishedName dn = buildGroupDn(groupCN);
//		
//		Group g = (Group) ldapTemplate.lookup(dn,  new GroupContextMapper() );
//		
//		if(g == null){
//			throw new NotFoundException("There is not a group with this cn: " + groupCN);
//		}
//		
//		return  g;
//	}
//
//	private DistinguishedName buildGroupDn(String  cn) {
//		DistinguishedName dn = new DistinguishedName();
//				
//		dn.add("ou", "groups");
//		dn.add("cn", cn);
//		
//		return dn;
//	}
//	
//	
//	private static class GroupUserContextMapper implements ContextMapper {
//
//		@Override
//		public Object mapFromContext(Object ctx) {
//			
//			DirContextAdapter context = (DirContextAdapter) ctx;
//
//			String uid = context.getStringAttribute("memberUid");
//
//			return uid;
//		}
//	}

	private static class GroupContextMapper implements ContextMapper {

		@Override
		public Object mapFromContext(Object ctx) {
			
			DirContextAdapter context = (DirContextAdapter) ctx;

			// set the group name
			Group g = GroupFactory.create();
			g.setName(context.getStringAttribute("cn"));
			
			// set the list of user
			Object[] members = getUsers(context);
			

			for (int i = 0; i < members.length; i++) {

				g.addUser((String) members[i]);
			}
				
			return g;
		}

		private Object[] getUsers(DirContextAdapter context) {
			Object[] members = context.getObjectAttributes(GroupSchema.MEMBER_UID_KEY);			
			if(members == null){
				
				members = new Object[0];
			}
			return members;
		}
	}

	@Override
	public void insert(Group group) throws DataServiceException, DuplicatedCommonNameException {
		assert group != null;
		
		// checks unique common name
		try{
			findByCN(group.getName());
			
			throw new DuplicatedCommonNameException("there is a group with this name: " + group.getName());

		} catch (NotFoundException e1) {
			// if not exist an account with this uid the new account can be added. 
		} 
		
		// insert the new user account
		Name dn = buildDn(group.getName());

		DirContextAdapter context = new DirContextAdapter(dn);
		mapToContext(group, context);

		this.ldapTemplate.bind(dn, context, null);
	}

	private void mapToContext(Group group, DirContextAdapter context) {

		context.setAttributeValues("objectclass", new String[] { "top", "posixGroup" });

		// person attributes
		setAccountField(context, GroupSchema.COMMON_NAME_KEY, group.getName());
		
		setAccountField(context, GroupSchema.DESCRIPTION_KEY, group.getDescription());
		
		setAccountField(context, "gidNumber", "10"); // TODO require a sequencer
	}
	
	private void setAccountField(DirContextOperations context,  String fieldName, Object value) {

		if( !isNullValue(value) ){
			context.setAttributeValue(fieldName, value);
		}
	}
	private boolean isNullValue(Object value) {

		if(value == null) return true;
		
		if(value instanceof String){
			if(((String)value).length() == 0) return true;
		}
		
		return false;
	}
	

	

}
