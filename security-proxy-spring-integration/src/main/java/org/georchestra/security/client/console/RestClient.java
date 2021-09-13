package org.georchestra.security.client.console;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "org.georchestra.security.client.console")
class RestClient {

    private URI baseUri;
    private RestTemplate restTemplate;

    RestClient(URI baseUri) {
        this.baseUri = baseUri;
        this.restTemplate = new RestTemplate();
    }

    public <T> T get(String path, Class<T> type, Object... uriVariables) {
        final String uri = targetUri(path).toString();
        log.info("Querying {}", uri);
        ResponseEntity<T> response = restTemplate.getForEntity(uri, type, uriVariables);
        return getBodyOrFail(response);
    }

    private <T> T getBodyOrFail(ResponseEntity<T> response) {
        final HttpStatus statusCode = response.getStatusCode();
        if (HttpStatus.NOT_FOUND.equals(statusCode)) {
            return null;
        }
        if (statusCode.is5xxServerError() || statusCode.is4xxClientError()) {
            throw new IllegalStateException("Server returned " + statusCode);
        }
        return response.getBody();
    }

    private URI targetUri(String path) {
        return this.baseUri.resolve(path);
    }

}
