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

package org.georchestra.dlform;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * ExtractorApp controller: this controller manages extraction requests
 * issued from the geOrchestra extractorapp application.
 *
 * author: pmauduit
 */
@Controller
@RequestMapping("/extractorapp")
public class ExtractorApp extends AbstractApplication {

    private final Log logger = LogFactory.getLog(getClass());

    private static String INSERT_DOWNLOAD_QUERY = "INSERT INTO downloadform.extractorapp_log (username, sessionid, first_name, second_name, " +
            "company, email, phone, comment, json_spec) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private final String insertLayersQuery = "INSERT INTO downloadform.extractorapp_layers(" +
            "extractorapp_log_id, projection, resolution, format, bbox_srs, " +
            "\"left\", bottom, \"right\", top, ows_url, ows_type, layer_name)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	public ExtractorApp(DataSource ds, boolean activated) {
		super(ds, activated, ExtractorApp.INSERT_DOWNLOAD_QUERY);
	}


	protected boolean isInvalid(DownloadQuery q) {
		return q.isInvalid() || (q.getJsonSpec() == null);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        OutputStream out = response.getOutputStream();

        if (! activated) {
            out = response.getOutputStream();
            out.write(Utils.serviceDisabled());
            out.close();
            return;
        }
		JSONObject object   = new JSONObject();
		ResultSet resultSet = null;

		Connection connection = null;
		PreparedStatement st = null;
		DownloadQuery q = new DownloadQuery(request);
		try {
			connection = dataSource.getConnection();
			if (connection == null) {
			    throw new RuntimeException("could not get a connection to the database");
			}
			connection.setAutoCommit(false);
			if (isInvalid(q)) {
				object.put("success", false);
				object.put("msg", "invalid form");
				out.write(object.toString().getBytes());
			} else {
				st = prepareStatement(connection, q);

				st.setString(9, q.getJsonSpec());

				st.executeUpdate();
				resultSet = st.getGeneratedKeys();
				resultSet.next();

				int idInserted = resultSet.getInt(1);

				insertDataUse(idInserted, q, connection);
				insertLayersLogs(idInserted, q, connection);
				connection.commit();

				object.put("success", true);
				object.put("msg", "Successfully added the record in database.");

				out.write(object.toString(4).getBytes());
			}
		} catch (Exception e) {
		    if (connection != null) {
		        connection.rollback();
		    }
			if (out != null) {
			    String message = "{ error: \"Unable to handle request: "+ e + "\" }";
				out.write(message.getBytes());
			}
			logger.error("Caught exception while executing service: ", e);
			response.setStatus(500);
		} finally {
			if (st != null) {
			    st.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (out != null) {
				out.close();
			}
			if (connection != null) {
				connection.setAutoCommit(true);
				connection.close();
			}
		}
	}

	protected void insertLayersLogs(int idInserted, DownloadQuery q, Connection c) throws Exception {

		PreparedStatement st = null;

		JSONObject obj 	= new JSONObject(q.getJsonSpec());
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

				st = c.prepareStatement(insertLayersQuery);
				st.setInt(1, idInserted);
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
				st.execute();
			}
		} catch(Exception e) {
			if(st != null) st.close();
			throw e;
		}
	}
}