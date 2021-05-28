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
package org.georchestra.datafeeder.service.geoserver;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.geoserver.openapi.model.catalog.DataStoreInfo;
import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.NamespaceInfo;
import org.geoserver.openapi.model.catalog.WorkspaceInfo;
import org.geoserver.openapi.v1.model.DataStoreResponse;
import org.geoserver.openapi.v1.model.Layer;
import org.geoserver.restconfig.client.DataStoresClient;
import org.geoserver.restconfig.client.FeatureTypesClient;
import org.geoserver.restconfig.client.GeoServerClient;
import org.geoserver.restconfig.client.LayersClient;
import org.geoserver.restconfig.client.NamespacesClient;
import org.geoserver.restconfig.client.ServerException;
import org.geoserver.restconfig.client.WorkspacesClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeoServerRemoteService {

    private @Autowired GeoServerClient _client;

    private static final WorkspaceLock LOCKS = new WorkspaceLock();

    public @VisibleForTesting GeoServerClient client() {
        return this._client;
    }

    public Optional<Layer> findLayerByName(@NonNull String workspace, @NonNull String layerName) {
        final LayersClient layersClient = client().layers();
        return layersClient.getLayer(workspace, layerName);
    }

    private Optional<WorkspaceInfo> findWorkspace(@NonNull String name) {
        LOCKS.readLock(name).lock();
        try {
            return findWorkspaceInternal(name);
        } catch (ServerException.NotFound notFound) {
            return Optional.empty();
        } finally {
            LOCKS.readLock(name).unlock();
        }
    }

    // find workspace without locking
    private Optional<WorkspaceInfo> findWorkspaceInternal(String name) {
        return client().workspaces().getAsInfo(name);
    }

    public @NonNull WorkspaceInfo getOrCreateWorkspace(@NonNull String workspaceName, @NonNull String namespaceURI) {
        Optional<WorkspaceInfo> found = findWorkspace(workspaceName);
        if (found.isPresent()) {
            return found.get();
        }
        LOCKS.writeLock(workspaceName).lock();
        try {
            return findWorkspaceInternal(workspaceName).orElseGet(() -> createWorkspace(workspaceName, namespaceURI));
        } finally {
            LOCKS.writeLock(workspaceName).unlock();
        }
    }

    private @NonNull WorkspaceInfo createWorkspace(@NonNull String workspaceName, @NonNull String namespaceURI) {
        log.info("Creating workspace {} with namespace URI {}", workspaceName, namespaceURI);
        final WorkspacesClient workspaces = this.client().workspaces();

        LOCKS.writeLock(workspaceName).lock();
        try {
            WorkspaceInfo wsInfo = new WorkspaceInfo();
            // create the workspace
            wsInfo.setName(workspaceName);
            wsInfo.setIsolated(Boolean.FALSE);
            workspaces.create(wsInfo);
            wsInfo = workspaces.getAsInfo(workspaceName).orElseThrow(
                    () -> new IllegalStateException("Workspace " + workspaceName + " not found after creation"));
            try {
                configureNamespace(workspaceName, namespaceURI);
            } catch (RuntimeException e) {
                workspaces.delete(workspaceName);
                throw e;
            }
            return wsInfo;
        } finally {
            LOCKS.writeLock(workspaceName).unlock();
        }
    }

    private void configureNamespace(@NonNull String workspaceName, @NonNull String namespaceURI) {
        final NamespacesClient namespaces = this.client().namespaces();

        // change the namespace URI associated to the workspace
        NamespaceInfo nsInfo = namespaces.findByPrefix(workspaceName)
                .orElseThrow(() -> new IllegalStateException("NamespaceInfo not found for workspace " + workspaceName));
        nsInfo.setUri(namespaceURI);
        namespaces.update(nsInfo.getPrefix(), nsInfo);
    }

    public @NonNull DataStoreResponse create(@NonNull DataStoreInfo dataStore) {
        requireNonNull(dataStore.getName());
        requireNonNull(dataStore.getConnectionParameters());
        requireNonNull(dataStore.getWorkspace());
        requireNonNull(dataStore.getWorkspace().getName());

        final DataStoresClient dataStoresClient = this.client().dataStores();
        final String workspaceName = dataStore.getWorkspace().getName();

        LOCKS.writeLock(workspaceName).lock();
        try {
            return dataStoresClient.create(workspaceName, dataStore);
        } catch (ServerException e) {
            log.error("Error creating GeoServer DataStore", e);
            throw e;
        } finally {
            LOCKS.writeLock(workspaceName).unlock();
        }
    }

    public FeatureTypeInfo create(@NonNull FeatureTypeInfo fti) {
        requireNonNull(fti.getStore(), "store required");
        requireNonNull(fti.getStore().getName(), "store name required");
        requireNonNull(fti.getName(), "name required");
        requireNonNull(fti.getNamespace(), "namespace required");
        requireNonNull(fti.getNamespace().getPrefix(), "namespace.prefix required");

        final String workspaceName = fti.getNamespace().getPrefix();
        LOCKS.writeLock(workspaceName).lock();
        try {
            FeatureTypesClient client = client().featureTypes();
            FeatureTypeInfo created = client.create(workspaceName, fti);
            return created;
        } finally {
            LOCKS.writeLock(workspaceName).unlock();
        }
    }

    public FeatureTypeInfo update(@NonNull FeatureTypeInfo fti) {
        requireNonNull(fti.getStore(), "store required");
        requireNonNull(fti.getStore().getName(), "store name required");
        requireNonNull(fti.getName(), "name required");
        requireNonNull(fti.getNamespace(), "namespace required");
        requireNonNull(fti.getNamespace().getPrefix(), "namespace.prefix required");

        final String workspaceName = fti.getNamespace().getPrefix();
        LOCKS.writeLock(workspaceName).lock();
        try {
            FeatureTypesClient client = client().featureTypes();
            String name = fti.getName();
            FeatureTypeInfo updated = client.update(workspaceName, name, fti);
            return updated;
        } finally {
            LOCKS.writeLock(workspaceName).unlock();
        }
    }

    public Optional<FeatureTypeInfo> findFeatureTypeInfo(@NonNull String publishedWorkspace, @NonNull String storeName,
            @NonNull String featureTypeName) {
        return client().featureTypes().getFeatureType(publishedWorkspace, storeName, featureTypeName);
    }

    public Optional<DataStoreResponse> findDataStore(@NonNull String workspace, @NonNull String dataStore) {
        return client().dataStores().findByWorkspaceAndName(workspace, dataStore);
    }

    /**
     * Per workspace name read-write locks, aids in preventing concurrent
     * modifications to the same resource at workspace granularity
     */
    private static class WorkspaceLock {

        private ConcurrentMap<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

        private ReadWriteLock get(String wsName) {
            return locks.computeIfAbsent(wsName, lockName -> new ReentrantReadWriteLock());
        }

        public Lock writeLock(@NonNull String wsName) {
            return get(wsName).writeLock();
        }

        public Lock readLock(@NonNull String wsName) {
            return get(wsName).readLock();
        }

    }
}
