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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Sets;

public class UploadPackageTest {

    public @Rule TemporaryFolder tmp = new TemporaryFolder();

    private UploadPackage pack;
    private FileStorageService service;

    public @Before void setUp() throws IOException {
        service = mock(FileStorageService.class);
        final UUID id = UUID.randomUUID();
        Path root = tmp.getRoot().toPath().resolve(id.toString());
        Files.createDirectory(root);
        when(service.resolve(eq(id))).thenReturn(root);

        pack = new UploadPackage(service, id);
    }

    public @Test void testIsArchive() {
        assertTrue(pack.isArchive("filename.tar"));
        assertTrue(pack.isArchive("File Name.TAR"));
        assertTrue(pack.isArchive("filename.zip"));
        assertTrue(pack.isArchive("filename.ZiP"));
        assertTrue(pack.isArchive("filename.gz"));
        assertTrue(pack.isArchive("filename.tgz"));
        assertTrue(pack.isArchive("filename.tar.gz"));
        assertTrue(pack.isArchive("filename.bz2"));
        assertTrue(pack.isArchive("some.file.name.zip"));
        assertTrue(pack.isArchive("andorra-180101-free.shp.zip"));

        assertFalse(pack.isArchive("filename.tar2"));
        assertFalse(pack.isArchive("File Name.shp"));
        assertFalse(pack.isArchive("File Name.json"));
    }

    public @Test void testIsDataset() {
        assertTrue(pack.isDataset("filename.shp"));
        assertTrue(pack.isDataset("File Name.Shp"));
        assertTrue(pack.isDataset("filename.gpkg"));
        assertTrue(pack.isDataset("filename.GPKG"));
        assertTrue(pack.isDataset("filename.geojson"));
        assertTrue(pack.isDataset("filename.GeoJSON"));
        assertTrue(pack.isDataset("andorra-180101.free.shp"));

        assertFalse(pack.isDataset(".shp"));

        assertFalse(pack.isDataset("filename.tar"));
        assertFalse(pack.isDataset("File Name.zip"));
        assertFalse(pack.isDataset("File Name.tgz"));
    }

    public @Test void testFindDatasets() throws IOException {
        createTestEmptyFiles(//
                "test.shp", "test.dbf", "test.shx", "test.prj", //
                "test2.shp", "test2.dbf", "test2.shx", "test2.prj", //
                "dir/test2.shp", "dir/test2.dbf", "dir/test2.shx", "dir/test2.prj"//
        );

        Set<String> datasets = pack.findDatasetFiles();
        Set<String> expected = Sets.newHashSet("test.shp", "test2.shp", "dir/test2.shp");
        assertEquals(expected, datasets);
    }

    public @Test void testResolve() {
        Path root = pack.root();
        assertEquals(root.resolve("f1"), pack.resolve("f1"));
    }

    public @Test void testFindAll() throws IOException {
        String[] fileNames = new String[] { //
                "test.shp", "test.dbf", "test.shx", "test.prj", //
                "test2.shp", "test2.dbf", "test2.shx", "test2.prj", //
                "dir/test2.shp", "dir/test2.dbf", "dir/test2.shx", "dir/test2.prj"//
        };
        createTestEmptyFiles(fileNames);

        Set<String> all = pack.findAll();
        for (String relativeName : fileNames) {
            assertTrue(relativeName + " not found", all.contains(relativeName));
        }
    }

    public @Test void testUnpack() throws IOException {
        String[] fileNames = new String[] { //
                "test.shp", "test.dbf", "test.shx", "test.prj", //
                "test2.shp", "test2.dbf", "test2.shx", "test2.prj", //
                "dir/test2.shp", "dir/test2.dbf", "dir/test2.shx", "dir/test2.prj"//
        };
        byte[] zfContents = createZipFile(fileNames);
        String zipName = "uploadFile.zip";
        Path zip = pack.resolve(zipName);
        Files.write(zip, zfContents);

        pack.unpack(zipName);
        Set<String> all = pack.findAll();
        assertTrue(all.contains(zipName));
        for (String relativeName : fileNames) {
            assertTrue(relativeName + " not found", all.contains(relativeName));
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testUnpackNonExistentArchive() throws IOException {
        pack.unpack("invalid.zip");
    }

    private byte[] createZipFile(String... names) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String name : names) {
                ZipEntry entry = new ZipEntry(name);
                zos.putNextEntry(entry);
                zos.write(new byte[] {});
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private void createTestEmptyFiles(String... names) throws IOException {
        Path root = pack.root();
        for (String name : names) {
            Path path = root.resolve(name);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
    }
}
