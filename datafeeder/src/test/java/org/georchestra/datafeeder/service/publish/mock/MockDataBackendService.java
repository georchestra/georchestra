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

package org.georchestra.datafeeder.service.publish.mock;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.geotools.data.shapefile.ShapefileDirectoryFactory;
import org.opengis.util.ProgressListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;

import lombok.NonNull;

public class MockDataBackendService implements DataBackendService, DisposableBean {

    private static final String NULL_ORG = "NULL_ORG";

    private @Autowired DatasetsService datasetsService;
    private File baseDirectory;
    private Set<File> directories = new TreeSet<>();

    public MockDataBackendService() {
        baseDirectory = new File(
                System.getProperty("java.io.tmpdir") + File.separator + "datafeeder" + File.separator + "mock_backend");
        baseDirectory.mkdirs();
    }

    @Override
    public void destroy() throws Exception {
        directories.forEach(FileSystemUtils::deleteRecursively);
    }

    @Override
    public void prepareBackend(@NonNull DataUploadJob job, @NonNull UserInfo user) {
        Map<String, String> connectionParams = resolveConnectionParams(job, user);
        try {
            datasetsService.createDataStore(connectionParams);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void importDataset(@NonNull DatasetUploadState dataset, @NonNull UserInfo user,
            @NonNull ProgressListener progressListener) {
        Map<String, String> connectionParams = resolveConnectionParams(dataset.getJob(), user);
        try {
            // use the same native featuretype name for the imported dataset featuretype
            dataset.getPublishing().setImportedName(dataset.getName());
            datasetsService.importDataset(dataset, connectionParams, progressListener);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> resolveConnectionParams(DataUploadJob job, @NonNull UserInfo user) {
        File targetDir = resolveKey(job, user);
        Map<String, String> connectionParams = new HashMap<>();
        String url;
        try {
            url = targetDir.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        connectionParams.put(ShapefileDirectoryFactory.URLP.key, url);
        connectionParams.put(ShapefileDirectoryFactory.CREATE_SPATIAL_INDEX.key, "true");
        connectionParams.put(ShapefileDirectoryFactory.FILE_TYPE.key, "shapefile");
        return connectionParams;
    }

    private File resolveKey(DataUploadJob job, UserInfo user) {
        String orgName = (user.getOrganization() == null || user.getOrganization().getShortName() == null) ? NULL_ORG
                : user.getOrganization().getShortName();
        File directory = baseDirectory.toPath().resolve(job.getJobId().toString()).resolve(orgName).toFile();
        directory.mkdirs();
        this.directories.add(directory);
        return directory;
    }

}
