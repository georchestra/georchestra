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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.georchestra.datafeeder.test.MultipartTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Sets;

public class FileStorageServiceTest {

    public @Rule TemporaryFolder tmp = new TemporaryFolder();
    public @Rule MultipartTestSupport multipartSupport = new MultipartTestSupport();

    private FileStorageService service;

    private Path root;

    public @Before void setUp() {
        root = tmp.getRoot().toPath();
        service = new FileStorageService(root);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNonExistentDirectory() {
        new FileStorageService(Paths.get("/non-existent-directory_aaaa"));
    }

    public @Test void testInitializePackage() throws IOException {
        UploadPackage p1 = service.initializePackage();
        UploadPackage p2 = service.initializePackage();
        assertNotNull(p1);
        assertNotNull(p2);
        assertNotNull(p1.getId());
        assertNotNull(p2.getId());
        assertNotEquals(p1.getId(), p2.getId());
        assertTrue(Files.isDirectory(root.resolve(p1.getId().toString())));
        assertTrue(Files.isDirectory(root.resolve(p2.getId().toString())));
    }

    @Test(expected = FileNotFoundException.class)
    public void testFindNotFound() throws IOException {
        service.find(UUID.randomUUID());
    }

    @Test(expected = NullPointerException.class)
    public void testFind() throws IOException {
        UploadPackage p1 = service.initializePackage();
        UploadPackage p2 = service.initializePackage();

        UploadPackage found1 = service.find(p1.getId());
        UploadPackage found2 = service.find(p2.getId());
        assertNotNull(found1);
        assertNotNull(found2);
        assertEquals(p1.getId(), found1.getId());
        assertEquals(p2.getId(), found2.getId());

        service.find(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDeletePackage() throws IOException {
        UploadPackage p1 = service.initializePackage();
        UploadPackage p2 = service.initializePackage();

        Path root1 = root.resolve(p1.getId().toString());
        Path root2 = root.resolve(p2.getId().toString());

        Files.createDirectory(root1.resolve("dir"));
        Files.createFile(root1.resolve("test.shp"));
        Files.createFile(root1.resolve("dir/test.shp"));

        service.deletePackage(p1.getId());
        assertFalse(Files.exists(root1));
        assertTrue(Files.exists(root2));

        // verify idempotency
        service.deletePackage(p1.getId());
        assertFalse(Files.exists(root1));
        assertTrue(Files.exists(root2));

        service.deletePackage(null);
    }

    public @Test void testResolve() {
        UUID id = UUID.randomUUID();
        assertEquals(root.resolve(id.toString()), service.resolve(id));
    }

    public @Test void testSaveUploadsNoFiles() throws IOException {
        List<MultipartFile> received = Collections.emptyList();
        UUID id = service.saveUploads(received);
        assertNotNull(id);
        UploadPackage pack = service.find(id);
        assertNotNull(pack);
        Path path = service.resolve(id);
        assertTrue(Files.isDirectory(path));
        assertTrue(pack.findAll().isEmpty());
    }

    public @Test void testSaveUploadsSingleFile() throws IOException {
        List<MultipartFile> received = Arrays.asList(multipartSupport.createFakeFile("test.geojson", 1024));
        UUID id = service.saveUploads(received);
        verifyUploads(id, received);
    }

    public @Test void testSaveUploadsMultipleFiles() throws IOException {
        List<MultipartFile> received = Arrays.asList(//
                multipartSupport.createFakeFile("test.shp", 4096), //
                multipartSupport.createFakeFile("test.shx", 1024), //
                multipartSupport.createFakeFile("test.prj", 128), //
                multipartSupport.createFakeFile("test.dbf", 1024 * 1024)//
        );
        UUID id = service.saveUploads(received);
        verifyUploads(id, received);
    }

    public @Test void testSaveUploadsZipFileIsExtractedAutomatically() throws IOException {
        List<MultipartFile> zipped = Arrays.asList(//
                multipartSupport.createFakeFile("test.shp", 4096), //
                multipartSupport.createFakeFile("test.shx", 1024), //
                multipartSupport.createFakeFile("test.prj", 128), //
                multipartSupport.createFakeFile("test.dbf", 1024 * 1024), //

                multipartSupport.createFakeFile("test2.shp", 2048), //
                multipartSupport.createFakeFile("test2.shx", 512), //
                multipartSupport.createFakeFile("test2.prj", 256), //
                multipartSupport.createFakeFile("test2.dbf", 2 * 1024 * 1024), //

                multipartSupport.createFakeFile("test3.geojson", 1024)//
        );
        MultipartFile zipFile = multipartSupport.createZipFile("test.zip", zipped);
        final UUID id = service.saveUploads(Collections.singletonList(zipFile));

        List<MultipartFile> expected = new ArrayList<>(zipped);
        expected.add(zipFile);
        verifyUploads(id, expected);

        UploadPackage pack = service.find(id);
        Set<String> actual = pack.findDatasetFiles();
        assertEquals(Sets.newHashSet("test.shp", "test2.shp", "test3.geojson"), actual);
    }

    private void verifyUploads(UUID id, List<MultipartFile> received) throws IOException {
        UploadPackage pack = service.find(id);
        for (MultipartFile mpf : received) {
            byte[] expectedContent = mpf.getBytes();
            String fileName = mpf.getOriginalFilename();
            Path path = pack.resolve(fileName);
            assertTrue(Files.isRegularFile(path));
            assertEquals(expectedContent.length, Files.size(path));
            byte[] actualContent = Files.readAllBytes(path);
            assertArrayEquals(expectedContent, actualContent);
        }
    }
}
