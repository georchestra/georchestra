/**
 *
 */
package org.georchestra.ldapadmin.ds;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.Configuration;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * Maintains the group of users in the ldap store.
 *
 *
 * @author Mauricio Pazos
 *
 */
public class GroupDaoImpl implements GroupDao {



	private static final Log LOG = LogFactory.getLog(GroupDaoImpl.class.getName());

	private LdapTemplate ldapTemplate;

    private String uniqueNumberField = "ou";

    private LdapRdn groupSearchBaseDN;
    private LdapRdn userSearchBaseDN;

    private AtomicInteger uniqueNumberCounter = new AtomicInteger(-1);

    public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
    public void setUniqueNumberField(String uniqueNumberField) {
        this.uniqueNumberField = uniqueNumberField;
    }

	public void setGroupSearchBaseDN(String groupSearchBaseDN) {
		this.groupSearchBaseDN = new LdapRdn(groupSearchBaseDN);
	}
	public void setUserSearchBaseDN(String userSearchBaseDN) {
		this.userSearchBaseDN = new LdapRdn(userSearchBaseDN);
	}


    /**
	 * Create an ldap entry for the group
	 *
	 * @param cn
	 * @return
	 */
	private DistinguishedName buildGroupDn(String cn) {
		DistinguishedName dn = new DistinguishedName();

		dn.add(groupSearchBaseDN);
		dn.add("cn", cn);

		return dn;
	}

	/**
	 * Create an ldap entry for the user
	 *
	 * @param uid
	 * @return DistinguishedName the dn of the user.
	 */
	private DistinguishedName buildUserDn(String uid) {
		DistinguishedName dn = new DistinguishedName();
		try {
			LdapContextSource ctxsrc = (LdapContextSource) this.ldapTemplate.getContextSource();
			dn.addAll(ctxsrc.getBaseLdapPath());
		} catch (InvalidNameException e) {
		    LOG.error("unable to construct the userDn: "+e.getMessage());
		}
		dn.add(userSearchBaseDN);
		dn.add("uid", uid);

		return dn;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.ds.GroupDao#addUser(java.lang.String, java.lang.String)
	 */
	@Override
	public void addUser(final String groupID, final String userId) throws NotFoundException, DataServiceException {

		Name dn = buildGroupDn(groupID);
		DirContextOperations context = ldapTemplate.lookupContext(dn);

		context.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });

