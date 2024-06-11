/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.DataSourceMetadata.DataSourceType;
import org.georchestra.datafeeder.service.DatasetsService.FeatureResult;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.georchestra.datafeeder.test.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.csv.CSVDataStore;
import org.geotools.data.csv.parse.CSVAttributesOnlyStrategy;
import org.geotools.data.csv.parse.CSVLatLonStrategy;
import org.junit.Rule;
import org.junit.Test;

public class DatasetsServiceTest {

    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();
    public @Rule TestData testData = new TestData();

    private DatasetsService service = new DatasetsService();

    @Test
    public void testLoadDataStore() throws IOException {
        testLoadDataStore(testData.archSitesShapefile());
        testLoadDataStore(testData.bugSitesShapefile());
        testLoadDataStore(testData.chinesePolyShapefile());
        testLoadDataStore(testData.roadsShapefile());
        testLoadDataStore(testData.statePopShapefile());
    }

    private void testLoadDataStore(Path path) throws IOException {
        DataStore ds = service.loadDataStore(path);
        try {
            assertNotNull(ds);
        } finally {
            ds.dispose();
        }
    }

    @Test
    public void testDescribe() throws IOException {
        testDescribeShapefile(testData.statePopShapefile());
        testDescribeShapefile(testData.bugSitesShapefile());
        testDescribeShapefile(testData.statePopShapefile());
        testDescribeShapefile(testData.chinesePolyShapefile());
        testDescribeShapefile(testData.archSitesShapefile());
    }

    private void testDescribeShapefile(Path path) {
        DataSourceMetadata ds = service.describe(path);
        assertNotNull(ds);
        assertEquals(DataSourceType.SHAPEFILE, ds.getType());
        assertNotNull(ds.getConnectionParameters());
        assertFalse(ds.getConnectionParameters().isEmpty());

        List<DatasetMetadata> mdl = ds.getDatasets();
        assertNotNull(mdl);
        assertEquals(1, mdl.size());
        DatasetMetadata md = mdl.get(0);
        assertEquals("encoding should default to shapefile spec default", "ISO-8859-1", md.getEncoding());
        assertNotNull(md.getTypeName());
        assertNotNull(md.getFeatureCount());
        assertNotNull(md.getNativeBounds());
        assertNotNull(md.getNativeBounds().getCrs());
        assertNotNull(md.getNativeBounds().getCrs().getWKT());
        assertNotNull(md.getSampleGeometry());
    }

