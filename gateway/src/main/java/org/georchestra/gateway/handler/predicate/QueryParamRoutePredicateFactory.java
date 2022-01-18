/*
 * Copyright (C) 2022 by the geOrchestra PSC
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
package org.georchestra.gateway.handler.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.validation.constraints.NotEmpty;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

/**
 * URI predicate filter based on the existence of a given query parameter
 * <p>
 * Usage:
 * 
 * <pre>
 *  
 * {@code
 * - id: <routeid>
 *   uri: <targeturi>
 *   predicates:
 *    - QueryParam=<param name>
 * }
 * </pre>
 */
public class QueryParamRoutePredicateFactory
        extends AbstractRoutePredicateFactory<QueryParamRoutePredicateFactory.Config> {

    public static final String PARAM_KEY = "param";

    public QueryParamRoutePredicateFactory() {
        super(QueryParamRoutePredicateFactory.Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(PARAM_KEY);
    }

    @Override
    public Predicate<ServerWebExchange> apply(QueryParamRoutePredicateFactory.Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                String param = config.param;
                if (exchange.getRequest().getQueryParams().containsKey(param)) {
                    return true;
                }
                return false;
            }

            public @Override String toString() {
                return String.format("Query: param=%s", config.getParam());
            }
        };
    }

    @Validated
    public static class Config {

        @NotEmpty
        private String param;

        public String getParam() {
            return param;
        }

        public QueryParamRoutePredicateFactory.Config setParam(String param) {
            this.param = param;
            return this;
        }
    }
}
