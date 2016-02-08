/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.analytics;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.analytics.model.GeonetworkStatsModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Geonetwork controller
 * 
 * @author: fgravin
 */

@Controller
public class GeonetworkStats extends AbstractApplication {

	protected GeonetworkStats(GeonetworkStatsModel model) {
		this.model = model;
	}
	
	private GeonetworkStatsModel model;
	
	private final String csvFiles= "GeonetworkFiles";
	private final String csvUsers= "GeonetworkUsers";
	private final String csvGroups= "GeonetworkGroups";
	
	@RequestMapping(method = RequestMethod.GET, value = "/geonetwork/files")
	public void getOGCLayersStats(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		getStats(request, response, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getFilesStats(month, year, start, limit, sort, filter);
			}
		});
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/geonetwork/users")
	public void getOGCUsersStats(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		getStats(request, response, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getUsersStats(month, year, start, limit, sort, filter);
			}
		});	
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/geonetwork/groups")
	public void getOGCGroupsStats(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		getStats(request, response, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getGroupsStats(month, year, start, limit, sort, filter);
			}
		});	
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/export/geonetworkfiles")
	public void exportLayers(HttpServletRequest request, HttpServletResponse response) throws Exception {

		exportCSV(request, response, csvFiles, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getFilesStats(month, year, 0, Integer.MAX_VALUE, sort, filter);
			}
		});	
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/export/geonetworkusers")
	public void exportUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		exportCSV(request, response, csvUsers, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getUsersStats(month, year, 0, Integer.MAX_VALUE, sort, filter);
			}
		});	
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/export/geonetworkgroups")
	public void exportGroups(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		exportCSV(request, response, csvGroups, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getGroupsStats(month, year, 0, Integer.MAX_VALUE, sort, filter);
			}
		});	
	}
}