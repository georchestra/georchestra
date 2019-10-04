package org.georchestra.console.integration.ws.backoffice.users;

import static org.junit.Assert.assertNotNull;

import org.georchestra.console.ws.backoffice.users.UserInfoExporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class UserInfoExporterIT {

    private @Autowired UserInfoExporter ldifExporter;

    public @Test void test() throws Exception {
        String ldifContents = ldifExporter.exportAsLdif("testuser");
        assertNotNull(ldifContents);
        System.err.println(ldifContents);
    }

}
