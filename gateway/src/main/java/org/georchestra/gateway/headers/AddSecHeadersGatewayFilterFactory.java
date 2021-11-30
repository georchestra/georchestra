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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

public class AddSecHeadersGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private @Autowired(required = false) List<HeaderProvider> providers = new ArrayList<>();

    public AddSecHeadersGatewayFilterFactory() {
        super(NameConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(NAME_KEY);
    }

    @Override
    public GatewayFilter apply(NameConfig config) {

        return (exchange, chain) -> {
//
//            Mono<Authentication> auth = ReactiveSecurityContextHolder.getContext()
//                    .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
//                    .map(SecurityContext::getAuthentication);
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
            ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
            providers.forEach(provider -> requestBuilder.headers(provider.prepare(exchange)));

            ServerHttpRequest request = requestBuilder.build();
//                    .header("sec-proxy", "true")//
//                    .header("sec-username", name.toFuture().join() + "testuser")//
//                    .header("sec-org", "PSC")//
//                    .header("sec-roles", "ROLE_ADMINISTRATOR")//
//                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private Optional<String> extractService(ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        return null;
    }

}
