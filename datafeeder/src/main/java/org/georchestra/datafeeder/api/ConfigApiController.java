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

import java.net.URI;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        final byte[] contents = props.loadResource(uri, CONFIG_PROPERTY_NAME);

        JsonNode node;
        try {
            node = new ObjectMapper().reader().readTree(contents);
        } catch (Exception e) {
            throw ApiException.internalServerError(e, "Invalid parsing file from config: %s=%s", CONFIG_PROPERTY_NAME,
                    uri);
        }
        ((ObjectNode) node).put("maxFileUploadSize", props.getFileUpload().getMaxFileSize());
        return ResponseEntity.ok(node);
    }
}
