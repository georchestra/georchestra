package org.georchestra.lib.springutils;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GeorchestraUserDetailsService  implements
AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
			throws AuthenticationException {
		Assert.notNull(token.getDetails());
		return createUserDetails(token, null);
	}

	protected UserDetails createUserDetails(Authentication token,
			Collection<? extends GrantedAuthority> authorities) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.
				currentRequestAttributes()).getRequest();
		String secRoles = request.getHeader("sec-roles");
		List<GrantedAuthority>  auth = StringUtils.isEmpty(secRoles) ?
				AuthorityUtils.NO_AUTHORITIES :
				semicolonSeparatedStringToAuthorityList(secRoles);

		return new User(token.getName(), "N/A", true, true, true, true, auth);
	}
	
	public static List<GrantedAuthority> semicolonSeparatedStringToAuthorityList(
			String authorityString) {
		return AuthorityUtils.createAuthorityList(StringUtils
				.tokenizeToStringArray(authorityString, ";"));
	}
}
