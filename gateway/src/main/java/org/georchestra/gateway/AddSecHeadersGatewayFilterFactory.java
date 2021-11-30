package org.georchestra.gateway;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

public class AddSecHeadersGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

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

//            Mono<Authentication> auth = ReactiveSecurityContextHolder.getContext()
//                    .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
//                    .map(SecurityContext::getAuthentication);
//
//            Mono<String> name = auth.doOnNext(principal -> {
//                System.err.println(principal);
//            }).map(Principal::getName);
            
            Mono<Principal> p = exchange.getPrincipal();
            Mono<String> name = p.doOnNext(principal -> {
                System.err.println(principal);
            }).map(Principal::getName);
            String email = null;

            ServerHttpRequest request = exchange.getRequest().mutate()//
                    .header("sec-proxy", "true")//
                    .header("sec-username", name.toFuture().join() + "testuser")//
                    .header("sec-org", "PSC")//
                    .header("sec-roles", "ROLE_ADMINISTRATOR")//
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

}
