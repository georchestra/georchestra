package org.georchestra.console.integration.ws.backoffice.platform;

import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.console.ws.backoffice.platform.InfosController;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class InfosControllerIT extends ConsoleIntegrationTest {

    private @Autowired InfosController infosController;

    public @Test @WithMockUser(username = "admin", roles = "SUPERUSER") void testAnalyticsExtractorappDisabled()
            throws Exception {
        // By default, analytics & extractorapp are enabled
        JSONObject ret = new JSONObject(infosController.getPlatformInfos());

        assertTrue(ret.getBoolean("analyticsEnabled") == true && ret.getBoolean("extractorappEnabled") == false);

        infosController.setAnalyticsEnabled(false);
        infosController.setExtractorappEnabled(false);

        ret = new JSONObject(infosController.getPlatformInfos());

        assertTrue(ret.getBoolean("analyticsEnabled") == false && ret.getBoolean("extractorappEnabled") == false);
    }
}
