/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ds;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dto.*;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.backoffice.roles.RoleProtected;
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
import org.springframework.ldap.support.LdapNameBuilder;


/**
 * Maintains the role of users in the ldap store.
 *
 *
 * @author Mauricio Pazos
 *
 */
public class RoleDaoImpl implements RoleDao {

	private static final Log LOG = LogFactory.getLog(RoleDaoImpl.class.getName());

	private LdapTemplate ldapTemplate;

	@Autowired
	private AdminLogDao logDao;
	
	@Autowired
	private RoleProtected roles;

	private String uniqueNumberField = "ou";

	private Name roleSearchBaseDN;
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

	public void setRoleSearchBaseDN(String roleSearchBaseDN) throws InvalidNameException {
		this.roleSearchBaseDN = LdapNameBuilder.newInstance(roleSearchBaseDN).build();
	}

	public void setUserSearchBaseDN(String userSearchBaseDN) {
		this.userSearchBaseDN = new LdapRdn(userSearchBaseDN);
	}

	public void setLogDao(AdminLogDao logDao) {
		this.logDao = logDao;
	}

	public void setRoles(RoleProtected roles) {
		this.roles = roles;
	}

    /**
	 * Create an ldap entry for the role
	 *
	 * @param cn
	 * @return
	 */
	private Name buildRoleDn(String cn) {
		try {
			return LdapNameBuilder.newInstance(this.roleSearchBaseDN).add("cn", cn).build();
		} catch (org.springframework.ldap.InvalidNameException ex){
			throw new IllegalArgumentException(ex.getMessage());
		}
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
	 * @see org.georchestra.console.ds.RoleDao#addUser(java.lang.String, java.lang.String)
	 */
	@Override
	public void addUser(final String roleID, final String userId, final String originLogin) throws NameNotFoundException, DataServiceException {


		/* TODO Add hierarchic behaviour here :
			* if configuration flag hierarchic_roles is set and,
			* if role name contain separator (also found in config)
			* then remove last suffix of current role DSI_RENNES_AGRO_SCENE_VOITURE --> DSI_RENNES_AGRO_SCENE,
			* and re-call this method addUser(DSI_RENNES_AGRO_SCENE, ...)
		 */

		Name dn = buildRoleDn(roleID);
		DirContextOperations context = ldapTemplate.lookupContext(dn);

		context.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });

