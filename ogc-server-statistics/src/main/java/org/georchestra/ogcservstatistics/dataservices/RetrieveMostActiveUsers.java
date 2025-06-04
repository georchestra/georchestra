/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ogcservstatistics.dataservices;

import static org.georchestra.ogcservstatistics.dataservices.LogColumns.QUALIFIED_TABLE_NAME;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.USER_COLUMN;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This command retrieves the list of the N most active users.
 *
 *
 * @author Mauricio Pazos
 *
 */
public final class RetrieveMostActiveUsers extends AbstractQueryCommand {

    private static final String CONNECTIONS_COLUMN = "connections";

    /**
     * builds the sql query taking into account if a month is or isn't specified.
     *
     * @return the sql statement
     */
    private String getSQLStatement() {

        StringBuilder sql = new StringBuilder();

        sql.append(" SELECT ").append(USER_COLUMN).append(",count(").append(USER_COLUMN).append(") as ")
                .append(CONNECTIONS_COLUMN).append(" FROM ").append(QUALIFIED_TABLE_NAME);
        if (this.month > 0) {
            sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? AND EXTRACT(MONTH FROM date) = ?");
        } else {
            sql.append(" WHERE EXTRACT(ISOYEAR FROM date) = ? ");
        }
        sql.append(" GROUP BY ").append(USER_COLUMN);
        sql.append(" ORDER BY ").append(CONNECTIONS_COLUMN).append(" DESC");
        sql.append(" LIMIT ?");

        return sql.toString();
    }

    @Override
    protected PreparedStatement prepareStatement() throws SQLException {

        PreparedStatement pStmt = this.connection.prepareStatement(getSQLStatement());
        assert year > 0 : "year is expected";

        pStmt.setInt(1, this.year);

        // if the month was specified then set it in the statement
        if (this.month > 0) {
            pStmt.setInt(2, this.month);
            assert this.limit > 0;
            pStmt.setInt(3, this.limit);
        } else {
            assert this.limit > 0;
            pStmt.setInt(2, this.limit);

        }

        return pStmt;
    }

    @Override
    protected Map<String, Object> getRow(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>(4);
        row.put(USER_COLUMN, rs.getString(USER_COLUMN));
        row.put(CONNECTIONS_COLUMN, rs.getInt(CONNECTIONS_COLUMN));

        return row;
    }

}
