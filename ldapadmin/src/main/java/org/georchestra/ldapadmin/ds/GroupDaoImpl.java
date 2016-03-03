/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ldapadmin.ds;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.dao.AdminLogDao;
import org.georchestra.ldapadmin.dto.*;
import org.georchestra.ldapadmin.model.AdminLogEntry;
import org.georchestra.ldapadmin.model.AdminLogType;
import org.georchestra.ldapadmin.ws.backoffice.groups.GroupProtected;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private AdminLogDao logDao;
	
	@Autowired
	private GroupProtected groups;

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

	public void setLogDao(AdminLogDao logDao) {
		this.logDao = logDao;
	}

	public void setGroups(GroupProtected groups) {
		this.groups = groups;
	}

	/**
	 * Retrieve immutable identifier (UUID) from Ldap
	 *
	 * @param uid
	 *            Muttable identifier of account
	 * @return immutable identifier
	 */

	private UUID findUUID(String uid){
 		String[] attRet =  UserSchema.ATTR_TO_RETRIEVE;
		AccountDaoImpl.AccountContextMapper acm = new AccountDaoImpl.AccountContextMapper();
		DistinguishedName dn = new DistinguishedName();
		dn.add(userSearchBaseDN);
		dn.add("uid", uid);
		Account a = (Account) ldapTemplate.lookup(dn,attRet, acm);
		return UUID.fromString(a.getUUID());
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
	public void addUser(final String groupID, final String userId, final String originUUID) throws NameNotFoundException, DataServiceException {


		/* TODO Add hierarchic behaviour here :
			* if configuration flag hierarchic_groups is set and,
			* if group name contain separator (also found in config)
			* then remove last suffix of current group DSI_RENNES_AGRO_SCENE_VOITURE --> DSI_RENNES_AGRO_SCENE,
			* and re-call this method addUser(DSI_RENNES_AGRO_SCENE, ...)
		 */

		Name dn = buildGroupDn(groupID);
		DirContextOperations context = ldapTemplate.lookupContext(dn);

		context.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });

		try {

			context.addAttributeValue("member", buildUserDn(userId).toString(), false);
			this.ldapTemplate.modifyAttributes(context);

			// Add log entry for this modification
			if(originUUID != null) {
				UUID admin = UUID.fromString(originUUID);
				UUID target = this.findUUID(userId);
				AdminLogType logType = this.groups.isProtected(groupID) ? AdminLogType.SYSTEM_GROUP_CHANGE : AdminLogType.OTHER_GROUP_CHANGE;
				AdminLogEntry log = new AdminLogEntry(admin, target, logType, new Date());
				this.logDao.save(log);
			}

		} catch (Exception e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}

	}

	/**
	 * Removes the uid from all groups
	 *
	 * @param uid user to remove
	 * @param originUUID UUID of admin that make request
	 */
	@Override
	public void deleteUser(String uid, final String originUUID) throws DataServiceException {

		List<Group> allGroups = findAllForUser(uid);

		for (Group group : allGroups) {
			deleteUser(group.getName(), uid, originUUID);
		}
	}

	public void deleteUser(String groupName, String uid, final String originUUID) throws DataServiceException {
		/* TODO Add hierarchic behaviour here like addUser method */

		Name dnSvUser = buildGroupDn(groupName);

		DirContextOperations ctx = ldapTemplate.lookupContext(dnSvUser);
		ctx.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });
		ctx.removeAttributeValue("member", buildUserDn(uid).toString());

		this.ldapTemplate.modifyAttributes(ctx);

		// Add log entry for this modification
		if(originUUID != null) {
			UUID admin = UUID.fromString(originUUID);
			UUID target = this.findUUID(uid);
			AdminLogType logType;
			if(groupName.equals(Group.PENDING)){
				logType = AdminLogType.ACCOUNT_MODERATION;
			} else if(this.groups.isProtected(groupName)){
				logType = AdminLogType.SYSTEM_GROUP_CHANGE;
			} else {
				logType = AdminLogType.OTHER_GROUP_CHANGE;
			}
			AdminLogEntry log = new AdminLogEntry(admin, target, logType, new Date());
			this.logDao.save(log);

		}
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
	 * @throws NameNotFoundException
	 */
	@Override
	public Group findByCommonName(String commonName) throws DataServiceException, NameNotFoundException {

		try{
			DistinguishedName dn = buildGroupDn(commonName);
			Group g = (Group) ldapTemplate.lookup(dn, new GroupContextMapper());

			return  g;

		} catch (NameNotFoundException e){

			throw new NameNotFoundException("There is not a group with this common name (cn): " + commonName);
		}
	}

	/**
	 * Removes the group
	 *
	 * @param commonName
	 *
	 */
	@Override
	public void delete(final String commonName) throws DataServiceException, NameNotFoundException {

		if (!this.groups.isProtected(commonName)) {
			this.ldapTemplate.unbind(buildGroupDn(commonName), true);
		} else {
			throw new DataServiceException("Group " + commonName + " is a protected group");
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

		} catch (NameNotFoundException e1) {
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
	 * @param groupName
	 * @param group
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 * @throws DuplicatedCommonNameException
	 */
	@Override
	public synchronized void update(final String groupName, final Group group) throws DataServiceException, NameNotFoundException, DuplicatedCommonNameException {

		if( group.getName().length()== 0 ){
			throw new IllegalArgumentException("given name is required");
		}

        if (!group.getName().equals(groupName)) {
            // checks unique common name
            try{
                findByCommonName(group.getName());

                throw new DuplicatedCommonNameException("there is a group with this name: " + group.getName());

            } catch (NameNotFoundException e1) {
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
	public void addUsers(String groupName, List<String> addList, final String originUUID) throws NameNotFoundException, DataServiceException {

		for (String uid : addList) {
			addUser(groupName, uid, originUUID);
		}
	}

	@Override
	public void deleteUsers(String groupName, List<String> deleteList, final String originUUID)
			throws DataServiceException, NameNotFoundException {

		for (String uid : deleteList) {
			deleteUser(groupName, uid, originUUID);
		}

	}

	@Override
	public void addUsersInGroups(List<String> putGroup, List<String> users, final String originUUID)
			throws DataServiceException, NameNotFoundException {


		for (String groupName : putGroup) {

			addUsers(groupName, users, originUUID);
		}
	}

	@Override
	public void deleteUsersInGroups(List<String> deleteGroup, List<String> users, final String originUUID)
			throws DataServiceException, NameNotFoundException {

		for (String groupName : deleteGroup) {

			deleteUsers(groupName, users, originUUID);
		}

	}
}
