package org.georchestra.gateway.security.ldap;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration(proxyBeanMethods = true)
@ConditionalOnProperty(name = "ldap.enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties
public class LdapSecurityAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "ldap")
    LdapConfigProperties ldapConfigProperties() {
        return new LdapConfigProperties();
    }

    @Bean
    BaseLdapPathContextSource contextSource() {
        LdapContextSource context = new LdapContextSource();
        LdapConfigProperties config = ldapConfigProperties();
        context.setUrl(config.getUrl());
        context.setBase(config.getBaseDn());
        context.afterPropertiesSet();
        return context;
    }

    @Bean
    public AuthenticationWebFilter ldapAuthenticationWebFilter(
            ReactiveAuthenticationManager ldapAuthenticationManager) {
        AuthenticationWebFilter ldapAuthFilter = new AuthenticationWebFilter(ldapAuthenticationManager);
        ldapAuthFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/auth/login"));
        return ldapAuthFilter;
    }

    @Bean
    ReactiveAuthenticationManager ldapAuthenticationManager() {
        BaseLdapPathContextSource contextSource = contextSource();
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = ldapAuthoritiesPopulator();
        GrantedAuthoritiesMapper authoritiesMapper = ldapAuthoritiesMapper();

        LdapConfigProperties config = ldapConfigProperties();
        String ldapUserSearchBase = config.getUsersRdn();
        String ldapUserSearchFilter = config.getUserSearchFilter();

        FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(ldapUserSearchBase, ldapUserSearchFilter,
                contextSource);

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(search);
        authenticator.afterPropertiesSet();

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
        provider.setAuthoritiesMapper(authoritiesMapper);

        AuthenticationManager manager = new ProviderManager(Arrays.asList(provider));
        return new ReactiveAuthenticationManagerAdapter(manager);
    }

    @Bean
    DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        BaseLdapPathContextSource contextSource = contextSource();
        LdapConfigProperties config = ldapConfigProperties();
        String ldapGroupSearchBase = config.getRolesRdn();
        String ldapGroupSearchFilter = config.getRolesSearchFilter();

        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource,
                ldapGroupSearchBase);
        authoritiesPopulator.setGroupSearchFilter(ldapGroupSearchFilter);
        authoritiesPopulator.setRolePrefix("ROLE_");

        return authoritiesPopulator;
    }

    @Bean
    GrantedAuthoritiesMapper ldapAuthoritiesMapper() {
        SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
        authorityMapper.setConvertToUpperCase(true);
        return authorityMapper;
    }
}
