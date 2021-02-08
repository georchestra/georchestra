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
package org.georchestra.datafeeder.config;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for the DataFeeder
 * application
 */
public @Data class DataFeederConfigurationProperties {

    private FileUploadConfig fileUpload = new FileUploadConfig();
    private PublishingConfiguration publishing = new PublishingConfiguration();

    public static @Data class FileUploadConfig {
        /** maximum size allowed for uploaded files. */
        private String maxFileSize;

        /** maximum size allowed for multipart/form-data requests */
        private String maxRequestSize;

        /** size threshold after which files will be written to disk. */
        private String fileSizeThreshold;

        /**
         * directory location where files will be stored by the servlet container once
         * the request exceeds the {@link #fileSizeThreshold}
         */
        private String temporaryLocation = "";

        /** directory location where files will be stored. */
        private Path persistentLocation = Paths.get("/tmp/datafeeder/uploads");
    }

    public static @Data class PublishingConfiguration {

        private ExternalApiConfiguration geoserver = new ExternalApiConfiguration();
        private ExternalApiConfiguration geonetwork = new ExternalApiConfiguration();
        private BackendConfiguration backend = new BackendConfiguration();
    }

    public static @Data class ExternalApiConfiguration {
        private URL apiUrl;
        private URL publicUrl;
        private String username;
        private String password;
    }

    public static @Data class BackendConfiguration {
        /**
         * Connection parameters for the back-end, matching the connection parameters of
         * a GeoTools data-store, used by this application to set up the target data
         * store where to import uploaded datasets. The application uses these
         * parameters as a template and may tweak them to match application specific
         * business rules. For example, the georchestra configuration strategy may
         * create one postgis database schema for each organization short name.
         */
        private Map<String, String> local = new HashMap<>();

        /**
         * Connection parameters used as template to create a matching geoserver data
         * store during publishing. It must provide access to the same backend as the
         * {@link #local} parameters, albeit it could use a different strategy (for
         * example, {@link #local} may set up a regular postgis data store, while in
         * geoserver it might use a JNDI resource)
         */
        private Map<String, String> geoserver = new HashMap<>();
    }
}
