/*
 * Copyright (C) 2020 by the geOrchestra PSC
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

import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

@RequestMapping(path = "/import")
public @Controller class DataImportWizardController {

    private @Autowired NativeWebRequest currentRequest;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("annonymous test OK. " + buildHeaders());
    }

    @GetMapping("/test/admin")
    @RolesAllowed("ROLE_ADMINISTRATOR")
    public ResponseEntity<String> testAdmin() {
        return ResponseEntity.ok("admin role test OK. " + buildHeaders());
    }

    @GetMapping("/test/user")
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<String> testUser() {
        return ResponseEntity.ok("user role test OK. " + buildHeaders());
    }

    private String buildHeaders() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(currentRequest.getHeaderNames(), 0), false)
                .map(name -> String.format("%s=%s", name, currentRequest.getHeader(name)))
                .collect(Collectors.joining(", "));
    }
}
