package org.georchestra.console.integration.ws.backoffice.users;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.console.ws.backoffice.users.UserInfoExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@SpringJUnitConfig(locations = { "classpath:/webmvc-config-test.xml" })
public class UserInfoExporterIT extends ConsoleIntegrationTest {

    private @Autowired UserInfoExporter ldifExporter;

    public @Test void test() throws Exception {
        String ldifContents = ldifExporter.exportAsLdif("testuser");
        assertNotNull(ldifContents);
        System.err.println(ldifContents);
    }

}
