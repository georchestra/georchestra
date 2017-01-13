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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;

/**
 * Abstract class that defines a generic behaviour for the different
 * applications that should ask the user for submitting a form, containing some
 * informations about his download.
 *
 * {@see ExtractorApp} {@see GeoNetwork}
 *
 * @author pmauduit
 */
public abstract class AbstractApplication {
    protected Log logger;

    protected DataSource dataSource;

    protected boolean activated;
    protected String insertDownloadQuery;

    protected String insertDataUseQuery = "INSERT INTO "
            + "    downloadform.logtable_datause (logtable_id, datause_id) "
            + "VALUES " + "    (?,?);";

    protected AbstractApplication(DataSource ds, boolean _activated,
            String _insertDownloadQuery) {
        dataSource = ds;
        activated = _activated;
        insertDownloadQuery = _insertDownloadQuery;
    }
    public abstract void handleRequest(HttpServletRequest request, HttpServletResponse mockedResponse)
            throws Exception;
    protected abstract boolean isInvalid(DownloadQuery q);

    /**
     * Prepares a DownloadQuery object, containing all the informations needed
     * to be saved in database.
     *
     * @param request
     * @return DownloadQuery a download query object.
     */
    protected DownloadQuery initializeVariables(HttpServletRequest request) {
        return new DownloadQuery(request);
    }

    /**
     * This method prepares a statement to insert the download request into
     * database.
     *
     * @param q
     * @return PreparedStatement the statement can be completed (if extra
     *         parameters to be set) by the daughter classes.
     *
     * @throws SQLException
     */
    protected PreparedStatement prepareStatement(Connection c, DownloadQuery q)
            throws SQLException {
        PreparedStatement st = c.prepareStatement(insertDownloadQuery, Statement.RETURN_GENERATED_KEYS);
        st.setString(1, q.getUserName());
        st.setString(2, q.getSessionId());
        st.setString(3, q.getFirstName());
        st.setString(4, q.getSecondName());
        st.setString(5, q.getCompany());
        st.setString(6, q.getEmail());
        st.setString(7, q.getTel());
        st.setString(8, q.getComment());
        return st;
    }

    /**
     * This method inserts the data_usage into the database.
     *
     * @param idInserted
     * @param q
     * @return PreparedStatement the statement can be completed (if extra
     *         parameters to be set) by the daughter classes.
     *
     * @throws SQLException
     */
    protected void insertDataUse(int idInserted, DownloadQuery q, Connection c)
            throws Exception {
        for (Integer dataUse : q.getDataUse()) {
            int dataUseI = dataUse.intValue();
            PreparedStatement dataUseSt = null;
            try {
                dataUseSt = c.prepareStatement(insertDataUseQuery);
                dataUseSt.setInt(1, idInserted);
                dataUseSt.setInt(2, dataUseI);
                dataUseSt.execute();
            } finally {
                if (dataUseSt != null)
                    dataUseSt.close();
            }
        }
    }

    /**
     * Convenience method used for testing purposes.
     * @param ds a DataSource
     */
    public void setDataSource(DataSource ds) {
        dataSource = ds;
    }
    /**
     * Convenience method used for testing purposes.
     * @param activated wheter the service is activated or not.
     */
    public void setActivated(boolean _activated) {
        activated = _activated;
    }
}