    @Test
    public void getBounds_native_crs_is_missing_and_not_overridden_results_in_null_bounds_crs() throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String targetSrs = null;
        String nativeSrsOverride = null;

        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertNull(bounds.getNativeCrs());
        assertNull(bounds.getCrs());
        assertFalse(bounds.isReprojected());
    }

    @Test
    public void getBounds_native_crs_is_missing_and_overridden_by_parameter() throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String targetSrs = null;
        String nativeSrsOverride = "EPSG:4326";

        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertFalse(bounds.isReprojected());
        assertEquals(nativeSrsOverride, bounds.getNativeCrs().getSrs());
        assertEquals(nativeSrsOverride, bounds.getCrs().getSrs());
    }

    @Test
    public void getBounds_native_crs_is_missing_and_overridden_by_parameter_reprojects_ok_to_target_crs()
            throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String nativeSrsOverride = "EPSG:4326";
        String targetSrs = "EPSG:3857";

        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertEquals(targetSrs, bounds.getCrs().getSrs());
        assertEquals(nativeSrsOverride, bounds.getNativeCrs().getSrs());
        assertTrue(bounds.isReprojected());
    }

    @Test
    public void getBounds_native_crs_is_missing_and_not_overriden_cant_be_reprojected_to_target_crs()
            throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String targetSrs = "EPSG:3857";
        String nativeSrsOverride = null;

        try {
            service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
            fail("Expected IAE trying to reproject with missing native CRS and no source SRS override");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Unable to reproject, dataset statepop doesn't declare a native CRS and no native SRS override was provided",
                    expected.getMessage());
        }
    }

    @Test
    public void getBounds_native_crs_is_present_and_no_override_nor_reproject_requested() throws IOException {
        Path path = testData.statePopShapefile();

        String targetSrs = null;
        String nativeSrsOverride = null;
        String nativeSrs = "EPSG:4269"; // native crs from .prj
        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertFalse(bounds.isReprojected());
        assertEquals(nativeSrs, bounds.getCrs().getSrs());
        assertEquals(nativeSrs, bounds.getNativeCrs().getSrs());
    }

    @Test
    public void getBounds_native_crs_is_present_and_reprojected_to_target_crs() throws IOException {
        Path path = testData.statePopShapefile();

        String nativeSrs = "EPSG:4269"; // native crs from .prj
        String targetSrs = "EPSG:3857";
        String nativeSrsOverride = null;

        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertTrue(bounds.isReprojected());
        assertEquals(targetSrs, bounds.getCrs().getSrs());
        assertEquals(nativeSrs, bounds.getNativeCrs().getSrs());
    }

    @Test
    public void getBounds_native_crs_is_present_and_overridden_no_reprojection_requested() throws IOException {
        Path path = testData.statePopShapefile();

        String targetSrs = null;
        String nativeSrsOverride = "EPSG:4326";

        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertFalse(bounds.isReprojected());
        assertEquals(nativeSrsOverride, bounds.getCrs().getSrs());
        assertEquals(nativeSrsOverride, bounds.getNativeCrs().getSrs());
    }

    @Test
    public void getBounds_native_crs_is_present_and_overridden_and_reprojected_to_target_crs() throws IOException {
        Path path = testData.statePopShapefile();

        String targetSrs = "EPSG:3857";
        String nativeSrsOverride = "EPSG:4326";

        BoundingBoxMetadata bounds = service.getBounds(path, "statepop", targetSrs, nativeSrsOverride);
        assertTrue(bounds.isReprojected());
        assertEquals(targetSrs, bounds.getCrs().getSrs());
        assertEquals(nativeSrsOverride, bounds.getNativeCrs().getSrs());
    }

    ///////////////////

    @Test
    public void getFeature_native_crs_is_missing_and_not_overridden_results_in_null_crs() throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String targetSrs = null;
        String nativeSrsOverride = null;

        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertNull(result.getNativeCrs());
        assertNull(result.getCrs());
        assertFalse(result.isReprojected());
    }

    @Test
    public void getFeature_native_crs_is_missing_and_overridden_by_parameter() throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String targetSrs = null;
        String nativeSrsOverride = "EPSG:4326";

        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertFalse(result.isReprojected());
        assertEquals(nativeSrsOverride, result.getNativeCrs().getSrs());
        assertEquals(nativeSrsOverride, result.getCrs().getSrs());
    }

    @Test
    public void getFeature_native_crs_is_missing_and_overridden_by_parameter_reprojects_ok_to_target_crs()
            throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String nativeSrsOverride = "EPSG:4326";
        String targetSrs = "EPSG:3857";

        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertEquals(targetSrs, result.getCrs().getSrs());
        assertEquals(nativeSrsOverride, result.getNativeCrs().getSrs());
        assertTrue(result.isReprojected());
    }

    @Test
    public void getFeature_native_crs_is_missing_and_not_overriden_cant_be_reprojected_to_target_crs()
            throws IOException {
        Path path = testData.statePopShapefile();
        Path prj = path.getParent().resolve("statepop.prj");
        assertTrue(prj.toFile().delete());

        String targetSrs = "EPSG:3857";
        String nativeSrsOverride = null;

        try {
            service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
            fail("Expected IAE trying to reproject with missing native CRS and no source SRS override");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Unable to reproject, dataset statepop doesn't declare a native CRS and no native SRS override was provided",
                    expected.getMessage());
        }
    }

    @Test
    public void getFeature_native_crs_is_present_and_no_override_nor_reproject_requested() throws IOException {
        Path path = testData.statePopShapefile();

        String targetSrs = null;
        String nativeSrsOverride = null;
        String nativeSrs = "EPSG:4269"; // native crs from .prj
        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertFalse(result.isReprojected());
        assertEquals(nativeSrs, result.getCrs().getSrs());
        assertEquals(nativeSrs, result.getNativeCrs().getSrs());
    }

    @Test
    public void getFeature_native_crs_is_present_and_reprojected_to_target_crs() throws IOException {
        Path path = testData.statePopShapefile();

        String nativeSrs = "EPSG:4269"; // native crs from .prj
        String targetSrs = "EPSG:3857";
        String nativeSrsOverride = null;

        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertTrue(result.isReprojected());
        assertEquals(targetSrs, result.getCrs().getSrs());
        assertEquals(nativeSrs, result.getNativeCrs().getSrs());
    }

    @Test
    public void getFeature_native_crs_is_present_and_overridden_no_reprojection_requested() throws IOException {
        Path path = testData.statePopShapefile();

        String targetSrs = null;
        String nativeSrsOverride = "EPSG:4326";

        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertFalse(result.isReprojected());
        assertEquals(nativeSrsOverride, result.getCrs().getSrs());
        assertEquals(nativeSrsOverride, result.getNativeCrs().getSrs());
    }

    @Test
    public void getFeature_native_crs_is_present_and_overridden_and_reprojected_to_target_crs() throws IOException {
        Path path = testData.statePopShapefile();

        String targetSrs = "EPSG:3857";
        String nativeSrsOverride = "EPSG:4326";

        FeatureResult result = service.getFeature(path, "statepop", (Charset) null, 0, targetSrs, nativeSrsOverride);
        assertNotNull(result.getFeature());
        assertTrue(result.isReprojected());
        assertEquals(targetSrs, result.getCrs().getSrs());
        assertEquals(nativeSrsOverride, result.getNativeCrs().getSrs());
    }

    @Test
    public void testResolveDataStore_csv() throws Exception {
        Path path = Paths
                .get(this.getClass().getResource("/org/georchestra/datafeeder/batch/analysis/basic.csv").toURI());
        DatasetUploadState state = new DatasetUploadState();
        state.setFileName(path.getFileName().toString());
        state.setFormat(DataSourceType.CSV);
        state.setAbsolutePath(path.toString());

        DataStore toTest = service.resolveSourceDataStore(state);

        assertTrue("Expected a CSV Datastore", toTest instanceof CSVDataStore);
        assertTrue("Expected a 'Attributes only' CSV strategy",
                ((CSVDataStore) toTest).getCSVStrategy() instanceof CSVAttributesOnlyStrategy);
    }

    @Test
    public void testResolveConnectionParameters_csv() throws Exception {
        Path path = Paths
                .get(this.getClass().getResource("/org/georchestra/datafeeder/batch/analysis/basic.csv").toURI());

        Map<String, String> toTest = service.resolveConnectionParameters(path);

        assertEquals("wrong number of parameters, expected 2", toTest.size(), 2);
    }

    @Test
    public void testResolveSourceDatastore_csv() throws Exception {
        Path path = Paths
                .get(this.getClass().getResource("/org/georchestra/datafeeder/batch/analysis/covoit-mel.csv").toURI());
        DatasetUploadState state = new DatasetUploadState();
        PublishSettings publishSettings = new PublishSettings();
        publishSettings.setOptions(Map.of("latField", "lat", //
                "lngField", "lon", //
                "quoteChar", "`", "delimiter", "|"));
        state.setFileName(path.getFileName().toString());
        state.setFormat(DataSourceType.CSV);
        state.setAbsolutePath(path.toString());
        state.setPublishing(publishSettings);

        DataStore toTest = service.resolveSourceDataStore(state);

        assertTrue("Expected a CSVDataStore", toTest instanceof CSVDataStore);
        assertTrue("Expected a CSVLatLonStrategy for parsing CSV file",
                ((CSVDataStore) toTest).getCSVStrategy() instanceof CSVLatLonStrategy);
        assertEquals("Expected a '|' as field separator / delimiter",
                ((CSVDataStore) toTest).getCSVStrategy().getSeparator(), '|');
        assertEquals("Expected a '`' as quote char", ((CSVDataStore) toTest).getCSVStrategy().getQuotechar(), '`');

    }

}
