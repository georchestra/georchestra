/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
                // .authorizeRequests().antMatchers("/**").permitAll().and()//
                .authorizeRequests()//
                .anyRequest()//
                .authenticated()//
                .and()//
                .addFilter(georchestraSecurityProxyAuthenticationFilter());
    }
}
