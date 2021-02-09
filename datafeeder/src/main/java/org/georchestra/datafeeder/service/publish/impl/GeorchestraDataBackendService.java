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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;

import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
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

    private @Autowired GeorchestraNameNormalizer normalizationService;

    @Override
    public void prepareBackend(@NonNull DataUploadJob job) {
        log.trace("START prepareBackend");
        Map<String, String> connectionParams = resolveConnectionParams(job);

        try {
            final boolean existsSchema = existsSchema(connectionParams);
            if (!existsSchema) {
                createSchema(connectionParams);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            datasetsService.createDataStore(connectionParams);
        } catch (IOException e) {
            // TODO Handle exception
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Connection getConnectionFromParams(Map<String, String> connectionParams) throws SQLException {

        DataStore dataStore = null;
        try {
            dataStore = DataStoreFinder.getDataStore(connectionParams);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Check if we can really cast:
        final JDBCDataStore jdbcDataStore = (JDBCDataStore) dataStore;
        final DataSource source = jdbcDataStore.getDataSource();
        return source.getConnection();
    }

    /**
     * Creates a Schema in the PostgreSQL database. The schema is defined in the
     * connection parameters under the key
     * 
     * @link{PostgisNGDataStoreFactory.SCHEMA.key}.
     * @param connectionParams
     * @throws SQLException
     */
    private void createSchema(Map<String, String> connectionParams) throws SQLException {
        final String schema = connectionParams.get(PostgisNGDataStoreFactory.SCHEMA.key);
        final String sql = String.format("CREATE SCHEMA %s", schema);

        try (Connection connection = getConnectionFromParams(connectionParams)) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Connects to the database and checks if the schema exists. The schema is found
     * in the connectionParams under
     * key @link{PostgisNGDataStoreFactory.SCHEMA.key}.
     * 
     * @param connectionParams
     * @return
     * @throws SQLException
     */
    private boolean existsSchema(Map<String, String> connectionParams) throws SQLException {

        final Map<String, String> schemaLessConnectionParams = new HashMap<>(connectionParams);
        final String schema = schemaLessConnectionParams.remove(PostgisNGDataStoreFactory.SCHEMA.key);

        if (schema == null) {
            // FIXME: Throw something
        }

        try (Connection connection = getConnectionFromParams(schemaLessConnectionParams)) {
            final DatabaseMetaData mObject = connection.getMetaData();
            final ResultSet schemas = mObject.getSchemas();
            while (schemas.next()) {
                // SEE:
                // https://javadoc.io/static/org.postgresql/postgresql/42.0.0.jre7/org/postgresql/jdbc/PgDatabaseMetaData.html#getSchemas()
                final String currSchema = schemas.getString("TABLE_SCHEM");
                log.trace("Next Schema to search: {}", currSchema);
                if (schema.equalsIgnoreCase(currSchema)) {
                    log.trace("Found schema!");
                    return true;
                }
            }
            return false;

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
