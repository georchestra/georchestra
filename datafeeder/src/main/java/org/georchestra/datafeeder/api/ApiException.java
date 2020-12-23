package org.georchestra.datafeeder.api;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@SuppressWarnings("serial")
public abstract class ApiException extends RuntimeException {

    private ApiException(String reason) {
        super(reason);
    }

    public abstract HttpStatus getStatus();

    public static ApiException notFound(String format, Object... args) {
        throw new NotFound(String.format(format, args));
    }

    public static ApiException forbidden(String format, Object... args) {
        throw new Forbidden(String.format(format, args));
    }

    @ResponseStatus(value = NOT_FOUND)
    static class NotFound extends ApiException {
        private @Getter final HttpStatus status = NOT_FOUND;

        public NotFound(String reason) {
            super(reason);
        }
    }

    @ResponseStatus(value = FORBIDDEN)
    static class Forbidden extends ApiException {
        private @Getter final HttpStatus status = FORBIDDEN;

        public Forbidden(String reason) {
            super(reason);
        }
    }

}
