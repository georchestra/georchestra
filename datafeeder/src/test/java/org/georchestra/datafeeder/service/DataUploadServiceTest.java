/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DataFeederServiceConfiguration.class, webEnvironment = WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
@Ignore(value = "not yet implemented")
public class DataUploadServiceTest {

    private DataUploadService service;

    public @Test void testAddFileToPackage() {
        fail("Not yet implemented");
    }

    public @Test void testAnalyze() {
        fail("Not yet implemented");
    }

    public @Test void testFindJobState() {
        fail("Not yet implemented");
    }

    public @Test void testFindAllJobs() {
        fail("Not yet implemented");
    }

    public @Test void testFindUserJobs() {
        fail("Not yet implemented");
    }

}
