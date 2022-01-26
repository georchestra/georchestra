/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.platform;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class InfosController {
    /**
     * Bring general information about georchestra or console.
     */
    private static final String BASE_MAPPING = "/private";

    @Value("${saslEnabled:false}")
    private boolean saslEnabled;

    @Value("${saslServer:#{null}}")
    private String saslServer;

    @GetMapping(value = BASE_MAPPING + "/platform/infos", produces = "application/json; charset=utf-8")
    @PreAuthorize(value = "hasRole('SUPERUSER')")
    @ResponseBody
    public String getPlatformInfos() {
        JSONObject ret = new JSONObject();
        ret.put("saslEnabled", saslEnabled);
        ret.put("saslServer", saslServer);
        return ret.toString();
    }
}
