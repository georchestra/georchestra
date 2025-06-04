/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.analytics.util;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import com.google.common.base.Strings;

public class QueryBuilderTest {

    @Test
    // Test parameter replacement
    public void testParameter() throws PropertyVetoException, SQLException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        String url = System.getProperty("JDBC_TEST_URL");
        if (Strings.isNullOrEmpty(url)) {
            url = System.getenv("JDBC_TEST_URL");
        }
        Assumptions.assumeTrue(!Strings.isNullOrEmpty(url));

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        QueryBuilder builder = new QueryBuilder();

        String sql = "SELECT CAST(COUNT(*) AS integer) AS count, to_char(date, {aggregateDate}) "
                + "FROM ogcstatistics.ogc_services_log "
                + "WHERE date >= CAST({startDate} AS timestamp without time zone) AND date < CAST({endDate} AS timestamp without time zone) "
                + "AND user = {user} " + "GROUP BY to_char(date, {aggregateDate}) "
                + "ORDER BY to_char(date, {aggregateDate})";

        Map<String, String> values = new HashMap<>();
        values.put("startDate", "2017-08-15");
        values.put("endDate", "2017-09-15");
        values.put("aggregateDate", "YYYY-mm-dd HH24");
        values.put("user", "biloute");

        String finalQuery = builder.generateQuery(sql, values);

        String sqlWithReplacments = "SELECT CAST(COUNT(*) AS integer) AS count, to_char(date, 'YYYY-mm-dd HH24') "
                + "FROM ogcstatistics.ogc_services_log "
                + "WHERE date >= CAST('2017-08-15' AS timestamp without time zone) AND date < CAST('2017-09-15' AS timestamp without time zone) "
                + "AND user = 'biloute' " + "GROUP BY to_char(date, 'YYYY-mm-dd HH24') "
                + "ORDER BY to_char(date, 'YYYY-mm-dd HH24')";

        Assertions.assertEquals(sqlWithReplacments, finalQuery);
    }
}
