package org.georchestra.analytics.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeonetworkStatsModel extends AbstractModel  {

	
	public GeonetworkStatsModel(PostGresqlConnection pgpool) {
		super(pgpool);
	}
	
	private final String selectFilesQ = "SELECT filename, metadata_id, count(*) as count FROM download.geonetwork_log " +
			"where extract(month from requested_at) = ? AND extract(year from requested_at) = ? " +
			"group by metadata_id, filename order by @sort@ LIMIT ? OFFSET ?;";
	
	private final String selectUsersQ = "SELECT username, count(*) as count FROM download.geonetwork_log " +
			"where extract(month from requested_at) = ? AND extract(year from requested_at) = ? " +
			"group by username order by @sort@ LIMIT ? OFFSET ?;";

	public JSONObject getFilesStats(final int month, final int year, final int start, final int limit, final String sort, final String filter) throws SQLException, JSONException {
		
		return getStats(month, year, start, limit, sort, filter, selectFilesQ, new StrategyModel() {

			protected JSONArray process(ResultSet rs) throws SQLException, JSONException {	
				JSONArray jsarr = new JSONArray();
				while (rs.next()) {
					JSONObject res = new JSONObject();
					res.put("filename", rs.getString("filename"));
					res.put("metadata_id", rs.getString("metadata_id"));
					res.put("count", rs.getInt("count"));
					jsarr.put(res);
			     }	
				return jsarr;
			}
		});
	}
	
	public JSONObject getUsersStats(final int month, final int year, final int start, final int limit, final String sort, final String filter) throws SQLException, JSONException {
		
		return getStats(month, year, start, limit, sort, filter, selectUsersQ, new StrategyModel() {

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
