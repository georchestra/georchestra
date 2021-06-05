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

import static org.georchestra.datafeeder.api.PublishStatusEnum.DONE;
import static org.georchestra.datafeeder.api.PublishStatusEnum.PENDING;
import static org.georchestra.datafeeder.api.PublishStatusEnum.RUNNING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = { DataFeederApplicationConfiguration.class }, webEnvironment = WebEnvironment.MOCK)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test", "mock" })
public class DataPublishingApiControllerTest {

    private @Autowired ApiTestSupport testSupport;
    private @Autowired FileStorageService storageService;
    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private @Autowired DataPublishingApi controller;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testPublish_SingleShapefile() throws IOException {
        List<MockMultipartFile> shapefileFiles = multipartSupport.statePopShapefile();
        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(shapefileFiles, "statepop");

        DatasetUploadState dset = upload.getDatasets().get(0);
        DatasetPublishRequest dsetReq = buildRequest(dset);

        PublishRequest publishRequest = new PublishRequest().addDatasetsItem(dsetReq);

        ResponseEntity<PublishJobStatus> response = controller.publish(upload.getJobId(), publishRequest);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        PublishJobStatus publishJob = response.getBody();
        assertNotNull(publishJob);
        assertEquals(upload.getJobId(), publishJob.getJobId());
        assertTrue(publishJob.getStatus() == PENDING || publishJob.getStatus() == RUNNING);

        DatasetMetadata expectedMd = dsetReq.getMetadata();
        final String requestedPublishedName = dsetReq.getPublishedName() == null ? dset.getName()
                : dsetReq.getPublishedName();
        final String expectedPublishedName = requestedPublishedName + "_mock";
        final String expectedWorkspace = "mock_workspace";
        {
            DataUploadJob finalJob = testSupport.awaitUntilPublishStateIs(upload.getJobId(), 5, JobStatus.DONE);
            DatasetUploadState dsetFinal = finalJob.getDatasets().get(0);
            PublishSettings publishing = dsetFinal.getPublishing();
            assertNotNull(publishing.getMetadataRecordId());
            assertEquals(expectedPublishedName, publishing.getPublishedName());
            String expectedEncoding = dsetReq.getEncoding() == null ? dset.getEncoding() : dsetReq.getEncoding();

            assertEquals(expectedEncoding, publishing.getEncoding());

            assertEquals(expectedMd.getTitle(), publishing.getTitle());
            assertEquals(expectedMd.getAbstract(), publishing.getAbstract());
            assertEquals(expectedMd.getCreationDate(), publishing.getDatasetCreationDate());
            assertEquals(expectedMd.getCreationProcessDescription(), publishing.getDatasetCreationProcessDescription());
            assertEquals(expectedMd.getScale(), publishing.getScale());
            assertEquals(expectedMd.getTags(), publishing.getKeywords());
            assertEquals(dsetReq.getSrs(), publishing.getSrs());
        }
        {
            response = controller.getPublishingStatus(upload.getJobId());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            publishJob = response.getBody();
            assertEquals(upload.getJobId(), publishJob.getJobId());
            assertEquals(DONE, publishJob.getStatus());
            assertEquals(Double.valueOf(1d), publishJob.getProgress());
            assertEquals(1, publishJob.getDatasets().size());

            DatasetPublishingStatus actual = publishJob.getDatasets().get(0);
            assertEquals(DONE, actual.getStatus());
            assertNull(actual.getError());
            assertEquals(expectedMd.getTitle(), actual.getTitle());
            assertEquals(dset.getName(), actual.getNativeName());
            assertEquals(expectedPublishedName, actual.getPublishedName());
            assertEquals(expectedWorkspace, actual.getPublishedWorkspace());
            assertNotNull(actual.getMetadataRecordId());
        }

        try {
            this.storageService.find(upload.getJobId());
            fail("Expected FileNotFoundException, data on disk should have been removed once the job completed");
        } catch (FileNotFoundException expected) {
            assertTrue(true);
        }

    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testPublish_ValidateRequiredFields() {
        List<MockMultipartFile> shapefileFiles = multipartSupport.statePopShapefile();
        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(shapefileFiles, "statepop");

        // minimum required fields: nativeName, metadata.title, metadata.abstract
        DatasetPublishRequest dsetReq = new DatasetPublishRequest().metadata(new DatasetMetadata());
        PublishRequest publishRequest = new PublishRequest().addDatasetsItem(dsetReq);
        try {
            controller.publish(upload.getJobId(), publishRequest);
            fail("Expected ConstraintViolationException");
        } catch (ConstraintViolationException expected) {
            Set<ConstraintViolation<?>> constraintViolations = expected.getConstraintViolations();
            assertEquals(3, constraintViolations.size());
            assertThat(expected.getMessage(), containsString("datasets[0].nativeName: must not be null"));
            assertThat(expected.getMessage(), containsString("datasets[0].metadata.title: must not be null"));
            assertThat(expected.getMessage(), containsString("datasets[0].metadata.abstract: must not be null"));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testPublish_PublishesOnlySpecifiedDatasetOutOfMultipleUploadedInZipFile() throws IOException {
        MultipartFile zipFile;
        {
            List<MockMultipartFile> archsites = multipartSupport.archSitesShapefile();
            List<MockMultipartFile> bugsites = multipartSupport.bugSitesShapefile();
            List<MockMultipartFile> roads = multipartSupport.roadsShapefile();

            zipFile = multipartSupport.createZipFile("zipfile1.zip", archsites, bugsites, roads);
        }

        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(zipFile, "archsites", "bugsites", "roads");
        testPublishSingleDataset(upload, "bugsites");
    }

    /**
     * CRS use case:
     * <ul>
     * <li>DatasetPublishRequest does not indicate an
     * {@link DatasetPublishRequest#getSrs() SRS}
     * <li>Native CRS is known through
     * {@link CoordinateReferenceSystemMetadata#getSrs()
     * DatasetUploadStatet.getNativeBounds().getCrs().getSrs()} (i.e., a known EPSG
     * code has been recognized)
     * </ul>
     */
    @WithMockUser(username = "testuser", roles = "USER")
    public @Test void testPublish_SrsSetToNativeSrsIfNotProvidedInPublishRequest() {

        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(multipartSupport.statePopShapefile(), "statepop");

        DatasetUploadState dset = upload.getDatasets().get(0);

        DatasetPublishRequest dsetReq = buildRequest(dset);
        dsetReq.setSrs(null);

        PublishRequest publishRequest = new PublishRequest().addDatasetsItem(dsetReq);

        ResponseEntity<PublishJobStatus> response = controller.publish(upload.getJobId(), publishRequest);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        DataUploadJob finalJob = testSupport.awaitUntilPublishStateIs(upload.getJobId(), 5, JobStatus.DONE);
        DatasetUploadState dsetFinal = finalJob.getDatasets().get(0);
        PublishSettings publishing = dsetFinal.getPublishing();

        String expected = dset.getNativeBounds().getCrs().getSrs();
        assertEquals(expected, publishing.getSrs());
    }

    private void testPublishSingleDataset(final DataUploadJob upload, final String nativeName) {
        DatasetUploadState dsetToPublish = upload.getDataset(nativeName).orElseThrow(IllegalStateException::new);
        DatasetPublishRequest dsetReq = buildRequest(dsetToPublish);

        PublishRequest publishRequest = new PublishRequest().addDatasetsItem(dsetReq);

        ResponseEntity<PublishJobStatus> response = controller.publish(upload.getJobId(), publishRequest);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        PublishJobStatus publishJob = response.getBody();
        assertNotNull(publishJob);
        assertEquals(upload.getJobId(), publishJob.getJobId());
        assertTrue(publishJob.getStatus() == PENDING || publishJob.getStatus() == RUNNING);

        DataUploadJob finalJob = testSupport.awaitUntilPublishStateIs(upload.getJobId(), 5, JobStatus.DONE);
        List<DatasetUploadState> publishableDatasets = finalJob.getPublishableDatasets();
        assertEquals(1, publishableDatasets.size());
        assertEquals(nativeName, publishableDatasets.get(0).getName());

        {
            DatasetMetadata expectedMd = dsetReq.getMetadata();
            final String requestedPublishedName = dsetReq.getPublishedName() == null ? dsetToPublish.getName()
                    : dsetReq.getPublishedName();
            final String expectedPublishedName = requestedPublishedName + "_mock";
            final String expectedWorkspace = "mock_workspace";

            response = controller.getPublishingStatus(upload.getJobId());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            publishJob = response.getBody();
            assertEquals(upload.getJobId(), publishJob.getJobId());
            assertEquals(DONE, publishJob.getStatus());
            assertEquals(Double.valueOf(1d), publishJob.getProgress());
            assertEquals(upload.getDatasets().size(), publishJob.getDatasets().size());

            DatasetPublishingStatus actual = publishJob.getDatasets().stream()
                    .filter(d -> d.getNativeName().equals(nativeName)).findFirst()
                    .orElseThrow(IllegalStateException::new);

            assertEquals(DONE, actual.getStatus());
            assertNull(actual.getError());
            assertEquals(expectedMd.getTitle(), actual.getTitle());
            assertEquals(dsetToPublish.getName(), actual.getNativeName());
            assertEquals(expectedPublishedName, actual.getPublishedName());
            assertEquals(expectedWorkspace, actual.getPublishedWorkspace());
            assertNotNull(actual.getMetadataRecordId());
        }

    }

    private DatasetPublishRequest buildRequest(DatasetUploadState dset) {
        DatasetPublishRequest dsetReq = new DatasetPublishRequest();
        dsetReq.setNativeName(dset.getName());
        dsetReq.setPublishedName(null);
        dsetReq.setEncoding("UTF-8");
        dsetReq.setSrs("EPSG:4326");

        DatasetMetadata mdRequest = new DatasetMetadata();
        mdRequest.setTitle(dset.getName() + " title");
        mdRequest.setAbstract(dset.getName() + " abstract");
        mdRequest.setCreationProcessDescription(dset.getName() + " creation process description");
        mdRequest.setScale(500_000);
        mdRequest.setTags(Arrays.asList(dset.getName() + " keyword 1", dset.getName() + " keyword 2"));

        dsetReq.setMetadata(mdRequest);
        return dsetReq;
    }

}
