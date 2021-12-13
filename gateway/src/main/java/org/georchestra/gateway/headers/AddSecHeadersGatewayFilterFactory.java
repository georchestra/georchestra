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
package org.georchestra.gateway.headers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.georchestra.gateway.config.GatewayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class AddSecHeadersGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private @Autowired(required = false) List<HeaderProvider> providers = new ArrayList<>();
    private @Autowired GatewayConfig gatewayConfig;

    public AddSecHeadersGatewayFilterFactory() {
        super(NameConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(NAME_KEY);
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return new AddSecHeadersGatewayFilter();
//        return (exchange, chain) -> {
//            Mono<Authentication> auth = ReactiveSecurityContextHolder.getContext()
//                    .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
//                    .map(SecurityContext::getAuthentication);
//
//            Mono<Principal> principal = exchange.getPrincipal();
//
//            Mono<String> name = auth.doOnNext(principal -> {
//                System.err.println(principal);
//            }).map(Principal::getName);
//
//            Mono<Principal> p = exchange.getPrincipal();
//            Mono<String> name = p.doOnNext(principal -> {
//                System.err.println(principal);
//            }).map(Principal::getName);
//
//            ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
//            providers.forEach(provider -> requestBuilder.headers(provider.prepare(exchange)));
//
//            ServerHttpRequest request = requestBuilder//
//                    .header("sec-proxy", "true")//
//                    .header("sec-username", "testuser")//
//                    .header("sec-org", "PSC")//
//                    .header("sec-roles", "ROLE_USER")//
//                    .build();
//
//            return chain.filter(exchange.mutate().request(request).build());
//        };
    }

    private static class AddSecHeadersGatewayFilter implements GatewayFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            Mono<String> name = exchange.getPrincipal().map(this::resolveName);
            Mono<ServerWebExchange> e = name.map(n -> this.addHeaders(n, exchange)).defaultIfEmpty(exchange);
            Mono<Void> flatMap = e.flatMap(chain::filter);
            return flatMap;
        }

        private ServerWebExchange addHeaders(String name, ServerWebExchange exchange) {
            ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
            // providers.forEach(provider ->
            // requestBuilder.headers(provider.prepare(exchange)));

            ServerHttpRequest request = requestBuilder//
                    .header("sec-proxy", "true")//
                    .header("sec-username", name)//
                    .header("sec-org", "PSC")//
                    .header("sec-roles", "ROLE_ADMINISTRATOR;ROLE_GNADMIN,ROLE_SUPERUSER")//
                    .build();

            return exchange.mutate().request(request).build();
        }

        private String resolveName(Principal p) {
            System.err.println("Resolving " + p.getClass().getName());
            String name;
            if (p instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken u = ((OAuth2AuthenticationToken) p);
                Collection<GrantedAuthority> authorities = u.getAuthorities();
                String authorizedClientRegistrationId = u.getAuthorizedClientRegistrationId();
                Object credentials = u.getCredentials();
                Object details = u.getDetails();
                String n = u.getName();
                OAuth2User principal = u.getPrincipal();
                Map<String, Object> attributes = principal.getAttributes();
                String name2 = principal.getName();
                Map<String, Object> attributes2 = principal.getAttributes();
                name = (String) attributes2.get("email");
            } else {
                name = p.getName();
            }
            return name;
        }
    }

//    return (exchange, chain) -> exchange.getPrincipal()
//            // .log("token-relay-filter")
//            .filter(principal -> principal instanceof OAuth2AuthenticationToken)
//            .cast(OAuth2AuthenticationToken.class)
//            .flatMap(authentication -> authorizedClient(exchange, authentication))
//            .map(OAuth2AuthorizedClient::getAccessToken).map(token -> withBearerAuth(exchange, token))
//            // TODO: adjustable behavior if empty
//            .defaultIfEmpty(exchange).flatMap(chain::filter);

    private Optional<String> extractService(ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        return null;
    }

}
