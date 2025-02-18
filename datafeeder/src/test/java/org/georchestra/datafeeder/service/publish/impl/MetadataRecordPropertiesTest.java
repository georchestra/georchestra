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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataRecordPropertiesTest {

    @Test
    public void testJAXB_Serialization() throws JAXBException {
        final MetadataRecordProperties mdprops = new MetadataPropertiesTestSupport().createTestProps();

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
}
