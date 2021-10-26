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
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.PublishingConfiguration;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.Envelope;
import org.georchestra.datafeeder.model.Organization;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkResponse;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.Address;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.ContactInfo;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.OnlineResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    public Optional<URI> buildMetadataRecordURL(@NonNull String recordId, @NonNull MediaType contentType) {
        URI uri = null;
        if (contentType.isCompatibleWith(MediaType.TEXT_XML)) {
            uri = geonetwork.buildMetadataRecordXmlURI(recordId);
        } else if (contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
            uri = geonetwork.buildMetadataRecordHtmlURI(recordId);
        }
        return Optional.ofNullable(uri);
    }

    @Override
    public void publish(@NonNull DatasetUploadState dataset, @NonNull UserInfo user) {
        Objects.requireNonNull(dataset.getPublishing());

        MetadataRecordProperties mdProps = toRecordProperties(dataset, user);
        String metadataId = mdProps.getMetadataId();

        Supplier<String> record = templateMapper.apply(mdProps);
        Organization organization = user.getOrganization();
        // The metadata is inserted into the group which already exists due to
        // GeoNetwork's LDAP sync (which currently garantees that a geonetwork group
        // exists for the publishing organization)
        String mdGroupId = organization.getShortName();
        GeoNetworkResponse response = geonetwork.publish(metadataId, record, mdGroupId);
        dataset.getPublishing().setMetadataRecordId(metadataId);
    }

    private MetadataRecordProperties toRecordProperties(@NonNull DatasetUploadState d, @NonNull UserInfo user) {
        PublishSettings publishing = d.getPublishing();

        final String metadataId = UUID.randomUUID().toString();

        MetadataRecordProperties m = new MetadataRecordProperties();
        m.setMetadataId(metadataId);

        m.setName(publishing.getPublishedName());
        m.setTitle(publishing.getTitle());
        m.setAbstract(publishing.getAbstract());
        if (null != publishing.getKeywords())
            m.setKeywords(new ArrayList<>(publishing.getKeywords()));
        m.setCreationDate(publishing.getDatasetCreationDate());
        m.setLineage(publishing.getDatasetCreationProcessDescription());
        m.setResourceType("series");
        m.getOnlineResources().add(wmsOnlineResource(d));
        m.getOnlineResources().add(wfsOnlineResource(d));
        m.getOnlineResources().add(downloadOnlineResource(d));

        URI uniqueResourceIdentifier = geonetwork.buildMetadataRecordIdentifier(metadataId);
        m.setDataIdentifier(uniqueResourceIdentifier);

        m.setDatasetLanguage("eng");// REVISIT, from config?
        m.setDistributionFormat("ESRI Shapefile");
        m.setDistributionFormatVersion("1.0");
        // m.setSpatialRepresentation("vector"); REVISIT
        Envelope bb = publishing.getGeographicBoundingBox();
        if (null != bb) {
            m.setWestBoundLongitude(bb.getMinx());
            m.setEastBoundLongitude(bb.getMaxx());
            m.setSouthBoundLatitude(bb.getMiny());
            m.setNorthBoundLatitude(bb.getMaxy());
        }
        m.setCoordinateReferenceSystem(publishing.getSrs());
        final LocalDateTime now = LocalDateTime.now();
        m.setMetadataPublicationDate(now.toLocalDate());
        m.setMetadataTimestamp(now);

        if (null != publishing.getScale()) {
            m.setSpatialResolution(publishing.getScale().intValue());
        }
        m.setUseLimitation("ODBL");// REVISIT from config?
        m.setAccessConstraints("otherRestrictions");
        m.setCharsetEncoding(toCodeListCharset(publishing.getEncoding()));
        m.setUseConstraints("license");
        m.setDatasetResponsibleParty(datasetResponsibleParty(user));
        m.setMetadataResponsibleParty(metadataResponsibleParty(user));
        m.setMetadataLanguage("eng");// REVISIT: from config?
        m.setGraphicOverview(graphicOverview(d));
        m.setUpdateFequency("asNeeded");
        return m;
    }

    // see
    // https://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_CharacterSetCode
    private String toCodeListCharset(String encoding) {
        if (encoding == null)
            return "utf8";
        try {
            Charset.forName(encoding);
        } catch (Exception e) {
            log.info("Invalid encoding, defaulting to utf8", e);
            return "utf8";
        }
        switch (encoding.toUpperCase()) {
        case "UCS-2":
            return "ucs2";
        case "UCS-4":
            return "ucs4";
        case "US-ASCII":
            return "usAscii";
        case "UTF-7":
            return "utf7";
        case "UTF-8":
            return "utf8";
        case "UTF-16":
            return "utf16";

        case "ISO-8859-1":
            return "8859part1";
        case "ISO-8859-2":
            return "8859part2";
        case "ISO-8859-3":
            return "8859part3";
        case "ISO-8859-4":
            return "8859part4";
        case "ISO-8859-5":
            return "8859part5";
        case "ISO-8859-6":
            return "8859part6";
        case "ISO-8859-7":
            return "8859part7";
        case "ISO-8859-8":
            return "8859part8";
        case "ISO-8859-9":
            return "8859part9";
        case "ISO-8859-10":
            return "8859part10";
        case "ISO-8859-11":
            return "8859part11";
        case "ISO-8859-13":
            return "8859part13";
        case "ISO-8859-14":
            return "8859part14";
        case "ISO-8859-15":
            return "8859part15";
        case "ISO-8859-16":
            return "8859part116";

        default:
            log.info("Unmapped Java to ISO19139 MD_CharacterSetCode {}, returning as-is", encoding);
            return encoding;
        }
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
    private ContactInfo metadataResponsibleParty(@NonNull UserInfo user) {
        Organization org = user.getOrganization();

        String individualName = individualName(user);
        ContactInfo contact = new ContactInfo();
        contact.setIndividualName(individualName);
        contact.setEmail(user.getEmail());
        contact.setName(org.getShortName());
        contact.setOrganizationName(org.getName());
        contact.setProtocol("URL");
        contact.setLinkage(org.getLinkage());

        Address address = new Address();// ?? postalAddress from LDAP (to be discussed)
        contact.setAddress(address);
        address.setDeliveryPoint(user.getPostalAddress());
        return contact;
    }

    private String individualName(UserInfo user) {
        String individualName = user.getFirstName();
        if (user.getLastName() != null) {
            individualName = (individualName == null ? "" : individualName) + " " + user.getLastName();
        }
        return individualName;
    }

//	"computed from LDAP / sec-* headers 
//	individualName = sec-firstname + ' ' + sec-lastname 
//	organisationName = sec-orgname
//	adress = postalAddress from LDAP (to be discussed) 
//	email = sec-email"
    private ContactInfo datasetResponsibleParty(@NonNull UserInfo user) {
        Organization org = user.getOrganization();

        String individualName = individualName(user);
        ContactInfo contact = new ContactInfo();
        contact.setIndividualName(individualName);
        contact.setEmail(user.getEmail());
        contact.setName(org.getShortName());
        contact.setOrganizationName(org.getName());
        contact.setProtocol("URL");
        contact.setLinkage(org.getLinkage());// ?? labeledURI from LDAP

        Address address = new Address();// ?? postalAddress from LDAP (to be discussed)
        contact.setAddress(address);
        address.setDeliveryPoint(org.getPostalAddress());
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
        String protocol = "WWW:DOWNLOAD-1.0-http--download";
        String description = d.getPublishing().getTitle() + " - WWW";
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
        String path = String.format("/%s/ows", workspace);
        builder.path(path);// appends path
        builder.query(queryString);

        URI linkage = builder.build().toUri();
        r.setLinkage(linkage);
        return r;
    }
}
