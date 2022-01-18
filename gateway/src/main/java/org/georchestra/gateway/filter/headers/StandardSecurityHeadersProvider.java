package org.georchestra.gateway.filter.headers;

import java.net.URI;
import java.util.function.Consumer;

import org.georchestra.gateway.config.GatewayConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

public class StandardSecurityHeadersProvider implements HeaderProvider {

    private @Autowired GatewayConfigProperties config;

    @Override
    public Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> {
            URI uri = exchange.getRequest().getURI();
            String path = uri.getPath();
            GatewayConfigProperties c = config;
            System.err.println(uri);
        };
    }

}
