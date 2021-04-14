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
package org.georchestra.datafeeder.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.rules.TemporaryFolder;

public class TestData extends TemporaryFolder {

    public @Override void create() throws IOException {
        super.create();
    }

    public Path archSitesShapefile() throws IOException {
        return copyShapefile("shapes/archsites");
    }

    public Path bugSitesShapefile() throws IOException {
        return copyShapefile("shapes/bugsites");
    }

    public Path roadsShapefile() throws IOException {
        return copyShapefile("shapes/roads");
    }

    public Path statePopShapefile() throws IOException {
        return copyShapefile("shapes/statepop");
    }

    // test("shapes/chinese_poly.shp", Charset.forName("GB18030"));
    public Path chinesePolyShapefile() throws IOException {
        return copyShapefile("shapes/chinese_poly");
    }

    private Path copyShapefile(String resourceWithoutExtension) throws IOException {
        Path path = extract(resourceWithoutExtension + ".shp");
        extract(resourceWithoutExtension + ".dbf");
        extract(resourceWithoutExtension + ".shx");
        extract(resourceWithoutExtension + ".prj");
        return path;
    }

    private Path extract(String resource) throws IOException {
        String fileName = Paths.get(resource).getFileName().toString();
        Path path = getRoot().toPath().resolve(fileName);
        if (!Files.isRegularFile(path)) {
            try (InputStream in = org.geotools.TestData.openStream(resource)) {
                Files.copy(in, path);
            }
        }
        return path;
    }
}
