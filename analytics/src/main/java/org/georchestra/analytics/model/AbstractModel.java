package org.georchestra.analytics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AbstractModel {

	protected PostGresqlConnection postgresqlConnection;

	private final String countQ = "SELECT count(*) from (@query@) as res;";
	
	public AbstractModel(PostGresqlConnection pgpool) {
		postgresqlConnection = pgpool;
	}
	
	/**
	 * Prepare the statement with controller attributes
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement prepareStatement(Connection con, final String query, 
			final int month, final int year, final int start, final int limit, final String sort) throws SQLException {
		
		String q = query.replace("@sort@", sort);
		PreparedStatement st = con.prepareStatement(q);
		
		st.setInt(1, month);
	    st.setInt(2, year);
	    st.setInt(3, limit);
	    st.setInt(4, start);

		return st;
	}
	
	/**
	 * Parse given client filter and add them into SQL select query. The WHERE key word
	 * must be in lower case in the request to be replaced by the new WHERE added by filters
	 * @param query base query
	 * @param filter JSON filter as String coming from client
	 * @return
	 * @throws JSONException
	 */
	protected String addFilters(final String query, final String filter) throws JSONException {
		if(filter == null || "".equals(filter)) return query;
		
		JSONArray arr = new JSONArray(filter);
		if(arr.length() <= 0 ) {
			return query;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("WHERE");
		for (int i=0;  i < arr.length() ; ++i) {
			JSONObject f = arr.getJSONObject(i);
			sb.append(" ");
			sb.append(f.getString("property"));
			sb.append(" = ");
			sb.append("'");
			sb.append(f.getString("value"));
			sb.append("'");
			sb.append(" AND");
		}
		sb.append(" ");
		
		// replace is case sensitive
		return query.replace("where", sb.toString());
	}
	
	/**
	 * Count all the results of the given query.
	 * Build the count query from the filter query, removed by LIMIT and OFFSET keywords,
	 * and included in an count query "countQ"
	 * 
	 * @return number of results
	 * @throws SQLException
	 */
	protected int getCount(Connection con, final String query,
			final int month, final int year, final int start, final int limit, final String sort) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement st = null;
		int count = 0;
		String q = query.replace("@sort@", sort);
		q = q.replace("LIMIT ? OFFSET ?;", "");
		q = countQ.replace("@query@", q);
		
		try {
			st = con.prepareStatement(q);
			st.setInt(1, month);
		    st.setInt(2, year);
		    rs = st.executeQuery();
		    
		    if(rs.next()) {
		    	count = rs.getInt(1);
		    }
		} catch(SQLException e) {
			throw e;
		} finally {
			if (st != null) st.close();		
			if (rs != null) rs.close();
		}
		
		return count;
	}
	
	/**
	 * Generic statistics data access. Get all statistics of a type, filter by date, order and
	 * samples (offset, limit). The ResultSet is parsed and all data are insert in a JSON object
	 * to return
	 * @param filter TODO
	 * 
	 * @return JSON object containing all results
	 * @throws SQLException
	 * @throws JSONException
	 */
	public JSONObject getStats(final int month, final int year, final int start, final int limit, 
			final String sort, String filter, final String query, StrategyModel strategy) throws SQLException, JSONException {
		
		JSONObject object = new JSONObject();
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement st = null;
		
		try {
			String q = addFilters(query, filter);
			con = postgresqlConnection.getConnection();
			int count = getCount(con, q, month, year, start, limit, sort);
			st = prepareStatement(con, q, month, year, start, limit, sort);
			rs = st.executeQuery();
			
			JSONArray jsarr = strategy.process(rs);
			object.put("success", true);
			object.put("results", jsarr);
			object.put("total", count);
			
			return object;
		
		} catch (SQLException e) {
			throw e;
			
		}  catch (JSONException e) {
			throw e;
			
		} finally {
			if (st != null) st.close();		
			if (rs != null) rs.close();
			
			if (con != null) {
				con.close();
			}
		}
	}
	
	protected abstract class StrategyModel {
		
		protected abstract JSONArray process(ResultSet rs) throws SQLException, JSONException;
	}
}