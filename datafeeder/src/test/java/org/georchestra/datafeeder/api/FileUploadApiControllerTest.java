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
package org.georchestra.datafeeder.api;

import static org.georchestra.datafeeder.model.JobStatus.DONE;
import static org.georchestra.datafeeder.model.JobStatus.ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.geotools.geojson.feature.FeatureJSON;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = { DataFeederApplicationConfiguration.class }, webEnvironment = WebEnvironment.MOCK)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test", "mock" })
public class FileUploadApiControllerTest {

    private @Autowired ApiTestSupport testSupport;
    private @Autowired DataUploadService uploadService;

    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private @Autowired FileUploadApi controller;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUploadFiles_SingleShapefile() {

        List<MultipartFile> shapefileFiles = multipartSupport.archSitesShapefile();

        testSupport.uploadAndWaitForSuccess(shapefileFiles, "archsites");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUploadFiles_ZipfileWithMultipleShapefiles() throws IOException {
        List<MultipartFile> archsites = multipartSupport.archSitesShapefile();
        List<MultipartFile> bugsites = multipartSupport.bugSitesShapefile();
        List<MultipartFile> roads = multipartSupport.roadsShapefile();
        List<MultipartFile> statepop = multipartSupport.statePopShapefile();
        List<MultipartFile> chinesePoly = multipartSupport.chinesePolyShapefile();

        MultipartFile zipFile = multipartSupport.createZipFile("shapefiles.zip", archsites, bugsites, roads, statepop,
                chinesePoly);

        List<MultipartFile> uploadedFiles = Collections.singletonList(zipFile);
        testSupport.uploadAndWaitForSuccess(uploadedFiles, "archsites", "bugsites", "roads", "statepop",
                "chinese_poly");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUploadFiles_TwoZipFilesWithShapefiles() throws IOException {

        List<MultipartFile> archsites = multipartSupport.archSitesShapefile();
        List<MultipartFile> bugsites = multipartSupport.bugSitesShapefile();
        List<MultipartFile> roads = multipartSupport.roadsShapefile();
        List<MultipartFile> statepop = multipartSupport.statePopShapefile();
        List<MultipartFile> chinesePoly = multipartSupport.chinesePolyShapefile();

        MultipartFile zipFile1 = multipartSupport.createZipFile("zipfile1.zip", archsites, bugsites);
        MultipartFile zipFile2 = multipartSupport.createZipFile("zipfile2.zip", roads, statepop, chinesePoly);

        List<MultipartFile> uploadedFiles = Arrays.asList(zipFile1, zipFile2);

        testSupport.uploadAndWaitForSuccess(uploadedFiles, "archsites", "bugsites", "roads", "statepop",
                "chinese_poly");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUploadFiles_CorruptShapefile() {
        List<MultipartFile> received = Arrays.asList(//
                multipartSupport.createFakeFile("test.shp", 4096), //
                multipartSupport.createFakeFile("test.shx", 1024), //
                multipartSupport.createFakeFile("test.prj", 128), //
                multipartSupport.createFakeFile("test.dbf", 1024)//
        );

        ResponseEntity<UploadJobStatus> response = controller.uploadFiles(received);

        assertEquals(ACCEPTED, response.getStatusCode());
        UploadJobStatus initialStatus = response.getBody();
        assertEquals(AnalysisStatusEnum.PENDING, initialStatus.getStatus());
        assertTrue(initialStatus.getDatasets().isEmpty());

        UUID id = initialStatus.getJobId();
        DataUploadJob job = testSupport.awaitUntilJobIsOneOf(id, 3, ERROR);
        job = uploadService.findJob(job.getJobId()).orElse(null);
        assertEquals(1, job.getDatasets().size());
        assertEquals("failed job should report full progress", 1d, job.getProgress(), 0d);
        assertNotNull(job.getError());

        testSupport.assertDataset(job.getDatasets(), "test", ERROR);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUploadFiles_ZipfileWithMultipleShapefiles_SomeFilesCorrupted() throws IOException {
        MultipartFile zipFile;
        {
            List<MultipartFile> corruptFile = Arrays.asList(//
                    multipartSupport.createFakeFile("test.shp", 4096), //
                    multipartSupport.createFakeFile("test.shx", 1024), //
                    multipartSupport.createFakeFile("test.prj", 128), //
                    multipartSupport.createFakeFile("test.dbf", 1024)//
            );
            List<MultipartFile> archsites = multipartSupport.archSitesShapefile();
            List<MultipartFile> bugsites = multipartSupport.bugSitesShapefile();

            zipFile = multipartSupport.createZipFile("shapefiles.zip", archsites, bugsites, corruptFile);
        }

        final List<MultipartFile> received = Collections.singletonList(zipFile);
        final ResponseEntity<UploadJobStatus> response = controller.uploadFiles(received);

        assertEquals(ACCEPTED, response.getStatusCode());
        UploadJobStatus initialStatus = response.getBody();
        assertEquals(AnalysisStatusEnum.PENDING, initialStatus.getStatus());
        assertTrue(initialStatus.getDatasets().isEmpty());

        final UUID id = initialStatus.getJobId();
        testSupport.awaitUntilJobDatasetUploadStatusIs(id, "test", 3, ERROR);

        DataUploadJob job = testSupport.awaitUntilJobIsOneOf(id, 3, ERROR);
        job = this.uploadService.findJob(job.getJobId()).orElse(null);
        assertEquals(3, job.getDatasets().size());
        assertEquals("failed job should report full progress", 1d, job.getProgress(), 0d);
        assertNotNull(job.getError());

        testSupport.assertDataset(job.getDatasets(), "archsites", DONE);
        testSupport.assertDataset(job.getDatasets(), "bugsites", DONE);
        testSupport.assertDataset(job.getDatasets(), "test", ERROR);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testFindUploadJob() {
        UploadJobStatus archsitesJob = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus statepopJob = testSupport.upload(multipartSupport.statePopShapefile());

        ResponseEntity<UploadJobStatus> response;
        response = controller.findUploadJob(archsitesJob.getJobId());
        assertEquals(archsitesJob.getJobId(), response.getBody().getJobId());

        response = controller.findUploadJob(statepopJob.getJobId());
        assertEquals(statepopJob.getJobId(), response.getBody().getJobId());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testFindUploadJob_non_existent_job_id_returns_404() {
        UUID invalidId = UUID.randomUUID();
        try {
            controller.findUploadJob(invalidId);
            fail("expected NOT_FOUND exception");
        } catch (ApiException expected) {
            assertEquals(NOT_FOUND, expected.getStatus());
        }
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMINISTRATOR")
    public void testFindAllUploadJobs() {
        testSupport.setCallingUser("user1", "USER");
        UploadJobStatus user1Job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user1Job2 = testSupport.upload(multipartSupport.roadsShapefile());

        testSupport.setCallingUser("user2", "USER");
        UploadJobStatus user2Job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user2Job2 = testSupport.upload(multipartSupport.chinesePolyShapefile());

        testSupport.setCallingUser("testadmin", "ADMINISTRATOR");
        ResponseEntity<List<UploadJobStatus>> response = controller.findAllUploadJobs();
        assertEquals(OK, response.getStatusCode());

        Set<UUID> expected = Arrays.asList(user1Job1, user1Job2, user2Job1, user2Job2).stream()
                .map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        Set<UUID> actual = response.getBody().stream().map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        assertTrue(actual.containsAll(expected));// actual may have more elements from other test cases
    }

    @Test
    public void testFindUserUploadJobs_returns_only_calling_user_jobs() {
        testSupport.setCallingUser("user1", "USER");
        UploadJobStatus user1Job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user1Job2 = testSupport.upload(multipartSupport.roadsShapefile());

        testSupport.setCallingUser("user2", "USER");
        UploadJobStatus user2Job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user2Job2 = testSupport.upload(multipartSupport.chinesePolyShapefile());

        testSupport.setCallingUser("user1", "USER");
        assertUserJobs(user1Job1, user1Job2);

        testSupport.setCallingUser("user2", "USER");
        assertUserJobs(user2Job1, user2Job2);

        testSupport.setCallingUser("user3", "USER");
        assertUserJobs();
    }

    private void assertUserJobs(UploadJobStatus... expectedUserJobs) {
        ResponseEntity<List<UploadJobStatus>> response = controller.findUserUploadJobs();
        assertEquals(OK, response.getStatusCode());
        List<UploadJobStatus> jobs = response.getBody();
        Set<UUID> expected = Arrays.stream(expectedUserJobs).map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        Set<UUID> actual = jobs.stream().map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        assertThat(actual, Matchers.hasItems(expected.toArray(new UUID[expectedUserJobs.length])));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testRemoveJob_ok_when_job_is_done() {
        UploadJobStatus job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus job2 = testSupport.upload(multipartSupport.roadsShapefile());
        UUID id1 = job1.getJobId();
        UUID id2 = job2.getJobId();

        testSupport.awaitUntilJobIsOneOf(id1, 3, DONE);
        testSupport.awaitUntilJobIsOneOf(id2, 3, DONE);

        final Boolean abort = null;

        assertEquals(OK, controller.findUploadJob(id1).getStatusCode());
        assertEquals(OK, controller.removeJob(id1, abort).getStatusCode());
        try {
            controller.findUploadJob(id1);
            fail("expected NOT_FOUND exception");
        } catch (ApiException expected) {
            assertEquals(NOT_FOUND, expected.getStatus());
        }

        assertEquals(OK, controller.findUploadJob(id2).getStatusCode());
        assertEquals(OK, controller.removeJob(id2, abort).getStatusCode());
        try {
            controller.findUploadJob(id2);
            fail("expected NOT_FOUND exception");
        } catch (ApiException expected) {
            assertEquals(NOT_FOUND, expected.getStatus());
        }
    }

    @Ignore("waiting for implementation of remove as part of GSGEODPT43-88")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testRemoveJob_ok_when_running_and_abort_is_true() {
        UploadJobStatus job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus job2 = testSupport.upload(multipartSupport.roadsShapefile());
        UUID id1 = job1.getJobId();
        UUID id2 = job2.getJobId();

        final Boolean abort = true;
        assertEquals(OK, controller.removeJob(id1, abort));
        assertEquals(OK, controller.removeJob(id2, abort));

        assertEquals(NOT_FOUND, controller.findUploadJob(id1).getStatusCode());
        assertEquals(NOT_FOUND, controller.findUploadJob(id2).getStatusCode());
    }

    @Ignore("waiting for implementation of remove as part of GSGEODPT43-88")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testRemoveJob_conflict_if_running_and_abort_not_specified() {
        UploadJobStatus job1 = testSupport.upload(multipartSupport.archSitesShapefile());
        UploadJobStatus job2 = testSupport.upload(multipartSupport.roadsShapefile());
        UUID id1 = job1.getJobId();
        UUID id2 = job2.getJobId();

        final Boolean abort = true;
        assertEquals(OK, controller.removeJob(id1, abort));
        assertEquals(OK, controller.removeJob(id2, abort));

        assertEquals(NOT_FOUND, controller.findUploadJob(id1).getStatusCode());
        assertEquals(NOT_FOUND, controller.findUploadJob(id2).getStatusCode());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testRemoveJob_forbidden_when_job_is_owned_by_another_user() {
        UploadJobStatus job = testSupport.upload(multipartSupport.archSitesShapefile());
        testSupport.setCallingUser("user2", "USER", "SOMEOTHERROLE");
        final Boolean abort = true;
        try {
            controller.removeJob(job.getJobId(), abort);
            fail("expected forbidden");
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(ApiException.class));
            assertEquals(FORBIDDEN, ((ApiException) e).getStatus());
        }
    }

    @Ignore("waiting for implementation of remove as part of GSGEODPT43-88")
    @Test
    public void testRemoveJob_administrator_can_remove_other_users_jobs() {
        testSupport.setCallingUser("testuser", "USER", "SOMEOTHERROLE");
        UploadJobStatus job = testSupport.upload(multipartSupport.archSitesShapefile());

        testSupport.setCallingUser("testadmin", "ADMINISTRATOR", "SOMEOTHERROLE");
        final Boolean abort = true;
        ResponseEntity<Void> response = controller.removeJob(job.getJobId(), abort);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testSampleFeatureEncoding() throws IOException {
        List<MultipartFile> uploadFiles = multipartSupport.chinesePolyShapefile();
        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(uploadFiles, "chinese_poly");
        DatasetUploadState dataset = upload.getDatasets().get(0);
        assertEquals("Expected default shapefile encoding when no .cpg file is provided", "ISO-8859-1",
                dataset.getEncoding());
        // correct chinese_poly's dbf charset: GB18030, NAME: 黑龙江省
        final String encoding = "GB18030";
        ResponseEntity<Object> response = controller.getSampleFeature(upload.getJobId(), "chinese_poly", 0, encoding,
                null, false);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String geoJsonFeature = response.getBody().toString();

        FeatureJSON decoder = new FeatureJSON();
        SimpleFeature feature = decoder.readFeature(new StringReader(geoJsonFeature));
        assertThat(feature.getDefaultGeometry(), Matchers.instanceOf(MultiPolygon.class));
        assertEquals(230000L, feature.getAttribute("ADCODE93"));
        assertEquals("黑龙江省", feature.getAttribute("NAME"));
    }

    /**
     * Verify {shapefile}.cpg code-page file is automatically detected if uploaded
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testSampleFeatureEncodingDetected() throws IOException {
        List<MultipartFile> uploadFiles = multipartSupport.chinesePolyShapefile();
        // correct chinese_poly's dbf charset: GB18030, NAME: 黑龙江省
        uploadFiles.add(
                multipartSupport.createMultipartFile("chinese_poly.cpg", "GB18030".getBytes(StandardCharsets.UTF_8)));

        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(uploadFiles, "chinese_poly");
        DatasetUploadState dataset = upload.getDatasets().get(0);
        assertEquals("encoding from .cpg file not detected", "GB18030", dataset.getEncoding());

        final String encodingParam = null;
        ResponseEntity<Object> response = controller.getSampleFeature(upload.getJobId(), "chinese_poly", 0,
                encodingParam, null, false);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String geoJsonFeature = response.getBody().toString();

        FeatureJSON decoder = new FeatureJSON();
        SimpleFeature feature = decoder.readFeature(new StringReader(geoJsonFeature));
        assertEquals("Auto-detected GB18030 charset from .cpg file not respected", "黑龙江省",
                feature.getAttribute("NAME"));
    }
}
