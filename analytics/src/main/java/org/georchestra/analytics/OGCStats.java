/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.analytics;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.analytics.model.OGCStatsModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ogc layers controller
 *
 * @author: fgravin
 */

@Controller
public class OGCStats extends AbstractApplication {

    protected OGCStats(OGCStatsModel model) {
        this.model = model;
    }

    private OGCStatsModel model;

    private final String csvLayers = "OgcLayers";
    private final String csvUsers = "OgcUsers";
    private final String csvOrgs = "OgcOrgs";

    @GetMapping("/ogc/layers")
    public void getOGCLayersStats(HttpServletRequest request, HttpServletResponse response) throws Exception {

        getStats(request, response, new StrategyController() {
            protected JSONObject process() throws SQLException, JSONException {
                return model.getLayersStats(month, year, start, limit, sort, filter);
            }
        });
    }

    @GetMapping("/ogc/users")
    public void getOGCUsersStats(HttpServletRequest request, HttpServletResponse response) throws Exception {

        getStats(request, response, new StrategyController() {
            protected JSONObject process() throws SQLException, JSONException {
                return model.getUsersStats(month, year, start, limit, sort, filter);
            }
        });
    }

    @GetMapping("/ogc/orgs")
    public void getOGCOrgsStats(HttpServletRequest request, HttpServletResponse response) throws Exception {

        getStats(request, response, new StrategyController() {
            protected JSONObject process() throws SQLException, JSONException {
                return model.getOrgsStats(month, year, start, limit, sort, filter);
            }
        });
    }

    @GetMapping("/export/ogclayers")
    public void exportLayers(HttpServletRequest request, HttpServletResponse response) throws Exception {

        exportCSV(request, response, csvLayers, new StrategyController() {
            protected JSONObject process() throws SQLException, JSONException {
                return model.getLayersStats(month, year, 0, Integer.MAX_VALUE, sort, filter);
            }
        });
    }

    @GetMapping("/export/ogcusers")
    public void exportUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {

        exportCSV(request, response, csvUsers, new StrategyController() {
            protected JSONObject process() throws SQLException, JSONException {
                return model.getUsersStats(month, year, 0, Integer.MAX_VALUE, sort, filter);
            }
        });
    }

    @GetMapping("/export/ogcorgs")
    public void exportOrgs(HttpServletRequest request, HttpServletResponse response) throws Exception {

        exportCSV(request, response, csvOrgs, new StrategyController() {
            protected JSONObject process() throws SQLException, JSONException {
                return model.getOrgsStats(month, year, 0, Integer.MAX_VALUE, sort, filter);
            }
        });
    }
}
