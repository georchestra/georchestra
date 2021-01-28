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

import java.nio.charset.Charset;
import java.util.List;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = { DataFeederApplicationConfiguration.class }, webEnvironment = WebEnvironment.MOCK)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class DataPublishingApiControllerTest {

    private @Autowired ApiTestSupport testSupport;
    private @Autowired DataUploadService datasets;
    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private @Autowired DataPublishingApi controller;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testPublish_SingleShapefile() {
        List<MultipartFile> shapefileFiles = multipartSupport.chinesePolyShapefile();
        DataUploadJob upload = testSupport.uploadAndWaitForSuccess(shapefileFiles, "chinese_poly");
        // correct chinese_poly's dbf charset: GB18030, NAME: 黑龙江省
        SimpleFeature sampleFeature = datasets.sampleFeature(upload.getJobId(), "chinese_poly", 0,
                Charset.forName("GB18030"), null, false);
        System.err.println(sampleFeature.getAttribute("NAME"));
    }
}
