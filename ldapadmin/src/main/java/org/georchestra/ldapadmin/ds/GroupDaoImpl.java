/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.util.List;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * Maintains the group of users in the ldap store.
 * 
 * @author Mauricio Pazos
 *
 */
public class GroupDaoImpl implements GroupDao {

	
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

			ldapTemplate.modifyAttributes(context);

		} catch (Exception e) {
			throw new DataServiceException(e);		
		}
		
	}
	
	@Override
	public List<String> findAllGroups() throws DataServiceException {
		
		EqualsFilter filter = new EqualsFilter("objectClass", "posixGroup");
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), new GroupContextMapper());
	}
	
	private Group findGroupByCN(String groupCN) throws NotFoundException {

		DistinguishedName dn = buildGroupDn(groupCN);
		
		Group g = (Group) ldapTemplate.lookup(dn,  new GroupContextMapper() );
		
		if(g == null){
			throw new NotFoundException("There is not a group with this cn: " + groupCN);
		}
		
		return  g;
	}

	private DistinguishedName buildGroupDn(String  cn) {
		DistinguishedName dn = new DistinguishedName();
				
		dn.add("ou", "groups");
		dn.add("cn", cn);
		
		return dn;
	}
	
	
	private static class GroupContextMapper implements ContextMapper {

		@Override
		public Object mapFromContext(Object ctx) {
			
			DirContextAdapter context = (DirContextAdapter) ctx;

			// set the group name
			Group g = GroupFactory.create();
			g.setName(context.getStringAttribute("cn"));
			
			// set the list of user
			try {
				Attributes attributes = context.getAttributes("memberUid");
				NamingEnumeration<? extends Attribute> all = attributes.getAll();
				while (all.hasMore()) {
					Attribute user = all.next();
					g.addUser((String) user.get());
				}
				
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return g;
		}
	}
	

}
