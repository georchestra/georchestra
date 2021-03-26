/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.PublishingConfiguration;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkResponse;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.ContactInfo;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.OnlineResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeorchestraMetadataPublicationService implements MetadataPublicationService {

    private GeoNetworkRemoteService geonetwork;
    private PublishingConfiguration publishingConfiguration;
    private TemplateMapper templateMapper;

    public @Autowired GeorchestraMetadataPublicationService(//
            @NonNull GeoNetworkRemoteService geonetwork, //
            @NonNull TemplateMapper templateMapper, //
            PublishingConfiguration publishingConfiguration) {
        this.templateMapper = templateMapper;
        this.publishingConfiguration = publishingConfiguration;
        this.geonetwork = geonetwork;
    }

    @Override
    public URI buildMetadataRecordURI(@NonNull String recordId) {
        return geonetwork.buildMetadataRecordURI(recordId);
    }

    @Override
    public void publish(@NonNull DatasetUploadState dataset) {
        Objects.requireNonNull(dataset.getPublishing());

        MetadataRecordProperties mdProps = toRecordProperties(dataset);
        String metadataId = mdProps.getMetadataId();

        Supplier<String> record = templateMapper.apply(mdProps);
        GeoNetworkResponse response = geonetwork.publish(metadataId, record);
        dataset.getPublishing().setMetadataRecordId(metadataId);
    }

    private MetadataRecordProperties toRecordProperties(DatasetUploadState d) {
        PublishSettings p = d.getPublishing();

        final String metadataId = UUID.randomUUID().toString();

        MetadataRecordProperties m = new MetadataRecordProperties();
        m.setMetadataId(metadataId);

        m.setName(p.getPublishedName());
        m.setTitle(p.getTitle());
        m.setAbstract(p.getAbstract());
        if (null != p.getKeywords())
            m.setKeywords(new ArrayList<>(p.getKeywords()));
        m.setCreationDate(p.getDatasetCreationDate());
        m.setLineage(null);
        m.setResourceType("series");
        m.getOnlineResources().add(wmsOnlineResource(d));
        m.getOnlineResources().add(wfsOnlineResource(d));
        m.getOnlineResources().add(downloadOnlineResource(d));

        URI uniqueResourceIdentifier = geonetwork.buildMetadataRecordURI(metadataId);
        m.setDataIdentifier(uniqueResourceIdentifier);

        m.setDatasetLanguage("eng");// REVISIT, from config?
        m.setDistributionFormat("ESRI Shapefile");
        m.setDistributionFormatVersion("1.0");
        // m.setSpatialRepresentation("vector"); REVISIT
        if (null != p.getGeographicBoundingBox()) {
            m.setGeographicBoundingBox(p.getGeographicBoundingBox().toJTS());
        }
        m.setCoordinateReferenceSystem(p.getSrs());
        final LocalDateTime now = LocalDateTime.now();
        m.setMetadataPublicationDate(now.toLocalDate());
        m.setMetadataTimestamp(now);

        if (null != p.getScale()) {
            m.setSpatialResolution(p.getScale().intValue());
        }
        m.setUseLimitation("ODBL");// REVISIT from config?
        m.setAccessConstraints("otherRestrictions");
        m.setUseConstraints("license");
        m.setDatasetResponsibleParty(datasetResponsibleParty(d));
        m.setMetadataResponsibleParty(metadataResponsibleParty(d));
        m.setMetadataLanguage("eng");// REVISIT: from config?
        m.setGraphicOverview(graphicOverview(d));
        m.setUpdateFequency("asNeeded");
        return m;
    }

    private URI graphicOverview(DatasetUploadState d) {
        // TODO Auto-generated method stub
        return null;
    }

//	"computed from LDAP / sec-* headers
//	individualName = sec-firstname + ' ' + sec-lastname
//	organisationName = sec-orgname
//	adress = postalAddress from LDAP (to be discussed)
//	email = sec-email
//	linkage = labeledURI from LDAP 
//	protocol = URL
//	name = sec-orgname
//	as in https://geobretagne.fr/geonetwork/srv/api/records/633f2882-2a90-4f98-9739-472a72d31b64/formatters/xml"
    private ContactInfo metadataResponsibleParty(DatasetUploadState d) {
        String organizationName = d.getJob().getOrganizationName();
        String username = d.getJob().getUsername();
        ContactInfo contact = new ContactInfo();
        contact.setIndividualName(username);
        contact.setName(organizationName);
        contact.setOrganizationName(organizationName);
        log.warn("TODO: implement metadataResponsibleParty");
        return contact;
    }

//	"computed from LDAP / sec-* headers 
//	individualName = sec-firstname + ' ' + sec-lastname 
//	organisationName = sec-orgname
//	adress = postalAddress from LDAP (to be discussed) 
//	email = sec-email"
    private ContactInfo datasetResponsibleParty(DatasetUploadState d) {
        String organizationName = d.getJob().getOrganizationName();
        String username = d.getJob().getUsername();

        ContactInfo contact = new ContactInfo();
        contact.setIndividualName(username);
        contact.setOrganizationName(organizationName);

        log.warn("TODO: implement datasetResponsibleParty");
        return contact;
    }

    private OnlineResource wmsOnlineResource(DatasetUploadState d) {
        String protocol = "OGC:WMS";
        String description = d.getPublishing().getTitle() + " - WMS";
        String queryString = "SERVICE=WMS&REQUEST=GetCapabilities";
        return onlineResource(d, protocol, description, queryString);
    }

    private OnlineResource wfsOnlineResource(DatasetUploadState d) {
        String protocol = "OGC:WFS";
        String description = d.getPublishing().getTitle() + " - WFS";
        String queryString = "SERVICE=WFS&REQUEST=GetCapabilities";
        return onlineResource(d, protocol, description, queryString);
    }

    private OnlineResource downloadOnlineResource(DatasetUploadState d) {
        String protocol = "OGC:WFS";
        String description = d.getPublishing().getTitle() + " - WFS";
        String layer = d.getPublishing().getPublishedName();
        String queryString = String.format("SERVICE=WFS&REQUEST=GetFeature&typename=%s&outputformat=shape-zip", layer);
        return onlineResource(d, protocol, description, queryString);
    }

    private OnlineResource onlineResource(DatasetUploadState d, String protocol, String description,
            String queryString) {
        OnlineResource r = new OnlineResource();
        PublishSettings p = d.getPublishing();
        String publishedName = p.getPublishedName();
        String workspace = p.getPublishedWorkspace();

        r.setName(publishedName);
        r.setDescription(description);
        r.setProtocol(protocol);

        URL base = publishingConfiguration.getGeoserver().getPublicUrl();
        UriComponentsBuilder builder;
        try {
            builder = UriComponentsBuilder.fromUri(base.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        String basePath = base.getPath();
        String path = String.format("%s/%s", basePath, workspace);
        builder.path(path);
        builder.query(queryString);

        URI linkage = builder.build().toUri();
        r.setLinkage(linkage);
        return r;
    }
}
