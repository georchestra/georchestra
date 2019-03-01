package org.georchestra.console;

import java.io.Serializable;
import java.util.*;

import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.SimpleAccount;
import org.georchestra.console.model.DelegationEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ConsolePermissionEvaluator implements PermissionEvaluator {

    private static GrantedAuthority ROLE_SUPERUSER = new SimpleGrantedAuthority("ROLE_SUPERUSER");

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private AdvancedDelegationDao advancedDelegationDao;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (isSuperAdministrator(authentication)) {
            return true;
        } else {
            String username = authentication.getName();
            DelegationEntry delegation = delegationDao.findOne(username);
            if (delegation == null) {
                return false;
            }

            // Filter based on object type
            if (targetDomainObject instanceof Role) {
                // Filter users in role and role itself
                Role r = (Role) targetDomainObject;
                List<String> userList = r.getUserList();
                // Remove users not under delegation
                userList.retainAll(this.advancedDelegationDao.findUsersUnderDelegation(username));
                r.setFavorite(true);
                // Remove role not under delegation
                return Arrays.asList(delegation.getRoles()).contains(r.getName());
            } else if(targetDomainObject instanceof Org){
                // Filter org
                Org org = (Org) targetDomainObject;
                return Arrays.asList(delegation.getOrgs()).contains(org.getId());
            } else if(targetDomainObject instanceof SimpleAccount){
                // filter account
                SimpleAccount account = (SimpleAccount) targetDomainObject;
                return Arrays.asList(delegation.getOrgs()).contains(account.getOrgId());
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
        return authentication.getAuthorities().contains(ROLE_SUPERUSER);
    }

}
