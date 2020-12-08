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

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public @Controller class FileUploadApiController implements FileUploadApi {

//    private final FileStorageService storageService;
//
//    public @Autowired FileUploadController(FileStorageService storageService) {
//        this.storageService = storageService;
//    }

    @RolesAllowed("ROLE_USER")
    public @Override ResponseEntity<Void> uploadFiles(@RequestPart(value = "filename") List<MultipartFile> filename) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }
}
