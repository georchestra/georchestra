/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityAutoconfiguration {

//
//    private @Autowired RouteLocator routeLocator;
//
    @Bean
    SecurityWebFilterChain configure(ServerHttpSecurity http) throws Exception {
        // http.oauth2Client(null/*withDefaults()*/)
        http//
                .anonymous().authorities("ROLE_ANONYMOUS")//
                // enable oauth2 and http basic auth
                .and().oauth2Login().and().httpBasic().and().formLogin()//
                // configure path matchers
                .and()//
                .authorizeExchange()//
                .pathMatchers("/", "/header/**").permitAll()//
                .pathMatchers("/**").authenticated();
        return http.build();
    }
}
