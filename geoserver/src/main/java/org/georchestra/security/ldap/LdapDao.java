package org.georchestra.security.ldap;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.LdapUserSearch;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

/**
 * A specific UserDetailsService for LDAP.
 * @author elemoine
 *
 */
public class LdapDao implements UserDetailsService, InitializingBean {
	
	/**
	 * 
	 */
	private LdapAuthoritiesPopulator authoritiesPopulator;
	
	/**
	 * 
	 */
	private LdapUserSearch userSearch;
	
	/**
	 * 
	 * @param userSearch
	 * @param authoritiesPopulator
	 */
	public LdapDao(LdapUserSearch userSearch,
			       LdapAuthoritiesPopulator authoritiesPopulator) {
		this.userSearch = userSearch;
		this.authoritiesPopulator = authoritiesPopulator;
	}

	/**
	 * 
	 */
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		
		LdapUserDetails ldapUser = userSearch.searchForUser(username);
		LdapUserDetailsImpl.Essence user =
			new LdapUserDetailsImpl.Essence(ldapUser);
        user.setUsername(username);
        user.setPassword(ldapUser.getPassword());

        GrantedAuthority[] extraAuthorities =
            authoritiesPopulator.getGrantedAuthorities(ldapUser);
        for(int i = 0; i < extraAuthorities.length; i++) {
            user.addAuthority(extraAuthorities[i]);
        }

        return user.createUserDetails();
	}

	/**
	 * 
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