		try {

			context.addAttributeValue("member", buildUserDn(userId).toString(), false);
			this.ldapTemplate.modifyAttributes(context);

		} catch (Exception e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}

	}

	/**
	 * Removes the uid from all groups
	 *
	 * @param uid
	 */
	@Override
	public void deleteUser(String uid) throws DataServiceException {

		List<Group> allGroups = findAll();

		for (Group group : allGroups) {
			deleteUser(group.getName(), uid);
		}
	}

	public void deleteUser(String groupName, String uid) throws DataServiceException {

		Name dnSvUser = buildGroupDn(groupName);

		DirContextOperations ctx = ldapTemplate.lookupContext(dnSvUser);
		ctx.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });
		ctx.removeAttributeValue("member", buildUserDn(uid).toString());

		this.ldapTemplate.modifyAttributes(ctx);
	}

    @Override
    public void modifyUser(String groupName, String oldUid, String newUid) throws DataServiceException {
        Name dnGroup = buildGroupDn(groupName);
        String oldUserDn = buildUserDn(oldUid).toString();
        String newUserDn = buildUserDn(newUid).toString();
        DirContextOperations ctx = ldapTemplate.lookupContext(dnGroup);
        ctx.removeAttributeValue("member", oldUserDn);
        ctx.addAttributeValue("member", newUserDn);
        this.ldapTemplate.modifyAttributes(ctx);
    }

	public List<Group> findAll() throws DataServiceException {

		EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");
		List<Group> groupList = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(),
				new GroupContextMapper());

		TreeSet<Group> sorted = new TreeSet<Group>();
		for (Group g : groupList) {
			sorted.add(g);
		}

		return new LinkedList<Group>(sorted);
	}

	public List<Group> findAllForUser(String userId) {
		EqualsFilter grpFilter = new EqualsFilter("objectClass", "groupOfMembers");
		AndFilter filter = new AndFilter();
		filter.and(grpFilter);

		filter.and(new EqualsFilter("member", buildUserDn(userId).toString()));
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(),
				new GroupContextMapper());
	}

	public List<String> findUsers(final String groupName) throws DataServiceException{

		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "ou"));
		filter.and(new EqualsFilter(groupSearchBaseDN.getKey(), groupSearchBaseDN.getValue()));
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
	@Override
	public Group findByCommonName(String commonName) throws DataServiceException, NotFoundException {

		try{
			DistinguishedName dn = buildGroupDn(commonName);
			Group g = (Group) ldapTemplate.lookup(dn, new GroupContextMapper());

			return  g;

		} catch (NameNotFoundException e){

			throw new NotFoundException("There is not a group with this common name (cn): " + commonName);
		}
	}

	/**
	 * Removes the group
	 *
	 * @param commonName
	 *
	 */
	@Override
	public void delete(final String commonName) throws DataServiceException, NotFoundException{
	    try {
	        this.ldapTemplate.unbind(buildGroupDn(commonName), true);
	    } catch (NameNotFoundException e) {
	        throw new NotFoundException(e);
	    }
	}


	private static class GroupContextMapper implements ContextMapper {

		@Override
		public Object mapFromContext(Object ctx) {

			DirContextAdapter context = (DirContextAdapter) ctx;

			// set the group name
			Group g = GroupFactory.create();
			g.setName(context.getStringAttribute(GroupSchema.COMMON_NAME_KEY));

			g.setDescription(context.getStringAttribute(GroupSchema.DESCRIPTION_KEY));


			// set the list of user
			Object[] members = getUsers(context);
			for (int i = 0; i < members.length; i++) {

				g.addUser((String) members[i]);
			}

			return g;
		}

		private Object[] getUsers(DirContextAdapter context) {
			Object[] members = context.getObjectAttributes(GroupSchema.MEMBER_KEY);
			if(members == null){

				members = new Object[0];
			}
			return members;
		}
	}

	@Override
	public synchronized void insert(Group group) throws DataServiceException, DuplicatedCommonNameException {

		if( group.getName().length()== 0 ){
			throw new IllegalArgumentException("given name is required");
		}
		// checks unique common name
		try{
			findByCommonName(group.getName());

			throw new DuplicatedCommonNameException("there is a group with this name: " + group.getName());

		} catch (NotFoundException e1) {
			// if an group with the specified name cannot be retrieved, then
			// the new group can be safely added.
		    LOG.debug("The group with name " + group.getName() + " does not exist yet, it can "
		            + "then be safely created." );
		}


        EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");
        Integer uniqueNumber = AccountDaoImpl.findUniqueNumber(filter, uniqueNumberField, this.uniqueNumberCounter, ldapTemplate);

        // inserts the new group
		Name dn = buildGroupDn(group.getName());

		DirContextAdapter context = new DirContextAdapter(dn);
		mapToContext(uniqueNumber, group, context);

		try {
		  this.ldapTemplate.bind(dn, context, null);
		} catch (org.springframework.ldap.NamingException e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}
	}

	private void mapToContext(Integer uniqueNumber, Group group, DirContextOperations context) {

		context.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });

        // person attributes
        if (uniqueNumber != null) {
            setAccountField(context, uniqueNumberField, uniqueNumber.toString());
        }
		// person attributes
		setAccountField(context, GroupSchema.COMMON_NAME_KEY, group.getName());

		setAccountField(context, GroupSchema.DESCRIPTION_KEY, group.getDescription());

		setMemberField(context, GroupSchema.MEMBER_KEY, group.getUserList());

	}


    private void setMemberField(DirContextOperations context,
            String memberAttr, List<String> users) {
        List<String> usersFullDn = new ArrayList<String>(users.size());
        for (String uid : users) {
            usersFullDn.add(buildUserDn(uid).encode());
        }
        context.setAttributeValues(memberAttr, usersFullDn.toArray());
    }

	/**
	 * if the value is not null then sets the value in the context.
	 *
	 * @param context
	 * @param fieldName
	 * @param value
	 */
	private void setAccountField(DirContextOperations context,  String fieldName, Object value) {

		if( !isNullValue(value) ){
			context.setAttributeValue(fieldName, value);
		}
	}

	private boolean isNullValue(Object value) {

		if(value == null) return true;

        if (value instanceof String && (((String) value).length() == 0)) {
            return true;
        }

		return false;
	}

	/**
	 * Updates the field of group in the LDAP store
	 *
	 *
	 * @param groupName groupName to modify
	 * @param modified new values
	 * @throws NotFoundException
	 *
	 */
	@Override
	public synchronized void update(final String groupName, final Group group) throws DataServiceException, NotFoundException, DuplicatedCommonNameException {

		if( group.getName().length()== 0 ){
			throw new IllegalArgumentException("given name is required");
		}

        if (!group.getName().equals(groupName)) {
            // checks unique common name
            try{
                findByCommonName(group.getName());

                throw new DuplicatedCommonNameException("there is a group with this name: " + group.getName());

            } catch (NotFoundException e1) {
                // if a group with the specified name cannot be retrieved, then
                // the new group can be safely renamed.
                LOG.debug("no account with name " + group.getName() + " can be found, it is then "
                        + "safe to rename the group.");
            }
        }

        DistinguishedName sourceDn = buildGroupDn(groupName);
        DistinguishedName destDn = buildGroupDn(group.getName());


        Integer uniqueNumber = (Integer) ldapTemplate.lookup(sourceDn, new AttributesMapper() {
            @Override
            public Object mapFromAttributes(Attributes attributes) throws NamingException {
                final Attribute attribute = attributes.get(uniqueNumberField);
                if (attribute == null || attribute.size() == 0) {
                    return -1;
                }
                try {
                    return Integer.parseInt(attribute.get().toString());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        });

        // because cn is part of distinguish name it cannot be updated. So the group is removed to include a new one with the new values
        delete(groupName);

        if (uniqueNumber == -1) {
            // no unique number defined so just insert it and which will assign a unique number
            insert(group);
        } else {
            // inserts the new group
            DirContextAdapter context = new DirContextAdapter(destDn);
            mapToContext(uniqueNumber, group, context);

            try {
                this.ldapTemplate.bind(destDn, context, null);
            } catch (org.springframework.ldap.NamingException e) {
                LOG.error(e);
                throw new DataServiceException(e);
            }
        }
	}

	@Override
	public void addUsers(String groupName, List<String> addList) throws NotFoundException, DataServiceException {

		for (String uid : addList) {
			addUser(groupName, uid);
		}
	}

	@Override
	public void deleteUsers(String groupName, List<String> deleteList)
			throws DataServiceException, NotFoundException {

		for (String uid : deleteList) {
			deleteUser(groupName, uid);
		}

	}

	@Override
	public void addUsersInGroups(List<String> putGroup, List<String> users)
			throws DataServiceException, NotFoundException {


		for (String groupName : putGroup) {

			addUsers(groupName, users);
		}
	}

	@Override
	public void deleteUsersInGroups(List<String> deleteGroup, List<String> users)
			throws DataServiceException, NotFoundException {

		for (String groupName : deleteGroup) {

			deleteUsers(groupName, users);
		}

	}

}
