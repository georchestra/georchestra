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

import java.util.Map;
import java.util.Optional;

import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.locationtech.jts.geom.Geometry;

import lombok.Data;

@Data
public class DatasetMetadata {

    private String encoding;
    private String typeName;

    private Integer featureCount;

    private BoundingBoxMetadata nativeBounds;

    private Geometry sampleGeometry;
    private Map<String, Object> sampleProperties;

    public Optional<Geometry> sampleGeometry() {
        return Optional.ofNullable(sampleGeometry);
    }

}
