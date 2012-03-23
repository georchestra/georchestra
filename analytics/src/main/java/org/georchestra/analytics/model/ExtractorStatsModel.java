package org.georchestra.analytics.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtractorStatsModel extends AbstractModel  {

	public ExtractorStatsModel(PostGresqlConnection pgpool) {
		super(pgpool);
	}
	
	private final String selectLayersQ = "SELECT y.ows_url, y.ows_type, y.layer_name, count(*) as count " +
			"FROM extractorapp_log l, extractorapp_layers y " +
			"where extract(month from requested_at) = ? AND extract(year from requested_at) = ? " +
			"AND l.id = y.extractorapp_log_id group by y.layer_name, y.ows_url, y.ows_type " +
			"order by @sort@ LIMIT ? OFFSET ?;";
	
	private final String selectUsersQ = "SELECT l.username as username, count(*) as count FROM extractorapp_log l, extractorapp_layers y " +
			"where extract(month from requested_at) = ? AND extract(year from requested_at) = ? " +
			"AND l.id = y.extractorapp_log_id group by l.username order by @sort@ LIMIT ? OFFSET ?;";

	public JSONObject getLayersStats(final int month, final int year, final int start, final int limit, final String sort) throws SQLException, JSONException {
		
		return getStats(month, year, start, limit, sort, selectLayersQ, new StrategyModel() {

			protected JSONArray process(ResultSet rs) throws SQLException, JSONException {	
				JSONArray jsarr = new JSONArray();
				while (rs.next()) {
					JSONObject res = new JSONObject();
					res.put("ows_type", rs.getString("ows_type"));
					res.put("ows_url", rs.getString("ows_url"));
					res.put("layer_name", rs.getString("layer_name"));
					res.put("count", rs.getInt("count"));
					jsarr.put(res);
			     }	
				return jsarr;
			}
		});
	}
	
	public JSONObject getUsersStats(final int month, final int year, final int start, final int limit, final String sort) throws SQLException, JSONException {
		
		return getStats(month, year, start, limit, sort, selectUsersQ, new StrategyModel() {

			protected JSONArray process(ResultSet rs) throws SQLException, JSONException {	
				JSONArray jsarr = new JSONArray();
				while (rs.next()) {
					JSONObject res = new JSONObject();
					res.put("username", rs.getString("username"));
					res.put("count", rs.getInt("count"));
					jsarr.put(res);
			     }	
				return jsarr;
			}
		});
	}
}
