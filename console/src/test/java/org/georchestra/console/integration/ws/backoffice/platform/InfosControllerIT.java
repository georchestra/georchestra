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
