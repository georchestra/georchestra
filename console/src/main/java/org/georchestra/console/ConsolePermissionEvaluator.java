package org.georchestra.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Org;
import org.georchestra.console.dto.Role;
import org.georchestra.console.model.DelegationEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ConsolePermissionEvaluator implements PermissionEvaluator {

    private static final Log LOG = LogFactory.getLog(ConsolePermissionEvaluator.class.getName());
    private static GrantedAuthority ROLE_SUPERADMIN = new SimpleGrantedAuthority("ROLE_ADMINISTRATOR");

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private OrgsDao orgsDao;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (isSuperAdministrator(authentication)) {
            return true;
        } else {
            // TODO: refactor with Delegation2 class
            String username = authentication.getName();
            DelegationEntry delegation = delegationDao.findOne(username);
            if (delegation == null) {
                return false;
            }
            String[] orgs = delegation.getOrgs();
            HashSet<String> delegationMembers = new HashSet<String>();
            for (String o : orgs) {
                Org orga = orgsDao.findByCommonName(o);
                delegationMembers.addAll(orga.getMembers());
            }
            // TODO: this should be done before, because no need
            // to iterate before if not of type Role
            if (targetDomainObject instanceof Role) {
                Role r = (Role) targetDomainObject;
                List<String> userList = r.getUserList();
                userList.retainAll(delegationMembers);
                return Arrays.asList(delegation.getRoles()).contains(r.getName());
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        if (isSuperAdministrator(authentication)) {
            return true;
        }
        return false;
    }

    private boolean isSuperAdministrator(Authentication authentication) {
        return authentication.getAuthorities().contains(ROLE_SUPERADMIN);
    }

}
