package org.georchestra.security.client.console;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "org.georchestra.security.client.console")
public class RestClient {

    private URI baseUri;
    private RestTemplate restTemplate;

    RestClient(@NonNull URI baseUri) {
        this.baseUri = baseUri;

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(3000);
        httpRequestFactory.setReadTimeout(6000);

        this.restTemplate = new RestTemplate(httpRequestFactory);
        log.info("Created geOrchestra console app REST client for " + baseUri);
    }

    public <T> Optional<T> get(String path, Class<T> type, Object... uriVariables) {
        final String uri = targetUri(path).toString();
        log.debug("Querying {}", uri);
        ResponseEntity<T> response = restTemplate.getForEntity(uri, type, uriVariables);
        return Optional.ofNullable(getBodyOrFail(response));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(String path, final Class<?> type, Object... uriVariables) {
        final Class<?> arrayType;
        final Class<T> componentType;
        if (type.isArray()) {
            arrayType = type;
            componentType = (Class<T>) type.getComponentType();
        } else {
            arrayType = java.lang.reflect.Array.newInstance(type, 0).getClass();
            componentType = (Class<T>) type;
        }
        Object array = this.get(path, arrayType, uriVariables).orElse(null);
        return array == null ? Collections.emptyList() : toList(array, componentType);

    }

    private <T> List<T> toList(Object array, Class<T> type) {
        int length = Array.getLength(array);
        return IntStream.range(0, length).mapToObj(i -> Array.get(array, i)).map(type::cast)
                .collect(Collectors.toList());
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
