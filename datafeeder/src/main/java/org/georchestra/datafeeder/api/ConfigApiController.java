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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import io.swagger.annotations.Api;

@Controller
@Api(tags = { "Config" }) // hides the empty *-api-controller entry in swagger-ui.html
public class ConfigApiController implements ConfigApi {

    private static final String CONFIG_PROPERTY_NAME = "datafeeder.front-end-config-file";

    private @Autowired DataFeederConfigurationProperties props;

    public @Override ResponseEntity<Object> getFrontendConfig() {
        URI uri = props.getFrontEndConfigFile();
        if (null == uri) {
            throw ApiException.internalServerError(null, "Invalid config: %s=%s", CONFIG_PROPERTY_NAME, uri);
        }

        final boolean isFile = null == uri.getScheme() || "file".equalsIgnoreCase(uri.getScheme());
        final byte[] contents = isFile ? loadFile(uri) : loadURL(uri);

        JsonNode node;
        try {
            node = new ObjectMapper().reader().readTree(contents);
        } catch (Exception e) {
            throw ApiException.internalServerError(e, "Invalid parsing file from config: %s=%s", CONFIG_PROPERTY_NAME,
                    uri);
        }
        return ResponseEntity.ok(node);
    }

    private byte[] loadURL(URI uri) {
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw ApiException.internalServerError(e, "Invalid config: %s=%s", CONFIG_PROPERTY_NAME, uri);
        }
        try (InputStream in = url.openStream()) {
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            throw ApiException.internalServerError(e, "Error loading: %s=%s", CONFIG_PROPERTY_NAME, uri);
        }
    }

    private byte[] loadFile(URI uri) {
        Path path = Paths.get(uri.getRawSchemeSpecificPart()).toAbsolutePath();
        if (!Files.exists(path)) {
            throw ApiException.internalServerError(null, "File does not exist: %s=%s, file:%s", CONFIG_PROPERTY_NAME,
                    uri, path.toAbsolutePath().toString());
        }
        uri = path.toUri();
        return loadURL(uri);
    }

}
