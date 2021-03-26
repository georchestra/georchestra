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

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.Address;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.ContactInfo;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.OnlineResource;

public class MetadataPropertiesTestSupport {

    public MetadataRecordProperties createTestProps() {
        MetadataRecordProperties p = new MetadataRecordProperties();
        assertEquals("vector", p.getSpatialRepresentation());
        assertEquals("series", p.getResourceType());
        assertEquals("asNeeded", p.getUpdateFequency());
        assertEquals("ODBL", p.getUseLimitation());
        assertEquals("otherRestrictions", p.getAccessConstraints());
        assertEquals("license", p.getUseConstraints());

        p.setMetadataId(UUID.randomUUID().toString());
        p.setTitle("test title");
        p.setAbstract("test abstract");
        p.setAccessConstraints("test constraints");
        p.setCharsetEncoding("8859part1");
        p.setCoordinateReferenceSystem("EPSG:4326");
        p.setCreationDate(LocalDate.now());
        p.setDataIdentifier(URI.create("http://test.com"));
        p.setDatasetLanguage("esp");
        p.setDatasetResponsibleParty(contactInfo());
        p.setDistributionFormat("GeoPackage");
        p.setDistributionFormatVersion("1.0");
        p.setWestBoundLongitude(-180);
        p.setEastBoundLongitude(180);
        p.setSouthBoundLatitude(-90);
        p.setNorthBoundLatitude(90);
        p.setGraphicOverview(URI.create("http://test.com/overview"));
        p.setKeywords(Arrays.asList("k1", "k2", "k 3"));
        p.setLineage("test lineage");
        p.setMetadataLanguage("eng");
        p.setMetadataPublicationDate(LocalDate.now());
        p.setMetadataResponsibleParty(contactInfo());
        p.setMetadataTimestamp(LocalDateTime.now());
        p.setName("test name");
        p.setOnlineResources(Arrays.asList(onlineResource("1"), onlineResource("2")));
        p.setSpatialResolution(500_000);
        return p;
    }

    private OnlineResource onlineResource(String name) {
        OnlineResource o = new OnlineResource();
        o.setDescription("test description " + name);
        o.setLinkage(URI.create("http://test.com/onlineresource/" + name));
        o.setName(name);
        o.setProtocol("http");
        return o;
    }

    private ContactInfo contactInfo() {
    	Address address = new Address();
    	address.setCity("Paris");
    	address.setDeliveryPoint("18 Rue du Test");
    	address.setPostalCode("2000");
    	address.setCountry("France");
    	
        ContactInfo c = new ContactInfo();
        c.setAddress(address);
        c.setEmail("test@email.com");
        c.setIndividualName("John Doe");
        c.setName("john");
        c.setProtocol("URL");
        c.setLinkage("http://test.com/johndoe");
        c.setOrganizationName("Test Org");
        return c;
    }

}
