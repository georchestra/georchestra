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

package org.georchestra.datafeeder.service.publish.impl;

import java.net.URI;

import org.fao.geonet.client.ApiException;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.GeonetworkPublishingConfiguration;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link TemplateMapper} specific to geOrchestra, uses properties in
 * geOrchestra data directory's {@code datafeeder/datafeeder.properties} to
 * provide the location of the {@link #loadTemplateRecord() template record} and
 * {@link #resolveTransformURI() XSL transform}.
 */
@Slf4j
public class GeorchestraTemplateMapper extends TemplateMapper {

    private @Autowired @Setter DataFeederConfigurationProperties config;
    private @Autowired @Setter GeoNetworkRemoteService geonetwork;

    /**
     * Overrides to load the template transformation XSL file from the URI provided
     * in {@code datafeeder/datafeeder.properties} geOrchestra datadir file, under
     * the {@code datafeeder.publishing.geonetwork.template-transform} key. Defers
     * to the default template at {@link TemplateMapper#resolveTransformURI()} if no
     * URI is configured.
     */
    @Override
    protected URI resolveTransformURI() {
        GeonetworkPublishingConfiguration gn = config.getPublishing().getGeonetwork();
        URI transformURI = gn.getTemplateTransform();
        if (transformURI != null) {
            return transformURI;
        }
        log.info("No metadata template provided in georchestra datadir, using default XSL transform");
        return super.resolveTransformURI();
    }

    /**
     * Overrides to load the template XML record from the URI provided in
     * {@code datafeeder/datafeeder.properties} geOrchestra datadir file.
     * <p>
     * If the {@code datafeeder.publishing.geonetwork.template-record-id} property
     * is provided, it shall contain an existing geonetwork record id, and it will
     * be used to fetch the record from Geonetwork.
     * <p>
     * Otherwise, if the {@code datafeeder.publishing.geonetwork.template-record} is
     * provided, the templare record is loaded from that URI.
     * <p>
     * If neither property is provided, defers to the default template record at
     * {@link TemplateMapper#loadTemplateRecord()}.
     */
    @Override
    protected String loadTemplateRecord() {
        GeonetworkPublishingConfiguration gn = config.getPublishing().getGeonetwork();
        URI transformURI = gn.getTemplateRecord();
        if (transformURI != null) {
            return config.loadResourceAsString(transformURI, "datafeeder.publishing.geonetwork.template-record");
        }
        String templateRecordId = gn.getTemplateRecordId();
        if (StringUtils.hasText(templateRecordId)) {
            templateRecordId = templateRecordId.trim();
            log.info("Loading template record {} from Geonetwork", templateRecordId);
            try {
                return geonetwork.getRecordById(templateRecordId);
            } catch (ApiException e) {
                String message = String.format(
                        "Error loading template record '%s' from GeoNetwork, check the config property datafeeder.publishing.geonetwork.template-record-id",
                        templateRecordId);
                throw new IllegalArgumentException(message, e);
            }
        }
        log.info("No template record id or URI in georchstra datadir, using default template");
        return super.loadTemplateRecord();
    }

}
