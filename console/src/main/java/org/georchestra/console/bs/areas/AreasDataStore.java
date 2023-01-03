package org.georchestra.console.bs.areas;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.data.Query;
import org.geotools.data.geojson.store.GeoJSONDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

class AreasDataStore {

    private final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();

    private URL geoJsonLocation;

    private SimpleFeatureSource featureSource;

    public AreasDataStore(URL geoJson) throws IOException {
        this.geoJsonLocation = geoJson;
        GeoJSONDataStore dataStore = new GeoJSONDataStore(geoJson);
        this.featureSource = dataStore.getFeatureSource();
    }

    public List<Geometry> findAreasById(List<String> ids) throws IOException {

        String typeName = featureSource.getName().getLocalPart();
        List<Filter> filters = ids.stream().map(id -> FF.equals(FF.property("INSEE_COM"), FF.literal(id)))
                .collect(Collectors.toList());
        Filter filter = FF.or(filters);
        Query query = new Query(typeName, filter);
        SimpleFeatureCollection features = featureSource.getFeatures(query);
        List<Geometry> areas = new ArrayList<>();
        try (SimpleFeatureIterator it = features.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                if (null != geom)
                    areas.add(geom);
            }
        }
        return areas;
    }
}
