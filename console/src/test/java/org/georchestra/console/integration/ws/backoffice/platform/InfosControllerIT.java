package org.georchestra.console.integration.ws.backoffice.platform;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.console.ws.backoffice.platform.InfosController;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@SpringJUnitConfig(locations = { "classpath:/webmvc-config-test.xml" })
public class InfosControllerIT extends ConsoleIntegrationTest {

    private @Autowired InfosController infosController;

    public @Test @WithMockUser(username = "admin", roles = "SUPERUSER") void testAnalyticsExtractorappDisabled()
            throws Exception {
        // By default, analytics & extractorapp are enabled
        JSONObject ret = new JSONObject(infosController.getPlatformInfos());

        assertTrue(ret.getBoolean("analyticsEnabled") == true && ret.getBoolean("extractorappEnabled") == false
                && ret.getBoolean("useLegacyHeader") == false);

        assertTrue(ret.getString("headerUrl").equals("/header/") && ret.getString("headerHeight").equals("90") && ret
                .getString("headerScript").equals("https://cdn.jsdelivr.net/gh/georchestra/header@dist/header.js"));

        infosController.setAnalyticsEnabled(false);
        infosController.setExtractorappEnabled(false);

        ret = new JSONObject(infosController.getPlatformInfos());

        assertTrue(ret.getBoolean("analyticsEnabled") == false && ret.getBoolean("extractorappEnabled") == false);
    }
}
