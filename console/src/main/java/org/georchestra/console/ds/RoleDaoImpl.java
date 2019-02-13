/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
import org.georchestra.console.dto.RoleSchema;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.backoffice.roles.RoleProtected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;


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

	private String basePath;
    private String roleSearchBaseDN;

	public void setBasePath(String basePath)
    {
		this.basePath = basePath;
	}

    public void setRoleSearchBaseDN(String roleSearchBaseDN)
    {
        this.roleSearchBaseDN = roleSearchBaseDN;
    }

	@Autowired
	private AdminLogDao logDao;

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private RoleProtected roles;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}



	public Name buildRoleDn(String cn) {
		try {
			return LdapNameBuilder.newInstance(this.roleSearchBaseDN).add("cn", cn).build();
		} catch (org.springframework.ldap.InvalidNameException ex){
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	public void setLogDao(AdminLogDao logDao) {
		this.logDao = logDao;
	}

	public void setRoles(RoleProtected roles) {
		this.roles = roles;
	}

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void addUser(String roleID, Account user, String originLogin) throws DataServiceException, NameNotFoundException {


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

			context.addAttributeValue("member", accountDao.buildFullUserDn(user), false);
			this.ldapTemplate.modifyAttributes(context);

			// Add log entry for this modification
			if(originLogin != null) {
				AdminLogType logType = this.roles.isProtected(roleID) ? AdminLogType.SYSTEM_ROLE_CHANGE : AdminLogType.OTHER_ROLE_CHANGE;
				AdminLogEntry log = new AdminLogEntry(originLogin, user.getUid(), logType, new Date());
				this.logDao.save(log);
			}

		} catch (Exception e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}

	}

	@Override
	public void deleteUser(Account account, final String originLogin) throws DataServiceException {

		List<Role> allRoles = findAllForUser(account);

		for (Role role : allRoles) {
			deleteUser(role.getName(), account, originLogin);
		}
	}

	public void deleteUser(String roleName, Account account, final String originLogin) throws DataServiceException {
		/* TODO Add hierarchic behaviour here like addUser method */

		Name dnSvUser = buildRoleDn(roleName);

		DirContextOperations ctx = ldapTemplate.lookupContext(dnSvUser);
		ctx.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });
		ctx.removeAttributeValue("member", accountDao.buildFullUserDn(account));

		this.ldapTemplate.modifyAttributes(ctx);

		// Add log entry for this modification
		if(originLogin != null) {
			AdminLogType logType;
			if(this.roles.isProtected(roleName)){
				logType = AdminLogType.SYSTEM_ROLE_CHANGE;
			} else {
				logType = AdminLogType.OTHER_ROLE_CHANGE;
			}
			AdminLogEntry log = new AdminLogEntry(originLogin, account.getUid(), logType, new Date());
			this.logDao.save(log);

		}
	}

    @Override
    public void modifyUser(Account oldAccount, Account newAccount) throws DataServiceException {
		for (Role role : findAllForUser(oldAccount)) {
			Name dnRole = buildRoleDn(role.getName());
			String oldUserDn = accountDao.buildFullUserDn(oldAccount);
			String newUserDn = accountDao.buildFullUserDn(newAccount);
			DirContextOperations ctx = ldapTemplate.lookupContext(dnRole);
			ctx.removeAttributeValue("member", oldUserDn);
			ctx.addAttributeValue("member", newUserDn);
			this.ldapTemplate.modifyAttributes(ctx);
		}
    }

	public List<Role> findAll() {

		EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");

		List<Role> roleList = ldapTemplate.search(roleSearchBaseDN, filter.encode(), new RoleContextMapper());

		TreeSet<Role> sorted = new TreeSet<Role>();
		for (Role g : roleList) {
			sorted.add(g);
		}

		return new LinkedList<Role>(sorted);
	}

	public List<Role> findAllForUser(Account account) {
		EqualsFilter grpFilter = new EqualsFilter("objectClass", "groupOfMembers");
		AndFilter filter = new AndFilter();
		filter.and(grpFilter);
		filter.and(new EqualsFilter("member", accountDao.buildFullUserDn(account)));
		return ldapTemplate.search(roleSearchBaseDN, filter.encode(),	new RoleContextMapper());
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

        // inserts the new role
		Name dn = buildRoleDn(role.getName());

		DirContextAdapter context = new DirContextAdapter(dn);
		mapToContext(role, context);

		try {
		  this.ldapTemplate.bind(dn, context, null);
		} catch (org.springframework.ldap.NamingException e) {
			LOG.error(e);
			throw new DataServiceException(e);
		}
	}

	private void mapToContext(Role role, DirContextOperations context) {

		context.setAttributeValues("objectclass", new String[] { "top", "groupOfMembers" });

		setAccountField(context, RoleSchema.COMMON_NAME_KEY, role.getName());
		setAccountField(context, RoleSchema.DESCRIPTION_KEY, role.getDescription());
		context.setAttributeValues(RoleSchema.MEMBER_KEY,role.getUserList().stream()
				.map(userUid -> {
					try {
						return accountDao.findByUID(userUid);
					} catch (DataServiceException e) {
						return null;
					}})
				.filter(account -> null != account)
				.map(account -> accountDao.buildFullUserDn(account))
				.collect(Collectors.toList()).toArray());
		if (role.isFavorite()) {
			setAccountField(context, RoleSchema.FAVORITE_KEY, RoleSchema.FAVORITE_VALUE);
		} else {
			context.removeAttributeValue(RoleSchema.FAVORITE_KEY, RoleSchema.FAVORITE_VALUE);
		}
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

		Name sourceDn = buildRoleDn(roleName);
		Name destDn = buildRoleDn(role.getName());

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

			ldapTemplate.rename(sourceDn, destDn);
        }

		DirContextOperations context = ldapTemplate.lookupContext(destDn);
        mapToContext(role, context);
		ldapTemplate.modifyAttributes(context);

	}

	private void addUsers(String roleName, List<Account> addList, final String originLogin) throws NameNotFoundException, DataServiceException {

		for (Account account : addList) {
			addUser(roleName, account, originLogin);
		}
	}

	private void deleteUsers(String roleName, List<Account> deleteList, final String originLogin)
			throws DataServiceException, NameNotFoundException {

		for (Account account : deleteList) {
			deleteUser(roleName, account, originLogin);
		}

	}

	@Override
	public void addUsersInRoles(List<String> putRole, List<Account> users, final String originLogin)
			throws DataServiceException, NameNotFoundException {

		for (String roleName : putRole) {
			addUsers(roleName, users, originLogin);
		}
	}

	@Override
	public void deleteUsersInRoles(List<String> deleteRole, List<Account> users, final String originLogin)
			throws DataServiceException, NameNotFoundException {

		for (String roleName : deleteRole) {
			deleteUsers(roleName, users, originLogin);
		}

	}
}
