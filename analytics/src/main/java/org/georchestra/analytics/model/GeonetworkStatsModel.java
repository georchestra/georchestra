/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.analytics.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeonetworkStatsModel extends AbstractModel  {

	private final String selectFilesQ = "SELECT "
	        + "    filename "
	        + "    , metadata_id "
	        + "    , COUNT(*) AS count "
	        + "FROM "
	        + "    downloadform.geonetwork_log "
	        + "WHERE "
	        + "    requested_at >= ?::timestamp "
	        + "AND "
	        + "    requested_at < ?::timestamp "
	        + "GROUP BY "
	        + "    metadata_id "
	        + "    , filename "
	        + "ORDER BY "
	        + "    @sort@ "
	        + "LIMIT ? OFFSET ?;";

	private final String selectUsersQ = "SELECT "
	        + "    username "
	        + "    , COUNT(*) AS count "
	        + "FROM "
	        + "    downloadform.geonetwork_log "
	        + "WHERE "
	        + "    requested_at >= ?::timestamp "
	        + "AND "
	        + "    requested_at < ?::timestamp "
			+ "GROUP BY "
			+ "    username "
			+ "ORDER BY "
			+ "    @sort@ "
			+ "LIMIT ? OFFSET ?;";

	private final String selectGroupsQ = "SELECT "
	        + "    company"
	        + "    , COUNT(*) AS count "
	        + "FROM "
	        + "    downloadform.geonetwork_log "
			+ "WHERE "
			+ "    requested_at >= ?::timestamp "
			+ "AND "
			+ "    requested_at < ?::timestamp "
			+ "GROUP BY "
			+ "    company "
			+ "ORDER BY "
			+ "    @sort@ "
			+ "LIMIT ? OFFSET ?;";

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

	public JSONObject getGroupsStats(final int month, final int year, final int start, final int limit, final String sort, final String filter) throws SQLException, JSONException {

		return getStats(month, year, start, limit, sort, filter, selectGroupsQ, new StrategyModel() {

			protected JSONArray process(ResultSet rs) throws SQLException, JSONException {
				JSONArray jsarr = new JSONArray();
				while (rs.next()) {
					JSONObject res = new JSONObject();
					res.put("company", rs.getString("company"));
					res.put("count", rs.getInt("count"));
					jsarr.put(res);
			     }
				return jsarr;
			}
		});
	}
}
