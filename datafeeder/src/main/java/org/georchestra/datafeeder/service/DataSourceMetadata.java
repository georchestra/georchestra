package org.georchestra.datafeeder.service;

import java.util.Map;

import lombok.Data;

@Data
public class DataSourceMetadata {

    public enum DataSourceType {
        SHAPEFILE, //
        GEOJSON, //
        GEOPACKAGE, //
        POSTGIS//
    }

    private DataSourceType type;
    private Map<String, Object> connectionParameters;
}
