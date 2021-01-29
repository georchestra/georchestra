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
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.model.AnalysisStatus;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.service.DataPublishingService;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;

@Service
public class ApiTestSupport {

    private @Autowired DataUploadService uploadService;
    private @Autowired DataPublishingService publishingService;

    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private @Autowired FileUploadApi uploadController;
    private @Autowired DataPublishingApi publishingController;

    public void setCallingUser(@NonNull String username, @NonNull String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles).map(role -> {
            assertFalse(role.startsWith("ROLE_"));
            return new SimpleGrantedAuthority("ROLE_" + role);
        }).collect(Collectors.toList());

        Authentication auth = new TestingAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public UploadJobStatus upload(List<MultipartFile> files) {
        ResponseEntity<UploadJobStatus> response = uploadController.uploadFiles(files);
        assertEquals(ACCEPTED, response.getStatusCode());
        return response.getBody();
    }

    public void assertUserJobs(UploadJobStatus... expectedUserJobs) {
        ResponseEntity<List<UploadJobStatus>> response = uploadController.findUserUploadJobs();
        assertEquals(OK, response.getStatusCode());
        List<UploadJobStatus> jobs = response.getBody();
        Set<UUID> expected = Arrays.stream(expectedUserJobs).map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        Set<UUID> actual = jobs.stream().map(UploadJobStatus::getJobId).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    public DataUploadJob uploadAndWaitForSuccess(List<MultipartFile> uploadedFiles, String... expectedDatasetNames) {

        ResponseEntity<UploadJobStatus> response = uploadController.uploadFiles(uploadedFiles);

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
        return state.get();
    }

    public void assertDataset(@NonNull List<DatasetUploadState> datasets, @NonNull String name,
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
        } else if (ERROR == expectedStatus) {
            assertNull(datasetState.getEncoding());
            assertNotNull(datasetState.getError());
            assertNull(datasetState.getNativeBounds());
            assertNull(datasetState.getSampleGeometryWKT());
            assertNotNull(datasetState.getSampleProperties());
            assertTrue(datasetState.getSampleProperties().isEmpty());
            assertNull(datasetState.getNativeBounds());
        }
    }

    public DataUploadJob awaitUntilJobIsOneOf(final UUID jobId, int seconds, AnalysisStatus... oneof) {
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

    public DatasetUploadState awaitUntilJobDatasetUploadStatusIs(final UUID jobId, final String datasetName,
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
                assertThat(state.getStatus(), equalTo(expectedStatus));
            }
        });
        return datasetStatus.get();
    }
}
