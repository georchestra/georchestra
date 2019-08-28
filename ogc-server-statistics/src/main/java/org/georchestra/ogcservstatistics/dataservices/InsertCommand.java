/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.ogcservstatistics.dataservices;

import static org.georchestra.ogcservstatistics.dataservices.LogColumns.DATE_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.LAYER_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.ORG_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.QUALIFIED_TABLE_NAME;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.REQUEST_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.SECROLE_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.SERVICE_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.USER_COLUMN;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Insert an ogc service log
 * 
 * @author Mauricio Pazos
 *
 */
public final class InsertCommand extends AbstractDataCommand {

    private static final Logger LOGGER = Logger.getLogger(InsertCommand.class);

    private static final String SQL_INSERT = "INSERT INTO " + QUALIFIED_TABLE_NAME + "(" + USER_COLUMN + ","
            + DATE_COLUMN + "," + SERVICE_COLUMN + "," + LAYER_COLUMN + "," + REQUEST_COLUMN + "," + ORG_COLUMN + ","
            + SECROLE_COLUMN + ") VALUES (?, ?, ?, ?, ?, ?, string_to_array(?, ','))";

    private Map<String, Object> rowValues;

    public void setRowValues(final Map<String, Object> ogcServiceLog) {

        this.rowValues = ogcServiceLog;
    }

    private PreparedStatement prepareStatement() throws SQLException {

        assert this.connection != null : "database connection is null, use setConnection";

        PreparedStatement pStmt = this.connection.prepareStatement(SQL_INSERT);
        pStmt.setString(1, (String) this.rowValues.get(USER_COLUMN));

        java.sql.Timestamp sqlDate = new java.sql.Timestamp(
                ((java.util.Date) this.rowValues.get(DATE_COLUMN)).getTime());
        pStmt.setTimestamp(2, sqlDate);
        pStmt.setString(3, ((String) this.rowValues.get(SERVICE_COLUMN)).trim());
        pStmt.setString(4, ((String) this.rowValues.get(LAYER_COLUMN)).trim());
        pStmt.setString(5, ((String) this.rowValues.get(REQUEST_COLUMN)).trim());
        pStmt.setString(6, ((String) this.rowValues.get(ORG_COLUMN)).trim());
        pStmt.setString(7, ((String) this.rowValues.get(SECROLE_COLUMN)).trim());

        return pStmt;
    }

    @Override
    public void execute() throws DataCommandException {

        assert this.connection != null : "database connection is null, use setConnection";

        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataCommandException(e);
        }

        // executes the sql statement and checks that the update operation will be
        // inserted one row in the table
        try (PreparedStatement pStmt = prepareStatement()) {
            pStmt.executeUpdate();
            this.connection.commit();
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException e1) {
                throw new DataCommandException(e);
            }
            throw new DataCommandException(e);
        } finally {
            try {
                this.connection.setAutoCommit(true);
            } catch (SQLException e1) {
                // ignore, it's bad practice to throw exceptions in finally blocks
                LOGGER.warn("Error rolling back SQL transaction", e1);
            }
        }
    }
}
