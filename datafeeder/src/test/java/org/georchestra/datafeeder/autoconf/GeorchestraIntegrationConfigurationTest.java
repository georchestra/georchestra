package org.georchestra.datafeeder.autoconf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.MultipartConfig;
import org.georchestra.datafeeder.test.BaseTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test suite for {@link GeorchestraIntegrationConfiguration}, which shall be
 * enabled through the {@code georchestra} spring profile
 */
@SpringBootTest(classes = BaseTestConfig.class)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class GeorchestraIntegrationConfigurationTest {

    private @Autowired ApplicationContext context;

    public @Test void testDefaultDotPropertiesFromGeorchestraDatadir() {
        Environment env = context.getEnvironment();
        assertEquals("is default.properties loaded from src/test/resources/datadir?", "https",
                env.getProperty("scheme"));
        assertEquals("is default.properties loaded from src/test/resources/datadir?", "georchestra.test.org",
                env.getProperty("domainName"));
    }

    public @Test void testDataFeederPropertiesFromGeorchestraDatadir() {
        DataFeederConfigurationProperties props = context.getBean(DataFeederConfigurationProperties.class);
        MultipartConfig fileUpload = props.getFileUpload();
        assertNotNull(fileUpload);

        long maxFileSize = fileUpload.getMaxFileSize();

        assertEquals("is default.properties loaded from src/test/resources/datadir/datafeeder?", //
                5, maxFileSize);
    }
}
