/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.datafeeder.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
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

    public static ApiException badRequest(String format, Object... args) {
        throw new BadRequest(String.format(format, args));
    }

    public static InternalServerError internalServerError(Throwable cause, String format, Object... args) {
        throw new InternalServerError(String.format(format, args), cause);
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    static class InternalServerError extends ApiException {
        private @Getter final HttpStatus status = INTERNAL_SERVER_ERROR;;

        public InternalServerError(String reason, Throwable cause) {
            super(reason);
            super.initCause(cause);
        }
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

    @ResponseStatus(value = BAD_REQUEST)
    static class BadRequest extends ApiException {
        private @Getter final HttpStatus status = BAD_REQUEST;

        public BadRequest(String reason) {
            super(reason);
        }
    }

}
