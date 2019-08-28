package org.georchestra.analytics.util;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
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
        Assume.assumeTrue(!Strings.isNullOrEmpty(url));

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

        Assert.assertEquals(sqlWithReplacments, finalQuery);
    }
}
