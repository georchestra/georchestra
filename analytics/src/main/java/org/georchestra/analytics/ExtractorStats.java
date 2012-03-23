package org.georchestra.analytics;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.analytics.model.ExtractorStatsModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Extractor controller
 * 
 * @author: fgravin
 */

@Controller
public class ExtractorStats extends AbstractApplication {

	protected ExtractorStats(ExtractorStatsModel model) {
		this.model = model;
	}
	private ExtractorStatsModel model;
	
	private final String csvLayers= "extractorlayers";
	private final String csvUsers= "extractorusers";
	
	@RequestMapping(method = RequestMethod.GET, value = "/extractor/layers")
	public void getLayersStats(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		getStats(request, response, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getLayersStats(month, year, start, limit, sort);
			}
		});
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/extractor/users")
	public void getUsersStats(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		getStats(request, response, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getUsersStats(month, year, start, limit, sort);
			}
		});
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/export/extractorlayers")
	public void exportLayers(HttpServletRequest request, HttpServletResponse response) throws Exception {

		exportCSV(request, response, csvLayers, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getLayersStats(month, year, 0, Integer.MAX_VALUE, sort);
			}
		});	
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/export/extractorusers")
	public void exportUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		exportCSV(request, response, csvUsers, new StrategyController(){
			protected JSONObject process() throws SQLException, JSONException {
				return model.getUsersStats(month, year, 0, Integer.MAX_VALUE, sort);
			}
		});	
	}
}