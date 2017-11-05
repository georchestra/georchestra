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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This controller manages the download requests made from geOrchestra's
 * geonetwork application.
 *
 * @author pmauduit
 *
 */

@Controller
@RequestMapping("/geonetwork")
public class GeoNetwork extends AbstractApplication {

    private static String INSERT_DOWNLOAD_QUERY = "INSERT INTO downloadform.geonetwork_log "
            + "   (username, sessionid, first_name, second_name, "
            + "company, email, phone, comment, metadata_id, filename) " + "VALUES "
            + "   (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    public GeoNetwork(DataSource ds, boolean activated) {
        super(ds, activated, GeoNetwork.INSERT_DOWNLOAD_QUERY);
    }

    private final Log logger = LogFactory.getLog(getClass());

    protected boolean isInvalid(DownloadQuery q) {
        return q.isInvalid() || (q.getFileName() == null) || (q.getMetadataId() == -1);
    }

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        OutputStream out = response.getOutputStream();
        JSONObject object = new JSONObject();

        if (!activated) {
            out.write(Utils.serviceDisabled());
            out.close();
            return;
        }
        DownloadQuery q = initializeVariables(request);

        Connection connection = null;
        ResultSet resultSet = null;

        try {

            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            // Check form validity
            if (isInvalid(q)) {
                object.put("success", false);
                object.put("msg", "invalid form");
                out.write(object.toString().getBytes());
            } else {
                PreparedStatement st = prepareStatement(connection, q);

                st.setInt(9, q.getMetadataId());
                st.setString(10, q.getFileName());

                st.executeUpdate();
                resultSet = st.getGeneratedKeys();
                resultSet.next();

                int idInserted = resultSet.getInt(1);

                insertDataUse(idInserted, q, connection);
                connection.commit();

                object.put("success", true);
                object.put("msg", "Successfully added the record in database.");
                out.write(object.toString().getBytes());
            }
        } catch (Exception e) {
            if (connection != null)
                connection.rollback();
            if (out != null) {
                String message = "{ error: \"Unable to handle request: " + e.toString().replaceAll("\"", "") + "\" }";
                out.write(message.getBytes());
            }
            logger.error("Caught exception while executing service: ", e);
            response.setStatus(500);
        } finally {
            if (out != null) {
                out.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

}