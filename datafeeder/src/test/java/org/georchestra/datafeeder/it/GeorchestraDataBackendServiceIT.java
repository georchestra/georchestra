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
package org.georchestra.datafeeder.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.georchestra.config.security.DatafeederAuthenticationTestSupport;
import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.DataSourceMetadata;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraDataBackendService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.georchestra.datafeeder.test.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.util.NullProgressListener;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { //
        DataFeederApplicationConfiguration.class, //
        IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
public class GeorchestraDataBackendServiceIT {

    static final String WKT_2154_ESRI = "PROJCS[\"RGF_1993_Lambert_93\",GEOGCS[\"GCS_RGF_1993\",DATUM[\"D_RGF_1993\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Lambert_Conformal_Conic\"],PARAMETER[\"False_Easting\",700000.0],PARAMETER[\"False_Northing\",6600000.0],PARAMETER[\"Central_Meridian\",3.0],PARAMETER[\"Standard_Parallel_1\",49.0],PARAMETER[\"Standard_Parallel_2\",44.0],PARAMETER[\"Latitude_Of_Origin\",46.5],UNIT[\"Meter\",1.0]]";
    static final String WKT_2154_OGC = "PROJCS[\"RGF93 / Lambert-93\",GEOGCS[\"RGF93\",DATUM[\"Reseau_Geodesique_Francais_1993\",SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6171\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4171\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Lambert_Conformal_Conic_2SP\"],PARAMETER[\"standard_parallel_1\",49],PARAMETER[\"standard_parallel_2\",44],PARAMETER[\"latitude_of_origin\",46.5],PARAMETER[\"central_meridian\",3],PARAMETER[\"false_easting\",700000],PARAMETER[\"false_northing\",6600000],AUTHORITY[\"EPSG\",\"2154\"],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH]]";

    private @Autowired GeorchestraNameNormalizer nameResolver;
    public @Rule DatafeederAuthenticationTestSupport authSupport = new DatafeederAuthenticationTestSupport();
    public @Autowired @Rule IntegrationTestSupport support;
    public @Rule TestData testData = new TestData();

    public @Autowired GeorchestraDataBackendService service;
    public @Autowired DatasetsService datasetsService;
    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private Connection testConnection;
    private DataUploadJob job;
    private DatasetUploadState dataset;
    private PublishSettings publishing;

    private ProgressListener importProgressListener = new NullProgressListener();

    String expectedSchemaName;
    UserInfo user;

    public @Before void before() throws SQLException {
        testConnection = support.createLocalPostgisConnection();
        user = authSupport.buildUser();
        expectedSchemaName = nameResolver.resolveDatabaseSchemaName(user.getOrganization().getShortName());
        support.deleteLocalDatabaseSchema(expectedSchemaName);

        job = new DataUploadJob();
        dataset = new DatasetUploadState();
        dataset.setJob(job);
        dataset.setFormat(DataSourceMetadata.DataSourceType.SHAPEFILE);
        publishing = new PublishSettings();
        dataset.setPublishing(publishing);
    }

    public @After void after() throws SQLException {
        if (testConnection != null)
            testConnection.close();
    }

    public @Test void prepareBackend_Null_Org_Name() {
        user.setOrganization(null);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.prepareBackend(job, user));
        assertThat(ex.getMessage(), containsString("Georchestra organization name not provided"));
    }

    public @Test void prepareBackend_Creates_Database_Schema() throws SQLException {
        List<String> pre = support.getDatabaseSchemas();
        assertFalse(pre.toString(), pre.contains(expectedSchemaName));

        service.prepareBackend(job, user);

        List<String> post = support.getDatabaseSchemas();
        assertTrue(post.toString(), post.contains(expectedSchemaName));

        service.prepareBackend(job, user);
        post = support.getDatabaseSchemas();
        assertTrue(post.toString(), post.contains(expectedSchemaName));
    }

    public @Test void importDataset_Null_PublishingSettings() {
        dataset.setName("somename");
        dataset.setPublishing(null);
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> service.importDataset(dataset, user, importProgressListener));
        assertThat(ex.getMessage(), containsString("Dataset 'publishing' settings is null"));
    }

    public @Test void importDataset_AbsoluteFile_Preconditions() {
        dataset.setName("somename");
        dataset.setAbsolutePath(null);
        dataset.getPublishing().setSrs("EPSG:4326");

        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> service.importDataset(dataset, user, importProgressListener));
        assertThat(npe.getMessage(), containsString("absolutePath not provided"));

        dataset.setAbsolutePath("/tmp/nonexistent/" + UUID.randomUUID());
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> service.importDataset(dataset, user, importProgressListener));
        assertThat(iae.getMessage(), containsString("Dataset absolutePath is not a file"));
    }

    public @Test void importDataset_Null_Name() throws IOException {
        dataset.setAbsolutePath(testData.archSitesShapefile().toAbsolutePath().toString());
        dataset.setName(null);

        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> service.importDataset(dataset, user, importProgressListener));
        assertThat(npe.getMessage(), containsString("Dataset name is null"));
    }

    public @Test void importDataset_Invalid_NativeName() throws IOException {
        dataset.setAbsolutePath(testData.archSitesShapefile().toAbsolutePath().toString());
        dataset.setName("invalidName");
        dataset.getPublishing().setSrs("EPSG:4326");
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> service.importDataset(dataset, user, importProgressListener));
        assertThat(iae.getMessage(), containsString("Dataset name 'invalidName' does not exist"));
    }

    public @Test void importDataset_Target_SRS_not_provided() throws IOException {
        dataset.setAbsolutePath(testData.archSitesShapefile().toAbsolutePath().toString());
        dataset.setName("archsites");

        NullPointerException iae = assertThrows(NullPointerException.class,
                () -> service.importDataset(dataset, user, importProgressListener));
        assertThat(iae.getMessage(), containsString("Dataset publish settings must provide the dataset's SRS"));
    }

    public @Test void importDataset_Shapefile_unknown_native_crs_code_force_request_crs() throws Exception {

        Path path = testData.archSitesShapefile().toAbsolutePath();
        dataset.setAbsolutePath(path.toString());
        dataset.setName("archsites");

        final String EPSG_26713_ESRI_WKT = "PROJCS[\"NAD_1927_UTM_Zone_13N\", GEOGCS[\"GCS_North_American_1927\", DATUM[\"D_North_American_1927\", SPHEROID[\"Clarke_1866\", 6378206.4, 294.9786982]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\", 0.017453292519943295], AXIS[\"Longitude\", EAST], AXIS[\"Latitude\", NORTH]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"central_meridian\", -105.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"scale_factor\", 0.9996], PARAMETER[\"false_easting\", 500000.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"m\", 1.0], AXIS[\"x\", EAST], AXIS[\"y\", NORTH]]";
        final CoordinateReferenceSystem nativeCRS = CRS.parseWKT(EPSG_26713_ESRI_WKT);

        CoordinateReferenceSystemMetadata crs = datasetsService.describe(path).getDatasets().get(0).getNativeBounds()
                .getCrs();
        assertNull(crs.getSrs());
        assertNotNull(crs.getWKT());
        CoordinateReferenceSystem esri_26713 = CRS.parseWKT(crs.getWKT());
        assertEquals(nativeCRS, esri_26713);

        // force importing as EPSG:26713 without reprojecting
        dataset.getPublishing().setSrs("EPSG:26713");
        dataset.getPublishing().setSrsReproject(false);

        service.prepareBackend(job, user);
        service.importDataset(dataset, user, importProgressListener);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(user));
        String origTypeName = dataset.getName();
        String importedName = dataset.getPublishing().getImportedName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(origTypeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(importedName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));
            CoordinateReferenceSystem origCRS = orig.getSchema().getCoordinateReferenceSystem();
            CoordinateReferenceSystem importedCRS = imported.getSchema().getCoordinateReferenceSystem();
            assertNotNull(origCRS);
            assertNotNull(importedCRS);
            assertEquals(esri_26713, origCRS);
            assertEquals(CRS.decode("EPSG:26713"), importedCRS);
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }

    public @Test void importDataset_Shapefile_missing_native_crs_import_force_requested_srs() throws Exception {

        Path path = multipartSupport.datafeederTestFile("circuits_2154_no_prj.shp").toAbsolutePath();
        dataset.setAbsolutePath(path.toString());
        dataset.setName("circuits_2154_no_prj");

        assertNull(datasetsService.describe(path).getDatasets().get(0).getNativeBounds().getCrs());

        // request import to force CRS as EPSG:2154
        dataset.getPublishing().setSrs("EPSG:2154");
        dataset.getPublishing().setSrsReproject(false);

        service.prepareBackend(job, user);
        service.importDataset(dataset, user, importProgressListener);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(user));
        String origTypeName = dataset.getName();
        String importedName = dataset.getPublishing().getImportedName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(origTypeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(importedName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));
            assertNull(orig.getSchema().getCoordinateReferenceSystem());
            CoordinateReferenceSystem importedCRS = imported.getSchema().getCoordinateReferenceSystem();
            assertNotNull(importedCRS);
            assertEquals(CRS.decode("EPSG:2154"), importedCRS);
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }

    public @Test void importDataset_Shapefile_override_native_crs_with_request_srs() throws Exception {

        Path path = multipartSupport.datafeederTestFile("states_4326.shp");
        dataset.setAbsolutePath(path.toString());
        dataset.setName("states_4326");

        CoordinateReferenceSystemMetadata crs = datasetsService.describe(path).getDatasets().get(0).getNativeBounds()
                .getCrs();
        String srs = crs.getSrs();
        assertEquals("EPSG:4326", srs);

        // import as EPSG:4327, overriding the native 4326 crs
        dataset.getPublishing().setSrs("EPSG:4327");
        dataset.getPublishing().setSrsReproject(false);

        service.prepareBackend(job, user);
        service.importDataset(dataset, user, importProgressListener);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(user));
        String typeName = dataset.getName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(typeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(typeName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));

            CoordinateReferenceSystem origCRS = orig.getSchema().getCoordinateReferenceSystem();
            CoordinateReferenceSystem importedCRS = imported.getSchema().getCoordinateReferenceSystem();
            assertNotNull(origCRS);
            assertNotNull(importedCRS);
            assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:4326"), origCRS));
            assertEquals(CRS.decode("EPSG:4327"), importedCRS);
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }

    public @Test void importDataset_Shapefile_known_native_crs() throws Exception {

        Path path = multipartSupport.datafeederTestFile("states_4326.shp");
        dataset.setAbsolutePath(path.toString());
        dataset.setName("states_4326");

        CoordinateReferenceSystemMetadata crs = datasetsService.describe(path).getDatasets().get(0).getNativeBounds()
                .getCrs();
        String srs = crs.getSrs();
        assertEquals("EPSG:4326", srs);

        dataset.getPublishing().setSrs("EPSG:4326");

        service.prepareBackend(job, user);
        service.importDataset(dataset, user, importProgressListener);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(user));
        String typeName = dataset.getName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(typeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(typeName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));
            CoordinateReferenceSystem origCRS = orig.getSchema().getCoordinateReferenceSystem();
            CoordinateReferenceSystem importedCRS = imported.getSchema().getCoordinateReferenceSystem();
            assertNotNull(origCRS);
            assertNotNull(importedCRS);
            CoordinateReferenceSystem ogcWGS84 = CRS.decode("EPSG:4326");
            assertTrue(CRS.equalsIgnoreMetadata(ogcWGS84, origCRS));
            assertEquals(ogcWGS84, importedCRS);
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }

    public @Test void importDataSetCsv_attributes_only_strategy() throws Exception {

        Path path = multipartSupport.datafeederTestFile("covoiturage.csv");
        dataset.setAbsolutePath(path.toString());
        dataset.setName("covoiturage");
        dataset.setFormat(DataSourceMetadata.DataSourceType.CSV);

        CoordinateReferenceSystemMetadata crs = datasetsService.describe(path).getDatasets().get(0).getNativeBounds()
                .getCrs();
        assertNull("CRS expected to be null", crs);

        dataset.getPublishing().setSrs("EPSG:4326");

        service.prepareBackend(job, user);
        service.importDataset(dataset, user, importProgressListener);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(user));
        String typeName = dataset.getName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(typeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(typeName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));
            CoordinateReferenceSystem origCRS = orig.getSchema().getCoordinateReferenceSystem();
            CoordinateReferenceSystem importedCRS = imported.getSchema().getCoordinateReferenceSystem();
            assertNull(origCRS);
            assertNull(importedCRS);
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }

    public @Test void importDataSetCsv_provide_lat_long_columns() throws Exception {

        Path path = multipartSupport.datafeederTestFile("covoiturage.csv");
        dataset.setAbsolutePath(path.toString());
        dataset.setName("covoiturage");
        dataset.setFormat(DataSourceMetadata.DataSourceType.CSV);
        PublishSettings publishSettings = new PublishSettings();
        publishSettings.setOptions(Map.of("latField", "Ylat", //
                "lngField", "Xlong", //
                "quoteChar", "\"", //
                "delimiter", ","//
        ));
        dataset.setPublishing(publishSettings);

        CoordinateReferenceSystemMetadata crs = datasetsService.describe(path).getDatasets().get(0).getNativeBounds()
                .getCrs();
        assertNull("CRS expected to be null", crs);

        dataset.getPublishing().setSrs("EPSG:4326");

        service.prepareBackend(job, user);
        service.importDataset(dataset, user, importProgressListener);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(user));
        String typeName = dataset.getName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(typeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(typeName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));
            SimpleFeatureIterator fi = orig.getFeatures().features();
            SimpleFeature feat = fi.next();
            Object geom = feat.getDefaultGeometry();
            assertNotNull("expected non-null geometry from the CSV", geom);
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }

}
