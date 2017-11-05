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

package org.georchestra.dlform;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;

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
 * data_usage controller: returns the content of the table downloadform.data_use
 * as a JSON object.
 *
 * author: pmauduit
 */
@Controller
@RequestMapping("/data_usage")
public class DataUsage {

	private final Log logger = LogFactory.getLog(getClass());

	private DataSource dataSource;
	private boolean activated;

	public DataUsage(DataSource ds, boolean _activated) {
		dataSource = ds;
		activated = _activated;
	}

	private JSONArray getUsage() throws Exception {
		JSONArray ret = new JSONArray();
		Connection connection = null;
		Statement sql = null;
		ResultSet results = null;
		try {
			connection = dataSource.getConnection();
			sql = connection.createStatement();

			results = sql.executeQuery("SELECT * FROM downloadform.data_use");

			if (results != null) {
		        ResultSetMetaData md = results.getMetaData();
				while (results.next()) {
					HashMap<String, String> record = new HashMap<String, String>();
					record.put(md.getColumnLabel(1), results.getString(1));
					record.put(md.getColumnLabel(2), results.getString(2));
					ret.put(record);
				}
	            results.close();
			}

		} finally {
			if (results != null) results.close();
			if (sql != null) sql.close();
			if (connection != null) connection.close();
		}
		return ret;

	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST})
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setHeader("Content-Type", "application/json");
	    OutputStream out = null;
	    out = response.getOutputStream();

	    if (! activated) {
		    out = response.getOutputStream();
		    out.write(Utils.serviceDisabled());
		    out.close();
		    return;
		}

		JSONObject object = new JSONObject();
		try {
			  object.put("rows", getUsage());
			  out.write(object.toString(4).getBytes("UTF-8"));
		} catch (Exception e) {
		    logger.debug("Failure obtaining the data_use table content", e);
		    throw e;
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Method used only for convenience (testing).
	 * @param activated whether the service should be activated or not.
	 */
	public void setActivated(boolean _activated) {
	    activated = _activated;
	}

}