package org.georchestra.datafeeder.service;

import java.util.Map;
import java.util.Optional;

import org.georchestra.datafeeder.api.BoundingBox;
import org.locationtech.jts.geom.Geometry;

import lombok.Data;

@Data
public class DatasetMetadata {

    private String encoding;
    private String typeName;
    private BoundingBox nativeBounds;
    private Geometry sampleGeometry;
    private Map<String, Object> sampleProperties;

    public Optional<Geometry> sampleGeometry() {
        return Optional.ofNullable(sampleGeometry);
    }

}
