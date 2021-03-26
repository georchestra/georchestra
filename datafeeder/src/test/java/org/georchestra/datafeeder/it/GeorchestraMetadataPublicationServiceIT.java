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

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.Envelope;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraMetadataPublicationService;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@SpringBootTest(classes = { //
        DataFeederApplicationConfiguration.class, //
        IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
public class GeorchestraMetadataPublicationServiceIT {

    public @Autowired @Rule IntegrationTestSupport support;

    private @Autowired GeorchestraMetadataPublicationService service;
    private @Autowired GeoNetworkRemoteService geonetwork;

    private @Autowired DataFeederConfigurationProperties configProperties;

    private DatasetUploadState shpDataset;

    private static final String ORG_NAME = "TEST org";

    private static final String NATIVE_LAYERNAME = "public_layer";
    private static final String PULISHED_LAYERNAME = "PublicLayer";

    public @Before void setup() {
        shpDataset = buildShapefileDatasetFromDefaultGeorchestraDataDirectory();

        // replace the configured geoserver datastore connection parameters by a
        // "directory of shapefiles" set of parameters
        Map<String, String> params = configProperties.getPublishing().getBackend().getGeoserver();
        params.clear();
        params.put(ShapefileDataStoreFactory.FILE_TYPE.key, "shapefile");
        params.put(ShapefileDataStoreFactory.URLP.key, "file:data/automated_tests");
    }

    private DatasetUploadState buildShapefileDatasetFromDefaultGeorchestraDataDirectory() {
        DataUploadJob job = new DataUploadJob();
        job.setOrganizationName(ORG_NAME);

        DatasetUploadState dset = new DatasetUploadState();
        dset.setJob(job);
        dset.setName(NATIVE_LAYERNAME);

        dset.setNativeBounds(new BoundingBoxMetadata());
        dset.getNativeBounds().setCrs(new CoordinateReferenceSystemMetadata());
        dset.getNativeBounds().getCrs().setSrs("EPSG:4326");
        dset.getNativeBounds().setMinx(-86d);
        dset.getNativeBounds().setMaxx(77d);
        dset.getNativeBounds().setMiny(-17d);
        dset.getNativeBounds().setMaxy(51d);

        PublishSettings publishing = new PublishSettings();
        dset.setPublishing(publishing);
        publishing.setPublishedName(PULISHED_LAYERNAME);
        publishing.setKeywords(Arrays.asList("tag1", "tag 2"));
        publishing.setSrs("EPSG:4326");
        return dset;
    }

    @Test
    public void testPublishSingleShapefile() {

        PublishSettings publishing = shpDataset.getPublishing();
        publishing.setMetadataRecordId(null);
        publishing.setTitle("Test Title");
        publishing.setAbstract("Test abstract");
        final LocalDate datasetCreationDate = LocalDate.now();
        publishing.setDatasetCreationDate(datasetCreationDate);
        publishing.setDatasetCreationProcessDescription("Test process description");
        publishing.setEncoding("ISO-8859-1");

        Envelope bounds = new Envelope();
        bounds.setMinx(-180d);
        bounds.setMaxx(180d);
        bounds.setMiny(-90d);
        bounds.setMaxy(90d);
        publishing.setGeographicBoundingBox(bounds);

        publishing.setKeywords(Arrays.asList("keyword 1", "key2"));
        publishing.setPublishedName(PULISHED_LAYERNAME);
        publishing.setSrs("EPSG:4326");

        service.publish(shpDataset);

        final String createdMdId = publishing.getMetadataRecordId();
        assertNotNull(createdMdId);

        final String publishedRecord = geonetwork.getRecordById(createdMdId);
        assertNotNull(publishedRecord);
        Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(publishedRecord)));
        } catch (Exception e) {
            fail("Published record not returned as valid XML", e);
        }
    }

}
