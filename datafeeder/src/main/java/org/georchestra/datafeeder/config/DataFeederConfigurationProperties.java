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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.io.ByteStreams;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for the DataFeeder
 * application
 */
@Slf4j(topic = "org.georchestra.datafeeder.config")
public @Data class DataFeederConfigurationProperties {

    private URI frontEndConfigFile;
    private FileUploadConfig fileUpload = new FileUploadConfig();
    private PublishingConfiguration publishing = new PublishingConfiguration();
    private EmailConfig email = new EmailConfig();

    public static @Data class EmailConfig {
        boolean send;
        URI ackTemplate;
        URI analysisFailedTemplate;
        URI publishFailedTemplate;
        URI publishSuccessTemplate;
    }

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
        private GeonetworkPublishingConfiguration geonetwork = new GeonetworkPublishingConfiguration();
        private BackendConfiguration backend = new BackendConfiguration();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class GeonetworkPublishingConfiguration extends ExternalApiConfiguration {
        private String templateRecordId;
        private URI templateRecord;
        private URI templateTransform;
    }

    public static @Data class ExternalApiConfiguration {
        private URL apiUrl;
        private URL publicUrl;
        private String username;
        private String password;
        private boolean logRequests;
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

    /**
     * Loads a resource from a configuration property.
     * 
     * @param uri            the resource URI, can be a file or a remote URL
     * @param configPropName the config property name, used for logging and
     *                       exception throwing
     * @return the contents of the resource
     * 
     * @throws IllegalArgumentException if the resource can't be loaded for any
     *                                  reason
     */
    public byte[] loadResource(URI uri, String configPropName) {
        log.info("loading {} from {}", configPropName, uri);
        final boolean isFile = null == uri.getScheme() || "file".equalsIgnoreCase(uri.getScheme());
        final byte[] contents = isFile ? loadFile(uri, configPropName) : loadURL(uri, configPropName);
        return contents;
    }

    public String loadResourceAsString(URI uri, String configPropName) {
        byte[] contents = loadResource(uri, configPropName);
        return new String(contents, StandardCharsets.UTF_8);
    }

    private byte[] loadURL(URI uri, String configPropName) {
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw throwIAE(e, "Invalid config: %s=%s", configPropName, uri);
        }
        try (InputStream in = url.openStream()) {
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            throw throwIAE(e, "Error loading: %s=%s", configPropName, uri);
        }
    }

    private byte[] loadFile(URI uri, String configPropName) {
        Path path = Paths.get(uri.getRawSchemeSpecificPart()).toAbsolutePath();
        if (!Files.exists(path)) {
            throw throwIAE(null, "File does not exist: %s=%s, file:%s", configPropName, uri,
                    path.toAbsolutePath().toString());
        }
        uri = path.toUri();
        return loadURL(uri, configPropName);
    }

    private IllegalArgumentException throwIAE(Exception cause, String msg, Object... msgParams) {
        String message = String.format(msg, msgParams);
        throw new IllegalArgumentException(message, cause);
    }

}
