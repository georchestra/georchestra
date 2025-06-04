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

package org.georchestra.datafeeder.autoconf;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class GeorchestraEmailFactoryTest {

    private static final String TEMPLATE = "to: ${user.email}\n"//
            + "cc: ${administratorEmail}\n"//
            + "bcc:\n"//
            + "sender: ${administratorEmail}\n"//
            + "from: Georchestra Importer Application\n"//
            + "subject: Your ${dataset.name} dataset has been published\n"//
            + "body:\n"//
            + "\n" + "Dear ${user.name}, \n"//
            + "\n"//
            + "We're pleased to inform you that your ${dataset.name} dataset\n"//
            + "submitted on ${job.createdAt} has been correctly processed and published.\n"//
            + "\n"//
            + "Browse to the following URL to access a map preview of the published layer:\n"//
            + "\n"//
            + "${publicUrl}/geoserver/${publish.workspace}/wms/reflect?format=application/openlayers&LAYERS=${publish.layerName}\n"//
            + "\n"//
            + "And to this URL to access its published metadata:\n"//
            + "\n"//
            + "${publicUrl}/geonetwork/srv/eng/catalog.search#/metadata/${metadata.id}\n"//
            + "";

    @Test
    public void testExtractVariableNames() {
        Set<String> expected = new TreeSet<>(Arrays.asList("${user.email}", "${administratorEmail}", "${dataset.name}",
                "${user.name}", "${job.createdAt}", "${publicUrl}", "${publish.workspace}", "${publish.layerName}",
                "${metadata.id}"));

        Set<String> actual = GeorchestraEmailFactory.extractVariableNames(TEMPLATE);
        assertEquals(expected, actual);
    }

}
