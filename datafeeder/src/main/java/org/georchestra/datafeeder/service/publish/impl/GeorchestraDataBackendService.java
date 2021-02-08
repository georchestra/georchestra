/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.datafeeder.service.publish.impl;

import java.io.IOException;
import java.util.Map;

import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link DataBackendService} implementing Georchestra-specific business rules
 * to import uploaded datasets using a PostGIS database as final storage
 * backend.
 * <p>
 * <ul>
 * <li>The calling user's geOrchestra organization short name must be provided
 * through {@link DataUploadJob#getOrganizationName()}. Absence of this property
 * results in an error.
 * <li>The organization name is used to create a PostgreSQL schema if such
 * doesn't already exist, and that schema is used to host the imported tables
 * </ul>
 */
@Slf4j
public class GeorchestraDataBackendService implements DataBackendService {

    private @Autowired DatasetsService datasetsService;
    private @Autowired DataFeederConfigurationProperties props;
    private @Autowired GeorchestraNameNormalizer nameResolver;

    @Override
    public void prepareBackend(@NonNull DataUploadJob job) {
        Map<String, String> connectionParams = resolveConnectionParams(job);
        try {
            datasetsService.createDataStore(connectionParams);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void importDataset(@NonNull DatasetUploadState dataset) {
        Map<String, String> connectionParams = resolveConnectionParams(dataset.getJob());
//		try {
//			datasetsService.importDataset(dataset, connectionParams);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
    }

    private Map<String, String> resolveConnectionParams(DataUploadJob job) {
        Map<String, String> connectionParams = props.getPublishing().getBackend().getLocal();
        String orgName = job.getOrganizationName();
        if (orgName == null) {
            throw new IllegalStateException("Georchestra organization name not provided in job.organizationName");
        }
        String schema = nameResolver.resolveDatabaseSchemaName(orgName);
        connectionParams.put(PostgisNGDataStoreFactory.SCHEMA.key, schema);
        return connectionParams;
    }
}
