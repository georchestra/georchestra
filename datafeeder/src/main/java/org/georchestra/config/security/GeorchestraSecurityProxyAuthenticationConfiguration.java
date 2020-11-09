package org.georchestra.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class GeorchestraSecurityProxyAuthenticationConfiguration extends WebSecurityConfigurerAdapter {

    public @Bean GeorchestraSecurityProxyAuthenticationManager georchestraSecurityProxyAuthenticationManager() {
        return new GeorchestraSecurityProxyAuthenticationManager();
    }

    public @Bean GeorchestraSecurityProxyAuthenticationFilter georchestraSecurityProxyAuthenticationFilter() {
        GeorchestraSecurityProxyAuthenticationFilter filter = new GeorchestraSecurityProxyAuthenticationFilter();
        filter.setAuthenticationManager(georchestraSecurityProxyAuthenticationManager());
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()//
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)//
                .and()//
                .csrf().disable()//
                .authorizeRequests().antMatchers("/import/test").permitAll().and().authorizeRequests()//
                .anyRequest()//
                .authenticated()//
                .and()//
                .addFilter(georchestraSecurityProxyAuthenticationFilter());
    }
}
