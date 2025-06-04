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

package org.georchestra.datafeeder.batch.service;

import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.geoserver.restconfig.client.ServerException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublishingBatchServiceTest {

    public @Test void testFormatGsClientServerExceptionMessage() {

        PublishingBatchService toTest = new PublishingBatchService();

        String errorMsg = "Status 500:\n"
                + "Request: POST https://test/geoserver/rest/workspaces/c2c/datastores/datafeeder_c2c/featuretypes HTTP/1.1\n"
                + "Accept: application/json\n" + "Authorization: Basic YWFhOmJiYg==\n" + "Content-Length: 468\n"
                + "Content-Type: application/json";

        assertFalse(toTest.formatGsClientServerExceptionMessage(errorMsg).contains("Authorization"));
    }

    public @Test void testFormatGsClientServerExceptionMessageFromGsExc() {

        PublishingBatchService toTest = new PublishingBatchService();
        ServerException exc = new ServerException(502, "unable to proceed", Map.of(),
                "POST https://test/geoserver/rest/workspaces/c2c/datastores/datafeeder_c2c/featuretypes HTTP/1.1\n"
                        + "Accept: application/json\n" + "sec-username: SUPERUSER\n"
                        + "sec-roles: ROLE_ADMINISTRATOR\n");

        assertFalse(toTest.formatGsClientServerExceptionMessage(exc.getMessage()).contains("Accept"));
    }

    public @Test void testFormatGsClientServerExceptionMessageOOB() {

        PublishingBatchService toTest = new PublishingBatchService();
        String errorMsg = "Status 500: Request: POST https://test/geoserver/rest/workspaces/c2c/datastores/datafeeder_c2c/featuretypes HTTP/1.1";

        String ret = toTest.formatGsClientServerExceptionMessage(errorMsg);
        assertTrue(ret.equals(errorMsg));
    }

}
