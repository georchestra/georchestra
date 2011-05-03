package com.camptocamp.security;

import java.util.List;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

public class LdapRolesDao {
    private LdapTemplate ldapTemplate;
    

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllContactNames() {
        AttributesMapper mapper = new AttributesMapper() {
            @Override
            public Object mapFromAttributes(javax.naming.directory.Attributes attrs)
                    throws javax.naming.NamingException {
                return attrs.get("cn").get();
            }
        };
        return ldapTemplate.search("ou=groups", "(cn=*)", mapper);
    }

}