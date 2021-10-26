package org.georchestra.console.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.console.mailservice.EmailFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.io.Files;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/mail-factory-test.xml" })
public class EmailFactoryIT {

    @Autowired
    private EmailFactory toTest;

    @Autowired
    private GeorchestraConfiguration georConfig;

    @Autowired
    private WebApplicationContext wac;

    @Test
    public void testObjectInstantiation() {
        assertTrue(toTest != null);
    }

    @Test
    public void testNoGeorchestraDatadir() throws MessagingException {
        toTest.sendAccountWasCreatedEmail(wac.getServletContext(), "root@localhost", "recipient@localhost", "pmauduit",
                false);
    }

    private File createGeorchestraDatadir() throws IOException {
        File georDatadir = Files.createTempDir();
        File consoleDir = Paths.get(georDatadir.getAbsolutePath(), "console", "templates").toFile();
        FileUtils.forceMkdir(consoleDir);
        String path = wac.getServletContext().getRealPath("/WEB-INF/templates");
        FileUtils.copyDirectory(new File(path), consoleDir);
        return georDatadir;
    }

    @Test
    public void testWithGeorchestraDatadir() throws MessagingException, IOException {
        File georDataDir = createGeorchestraDatadir();
        System.setProperty("georchestra.datadir", georDataDir.getAbsolutePath());
        GeorchestraConfiguration withGeorDatadir = new GeorchestraConfiguration("console");
        toTest.setGeorConfig(withGeorDatadir);

        toTest.sendAccountWasCreatedEmail(wac.getServletContext(), "root@localhost", "recipient@localhost", "pmauduit",
                false);

        System.clearProperty("georchestra.datadir");
        toTest.setGeorConfig(georConfig);
    }

}
