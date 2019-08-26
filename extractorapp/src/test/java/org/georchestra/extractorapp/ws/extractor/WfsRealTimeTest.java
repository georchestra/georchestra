package org.georchestra.extractorapp.ws.extractor;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WfsRealTimeTest {
	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();

	@Before
	public void configureGeoToolsForceXY() {
		System.setProperty("org.geotools.referencing.forceXY", "true");
	}

	@Test
	@Ignore("Need to be mocked instead of hitting remote servers")
	public void testRemoteWfsMapserver() throws Exception {
		JSONObject layerJson = new JSONObject();

		layerJson.put(ExtractorLayerRequest.URL_KEY, "http://services.sandre.eaufrance.fr/geo/stq_FXX");
		layerJson.put(ExtractorLayerRequest.PROJECTION_KEY, "EPSG:4326");
		layerJson.put(ExtractorLayerRequest.TYPE_KEY, "WFS");
		layerJson.put(ExtractorLayerRequest.LAYER_NAME_KEY, "StationMesureEauxSurfacePointsPrel");
		layerJson.put(ExtractorLayerRequest.FORMAT_KEY, "shp");
		JSONObject bbox = new JSONObject();
		bbox.put(ExtractorLayerRequest.BBOX_SRS_KEY, "EPSG:4326");
		JSONArray bboxValue = new JSONArray("[46.127729257642,5.2991266375546,46.82423580786,6.235807860262]");
		bbox.put(ExtractorLayerRequest.BBOX_VALUE_KEY, bboxValue);
		layerJson.put(ExtractorLayerRequest.BBOX_KEY, bbox);
		JSONObject globalJson = new JSONObject();
		JSONArray emails = new JSONArray();

		ExtractorLayerRequest elr = new ExtractorLayerRequest(layerJson, globalJson, emails);
		WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot());
		final File extract = wfsExtractor.extract(elr);

	}

	@Test
	@Ignore("Need to be mocked instead of hitting remote servers")
	public void testRemoteWfsGeoserver() throws Exception {
		JSONObject layerJson = new JSONObject();

		layerJson.put(ExtractorLayerRequest.URL_KEY, "https://sdi.georchestra.org/geoserver/wfs");
		layerJson.put(ExtractorLayerRequest.PROJECTION_KEY, "EPSG:4326");
		layerJson.put(ExtractorLayerRequest.TYPE_KEY, "WFS");
		layerJson.put(ExtractorLayerRequest.LAYER_NAME_KEY, "geor:sdi");
		layerJson.put(ExtractorLayerRequest.FORMAT_KEY, "shp");
		JSONObject bbox = new JSONObject();
		bbox.put(ExtractorLayerRequest.BBOX_SRS_KEY, "EPSG:4326");
		JSONArray bboxValue = new JSONArray("[46.127729257642,5.2991266375546,46.82423580786,6.235807860262]");
		bbox.put(ExtractorLayerRequest.BBOX_VALUE_KEY, bboxValue);
		layerJson.put(ExtractorLayerRequest.BBOX_KEY, bbox);
		JSONObject globalJson = new JSONObject();
		JSONArray emails = new JSONArray();

		ExtractorLayerRequest elr = new ExtractorLayerRequest(layerJson, globalJson, emails);
		WfsExtractor wfsExtractor = new WfsExtractor(testDir.getRoot());
		final File extract = wfsExtractor.extract(elr);

	}

}
