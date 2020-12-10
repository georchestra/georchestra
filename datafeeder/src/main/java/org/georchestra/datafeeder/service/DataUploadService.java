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
package org.georchestra.datafeeder.service;

import java.util.List;
import java.util.UUID;

import org.georchestra.datafeeder.model.DataUploadState;
import org.springframework.beans.factory.annotation.Autowired;

public class DataUploadService {

    private @Autowired FileStorageService storageService;

    public void analyze(UUID uploadId) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public DataUploadState findJobState(UUID uploadId) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public List<DataUploadState> findAllJobs() {
        throw new UnsupportedOperationException("unimplemented");
    }

    public List<DataUploadState> findUserJobs(String userName) {
        throw new UnsupportedOperationException("unimplemented");
    }
}
