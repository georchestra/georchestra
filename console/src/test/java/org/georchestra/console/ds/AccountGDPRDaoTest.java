package org.georchestra.console.ds;

import static org.georchestra.console.ds.AccountGDPRDaoImpl.GEONETWORK_DATE_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

import org.georchestra.console.ds.AccountGDPRDao.MetadataRecord;
import org.georchestra.console.ds.AccountGDPRDao.OgcStatisticsRecord;
import org.georchestra.console.integration.ds.AccountGDPRDaoIT;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

/**
 *
 * Test suite for {@link AccountGDPRDao}, runs quickly and verifies the mapping
 * of {@code ResultSet} records to the data structures used to export user data.
 * See {@link AccountGDPRDaoIT} for integration tests
 */
public class AccountGDPRDaoTest {

    private LocalDateTime ldt1, ldt2;
    private Timestamp ts1, ts2;

    public @Before void before() {
        ldt1 = LocalDateTime.now().withNano(0);
        ldt2 = LocalDateTime.now().withNano(0);
        ZoneId zoneId = ZoneId.systemDefault();
        ts1 = new Timestamp(ZonedDateTime.of(ldt1, zoneId).toInstant().toEpochMilli());
        ts2 = new Timestamp(ZonedDateTime.of(ldt2, zoneId).toInstant().toEpochMilli());
    }

    public @Test void testOgcStatisticsRecord() throws Exception {
        String service = "WFS";
        String layer = "roads";
        String request = "http://localhost:8080/geoserver/wf?request=GetFeatures&featureType=roads";
        String org = "geOrchestra";
        List<String> roles = Arrays.asList("ROLE_ADMINISTRATOR", "ROLE_USER");
        OgcStatisticsRecord expected = new OgcStatisticsRecord(ldt1, service, layer, request, org, roles);
        ResultSet rs = mockResultset("date", ts1, "service", service, "layer", layer, "request", request, "org", org,
                "roles", roles.toArray(new String[roles.size()]));

        OgcStatisticsRecord record = AccountGDPRDaoImpl.createOgcStatisticsRecord(rs);
        assertNotNull(record);
        assertEquals(expected, record);
    }

    public @Test void testMetadataRecord() throws Exception {

        long id = 101;
        LocalDateTime createdDate = ldt1;
        String schemaId = "iso19139";
        String documentContent = "<MD_Metadata/>";
        MetadataRecord expected = new MetadataRecord(id, createdDate, schemaId, documentContent, "name", "surname");

        String createDateStr = createdDate.toString();
        ResultSet rs = mockResultset("id", id, "createdate", createDateStr, "schemaid", schemaId, "data",
                documentContent, "name", "name", "surname", "surname");

        MetadataRecord record = AccountGDPRDaoImpl.createMetadataRecord(rs);
        assertNotNull(record);
        assertEquals(expected, record);
    }

    public @Test void testParseGeonetworkDate() {
        String date = "2019-09-11T12:41:38";
        TemporalAccessor parsed = GEONETWORK_DATE_FORMAT.parse(date);
        // Making sure the date has correctly been parsed
        assertTrue(parsed.get(ChronoField.DAY_OF_MONTH) == 11 && parsed.get(ChronoField.SECOND_OF_MINUTE) == 38);
    }

    public @Test(expected = DateTimeParseException.class) void testParseISO8601Date() {
        String date = "2024-04-09T11:38:57.658245Z";

        GEONETWORK_DATE_FORMAT.parse(date);
    }

    public @Test void testCreateMetadataRecord_iso8601date() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(eq("createdate"))).thenReturn("2024-04-09T11:38:57.658245Z");
        AccountGDPRDao.MetadataRecord record = AccountGDPRDaoImpl.createMetadataRecord(rs);

        assertTrue(record.getCreatedDate().getDayOfMonth() == 9 && record.getCreatedDate().getYear() == 2024);
    }

    public @Test void testCreateMetadataRecord_geonetworkdate() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(eq("createdate"))).thenReturn("2019-09-11T12:41:38");
        AccountGDPRDao.MetadataRecord record = AccountGDPRDaoImpl.createMetadataRecord(rs);

        assertTrue(record.getCreatedDate().getDayOfMonth() == 11 && record.getCreatedDate().getYear() == 2019);
    }

    public @Test(expected = RuntimeException.class) void testCreateMetadataRecord_invaliddate() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(eq("createdate"))).thenReturn("unparseable_junk");
        AccountGDPRDao.MetadataRecord record = AccountGDPRDaoImpl.createMetadataRecord(rs);

        assertTrue(record.getCreatedDate().getDayOfMonth() == 11 && record.getCreatedDate().getYear() == 2019);
    }

    // Test resiliency to unexpected null values, see GSHDF-291
    public @Test void testParsingNullResiliency() {
        ResultSet rs = nullsMockResultset();
        assertNotNull(AccountGDPRDaoImpl.createMetadataRecord(rs));
        assertNotNull(AccountGDPRDaoImpl.createOgcStatisticsRecord(rs));
    }

    // returns a ResultSet that returns null for any ResultSet.getXXX() call
    private ResultSet nullsMockResultset() {
        ResultSet rs = mock(ResultSet.class);
        return rs;
    }

    private ResultSet mockResultset(Object... kvps) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        for (int i = 0; i < kvps.length; i += 2) {
            String name = (String) kvps[i];
            Object value = kvps[i + 1];
            OngoingStubbing<Object> when;
            final Class<? extends Object> valueClass = value.getClass();
            if (String.class.equals(valueClass)) {
                when = when(rs.getString(eq(name)));
            } else if (Boolean.class.equals(valueClass)) {
                when = when(rs.getBoolean(eq(name)));
            } else if (Integer.class.equals(valueClass)) {
                when = when(rs.getInt(eq(name)));
            } else if (Long.class.equals(valueClass)) {
                when = when(rs.getLong(eq(name)));
            } else if (java.sql.Date.class.equals(valueClass)) {
                when = when(rs.getDate(eq(name)));
            } else if (Time.class.equals(valueClass)) {
                when = when(rs.getTime(eq(name)));
            } else if (Timestamp.class.equals(valueClass)) {
                when = when(rs.getTimestamp(eq(name)));
            } else if (String[].class.equals(valueClass)) {
                java.sql.Array arr = mock(java.sql.Array.class);
                when(arr.getArray()).thenReturn(value);
                value = arr;
                when = when(rs.getArray(eq(name)));
            } else if (Geometry.class.isAssignableFrom(valueClass)) {
                value = new WKBWriter().write((Geometry) value);
                when = when(rs.getBytes(eq(name)));
            } else {
                throw new IllegalArgumentException(valueClass.getName());
            }
            when.thenReturn(value);
        }
        return rs;
    }
}
