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

package org.georchestra.datafeeder.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.geotools.TestData;
import org.junit.rules.ExternalResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.ByteStreams;

import lombok.NonNull;

public class MultipartTestSupport extends ExternalResource {

    public List<MockMultipartFile> archSitesShapefile() {
        return loadGeoToolsTestFiles("shapes/archsites.shp", "shapes/archsites.dbf", "shapes/archsites.prj",
                "shapes/archsites.shx");
    }

    public List<MockMultipartFile> bugSitesShapefile() {
        return loadGeoToolsTestFiles("shapes/bugsites.shp", "shapes/bugsites.dbf", "shapes/bugsites.prj",
                "shapes/bugsites.shx");
    }

    public List<MockMultipartFile> roadsShapefile() {
        return loadGeoToolsTestFiles("shapes/roads.shp", "shapes/roads.dbf", "shapes/roads.prj", "shapes/roads.shx");
    }

    public List<MockMultipartFile> statePopShapefile() {
        return loadGeoToolsTestFiles("shapes/statepop.shp", "shapes/statepop.dbf", "shapes/statepop.prj",
                "shapes/statepop.shx");
    }

    // test("shapes/chinese_poly.shp", Charset.forName("GB18030"));
    public List<MockMultipartFile> chinesePolyShapefile() {
        return loadGeoToolsTestFiles("shapes/chinese_poly.shp", "shapes/chinese_poly.dbf", "shapes/chinese_poly.prj",
                "shapes/chinese_poly.shx");
    }

    public List<MockMultipartFile> loadDatafeederTestShapefile(String typeName) {
        return loadDatafeederTestShapefile(typeName, true);
    }

    public List<MockMultipartFile> loadDatafeederTestShapefile(String typeName, boolean loadPrj) {
        String[] names = Stream.of(".shp", ".dbf", ".shx", ".prj").filter(ext -> loadPrj ? true : !".prj".equals(ext))
                .map(ext -> typeName + ext).toArray(String[]::new);
        return datafeederTestData(names);
    }

    public Path datafeederTestFile(String file) {
        try {
            URI uri = getClass().getResource("/org/geotools/test-data/datafeeder/" + file).toURI();
            return Paths.get(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads {@code fileNames} from
     * {@code src/test/resources/org/geotools/test-data/datafeeder/}
     *
     * @param fileNames file names to load relative to
     *                  {@code src/test/resources/org/geotools/test-data/datafeeder/}
     */
    public List<MockMultipartFile> datafeederTestData(String... fileNames) {
        String[] names = Arrays.stream(fileNames).map(name -> "datafeeder/" + name).toArray(String[]::new);
        return loadGeoToolsTestFiles(names);
    }

    public List<MockMultipartFile> renameDataset(String newName, List<? extends MultipartFile> datasetFiles)
            throws IOException {
        List<MockMultipartFile> renamed = new ArrayList<>();
        for (MultipartFile f : datasetFiles) {
            String extension = FilenameUtils.getExtension(f.getOriginalFilename());
            String name = String.format("%s.%s", newName, extension);
            renamed.add(createMultipartFile(name, f.getBytes()));
        }
        return renamed;
    }

    private List<MockMultipartFile> loadGeoToolsTestFiles(String... names) {
        List<MockMultipartFile> files = new ArrayList<>();
        for (String name : names) {
            try {
                files.add(loadGeoToolsTestFile(name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return files;
    }

    private MockMultipartFile loadGeoToolsTestFile(String testDataPath) throws IOException {
        try (InputStream in = TestData.openStream(testDataPath)) {
            byte[] contents = ByteStreams.toByteArray(in);
            String name = Paths.get(testDataPath).getFileName().toString();
            return createMultipartFile(name, contents);
        }
    }

    public MockMultipartFile createFakeFile(@NonNull String name, int fileSize) {
        byte[] content = createContents(fileSize);
        return createMultipartFile(name, content);
    }

    public MockMultipartFile createMultipartFile(String originalFileName, byte[] content) {
        String contentType = null;
        String formFieldName = "filename";
        String originalFilename = originalFileName;
        return new MockMultipartFile(formFieldName, originalFilename, contentType, content);
    }

    public byte[] createContents(int fileSize) {
        byte[] content = new byte[fileSize];
        for (int i = 0; i < fileSize; content[i] = (byte) i, i++)
            ;
        return content;
    }

    @SafeVarargs
    public final MockMultipartFile createZipFile(String zipfileName, List<? extends MultipartFile>... filesets)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (List<? extends MultipartFile> files : filesets) {
                for (MultipartFile zipped : files) {
                    String name = zipped.getOriginalFilename();
                    byte[] content = zipped.getBytes();
                    ZipEntry entry = new ZipEntry(name);
                    zos.putNextEntry(entry);
                    zos.write(content);
                    zos.closeEntry();
                }
            }
        }
        byte[] zipfileContent = baos.toByteArray();
        return createMultipartFile(zipfileName, zipfileContent);
    }
}
