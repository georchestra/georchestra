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
