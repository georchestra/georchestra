package org.georchestra.gateway.headers;

import java.net.URI;
import java.util.function.Consumer;

import org.georchestra.gateway.config.GatewayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

public class StandardSecurityHeadersProvider implements HeaderProvider {

    private @Autowired GatewayConfig config;

    @Override
    public Consumer<HttpHeaders> prepare(ServerWebExchange exchange) {
        return headers -> {
            URI uri = exchange.getRequest().getURI();
            String path = uri.getPath();
            GatewayConfig c = config;
            System.err.println(uri);
        };
    }

}
