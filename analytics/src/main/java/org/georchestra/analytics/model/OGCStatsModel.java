package org.georchestra.analytics.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OGCStatsModel extends AbstractModel  {

	private final String selectLayersQ = "SELECT "
	        + "    service"
	        + "    , layer"
	        + "    , request"
	        + "    , COUNT(*) AS count "
	        + "FROM "
	        + "    ogcstatistics.ogc_services_log "
	        + "WHERE "
	        + "    date >= ?::timestamp "
	        + "AND "
	        + "    date < ?::timestamp "
	        + "GROUP BY "
	        + "    layer"
	        + "    , service"
	        + "    , request "
	        + "ORDER BY "
	        + "    @sort@ "
	        + "LIMIT ? OFFSET ?;";

	private final String selectUsersQ = "SELECT "
	        + "    user_name "
	        + "    , COUNT(*) AS count "
	        + "FROM "
	        + "    ogcstatistics.ogc_services_log "
	        +"WHERE "
	        + "    date >= ?::timestamp "
	        + "AND "
	        + "    date < ?::timestamp "
	        + "GROUP BY "
	        + "    user_name "
	        + "ORDER BY "
	        + "    @sort@ "
	        + "LIMIT ? OFFSET ?;";

	private final String selectGroupsQ = "SELECT "
	        + "    org"
	        + "    , COUNT(*) AS count "
	        + "FROM "
	        + "    ogcstatistics.ogc_services_log "
	        + "WHERE "
	        + "    date >= ?::timestamp "
	        + "AND "
	        + "    date < ?::timestamp "
	        + "GROUP BY "
	        + "    org "
	        + "ORDER BY "
	        + "    @sort@ "
	        + "LIMIT ? OFFSET ?;";

	public JSONObject getLayersStats(final int month, final int year, final int start, final int limit, final String sort, final String filter) throws SQLException, JSONException {

		return getStats(month, year, start, limit, sort, filter, selectLayersQ, new StrategyModel() {

			protected JSONArray process(ResultSet rs) throws SQLException, JSONException {
				JSONArray jsarr = new JSONArray();
				while (rs.next()) {
					JSONObject res = new JSONObject();
					res.put("service", rs.getString("service"));
					res.put("layer", rs.getString("layer"));
					res.put("request", rs.getString("request"));
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
					res.put("user_name", rs.getString("user_name"));
					res.put("count", rs.getInt("count"));
					jsarr.put(res);
			     }
				return jsarr;
			}
		});
	}

	public JSONObject getGroupsStats(final int month, final int year, final int start, final int limit, final String sort, final String filter) throws SQLException, JSONException {

		return getStats(month, year, start, limit, sort, filter, selectGroupsQ, new StrategyModel() {
			protected JSONArray process(ResultSet rs) throws SQLException, JSONException {
				JSONArray jsarr = new JSONArray();
				while (rs.next()) {
					JSONObject res = new JSONObject();
					res.put("org", rs.getString("org"));
					res.put("count", rs.getInt("count"));
					jsarr.put(res);
			     }
				return jsarr;
			}
		});
	}
}
