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

import static org.georchestra.ogcservstatistics.dataservices.LogColumns.LAYER_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.QUALIFIED_TABLE_NAME;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.USER_COLUMN;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * For each user : list of layers and number of connections
 * 
 * @author Mauricio Pazos
 *
 */
public final class RetrieveLayerConnectionsForUserCommand extends AbstractQueryCommand {

    private static final String CONNECTIONS_COLUMN = "connections";

    /**
     * builds the sql query taking into account if a month is or isn't specified.
     * 
     * @return the sql statement
     */
    private String getSQLStatement() {

        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT ").append(USER_COLUMN).append(",").append(LAYER_COLUMN).append(",count(")
                .append(LAYER_COLUMN).append(") as ").append(CONNECTIONS_COLUMN).append(" FROM ")
                .append(QUALIFIED_TABLE_NAME);
        if (this.month > 0) {
            sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? AND EXTRACT(MONTH FROM date) = ?");
        } else {
            sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? ");
        }
        sql.append(" GROUP BY ").append(USER_COLUMN).append(",").append(LAYER_COLUMN);
        sql.append(" ORDER BY ").append(USER_COLUMN).append(",").append(LAYER_COLUMN);

        return sql.toString();
    }

    /**
     * Prepares the Statement setting the year and month.
     */
    @Override
    protected PreparedStatement prepareStatement() throws SQLException {

        PreparedStatement pStmt = this.connection.prepareStatement(getSQLStatement());
        assert year > 0 : "year is expected";

        pStmt.setInt(1, this.year);

        // if the month was specified then set it in the statement
        if (this.month > 0) {
            pStmt.setInt(2, this.month);
        }

        return pStmt;
    }

    @Override
    protected Map<String, Object> getRow(ResultSet rs) throws SQLException {

        Map<String, Object> row = new HashMap<String, Object>(4);
        row.put(USER_COLUMN, rs.getString(USER_COLUMN));
        row.put(LAYER_COLUMN, rs.getString(LAYER_COLUMN));
        row.put(CONNECTIONS_COLUMN, rs.getInt(CONNECTIONS_COLUMN));

        return row;
    }

}
