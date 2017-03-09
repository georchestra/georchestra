package org.georchestra.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class MyUserDetailsService implements UserDetailsService {

    private static final Log logger = LogFactory.getLog(MyUserDetailsService.class.getPackage().getName());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.debug("Log user : " + username);

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(this.createGrantedAuthority("ROLE_USER"));
        authorities.add(this.createGrantedAuthority("ROLE_ADMINISTRATOR"));
        

        UserDetails res = new User(username, "N/A", authorities);
        return res;
    }

    private GrantedAuthority createGrantedAuthority(final String role){
        return new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return role;
            }
        };
    }
}
