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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
import org.georchestra.config.security.DatafeederAuthenticationTestSupport;
import org.georchestra.datafeeder.api.AnalysisStatusEnum;
import org.georchestra.datafeeder.api.DatasetMetadata;
import org.georchestra.datafeeder.api.DatasetPublishRequest;
import org.georchestra.datafeeder.api.DatasetPublishingStatus;
import org.georchestra.datafeeder.api.DatasetUploadStatus;
import org.georchestra.datafeeder.api.PublishJobStatus;
import org.georchestra.datafeeder.api.PublishRequest;
import org.georchestra.datafeeder.api.PublishStatusEnum;
import org.georchestra.datafeeder.api.UploadJobStatus;
import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.service.geoserver.GeoServerRemoteService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.ProjectionPolicy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;

@SpringBootTest(classes = { //
        DataFeederApplicationConfiguration.class, //
        IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT, //
        // disable sending emails
        properties = "datafeeder.email.send=false"//
)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
public class GeorchestraIntegrationIT {

    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();
    public @Rule DatafeederAuthenticationTestSupport authSupport = new DatafeederAuthenticationTestSupport();

    public @Autowired @Rule IntegrationTestSupport testSupport;

    private @Autowired PublishingBatchService publishingInternalService;
    private @Autowired GeoServerRemoteService geoserver;

    @LocalServerPort
    private int port;

    public @Before void before() {
        authSupport.secOrg("DF IT 1");
    }

    public @Test void testPublishDifferentUserOrganizations() throws Exception {
        final List<MockMultipartFile> files = multipartSupport.loadDatafeederTestShapefile("states_4326");

        // upload and publish as user1 of test_org_1
        authSupport.secOrg("DF IT 1").secOrgname("Test Org 1").secUsername("user1");
        final PublishJobStatus publish1 = uploadAndPublish(files);

        // upload and publish as user2 of test_org_2
        authSupport.secOrg("DF IT 2").secOrgname("Test Org 2").secUsername("user2");
        final PublishJobStatus publish2 = uploadAndPublish(files);

        final DatasetPublishingStatus dataset1 = publish1.getDatasets().get(0);
        final DatasetPublishingStatus dataset2 = publish2.getDatasets().get(0);
        assertEquals("df_it_1", dataset1.getPublishedWorkspace());
        assertEquals("df_it_2", dataset2.getPublishedWorkspace());

        // upload and publish both a second time
        authSupport.secOrg("DF IT 1").secOrgname("Test Org 1").secUsername("user1");
        final PublishJobStatus publish1_1 = uploadAndPublish(files);
        authSupport.secOrg("DF IT 2").secOrgname("Test Org 2").secUsername("user2");
        final PublishJobStatus publish2_1 = uploadAndPublish(files);

        final DatasetPublishingStatus dataset1_1 = publish1_1.getDatasets().get(0);
        final DatasetPublishingStatus dataset2_1 = publish2_1.getDatasets().get(0);

        assertEquals("df_it_1", dataset1_1.getPublishedWorkspace());
        assertEquals("df_it_2", dataset2_1.getPublishedWorkspace());
        assertNotEquals(dataset1.getPublishedName(), dataset1_1.getPublishedName());
    }

    private PublishJobStatus uploadAndPublish(final List<MockMultipartFile> files) {
        UploadJobStatus upload = uploadAndWaitForSuccess(files);
        UUID jobId = upload.getJobId();
        DatasetUploadStatus dataset = upload.getDatasets().get(0);
        PublishRequest publishRequest = publishRequest(dataset);
        final PublishJobStatus publishResponse = publishAndWaitForSuccess(jobId, publishRequest);
        return publishResponse;
    }

    /**
     * CRS use case:
     * <p>
     * When:
     * <ul>
     * <li>DatasetPublishRequest does not explicitly provide an
     * {@link DatasetPublishRequest#getSrs() SRS} to publish the layer with
     * <li>Native CRS is known through
     * {@link CoordinateReferenceSystemMetadata#getSrs()
     * DatasetUploadStatet.getNativeBounds().getCrs().getSrs()} (i.e., a known EPSG
     * code has been recognized)
     * </ul>
     * Then:
     * <ul>
     * <li>The GeoServer layer is correctly published using the native EPSG code
     * </ul>
     */
    public @Test void testPublish_SRS_set_to_native_if_not_provided_in_publish_request() throws Exception {
        final String testFile = "states_4326";
        final boolean loadPrj = true;
        final String publishRequestCRS = null;
        final String expectedRecognizedSRS = "EPSG:4326";
        final String expectedGeoServerNativeCRS = "EPSG:4326";
        final String expectedGeoServerSRS = "EPSG:4326";
        final ProjectionPolicy expectedProjectionPolicy = ProjectionPolicy.FORCE_DECLARED;
        testPublishAssertCRS(testFile, loadPrj, expectedRecognizedSRS, publishRequestCRS, expectedGeoServerNativeCRS,
                expectedGeoServerSRS, expectedProjectionPolicy);
    }

    public @Test void testPublish_prj_not_provided_crs_provided_by_publish_request() throws Exception {
        final String testFile = "states_4326";
        final boolean loadPrj = false;
        final String publishRequestCRS = "EPSG:4326";
        final String expectedRecognizedSRS = "EPSG:4326";
        final String expectedGeoServerNativeCRS = "EPSG:4326";
        final String expectedGeoServerSRS = "EPSG:4326";
        final ProjectionPolicy expectedProjectionPolicy = ProjectionPolicy.FORCE_DECLARED;
        testPublishAssertCRS(testFile, loadPrj, expectedRecognizedSRS, publishRequestCRS, expectedGeoServerNativeCRS,
                expectedGeoServerSRS, expectedProjectionPolicy);
    }

    /**
     * Uploads and publishes the {@code testFile} from
     * {@code src/test/resources/org/geotools/datafeeder/<testFile>.shp}, and
     * verifies the expected coordinate reference system settings based on the
     * recognized CRS from {@code .prj} file (if present), the one requested in the
     * publish request, and what's been actually published as in GeoServer.
     * 
     * @param testFile                   the file to load from
     *                                   {@code src/test/resources/org/geotools/datafeeder/}
     * @param loadPrj                    whether to include the file's {@code .prj}
     *                                   in the upload
     * @param expectedRecognizedSRS      the EPSG code the dataset is expected to be
     *                                   recognized with the upload, or {@code null}
     *                                   if no EPSG:XXX code is to be recognized
     * @param publishRequestSRS          the EPSG code provided in the publish
     *                                   request, or {@code null}
     * @param expectedGeoServerNativeCRS which CRS the GeoServer feature type shall
     *                                   have (GeoServer returns it as WKT)
     * @param expectedGeoServerSRS       which EPSG code GeoServer shall declare in
     *                                   the {@link FeatureTypeInfo#getSrs()}
     * @param expectedProjectionPolicy   which projection policy GeoServer shall
     *                                   apply between the layer's "native CRS" and
     *                                   "declared SRS" (e.g. "force declared" or
     *                                   "reproject native to declared")
     */
    private void testPublishAssertCRS(String testFile, boolean loadPrj, String expectedRecognizedSRS,
            String publishRequestSRS, String expectedGeoServerNativeCRS, String expectedGeoServerSRS,
            ProjectionPolicy expectedProjectionPolicy) {

        final UUID jobId;
        final UploadJobStatus upload;
        final DatasetUploadStatus dataset;
        {
            final List<MockMultipartFile> filesToUpload;
            filesToUpload = multipartSupport.loadDatafeederTestShapefile(testFile, loadPrj);
            upload = uploadAndWaitForSuccess(filesToUpload);
            dataset = upload.getDatasets().get(0);
            jobId = upload.getJobId();
        }
        // Verify the dataset was set to the expected CRS from the prj, may be null if
        // no prj is present or it's present but not recognized as an EPSG CRS, in which
        // case the WKT must exist
        if (loadPrj == true) {
            assertEquals(expectedRecognizedSRS, dataset.getNativeBounds().getCrs().getSrs());
            assertNotNull(dataset.getNativeBounds().getCrs().getWkt());
        } else {
            assertNull(dataset.getNativeBounds().getCrs());
        }

        // Send the publish request with the SRS code to force, may be null to use the
        // recognized native EPSG code.
        // The publish process shall fail if no native SRS is present and no force SRS
        // is included in the publish request
        PublishRequest publishRequest = publishRequest(dataset);
        publishRequest.getDatasets().get(0).setSrs(publishRequestSRS);

        final PublishJobStatus publishResponse = publishAndWaitForSuccess(jobId, publishRequest);
        DatasetPublishingStatus datasetResponse = publishResponse.getDatasets().get(0);
        assertEquals(PublishStatusEnum.DONE, datasetResponse.getStatus());

        DataUploadJob uploadJob = publishingInternalService.findJob(jobId);
        DatasetUploadState dset = uploadJob.getPublishableDatasets().get(0);
        String srs = dset.getPublishing().getSrs();
        // The job's publishing SRS code must have been set
        {
            final String expectedSRS = publishRequestSRS == null ? expectedRecognizedSRS : publishRequestSRS;
            assertEquals(expectedSRS, srs);
        }
        // load the FeatureType published to GeoServer
        FeatureTypeInfo featureType = loadGeoServerFeatureType(dset);
        String nativeCRS = featureType.getNativeCRS();
        String gsPublishedSrs = featureType.getSrs();
        ProjectionPolicy projectionPolicy = featureType.getProjectionPolicy();

        assertEquals(expectedGeoServerSRS, gsPublishedSrs);
        assertEquals(expectedProjectionPolicy, projectionPolicy);
    }

    private FeatureTypeInfo loadGeoServerFeatureType(DatasetUploadState dset) {
        String workspace = dset.getPublishing().getPublishedWorkspace();
        String typeName = dset.getPublishing().getPublishedName();
        String expectedStoreName = String.format("datafeeder_%s", workspace);
        FeatureTypeInfo featureType = geoserver.findFeatureTypeInfo(workspace, expectedStoreName, typeName)
                .orElseThrow(IllegalStateException::new);
        return featureType;
    }

    private PublishRequest publishRequest(@NonNull DatasetUploadStatus dataset) {

        DatasetPublishRequest item = new DatasetPublishRequest();
        DatasetMetadata metadata = new DatasetMetadata();
        item.setMetadata(metadata);
        PublishRequest requestBody = new PublishRequest();
        requestBody.addDatasetsItem(item);

        // mandatory fields...
        item.setNativeName(dataset.getName());
        item.setPublishedName(dataset.getName());
        metadata.setTitle(dataset.getName() + " Title");
        metadata.setAbstract(dataset.getName() + " Abstract");

        // optional fields.
        item.setEncoding(null);
        item.setSrs(null);
        metadata.setCreationDate(null);
        metadata.setCreationProcessDescription(null);
        metadata.setScale(null);
        metadata.setTags(null);
        return requestBody;
    }

    private PublishJobStatus publishAndWaitForSuccess(final @NonNull UUID jobId,
            final @NonNull PublishRequest requestBody) {

        final ResponseEntity<PublishJobStatus> response = publish(jobId, requestBody);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        final AtomicReference<PublishJobStatus> job = new AtomicReference<>();

        getPublishingStatus(jobId);

        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<PublishJobStatus> jobResponse = getPublishingStatus(jobId);
            job.set(jobResponse.getBody());
            PublishStatusEnum status = jobResponse.getBody().getStatus();
            assertEquals(PublishStatusEnum.DONE, status);
        });
        return job.get();

    }

    private ResponseEntity<PublishJobStatus> getPublishingStatus(UUID jobId) {
        HttpHeaders headers = authHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/json");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        TestRestTemplate template = testSupport.getTemplate();
        String url = buildUrl("/upload/{jobId}/publish");
        ResponseEntity<PublishJobStatus> response = template.exchange(url, HttpMethod.GET, requestEntity,
                PublishJobStatus.class, jobId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response;
    }

    private ResponseEntity<PublishJobStatus> publish(UUID jobId, PublishRequest requestBody) {
        HttpHeaders headers = authHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/json");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity<PublishRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        TestRestTemplate template = testSupport.getTemplate();
        String url = buildUrl("/upload/{jobId}/publish");
        ResponseEntity<PublishJobStatus> response = template.exchange(url, HttpMethod.POST, requestEntity,
                PublishJobStatus.class, jobId);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        return response;
    }

    private UploadJobStatus uploadAndWaitForSuccess(List<? extends MultipartFile> files) {
        UploadJobStatus job = uploadAndWaitUntilDoneOrError(files);
        assertEquals(AnalysisStatusEnum.DONE, job.getStatus());
        return job;
    }

    private UploadJobStatus uploadAndWaitUntilDoneOrError(List<? extends MultipartFile> files) {
        final ResponseEntity<UploadJobStatus> uploadResponse = upload(files);
        final UUID jobId = uploadResponse.getBody().getJobId();

        final AtomicReference<UploadJobStatus> job = new AtomicReference<>();

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            UploadJobStatus jobResponse = getUploadJob(jobId);
            job.set(jobResponse);
            AnalysisStatusEnum status = jobResponse.getStatus();
            assertTrue(status == AnalysisStatusEnum.DONE || status == AnalysisStatusEnum.ERROR);
        });
        return job.get();
    }

    private UploadJobStatus getUploadJob(UUID jobId) {
        ResponseEntity<UploadJobStatus> response = findUploadJob(jobId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private ResponseEntity<UploadJobStatus> findUploadJob(UUID jobId) {
        String url = buildUrl("/upload/{jobId}");
        TestRestTemplate template = testSupport.getTemplate();
        HttpHeaders headers = authHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UploadJobStatus> response = template.exchange(url, HttpMethod.GET, entity, UploadJobStatus.class,
                jobId);
        return response;
    }

    private ResponseEntity<UploadJobStatus> upload(List<? extends MultipartFile> files) {
        return upload(authHeaders(), files);
    }

    private ResponseEntity<UploadJobStatus> upload(HttpHeaders headers, List<? extends MultipartFile> files) {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HttpHeaders.ACCEPT, "application/json");

        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            Resource resource = file.getResource();
            parameters.add("filename", resource);
        }

        TestRestTemplate template = testSupport.getTemplate();
        String url = buildUrl("/upload");

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);

        ResponseEntity<UploadJobStatus> response = template.exchange(url, HttpMethod.POST, entity,
                UploadJobStatus.class);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        return response;
    }

    private String buildUrl(String path) {
        assertTrue("path must start with / (e.g. /upload)", path.startsWith("/"));
        return String.format("http://localhost:%d/datafeeder%s", port, path);
    }

    private HttpHeaders authHeaders() {
        return authSupport.buildHttpHeaders();
    }

}
