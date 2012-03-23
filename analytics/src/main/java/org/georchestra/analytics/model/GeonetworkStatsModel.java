package org.georchestra.analytics.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeonetworkStatsModel extends AbstractModel  {

	
	public GeonetworkStatsModel(PostGresqlConnection pgpool) {
		super(pgpool);
	}
	
	private final String selectFilesQ = "SELECT filename, metadata_id, count(*) as count FROM geonetwork_log " +
			"where extract(month from requested_at) = ? AND extract(year from requested_at) = ? " +
			"group by metadata_id, filename order by @sort@ LIMIT ? OFFSET ?;";
	
	private final String selectUsersQ = "SELECT username, count(*) as count FROM geonetwork_log " +
			"where extract(month from requested_at) = ? AND extract(year from requested_at) = ? " +
			"group by username order by @sort@ LIMIT ? OFFSET ?;";

	public JSONObject getFilesStats(final int month, final int year, final int start, final int limit, final String sort) throws SQLException, JSONException {
		
		return getStats(month, year, start, limit, sort, selectFilesQ, new StrategyModel() {

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
	
public void test() throws Exception {
		
		Connection con = null;
		PreparedStatement st = null;
		
		String query = "INSERT INTO extractorapp_layers(" +
	            "extractorapp_log_id, projection, resolution, format, bbox_srs, " +
	            "\"left\", bottom, \"right\", top, ows_url, ows_type, layer_name)" +
	    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	    
		String jsonSpec = "{\"emails\":[\"francois.vanderbiest@camptocamp.com\"],\"globalProperties\":{\"projection\":\"EPSG:4326\",\"resolution\":0.5,\"rasterFormat\":\"geotiff\",\"vectorFormat\":\"shp\",\"bbox\":{\"srs\":\"EPSG:4326\",\"value\":[-2.2,42.6,1.9,46]}},\"layers\":[{\"projection\":\"EPSG:5476\",\"resolution\":2,\"format\":null,\"bbox\":{\"srs\":null,\"value\":[-10.2,42.6,1.9,46]},\"owsUrl\":\"http://ns383241.ovh.net:80/geoserver/wfs/WfsDispatcher?\",\"owsType\":\"WCS\",\"layerName\":\"pigma:cantons\"},{\"projection\":\"EPSG:5476\",\"resolution\":2,\"format\":null,\"bbox\":{\"srs\":\"EPSG:5000\",\"value\":[-10.2,42.6,1.9,46]},\"owsUrl\":\"http://ns383241.ovh.net:80/geoserver/wfs/WfsDispatcher?\",\"owsType\":\"WCS\",\"layerName\":\"toto\"}]}";
		JSONObject obj 	= new JSONObject(jsonSpec);
		
		JSONObject jProp = obj.getJSONObject("globalProperties");
		String projection = jProp.getString("projection");
		double resolution = jProp.getDouble("resolution");
		String rasterFormat  = jProp.getString("rasterFormat");
		String vectorFormat  = jProp.getString("vectorFormat");
		JSONObject jBbox = jProp.getJSONObject("bbox");
		String bbox_srs = jBbox.getString("srs");
		JSONArray jValue = jBbox.getJSONArray("value");
		double left = jValue.getDouble(0);
		double bottom = jValue.getDouble(1);
		double right = jValue.getDouble(2);
		double top = jValue.getDouble(3);
		
		try {
			con = postgresqlConnection.getConnection();
			con.setAutoCommit(false);
			
			JSONArray jLayers = obj.getJSONArray("layers");
			for (int i =0;  i < jLayers.length() ; ++i) {
				JSONObject jLayer = jLayers.getJSONObject(i);
				String lProjection = jLayer.getString("projection").equals("null") ? projection : jLayer.getString("projection");
				double lResolution = jLayer.getString("resolution").equals("null") ? resolution : jLayer.getDouble("resolution");
				String lFormat  = jLayer.getString("format");
				String owsType  = jLayer.getString("owsType");
				String owsUrl  = jLayer.getString("owsUrl");
				String layerName  = jLayer.getString("layerName");
				
				double lLeft=left, lBottom=bottom, lRight=right, lTop=top;
				String lSrs=bbox_srs;
				
				if(!jLayer.getString("bbox").equals("null")) {
					JSONObject lBbox = jLayer.getJSONObject("bbox");
					lSrs = lBbox.getString("srs").equals("null") ? lSrs : lBbox.getString("srs");
					if(!lBbox.getString("value").equals("null")) {
						JSONArray lValue = lBbox.getJSONArray("value");
						lLeft = lValue.getDouble(0);
						lBottom = lValue.getDouble(1);
						lRight = lValue.getDouble(2);
						lTop = lValue.getDouble(3);
					}
				}
				
				if("WFS".equals(owsType)) {
					lFormat = lFormat.equals("null") ? vectorFormat : lFormat;
				} else if ("WCS".equals(owsType)) {
					lFormat = lFormat.equals("null") ? rasterFormat : lFormat;
				}
				
				st = con.prepareStatement(query);
				st.setInt(1, 684);
				st.setString(2, lProjection);
				st.setDouble(3, lResolution);
				st.setString(4, lFormat);
				st.setString(5, lSrs);
				st.setDouble(6, lLeft);
				st.setDouble(7, lBottom);
				st.setDouble(8, lRight);
				st.setDouble(9, lTop);
				st.setString(10, owsUrl);
				st.setString(11, owsType);
				st.setString(12, layerName);
				st.executeUpdate();
				
				con.commit();
			}
		} catch(Exception e) {
			throw e;
		} finally {
			if (st != null) st.close();
		}
	}
}
