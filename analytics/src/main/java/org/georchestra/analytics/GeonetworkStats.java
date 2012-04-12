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
}