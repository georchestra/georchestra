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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.georchestra.datafeeder.service.DataSourceMetadata.DataSourceType;
import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.georchestra.datafeeder.test.TestData;
import org.geotools.data.DataStore;
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

    private void testLoadDataStore(Path path) {
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

}
