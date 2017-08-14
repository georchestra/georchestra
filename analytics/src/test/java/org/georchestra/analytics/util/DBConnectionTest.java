package org.georchestra.analytics.util;

import org.georchestra.analytics.util.DBConnection;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBConnectionTest {

    @Autowired
    private GeorchestraConfiguration georConfig;

    @Test
    public void testParameter() throws PropertyVetoException, SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Map<String, String> env = System.getenv();
        Assume.assumeTrue(env.containsKey("JDBC_TEST_URL"));

        DBConnection conn = new DBConnection(env.get("JDBC_TEST_URL"));

        String sql = "SELECT CAST(COUNT(*) AS integer) AS count, to_char(date, {aggregateDate}) " +
                "FROM ogcstatistics.ogc_services_log " +
                "WHERE date >= CAST({startDate} AS timestamp without time zone) AND date < CAST({endDate} AS timestamp without time zone) " +
                "AND user = {user} " +
                "GROUP BY to_char(date, {aggregateDate}) " +
                "ORDER BY to_char(date, {aggregateDate})";

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("startDate", "2017-08-15");
        values.put("endDate", "2017-09-15");
        values.put("aggregateDate", "YYYY-mm-dd HH24");
        values.put("user","biloute");

        String finalQuery = conn.generateQuery(sql, values);

        String sqlWithReplacments = "SELECT CAST(COUNT(*) AS integer) AS count, to_char(date, 'YYYY-mm-dd HH24') " +
                "FROM ogcstatistics.ogc_services_log " +
                "WHERE date >= CAST('2017-08-15' AS timestamp without time zone) AND date < CAST('2017-09-15' AS timestamp without time zone) " +
                "AND user = 'biloute' " +
                "GROUP BY to_char(date, 'YYYY-mm-dd HH24') " +
                "ORDER BY to_char(date, 'YYYY-mm-dd HH24')";

        Assert.assertEquals(sqlWithReplacments, finalQuery);
    }
}
