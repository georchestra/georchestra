package com.camptocamp.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.util.Assert;

/**
 * Reads information from a user node in LDAP and adds the information as
 * headers to the request.
 * 
 * @author jeichar
 */
public class LdapUserDetailsRequestHeaderProvider extends HeaderProvider {

    protected static final Log logger = LogFactory.getLog(LdapUserDetailsRequestHeaderProvider.class.getPackage().getName());

    private LdapUserSearch      _userSearch;
    private Map<String, String> _headerMapping;
    
    public LdapUserDetailsRequestHeaderProvider(LdapUserSearch userSearch, Map<String, String> headerMapping) {
        Assert.notNull(userSearch, "userSearch must not be null");
        Assert.notNull(headerMapping, "headerMapping must not be null");
        this._userSearch = userSearch;
        this._headerMapping = headerMapping;
    }

    @Override
    protected Collection<Header> getCustomRequestHeaders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof AnonymousAuthenticationToken){
            return Collections.emptyList();
        }
        String username = authentication.getName();
        DirContextOperations userData;
        try {
            userData = _userSearch.searchForUser(username);
        } catch (Exception e) {
            logger.info("Unable to lookup user:"+username,e);
            return Collections.emptyList();
        }
        ArrayList<Header> headers = new ArrayList<Header>();
        for (Map.Entry<String, String> entry : _headerMapping.entrySet()) {
            try {
                Attribute attributes = userData.getAttributes().get(entry.getValue());
                NamingEnumeration<?> all = attributes.getAll();
                StringBuilder value = new StringBuilder();
                while (all.hasMore()) {
                    if (value.length() > 0) {
                        value.append(',');
                    }
                    value.append(all.next());
                }
                headers.add(new BasicHeader(entry.getKey(), value.toString()));
            } catch (javax.naming.NamingException e) {
                logger.error("problem adding headers for request:" + entry.getKey(), e);
            }
        }
        return headers;
    }
}
