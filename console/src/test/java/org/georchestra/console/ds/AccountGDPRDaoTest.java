package org.georchestra.console.ds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.georchestra.console.ds.AccountGDPRDao.ExtractorRecord;
import org.georchestra.console.ds.AccountGDPRDao.GeodocRecord;
import org.georchestra.console.ds.AccountGDPRDao.MetadataRecord;
import org.georchestra.console.ds.AccountGDPRDao.OgcStatisticsRecord;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
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
		ldt1 = LocalDateTime.now();
		ldt2 = LocalDateTime.now();
		ZoneId zoneId = ZoneId.systemDefault();
		ts1 = new Timestamp(ZonedDateTime.of(ldt1, zoneId).toInstant().toEpochMilli());
		ts2 = new Timestamp(ZonedDateTime.of(ldt2, zoneId).toInstant().toEpochMilli());
	}

	public @Test void testGeodocRecord() throws Exception {
		GeodocRecord expected = new GeodocRecord("SLD", "<StyledLayerDescriptor/>", "abcdef123", ldt1, ldt2, 10);

		ResultSet rs = mockResultset("standard", "SLD", "raw_file_content", "<StyledLayerDescriptor/>", "file_hash",
				"abcdef123", "created_at", ts1, "last_access", ts2, "access_count", 10);
		GeodocRecord record = AccountGDPRDaoImpl.createGeodocRecord(rs);
		assertNotNull(record);
		assertEquals(expected, record);
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

	public @Test void testExtractorRecord() throws Exception {
		Geometry bbox = new WKTReader().read("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
		LocalDateTime creationDate = ldt1;
		Time duration = new Time(10_000L);
		List<String> roles = Arrays.asList("ROLE_ADMINISTRATOR", "ROLE_USER");
		String org = "geOrchestra";
		String projection = "EPSG:4326";
		Integer resolution = 1000;
		String format = "TIFF";
		String owstype = "WCS";
		String owsurl = "http://localhost/geoserver/wcs?";
		String layerName = "some_coverage";
		boolean success = true;

		ExtractorRecord expected = new ExtractorRecord(creationDate, duration, roles, org, projection, resolution,
				format, bbox, owstype, owsurl, layerName, success);

		ResultSet rs = mockResultset("creation_date", ts1, "duration", duration, "roles",
				roles.toArray(new String[roles.size()]), "org", org, "projection", projection, "resolution", resolution,
				"format", format, "bbox", bbox, "owstype", owstype, "owsurl", owsurl, "layer_name", layerName,
				"is_successful", success);

		ExtractorRecord record = AccountGDPRDaoImpl.createExtractorRecord(rs);
		assertNotNull(record);
		assertEquals(expected, record);
	}

	public @Test void testMetadataRecord() throws Exception {

		long id = 101;
		LocalDateTime createdDate = ldt1;
		String schemaId = "iso19139";
		String documentContent = "<MD_Metadata/>";
		MetadataRecord expected = new MetadataRecord(id, createdDate, schemaId, documentContent, "name", "surname");

		String createDateStr = createdDate.toString().replace('T', ' ');
		ResultSet rs = mockResultset("id", id, "createdate", createDateStr, "schemaid", schemaId, "data",
				documentContent, "name", "name", "surname", "surname");

		MetadataRecord record = AccountGDPRDaoImpl.createMetadataRecord(rs);
		assertNotNull(record);
		assertEquals(expected, record);
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
