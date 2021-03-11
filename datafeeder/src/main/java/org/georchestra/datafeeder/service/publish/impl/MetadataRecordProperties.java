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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;

import lombok.Data;

@Data
public class MetadataRecordProperties {
    String metadataId;
    String name;
    String title;
    String Abstract;
    List<String> keywords = new ArrayList<>();
    LocalDate creationDate;
    String lineage;
    String resourceType = "series";
    List<OnlineResource> onlineResources = new ArrayList<>();
    URI dataIdentifier;
    String datasetLanguage = "eng";
    String distributionFormat;
    String distributionFormatVersion;
    String charsetEncoding = "UTF-8";

    // provided by metadata template?
    String spatialRepresentation = "vector";

    // TBD
    // String topicCategory;
    Envelope geographicBoundingBox;
    String coordinateReferenceSystem;
    LocalDate metadataPublicationDate;
    Integer spatialResolution;
    String useLimitation = "ODBL";
    String accessConstraints = "otherRestrictions";
    String useConstraints = "license";
    ContactInfo datasetResponsibleParty;
    ContactInfo metadataResponsibleParty;
    LocalDateTime metadataTimestamp;
    String metadataLanguage;
    URI graphicOverview;// provided by template?
    String updateFequency = "asNeeded";

    public static @Data class OnlineResource {
        URI linkage;
        String protocol;
        String name;
        String description;
    }

    public static @Data class ContactInfo {
        String individualName;
        String organizationName;
        String address;
        String email;
        String linkage;
        String protocol = "URL";
        String name;
    }
}