		try {

			context.addAttributeValue("member", buildUserDn(userId).toString(), false);
			this.ldapTemplate.modifyAttributes(context);

			// Add log entry for this modification
			if(originLogin != null) {
				AdminLogType logType = this.roles.isProtected(roleID) ? AdminLogType.SYSTEM_ROLE_CHANGE : AdminLogType.OTHER_ROLE_CHANGE;
				AdminLogEntry log = new AdminLogEntry(originLogin, userId, logType, new Date());
				this.logDao.save(log);
			}

		} catch (Exception e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}

	}

	/**
	 * Removes the uid from all roles
	 *
	 * @param uid user to remove
	 * @param originLogin login of admin that make request
	 */
	@Override
	public void deleteUser(String uid, final String originLogin) throws DataServiceException {

		List<Role> allRoles = findAllForUser(uid);

		for (Role role : allRoles) {
			deleteUser(role.getName(), uid, originLogin);
		}
	}

	public void deleteUser(String roleName, String uid, final String originLogin) throws DataServiceException {
		/* TODO Add hierarchic behaviour here like addUser method */

		Name dnSvUser = buildRoleDn(roleName);

		DirContextOperations ctx = ldapTemplate.lookupContext(dnSvUser);
		ctx.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });
		ctx.removeAttributeValue("member", buildUserDn(uid).toString());

		this.ldapTemplate.modifyAttributes(ctx);

		// Add log entry for this modification
		if(originLogin != null) {
			AdminLogType logType;
			if(roleName.equals(Role.PENDING)){
				logType = AdminLogType.ACCOUNT_MODERATION;
			} else if(this.roles.isProtected(roleName)){
				logType = AdminLogType.SYSTEM_ROLE_CHANGE;
			} else {
				logType = AdminLogType.OTHER_ROLE_CHANGE;
			}
			AdminLogEntry log = new AdminLogEntry(originLogin, uid, logType, new Date());
			this.logDao.save(log);

		}
	}

    @Override
    public void modifyUser(String roleName, String oldUid, String newUid) throws DataServiceException {
        Name dnRole = buildRoleDn(roleName);
        String oldUserDn = buildUserDn(oldUid).toString();
        String newUserDn = buildUserDn(newUid).toString();
        DirContextOperations ctx = ldapTemplate.lookupContext(dnRole);
        ctx.removeAttributeValue("member", oldUserDn);
        ctx.addAttributeValue("member", newUserDn);
        this.ldapTemplate.modifyAttributes(ctx);
    }

	public List<Role> findAll() throws DataServiceException {

		EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");

		List<Role> roleList = ldapTemplate.search(this.roleSearchBaseDN, filter.encode(), new RoleContextMapper());

		TreeSet<Role> sorted = new TreeSet<Role>();
		for (Role g : roleList) {
			sorted.add(g);
		}

		return new LinkedList<Role>(sorted);
	}

	public List<Role> findAllForUser(String userId) {
		EqualsFilter grpFilter = new EqualsFilter("objectClass", "groupOfMembers");
		AndFilter filter = new AndFilter();
		filter.and(grpFilter);
		filter.and(new EqualsFilter("member", buildUserDn(userId).toString()));
		return ldapTemplate.search(this.roleSearchBaseDN, filter.encode(),	new RoleContextMapper());
	}

	public List<String> findUsers(final String roleName) throws DataServiceException{

		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "groupOfMembers"));
		filter.and(new EqualsFilter("cn", roleName));

		return ldapTemplate.search(roleSearchBaseDN, filter.encode(), new RoleContextMapper());
	}


	/**
	 * Searches the role by common name (cn)
	 *
	 * @param commonName
	 * @throws NameNotFoundException
	 */
	@Override
	public Role findByCommonName(String commonName) throws DataServiceException, NameNotFoundException {

		try{
			Name dn = buildRoleDn(commonName);
			Role g = (Role) ldapTemplate.lookup(dn, new RoleContextMapper());

			return  g;

		} catch (NameNotFoundException e){

			throw new NameNotFoundException("There is not a role with this common name (cn): " + commonName);
		}
	}

	/**
	 * Removes the role
	 *
	 * @param commonName
	 *
	 */
	@Override
	public void delete(final String commonName) throws DataServiceException, NameNotFoundException {

		if (!this.roles.isProtected(commonName)) {
			this.ldapTemplate.unbind(buildRoleDn(commonName), true);
		} else {
			throw new DataServiceException("Role " + commonName + " is a protected role");
		}

	}

	private static class RoleContextMapper implements ContextMapper {

		@Override
		public Object mapFromContext(Object ctx) {

			DirContextAdapter context = (DirContextAdapter) ctx;

			// set the role name
			Role g = RoleFactory.create();
			g.setName(context.getStringAttribute(RoleSchema.COMMON_NAME_KEY));
			g.setDescription(context.getStringAttribute(RoleSchema.DESCRIPTION_KEY));
			boolean isFavorite = RoleSchema.FAVORITE_VALUE.equals(context.getStringAttribute(RoleSchema.FAVORITE_KEY));
			g.setFavorite(isFavorite);

			// set the list of user
			Object[] members = getUsers(context);
			for (int i = 0; i < members.length; i++) {
				g.addUser((String) members[i]);
			}

			return g;
		}

		private Object[] getUsers(DirContextAdapter context) {
			Object[] members = context.getObjectAttributes(RoleSchema.MEMBER_KEY);
			if(members == null){

				members = new Object[0];
			}
			return members;
		}
	}

	@Override
	public synchronized void insert(Role role) throws DataServiceException, DuplicatedCommonNameException {

		if( role.getName().length()== 0 ){
			throw new IllegalArgumentException("given name is required");
		}
		// checks unique common name
		try{
			if(findByCommonName(role.getName()) == null)
				throw new NameNotFoundException("Not found");

			throw new DuplicatedCommonNameException("there is a role with this name: " + role.getName());

		} catch (NameNotFoundException e1) {
			// if an role with the specified name cannot be retrieved, then
			// the new role can be safely added.
		    LOG.debug("The role with name " + role.getName() + " does not exist yet, it can "
		            + "then be safely created." );
		}


        EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");
        Integer uniqueNumber = AccountDaoImpl.findUniqueNumber(filter, uniqueNumberField, this.uniqueNumberCounter, ldapTemplate);

        // inserts the new role
		Name dn = buildRoleDn(role.getName());

		DirContextAdapter context = new DirContextAdapter(dn);
		mapToContext(uniqueNumber, role, context);

		try {
		  this.ldapTemplate.bind(dn, context, null);
		} catch (org.springframework.ldap.NamingException e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}
	}

	private void mapToContext(Integer uniqueNumber, Role role, DirContextOperations context) {

		context.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });

        // person attributes
        if (uniqueNumber != null) {
            setAccountField(context, uniqueNumberField, uniqueNumber.toString());
        }
		// person attributes
		setAccountField(context, RoleSchema.COMMON_NAME_KEY, role.getName());
		setAccountField(context, RoleSchema.DESCRIPTION_KEY, role.getDescription());
		setMemberField(context, RoleSchema.MEMBER_KEY, role.getUserList());
		if(role.isFavorite())
			setAccountField(context, RoleSchema.FAVORITE_KEY, RoleSchema.FAVORITE_VALUE);
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
	 * Updates the field of role in the LDAP store
	 *
	 * @param roleName
	 * @param role
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 * @throws DuplicatedCommonNameException
	 */
	@Override
	public synchronized void update(final String roleName, final Role role) throws DataServiceException, NameNotFoundException, DuplicatedCommonNameException {

		if( role.getName().length()== 0 ){
			throw new IllegalArgumentException("given name is required");
		}

        if (!role.getName().equals(roleName)) {
            // checks unique common name
            try{
                findByCommonName(role.getName());

                throw new DuplicatedCommonNameException("there is a role with this name: " + role.getName());

            } catch (NameNotFoundException e1) {
                // if a role with the specified name cannot be retrieved, then
                // the new role can be safely renamed.
                LOG.debug("no account with name " + role.getName() + " can be found, it is then "
                        + "safe to rename the role.");
            }
        }

        Name sourceDn = buildRoleDn(roleName);
        Name destDn = buildRoleDn(role.getName());


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

        // because cn is part of distinguish name it cannot be updated. So the role is removed to include a new one with the new values
        delete(roleName);

        if (uniqueNumber == -1) {
            // no unique number defined so just insert it and which will assign a unique number
            insert(role);
        } else {
            // inserts the new role
            DirContextAdapter context = new DirContextAdapter(destDn);
            mapToContext(uniqueNumber, role, context);

            try {
                this.ldapTemplate.bind(destDn, context, null);
            } catch (org.springframework.ldap.NamingException e) {
                LOG.error(e);
                throw new DataServiceException(e);
            }
        }
	}

	@Override
	public void addUsers(String roleName, List<String> addList, final String originLogin) throws NameNotFoundException, DataServiceException {

		for (String uid : addList) {
			addUser(roleName, uid, originLogin);
		}
	}

	@Override
	public void deleteUsers(String roleName, List<String> deleteList, final String originLogin)
			throws DataServiceException, NameNotFoundException {

		for (String uid : deleteList) {
			deleteUser(roleName, uid, originLogin);
		}

	}

	@Override
	public void addUsersInRoles(List<String> putRole, List<String> users, final String originLogin)
			throws DataServiceException, NameNotFoundException {

		for (String roleName : putRole) {
			addUsers(roleName, users, originLogin);
		}
	}

	@Override
	public void deleteUsersInRoles(List<String> deleteRole, List<String> users, final String originLogin)
			throws DataServiceException, NameNotFoundException {

		for (String roleName : deleteRole) {
			deleteUsers(roleName, users, originLogin);
		}

	}
}
