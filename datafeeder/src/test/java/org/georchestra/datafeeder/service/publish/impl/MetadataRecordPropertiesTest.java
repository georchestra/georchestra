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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;

import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.ContactInfo;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.OnlineResource;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataRecordPropertiesTest {

    @Test
    public void testJAXB_Serialization() throws JAXBException {
        final MetadataRecordProperties mdprops = createTestProps();

        JAXBContext context = JAXBContext.newInstance(MetadataRecordProperties.class);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(mdprops, writer);
        final String serialized = writer.toString();
        log.info("Sample MetadataRecordProperties:\n{}", serialized);

        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object unmarshalled = unmarshaller.unmarshal(new StringReader(serialized));
        assertEquals(mdprops, unmarshalled);
    }

    private MetadataRecordProperties createTestProps() {
        MetadataRecordProperties p = new MetadataRecordProperties();
        assertEquals("vector", p.getSpatialRepresentation());
        assertEquals("series", p.getResourceType());
        assertEquals("asNeeded", p.getUpdateFequency());
        assertEquals("ODBL", p.getUseLimitation());
        assertEquals("otherRestrictions", p.getAccessConstraints());
        assertEquals("license", p.getUseConstraints());

        p.setMetadataId(UUID.randomUUID().toString());
        p.setAbstract("test abstract");
        p.setAccessConstraints("test constraints");
        p.setCharsetEncoding("ISO-8859-1");
        p.setCoordinateReferenceSystem("EPSG:4326");
        p.setCreationDate(LocalDate.now());
        p.setDataIdentifier(URI.create("http://test.com"));
        p.setDatasetLanguage("esp");
        p.setDatasetResponsibleParty(contactInfo());
        p.setDistributionFormat("GeoPackage");
        p.setDistributionFormatVersion("1.0");
        p.setGeographicBoundingBox(new Envelope(-180, 180, -90, 90));
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
        ContactInfo c = new ContactInfo();
        c.setAddress("test address");
        c.setEmail("test@email.com");
        c.setIndividualName("John Doe");
        c.setName("john");
        c.setLinkage("http://test.com/johndoe");
        c.setOrganizationName("Test Org");
        c.setProtocol("Dr.");
        return c;
    }

}
