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
package org.georchestra.datafeeder.batch.analysis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.georchestra.datafeeder.batch.analysis.BatchTestConfiguration.UploadJobLauncherTestUtils;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.Organization;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.georchestra.datafeeder.service.DataFeederServiceConfiguration;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = { BatchTestConfiguration.class,
        DataFeederServiceConfiguration.class }, webEnvironment = WebEnvironment.NONE)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test", "mock" })
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UploadAnalysisJobConfigurationTest {

    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private @Autowired UploadJobLauncherTestUtils jobLauncherTestUtils;

    private @Autowired DataUploadService uploadService;
    private @Autowired FileStorageService storageService;
    private @Autowired DataUploadJobRepository repository;
    private @Autowired DatasetUploadStateRepository datasetRepository;

    private Map<String, JobParameter> jobParameters;

    private UserInfo testUser;

    public @Before void before() {
        jobParameters = new HashMap<>();
        testUser = new UserInfo();
        Organization org = new Organization();
        org.setShortName("test-org");
        testUser.setOrganization(org);
    }

    private JobParameters jobParameters() {
        return new JobParameters(this.jobParameters);
    }

    @Test
    public void step1_ReadUploadPack_UploadId_Not_Provided() {
        JobExecution execution = readUploadPack(null);
        ExitStatus exitStatus = execution.getExitStatus();
        assertEquals("FAILED", exitStatus.getExitCode());
        assertThat(exitStatus.getExitDescription(),
                containsString("Job parameter not provided: " + UploadAnalysisJobConfiguration.JOB_PARAM_NAME));
    }

    @Test
    public void step1_ReadUploadPack_single_shapefile() throws IOException {
        List<MockMultipartFile> received = multipartSupport.roadsShapefile();
        UUID uploadId = storageService.createPackageFromUpload(received).getId();
        DataUploadJob initial = uploadService.createJob(uploadId, "testuser");
        assertEquals(JobStatus.PENDING, initial.getAnalyzeStatus());

        JobExecution execution = readUploadPack(uploadId);
        ExitStatus exitStatus = execution.getExitStatus();
        assertEquals(ExitStatus.COMPLETED, exitStatus);

        Optional<DataUploadJob> saved = repository.findByJobId(uploadId);
        assertTrue(saved.isPresent());
        DataUploadJob state = saved.get();

        assertEquals(uploadId, state.getJobId());
        assertEquals(JobStatus.RUNNING, state.getAnalyzeStatus());
        assertEquals(1, state.getDatasets().size());

        DatasetUploadState dset = state.getDatasets().get(0);
        assertEquals(JobStatus.PENDING, dset.getAnalyzeStatus());
        assertTrue(Files.exists(Paths.get(dset.getAbsolutePath())));
        assertNotNull(dset.getFileName());
        assertNotNull(dset.getName());

        List<DatasetUploadState> dsets = datasetRepository.findAllByJobId(uploadId);
        assertEquals(1, dsets.size());
        dset = dsets.get(0);
        assertEquals(JobStatus.PENDING, dset.getAnalyzeStatus());
        assertTrue(Files.exists(Paths.get(dset.getAbsolutePath())));
        assertNotNull(dset.getFileName());
        assertNotNull(dset.getName());
    }

    @Test
    public void analyze_single_shapefile() throws Exception {
        List<MockMultipartFile> received = multipartSupport.roadsShapefile();
        UUID uploadId = storageService.createPackageFromUpload(received).getId();
        DataUploadJob initial = uploadService.createJob(uploadId, "testuser");
        assertEquals(JobStatus.PENDING, initial.getAnalyzeStatus());

        JobExecution execution = launchJob(uploadId);
        ExitStatus exitStatus = execution.getExitStatus();
        assertEquals(ExitStatus.COMPLETED, exitStatus);

        Optional<DataUploadJob> saved = repository.findByJobId(uploadId);
        assertTrue(saved.isPresent());
        DataUploadJob state = saved.get();

        assertEquals(uploadId, state.getJobId());
        assertEquals(JobStatus.DONE, state.getAnalyzeStatus());
        assertEquals(1, state.getDatasets().size());

        DatasetUploadState dset = state.getDatasets().get(0);
        assertEquals(JobStatus.DONE, dset.getAnalyzeStatus());
        assertTrue(Files.exists(Paths.get(dset.getAbsolutePath())));
        assertNotNull(dset.getFileName());
        assertNotNull(dset.getName());

        List<DatasetUploadState> dsets = datasetRepository.findAllByJobId(uploadId);
        assertEquals(1, dsets.size());
        dset = dsets.get(0);
        assertEquals(JobStatus.DONE, dset.getAnalyzeStatus());
        assertTrue(Files.exists(Paths.get(dset.getAbsolutePath())));
        assertNotNull(dset.getFileName());
        assertNotNull(dset.getName());
    }

    @Test
    public void analyze_zipfile_multiple_shapefiles() throws Exception {
        List<MockMultipartFile> roads = multipartSupport.roadsShapefile();
        List<MockMultipartFile> states = multipartSupport.statePopShapefile();
        List<MockMultipartFile> chinesePoly = multipartSupport.chinesePolyShapefile();

        MultipartFile received = multipartSupport.createZipFile("test upload.zip", roads, states, chinesePoly);

        UUID uploadId = storageService.createPackageFromUpload(Collections.singletonList(received)).getId();
        DataUploadJob initial = uploadService.createJob(uploadId, "testuser");
        assertEquals(JobStatus.PENDING, initial.getAnalyzeStatus());

        JobExecution execution = launchJob(uploadId);
        ExitStatus exitStatus = execution.getExitStatus();
        assertEquals(ExitStatus.COMPLETED, exitStatus);

        Optional<DataUploadJob> saved = repository.findByJobId(uploadId);
        assertTrue(saved.isPresent());
        DataUploadJob state = saved.get();

        assertEquals(uploadId, state.getJobId());
        assertEquals(JobStatus.DONE, state.getAnalyzeStatus());
        assertEquals(3, state.getDatasets().size());

        List<DatasetUploadState> dsets = datasetRepository.findAllByJobId(uploadId);
        assertEquals(3, dsets.size());
        for (DatasetUploadState dset : dsets) {
            dset = dsets.get(0);
            assertEquals(JobStatus.DONE, dset.getAnalyzeStatus());
            assertTrue(Files.exists(Paths.get(dset.getAbsolutePath())));
            assertNotNull(dset.getFileName());
            assertNotNull(dset.getName());
        }
    }

    @Test
    public void dataUploadAnalysisService_analyzeCsvAsB64Test() throws Exception {
        String testCsvPath = Paths.get(this.getClass().getResource("covoit-mel.csv").toURI()).toString();

        Map<String, String> params = uploadService.getAnalysisService().analyzeCsv(testCsvPath);

        assertTrue("params contain expected key 'quoteChar'", params.containsKey("quoteChar"));
        assertTrue("params contain expected key 'delimiter'", params.containsKey("delimiter"));
        assertTrue("base-64 encoded csv does not correspond to the expected value",
                params.get("csv").startsWith("ImlkX2xpZXUiLCJpZF9sb2NhbCI"));
    }

    private JobExecution readUploadPack(UUID uploadId) {
        if (uploadId != null) {
            String parameter = uploadId.toString();
            boolean identifying = true;
            this.jobParameters.put(UploadAnalysisJobConfiguration.JOB_PARAM_NAME,
                    new JobParameter(parameter, identifying));
        }
        JobParameters params = jobParameters();
        JobExecution execution = jobLauncherTestUtils.launchStep("initializeDataUploadState", params);
        return execution;
    }

    private JobExecution launchJob(UUID uploadId) throws Exception {
        if (uploadId != null) {
            String parameter = uploadId.toString();
            boolean identifying = true;
            this.jobParameters.put(UploadAnalysisJobConfiguration.JOB_PARAM_NAME,
                    new JobParameter(parameter, identifying));
        }
        JobParameters params = jobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        return execution;
    }
}
