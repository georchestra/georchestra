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
package org.georchestra.gateway.filter.headers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.stream.IntStream;

import org.georchestra.gateway.filter.headers.RemoveHeadersGatewayFilterFactory;
import org.georchestra.gateway.filter.headers.RemoveHeadersGatewayFilterFactory.RegExConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

class RemoveHeadersGatewayFilterFactoryTest {

    private RemoveHeadersGatewayFilterFactory filter;

    private final RegExConfig secHeadersConfig = new RemoveHeadersGatewayFilterFactory.RegExConfig(
            "(?i)(sec-.*|Authorization)");

    @BeforeEach
    void setUp() throws Exception {
        filter = new RemoveHeadersGatewayFilterFactory();
    }

    @Test
    void testApply() {
        GatewayFilter gatewayFilter = filter.apply(secHeadersConfig);
        assertNotNull(gatewayFilter);

        HttpHeaders headers = headers("sec-proxy", "true", "sec-username", "testadmin", "Host", "localhost", "sec-org",
                "PSC", "ETag", null);
        MockServerHttpRequest request = MockServerHttpRequest.get("/").headers(headers).build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        ArgumentCaptor<ServerWebExchange> filterArgCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        gatewayFilter.filter(exchange, chain);

        verify(chain).filter(filterArgCaptor.capture());

        ServerWebExchange mutatedExchange = filterArgCaptor.getValue();
        HttpHeaders mutatedHeaders = mutatedExchange.getRequest().getHeaders();
        HttpHeaders expected = headers("Host", "localhost", "ETag", null);
        assertEquals(expected, mutatedHeaders);
    }

    @Test
    void testNeedsFiltering() {
        HttpHeaders headers = headers("sec-proxy", "true", "sec-username", "testadmin", "Host", "localhost", "sec-org",
                "PSC", "ETag", null);
        assertTrue(secHeadersConfig.anyMatches(headers));
        headers.remove("sec-proxy");
        assertTrue(secHeadersConfig.anyMatches(headers));
        headers.remove("sec-username");
        assertTrue(secHeadersConfig.anyMatches(headers));
        headers.remove("sec-org");
        assertFalse(secHeadersConfig.anyMatches(headers));
    }

    @Test
    void testRemoveSecurityHeaders() {
        HttpHeaders headers = headers("sec-proxy", "true", "sec-username", "testadmin", "Host", "localhost", "sec-org",
                "PSC", "ETag", null);

        secHeadersConfig.removeMatching(headers);
        HttpHeaders expected = headers("Host", "localhost", "ETag", null);
        assertEquals(expected, headers);
    }

    @Test
    void testIsSecurityHeader() {
        assertTrue(secHeadersConfig.matches("sec-user"));
        assertTrue(secHeadersConfig.matches("SEC-USER"));
        assertTrue(secHeadersConfig.matches("sec-org"));
        assertTrue(secHeadersConfig.matches("SEC-ORG"));
        assertTrue(secHeadersConfig.matches("authoriZation"), "Basic Auth header shoulud should be filtered");
        assertFalse(secHeadersConfig.matches("SECORG"));
    }

    private HttpHeaders headers(String... kvp) {
        assertTrue(kvp.length % 2 == 0);
        HttpHeaders headers = new HttpHeaders();
        IntStream.range(0, kvp.length - 1)//
                .filter(i -> i % 2 == 0)//
                .forEach(keyIndex -> headers.set(kvp[keyIndex], kvp[keyIndex + 1]));
        return headers;
    }
}
