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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;
import io.github.threetenjaxb.core.LocalDateXmlAdapter;
import lombok.Data;

@Data
@XmlRootElement(name = "properties", namespace = "https://camptocamp.com/datafeeder")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetadataRecordProperties {
    String metadataId;

    String name;

    String title;

    @XmlElement(name = "abstract")
    String Abstract;

    @XmlElementWrapper(name = "keywords")
    @XmlElement(name = "keyword")
    List<String> keywords = new ArrayList<>();

    @XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
    LocalDate creationDate;

    String lineage;

    String resourceType = "dataset";

    @XmlElementWrapper(name = "onlineResources")
    @XmlElement(name = "onlineResource")
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

    double westBoundLongitude;
    double eastBoundLongitude;
    double southBoundLatitude;
    double northBoundLatitude;

    String coordinateReferenceSystem;

    @XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
    LocalDate metadataPublicationDate;

    Integer spatialResolution;

    String useLimitation = "ODBL";

    String accessConstraints = "otherRestrictions";

    String useConstraints = "license";

    ContactInfo datasetResponsibleParty;

    ContactInfo metadataResponsibleParty;

    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    LocalDateTime metadataTimestamp;

    String metadataLanguage;

    URI graphicOverview;// provided by template?

    String updateFequency = "asNeeded";

    Boolean showExtent = true;

    public static @Data class OnlineResource {
        URI linkage;
        String protocol;
        String name;
        String description;
    }

    public static @Data class ContactInfo {
        String individualName;
        String organizationName;
        Address address = new Address();
        String email;
        String linkage;
        String protocol = "URL";
        String name;
    }

    public static @Data class Address {
        String deliveryPoint;
        String city;
        String postalCode;
        String country;
    }
}
