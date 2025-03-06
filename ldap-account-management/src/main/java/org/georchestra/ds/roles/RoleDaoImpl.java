/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.ds.roles;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.Name;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.DuplicatedCommonNameException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.users.Account;

import org.georchestra.ds.users.AccountDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;

/**
 * @author Mauricio Pazos
 */
public class RoleDaoImpl implements RoleDao {

    private static final Log LOG = LogFactory.getLog(RoleDaoImpl.class.getName());

    private LdapTemplate ldapTemplate;

    private String roleSearchBaseDN;

    @Autowired
    private AccountDaoImpl accountDao;

    @Autowired
    private OrgsDaoImpl orgDao;

    @Autowired
    private RoleProtected roles;

    public void setRoleSearchBaseDN(String roleSearchBaseDN) {
        this.roleSearchBaseDN = roleSearchBaseDN;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setRoles(RoleProtected roles) {
        this.roles = roles;
    }

    public void setAccountDao(AccountDaoImpl accountDao) {
        this.accountDao = accountDao;
    }

    public void setOrgDao(OrgsDaoImpl orgDao) {
        this.orgDao = orgDao;
    }

    @Override
    public void addUser(String roleName, Account user) throws DataServiceException, NameNotFoundException {
        /*
         * TODO Add hierarchic behaviour here : if configuration flag hierarchic_roles
         * is set and, if role name contain separator (also found in config) then remove
         * last suffix of current role DSI_RENNES_AGRO_SCENE_VOITURE -->
         * DSI_RENNES_AGRO_SCENE, and re-call this method addUser(DSI_RENNES_AGRO_SCENE,
         * ...)
         */

        Name dn = buildRoleDn(roleName);
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        try {
            context.addAttributeValue("member", accountDao.buildFullUserDn(user), false);
            this.ldapTemplate.modifyAttributes(context);
        } catch (Exception e) {
            throw new DataServiceException(e);
        }
    }

    @Override
    public void addOrg(String roleName, Org org) {
        Name dn = buildRoleDn(roleName);
        DirContextOperations context = ldapTemplate.lookupContext(dn);
        context.addAttributeValue("member", orgDao.buildFullOrgDn(org), false);
        this.ldapTemplate.modifyAttributes(context);
    }

    @Override
    public void deleteUser(Account account) throws DataServiceException {
        List<Role> allRoles = findAllForUser(account);
        for (Role role : allRoles) {
            deleteUser(role.getName(), account);
        }
    }

    @Override
    public void deleteUser(String roleName, Account account) throws NameNotFoundException, DataServiceException {
        /* TODO Add hierarchic behaviour here like addUser method */
        Role role = this.findByCommonName(roleName);
        String username = account.getUid();
        List<String> userList = role.getUserList();
        boolean removed = userList != null && userList.remove(username);
        if (removed) {
            try {
                this.update(roleName, role);
            } catch (NameNotFoundException | DuplicatedCommonNameException e) {
                throw new DataServiceException(e);
            }
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

    @Override
    public List<Role> findAll() {
        EqualsFilter filter = new EqualsFilter("objectClass", "groupOfMembers");
        List<Role> roleList = ldapTemplate.search(roleSearchBaseDN, filter.encode(), new RoleContextMapper());
        roleList.sort(Role::compareTo);
        return roleList;
    }

    @Override
    public List<Role> findAllForUser(Account account) {
        EqualsFilter grpFilter = new EqualsFilter("objectClass", "groupOfMembers");
        AndFilter filter = new AndFilter();
        filter.and(grpFilter);
        filter.and(new EqualsFilter("member", accountDao.buildFullUserDn(account)));
        return ldapTemplate.search(roleSearchBaseDN, filter.encode(), new RoleContextMapper());
    }

    /**
     * Searches the role by common name (cn)
     */
    @Override
    public Role findByCommonName(String commonName) throws DataServiceException, NameNotFoundException {
        try {
            Name dn = buildRoleDn(commonName);
            return ldapTemplate.lookup(dn, new RoleContextMapper());
        } catch (NameNotFoundException e) {
            throw new NameNotFoundException("There is not a role with this common name (cn): " + commonName);
        }
    }

    @Override
    public void delete(final String commonName) throws DataServiceException, NameNotFoundException {
        if (this.roles.isProtected(commonName)) {
            throw new DataServiceException("Role " + commonName + " is a protected role");
        }
        try {
            this.ldapTemplate.unbind(buildRoleDn(commonName), true);
        } catch (NameNotFoundException ignore) {
            LOG.debug("Tried to remove a non exising role, ignoring: " + commonName);
        }
    }

    @Override
    public synchronized void insert(Role role) throws DataServiceException, DuplicatedCommonNameException {
        if (role.getName().length() == 0) {
            throw new IllegalArgumentException("given name is required");
        }

        checkThereIsNoRoleWithThisName(role.getName());

        // inserts the new role
        Name dn = buildRoleDn(role.getName());
        DirContextAdapter context = new DirContextAdapter(dn);
        mapToContext(role, context);
        try {
            this.ldapTemplate.bind(dn, context, null);
        } catch (org.springframework.ldap.NamingException e) {
            throw new DataServiceException(e);
        }
    }

    /**
     * Updates the field of role in the LDAP store
     */
    @Override
    public synchronized void update(final String roleName, final Role role)
            throws DataServiceException, NameNotFoundException, DuplicatedCommonNameException {
        if (role.getName().length() == 0) {
            throw new IllegalArgumentException("given name is required");
        }

        Name sourceDn = buildRoleDn(roleName);
        Name destDn = buildRoleDn(role.getName());

        if (!role.getName().equals(roleName)) {
            checkThereIsNoRoleWithThisName(role.getName());
            ldapTemplate.rename(sourceDn, destDn);
        }

        DirContextOperations context = ldapTemplate.lookupContext(destDn);
        mapToContext(role, context);
        ldapTemplate.modifyAttributes(context);
    }

    @Override
    public void addUsersInRoles(List<String> putRole, List<Account> users)
            throws DataServiceException, NameNotFoundException {
        for (String roleName : putRole) {
            addUsers(roleName, users);
        }
    }

    @Override
    public void deleteUsersInRoles(List<String> deleteRole, List<Account> users)
            throws DataServiceException, NameNotFoundException {
        for (String roleName : deleteRole) {
            deleteUsers(roleName, users);
        }
    }

    @Override
    public void addOrgsInRoles(List<String> putRole, List<Org> orgs)
            throws DataServiceException, NameNotFoundException {
        putRole.stream().forEach(roleName -> {

            Name dn = buildRoleDn(roleName);
            DirContextOperations context = ldapTemplate.lookupContext(dn);

            try {
                orgs.stream().forEach(org -> {
                    context.addAttributeValue("member", orgDao.buildFullOrgDn(org), false);
                });
                this.ldapTemplate.modifyAttributes(context);
            } catch (Exception e) {
            }
        });
    }

    @Override
    public void deleteOrgsInRoles(List<String> deleteRole, List<Org> orgs)
            throws DataServiceException, NameNotFoundException {
        deleteRole.stream().forEach(roleName -> {
            Name dn = buildRoleDn(roleName);
            DirContextOperations context = ldapTemplate.lookupContext(dn);

            try {
                orgs.stream().forEach(org -> {
                    context.removeAttributeValue("member", orgDao.buildFullOrgDn(org));
                });
                this.ldapTemplate.modifyAttributes(context);
            } catch (Exception e) {
            }
        });
    }

    private class RoleContextMapper implements ContextMapper<Role> {
        @Override
        public Role mapFromContext(Object ctx) {
            DirContextAdapter context = (DirContextAdapter) ctx;

            // set the role name
            Role role = RoleFactory.create();
            String suuid = context.getStringAttribute(RoleSchema.UUID_KEY);
            UUID uuid = null == suuid ? null : UUID.fromString(suuid);

            role.setUniqueIdentifier(uuid);
            role.setName(context.getStringAttribute(RoleSchema.COMMON_NAME_KEY));
            role.setDescription(context.getStringAttribute(RoleSchema.DESCRIPTION_KEY));
            boolean isFavorite = RoleSchema.FAVORITE_VALUE.equals(context.getStringAttribute(RoleSchema.FAVORITE_KEY));
            role.setFavorite(isFavorite);

            // set the list of user
            Object[] members = getUsers(context);
            for (int i = 0; i < members.length; i++) {
                if (!((String) members[i]).endsWith(orgDao.getOrgSearchBaseDN())) {
                    role.addUser((String) members[i]);
                }
            }
            members = getOrgs(context);
            for (int i = 0; i < members.length; i++) {
                if (((String) members[i]).endsWith(orgDao.getOrgSearchBaseDN())) {
                    role.addOrg((String) members[i]);
                }
            }

            return role;
        }

        private Object[] getUsers(DirContextAdapter context) {
            Object[] members = context.getObjectAttributes(RoleSchema.MEMBER_KEY);
            if (members == null) {
                members = new Object[0];
            }
            return members;
        }

        private Object[] getOrgs(DirContextAdapter context) {
            Object[] members = context.getObjectAttributes(RoleSchema.MEMBER_KEY);
            if (members == null) {
                members = new Object[0];
            }
            return members;
        }

    }

    @VisibleForTesting
    protected Name buildRoleDn(String cn) {
        try {
            return LdapNameBuilder.newInstance(this.roleSearchBaseDN).add("cn", cn).build();
        } catch (org.springframework.ldap.InvalidNameException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private void mapToContext(Role role, DirContextOperations context) {
        Set<String> objectClass = new HashSet<>();

        if (context.getStringAttributes("objectClass") != null) {
            Collections.addAll(objectClass, context.getStringAttributes("objectClass"));
        }
        Collections.addAll(objectClass, "top", "groupOfMembers", "georchestraRole");

        context.setAttributeValues("objectClass", objectClass.toArray());

        if (null == role.getUniqueIdentifier()) {
            role.setUniqueIdentifier(UUID.randomUUID());
        }
        setContextField(context, RoleSchema.UUID_KEY, role.getUniqueIdentifier().toString());
        setContextField(context, RoleSchema.COMMON_NAME_KEY, role.getName());
        setContextField(context, RoleSchema.DESCRIPTION_KEY, role.getDescription());

        Stream<String> userMembers = role.getUserList().stream() //
                .map(accountDao::findByUID) //
                .filter(Objects::nonNull) //
                .map(account -> accountDao.buildFullUserDn(account));

        Stream<String> orgMembers = role.getOrgList().stream() //
                .map(orgDao::findByCommonName) //
                .filter(Objects::nonNull) //
                .map(org -> orgDao.buildFullOrgDn(org));

        String[] members = Stream.concat(userMembers, orgMembers).collect(Collectors.toList()).toArray(new String[0]);
        context.setAttributeValues(RoleSchema.MEMBER_KEY, members);

        if (role.isFavorite()) {
            setContextField(context, RoleSchema.FAVORITE_KEY, RoleSchema.FAVORITE_VALUE);
        } else {
            context.removeAttributeValue(RoleSchema.FAVORITE_KEY, RoleSchema.FAVORITE_VALUE);
        }
    }



    /**
     * if the value is not null then sets the value in the context.
     *
     */
    private void setContextField(DirContextOperations context, String fieldName, Object value) {
        if (!isNullValue(value)) {
            context.setAttributeValue(fieldName, value);
        }
    }

    private boolean isNullValue(Object value) {
        if (value == null)
            return true;
        if (value instanceof String && (((String) value).length() == 0)) {
            return true;
        }
        return false;
    }

    private void addUsers(String roleName, List<Account> addList) throws NameNotFoundException, DataServiceException {
        for (Account account : addList) {
            addUser(roleName, account);
        }
    }

    private void deleteUsers(String roleName, List<Account> deleteList)
            throws DataServiceException, NameNotFoundException {
        for (Account account : deleteList) {
            deleteUser(roleName, account);
        }
    }

    private void checkThereIsNoRoleWithThisName(String roleName)
            throws DataServiceException, DuplicatedCommonNameException {
        try {
            if (findByCommonName(roleName) == null) {
                throw new NameNotFoundException("Not found");
            }
            throw new DuplicatedCommonNameException("there is a role with this name: " + roleName);
        } catch (NameNotFoundException e) {
            LOG.debug("Role with name " + roleName
                    + " does not exist yet, name can be safely used for creation / renaming.");
        }
    }
}
