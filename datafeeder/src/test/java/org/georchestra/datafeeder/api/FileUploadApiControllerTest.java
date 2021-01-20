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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.georchestra.datafeeder.model.AnalysisStatus.ANALYZING;
import static org.georchestra.datafeeder.model.AnalysisStatus.DONE;
import static org.georchestra.datafeeder.model.AnalysisStatus.ERROR;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.AnalysisStatus;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;

@SpringBootTest(classes = { DataFeederApplicationConfiguration.class }, webEnvironment = WebEnvironment.MOCK)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class FileUploadApiControllerTest {

    private @Autowired DataUploadService uploadService;

    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private @Autowired FileUploadApi controller;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUploadFiles_SingleShapefile() {

        List<MultipartFile> shapefileFiles = multipartSupport.archSitesShapefile();

        testUploadSuccess(shapefileFiles, "archsites");
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
        testUploadSuccess(uploadedFiles, "archsites", "bugsites", "roads", "statepop", "chinese_poly");
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

        testUploadSuccess(uploadedFiles, "archsites", "bugsites", "roads", "statepop", "chinese_poly");
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
        assertEquals(UploadJobStatus.StatusEnum.PENDING, initialStatus.getStatus());
        assertTrue(initialStatus.getDatasets().isEmpty());

        UUID id = initialStatus.getJobId();
        DataUploadJob job = awaitUntilJobIsOneOf(id, 3, ERROR);
        job = uploadService.findJob(job.getJobId()).orElse(null);
        assertEquals(1, job.getDatasets().size());
        assertEquals("failed job should report full progress", 1d, job.getProgress(), 0d);
        assertNotNull(job.getError());

        assertDataset(job.getDatasets(), "test", ERROR);
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
        assertEquals(UploadJobStatus.StatusEnum.PENDING, initialStatus.getStatus());
        assertTrue(initialStatus.getDatasets().isEmpty());

        final UUID id = initialStatus.getJobId();
        awaitUntilJobDatasetUploadStatusIs(id, "test", 3, ERROR);

        DataUploadJob job = awaitUntilJobIsOneOf(id, 3, ERROR);
        job = this.uploadService.findJob(job.getJobId()).orElse(null);
        assertEquals(3, job.getDatasets().size());
        assertEquals("failed job should report full progress", 1d, job.getProgress(), 0d);
        assertNotNull(job.getError());

        assertDataset(job.getDatasets(), "test", ERROR);
        assertDataset(job.getDatasets(), "archsites", DONE);
        assertDataset(job.getDatasets(), "bugsites", DONE);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testFindUploadJob() {
        UploadJobStatus archsitesJob = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus statepopJob = upload(multipartSupport.statePopShapefile());

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

    private UploadJobStatus upload(List<MultipartFile> files) {
        ResponseEntity<UploadJobStatus> response = controller.uploadFiles(files);
        assertEquals(ACCEPTED, response.getStatusCode());
        return response.getBody();
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMINISTRATOR")
    public void testFindAllUploadJobs() {
        setCallingUser("user1", "USER");
        UploadJobStatus user1Job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user1Job2 = upload(multipartSupport.roadsShapefile());

        setCallingUser("user2", "USER");
        UploadJobStatus user2Job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user2Job2 = upload(multipartSupport.chinesePolyShapefile());

        setCallingUser("testadmin", "ADMINISTRATOR");
        ResponseEntity<List<UploadJobStatus>> response = controller.findAllUploadJobs();
        assertEquals(OK, response.getStatusCode());

        Set<UUID> expected = Arrays.asList(user1Job1, user1Job2, user2Job1, user2Job2).stream()
                .map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        Set<UUID> actual = response.getBody().stream().map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        assertTrue(actual.containsAll(expected));// actual may have more elements from other test cases
    }

    @Test
    public void testFindUserUploadJobs_returns_only_calling_user_jobs() {
        setCallingUser("user1", "USER");
        UploadJobStatus user1Job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user1Job2 = upload(multipartSupport.roadsShapefile());

        setCallingUser("user2", "USER");
        UploadJobStatus user2Job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus user2Job2 = upload(multipartSupport.chinesePolyShapefile());

        setCallingUser("user1", "USER");
        assertUserJobs(user1Job1, user1Job2);

        setCallingUser("user2", "USER");
        assertUserJobs(user2Job1, user2Job2);

        setCallingUser("user3", "USER");
        assertUserJobs();
    }

    private void assertUserJobs(UploadJobStatus... expectedUserJobs) {
        ResponseEntity<List<UploadJobStatus>> response = controller.findUserUploadJobs();
        assertEquals(OK, response.getStatusCode());
        List<UploadJobStatus> jobs = response.getBody();
        Set<UUID> expected = Arrays.stream(expectedUserJobs).map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        Set<UUID> actual = jobs.stream().map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    private void setCallingUser(@NonNull String username, @NonNull String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles).map(role -> {
            assertFalse(role.startsWith("ROLE_"));
            return new SimpleGrantedAuthority("ROLE_" + role);
        }).collect(Collectors.toList());

        Authentication auth = new TestingAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testRemoveJob_ok_when_job_is_done() {
        UploadJobStatus job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus job2 = upload(multipartSupport.roadsShapefile());
        UUID id1 = job1.getJobId();
        UUID id2 = job2.getJobId();

        awaitUntilJobIsOneOf(id1, 3, DONE);
        awaitUntilJobIsOneOf(id2, 3, DONE);

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

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testRemoveJob_ok_when_running_and_abort_is_true() {
        UploadJobStatus job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus job2 = upload(multipartSupport.roadsShapefile());
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
    public void testRemoveJob_conflict_if_running_and_abort_not_specified() {
        UploadJobStatus job1 = upload(multipartSupport.archSitesShapefile());
        UploadJobStatus job2 = upload(multipartSupport.roadsShapefile());
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
        UploadJobStatus job = upload(multipartSupport.archSitesShapefile());
        setCallingUser("user2", "USER", "SOMEOTHERROLE");
        final Boolean abort = true;
        ResponseEntity<Void> response = controller.removeJob(job.getJobId(), abort);
        assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testRemoveJob_administrator_can_remove_other_users_jobs() {
        setCallingUser("testuser", "USER", "SOMEOTHERROLE");
        UploadJobStatus job = upload(multipartSupport.archSitesShapefile());

        setCallingUser("testadmin", "ADMINISTRATOR", "SOMEOTHERROLE");
        final Boolean abort = true;
        ResponseEntity<Void> response = controller.removeJob(job.getJobId(), abort);
        assertEquals(OK, response.getStatusCode());
    }

    private void testUploadSuccess(List<MultipartFile> uploadedFiles, String... expectedDatasetNames) {

        ResponseEntity<UploadJobStatus> response = controller.uploadFiles(uploadedFiles);

        assertEquals(ACCEPTED, response.getStatusCode());
        UploadJobStatus initialStatus = response.getBody();
        assertNotNull(initialStatus);
        assertNotNull(initialStatus.getJobId());
        assertEquals(UploadJobStatus.StatusEnum.PENDING, initialStatus.getStatus());
        assertNotNull(initialStatus.getDatasets());
        assertTrue(initialStatus.getDatasets().isEmpty());

        final UUID id = initialStatus.getJobId();
        awaitUntilJobIsOneOf(id, 1, ANALYZING, DONE);
        awaitUntilJobIsOneOf(id, 5, DONE);

        Optional<DataUploadJob> state = uploadService.findJob(id);
        assertTrue(state.isPresent());
        assertNull(state.get().getError());
        assertEquals(1d, state.get().getProgress(), 0d);
        List<DatasetUploadState> datasets = state.get().getDatasets();

        assertEquals(expectedDatasetNames.length, datasets.size());
        for (String datasetName : expectedDatasetNames) {
            assertDataset(datasets, datasetName, DONE);
        }
    }

    private void assertDataset(@NonNull List<DatasetUploadState> datasets, @NonNull String name,
            @NonNull AnalysisStatus expectedStatus) {

        DatasetUploadState datasetState = datasets.stream().filter(d -> name.equals(d.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Expected dataset not returned: " + name));
        datasetState.getEncoding();
        datasetState.getNativeBounds();
        datasetState.getSampleGeometryWKT();
        datasetState.getSampleProperties();
        assertEquals(expectedStatus, datasetState.getStatus());
        if (DONE == expectedStatus) {
            assertNotNull(datasetState.getEncoding());
            assertNull(datasetState.getError());
            assertNotNull(datasetState.getNativeBounds());
            assertNotNull(datasetState.getSampleGeometryWKT());
            assertNotNull(datasetState.getSampleProperties());
            assertFalse(datasetState.getSampleProperties().isEmpty());
            assertNotNull(datasetState.getNativeBounds());
            assertNotNull(datasetState.getNativeBounds().getCrs());
            assertNotNull(datasetState.getNativeBounds().getCrs().getWKT());
        } else if (ERROR == expectedStatus || ANALYZING == expectedStatus) {
            assertNull(datasetState.getEncoding());
            assertNull(datasetState.getError());
            assertNull(datasetState.getNativeBounds());
            assertNull(datasetState.getSampleGeometryWKT());
            assertNotNull(datasetState.getSampleProperties());
            assertTrue(datasetState.getSampleProperties().isEmpty());
            assertNull(datasetState.getNativeBounds());
        }
    }

    private DataUploadJob awaitUntilJobIsOneOf(final UUID jobId, int seconds, AnalysisStatus... oneof) {
        final AtomicReference<DataUploadJob> jobStatus = new AtomicReference<>();
        await().atMost(seconds, SECONDS).untilAsserted(() -> {
            DataUploadJob jobState = uploadService.findJob(jobId).orElseThrow(NoSuchElementException::new);
            jobStatus.set(jobState);

            AnalysisStatus status = jobState.getStatus();
            List<Matcher<? super AnalysisStatus>> matchers;
            matchers = Arrays.stream(oneof).map(Matchers::equalTo).collect(Collectors.toList());
            assertThat(status, anyOf(matchers));
        });
        return jobStatus.get();
    }

    private DatasetUploadState awaitUntilJobDatasetUploadStatusIs(final UUID jobId, final String datasetName,
            final int seconds, final AnalysisStatus expectedStatus) {

        final AtomicReference<DatasetUploadState> datasetStatus = new AtomicReference<>();
        await().atMost(seconds, SECONDS).untilAsserted(() -> {
            DataUploadJob jobState = uploadService.findJob(jobId).orElseThrow(NoSuchElementException::new);
            List<DatasetUploadState> datasets = jobState.getDatasets();
            Optional<DatasetUploadState> dataset = datasets.stream().filter(ds -> datasetName.equals(ds.getName()))
                    .findFirst();
            if (dataset.isPresent()) {
                DatasetUploadState state = dataset.get();
                datasetStatus.set(state);
                assertThat(state, equalTo(expectedStatus));
            }
        });
        return datasetStatus.get();
    }
}
