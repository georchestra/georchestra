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
package org.georchestra.datafeeder.service.publish.impl;

import java.util.Optional;

import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.WorkspaceInfo;
import org.geoserver.openapi.v1.model.Layer;

import lombok.NonNull;

public class GeoServerRemoteService {

    public Optional<Layer> findLayerByName(@NonNull String workspace, @NonNull String proposedName) {
        throw new UnsupportedOperationException();
    }

    public WorkspaceInfo getOrCreateWorkspace(String workspaceName) {
        throw new UnsupportedOperationException();
    }

    public void create(FeatureTypeInfo fti) {
        // TODO Auto-generated method stub

    }

    public void update(FeatureTypeInfo fti) {
        // TODO Auto-generated method stub

    }

    public FeatureTypeInfo getFeatureTypeInfo(String publishedWorkspace, String publishedName) {
        // TODO Auto-generated method stub
        return null;
    }

}
