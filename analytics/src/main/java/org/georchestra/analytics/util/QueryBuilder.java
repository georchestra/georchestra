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

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to keep parameter replacement feature without
 * using SQL prepared statements.
 *
 * Prepared statements implies that SQL plan execution is computed before
 * parameters values are known. So with partitioned table, execution plan is
 * always wrong and all tables are scanned. This lead to very long query. To fix
 * this issue, this class, use a standard Statement instance to use parameter
 * replacement and then get raw SQL query with toString() method. Then a second
 * method provide a reconnection feature and test db connection before sending
 * real query to the database. If test fails, one reconnection is tried. Another
 * feature of this class, it adds a name on sql parameter: '?' is replaced by
 * parameter name in braces : {\w+}
 *
 * Example :
 *
 * <pre>
 * <code>
 * String sql = "SELECT * FROM users WHERE group_name = {group} LIMIT {count}";
 * Map<String, Object> sqlValues = new HashMap<String, Object>();
 * sqlValues.put("group", "admin"); sqlValues.put("count", 100);
 *
 * QueryBuilder builder = new QueryBuilder();
 * String rawSql = builder.generateQuery(sql, sqlValues);
 * Statement st = ...
 * ResultSet res = st.executeQuery(rawSql);
 * </code>
 * </pre>
 */
public class QueryBuilder {

    private static final Pattern namedParameterPattern = Pattern.compile("\\{(\\w+)\\}");

    public String generateQuery(String sql, Map<String, String> values) throws SQLException {
        // Replace named parameter with standard prepared statement parameter : '?'
        Matcher m = namedParameterPattern.matcher(sql);

        while (m.find()) {
            String parameterName = m.group(1);
            if (!values.containsKey(parameterName)) {
                throw new IllegalArgumentException(
                        "No value specified for parameter : " + parameterName + " in " + sql);
            }
            String parameterValue = values.get(parameterName);
            String value = null == parameterValue ? "null" : String.format("'%s'", parameterValue);
            sql = sql.replaceFirst("\\{" + parameterName + "\\}", value);
            m = namedParameterPattern.matcher(sql);
        }

        return sql;
    }
}
