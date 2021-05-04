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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.model.UserInfo.Organization;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.util.ProgressListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

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

    /**
     * Sets up, if necessary, the target geotools {@link DataStore} where to import
     * the {@link DataUploadJob} datasets to.
     * <p>
     * This will create a schema in the target postgres database matching the
     * {@link UserInfo#getOrganization()}'s short name.
     */
    @Override
    public void prepareBackend(@NonNull DataUploadJob unused, @NonNull UserInfo user) {
        final Map<String, String> connectionParams = resolveConnectionParams(user);
        createSchema(connectionParams);
        try {
            datasetsService.createDataStore(connectionParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a Schema in the PostgreSQL database. The schema is defined in the
     * connection parameters under the key
     * 
     * @link{PostgisNGDataStoreFactory.SCHEMA.key}.
     * @param connectionParams
     * @throws SQLException
     */
    private void createSchema(Map<String, String> connectionParams) {
        final String schema = connectionParams.get(PostgisNGDataStoreFactory.SCHEMA.key);
        final String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s ", schema);

        DataStore dataStore = null;
        try {
            dataStore = DataStoreFinder.getDataStore(connectionParams);

            // TODO Check if we can really cast:
            final JDBCDataStore jdbcDataStore;
            if (dataStore instanceof JDBCDataStore) {
                jdbcDataStore = (JDBCDataStore) dataStore;
            } else {
                throw new IllegalStateException("Could not cast DataStore to JDBCDataStore.");
            }
            final DataSource source = jdbcDataStore.getDataSource();
            try (Connection connection = source.getConnection()) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
    }

    @Override
    public void importDataset(@NonNull DatasetUploadState dataset, @NonNull UserInfo user,
            @NonNull ProgressListener progressListener) {
        requireNonNull(dataset.getName(), "Dataset name is null");
        requireNonNull(user.getOrganization(), "Organization is null");
        requireNonNull(user.getOrganization().getId(), "Organization is null");
        requireNonNull(dataset.getPublishing(), "Dataset 'publishing' settings is null");

        final Map<String, String> connectionParams = resolveConnectionParams(user);
        final String postgresSchema = connectionParams.get(PostgisNGDataStoreFactory.SCHEMA.key);
        String uniqueTargetName;
        try {
            uniqueTargetName = resolveTargetTypeName(dataset, connectionParams);
        } catch (Exception e) {
            log.error("Error resolving unique database table name for {}", dataset.getName(), e);
            throw new RuntimeException(e);
        }
        try {
            dataset.getPublishing().setImportedName(uniqueTargetName);
            log.info("Importing dataset {} into PostGIS as {}.{}", dataset.getName(), postgresSchema, uniqueTargetName);
            datasetsService.importDataset(dataset, connectionParams, progressListener);
        } catch (IOException e) {
            log.error("Error importing dataset {} into PostGIS as {}.{}", dataset.getName(), postgresSchema,
                    uniqueTargetName, e);
            throw new RuntimeException(e);
        }
    }

    private String resolveTargetTypeName(@NonNull DatasetUploadState dataset, Map<String, String> connectionParams)
            throws IOException {

        final String typeName = nameResolver.resolveDatabaseTableName(dataset.getName());
        String resolvedTypeName = typeName;
        DataStore targetStore = datasetsService.loadDataStore(connectionParams);
        try {
            final Set<String> typeNames = new HashSet<>(Arrays.asList(targetStore.getTypeNames()));
            for (int deduplicator = 1; typeNames.contains(resolvedTypeName); deduplicator++) {
                resolvedTypeName = typeName + "_" + (deduplicator);
            }
            return resolvedTypeName;
        } finally {
            targetStore.dispose();
        }
    }

    public @VisibleForTesting Map<String, String> resolveConnectionParams(@NonNull UserInfo user) {
        Map<String, String> connectionParams = props.getPublishing().getBackend().getLocal();
        Organization org = user.getOrganization();
        String orgName = org == null ? null : org.getId();
        if (orgName == null) {
            throw new IllegalStateException("Georchestra organization name not provided in job.user.organization.id");
        }
        String schema = nameResolver.resolveDatabaseSchemaName(orgName);
        connectionParams.put(PostgisNGDataStoreFactory.SCHEMA.key, schema);
        return connectionParams;
    }
}
