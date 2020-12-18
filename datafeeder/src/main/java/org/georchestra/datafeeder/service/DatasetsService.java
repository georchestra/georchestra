package org.georchestra.datafeeder.service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Formattable;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatasetsService {

    public List<DatasetMetadata> describe(@NonNull Path path) {
        DataStore ds;
        try {
            ds = loadDataStore(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            String[] typeNames = ds.getTypeNames();
            List<DatasetMetadata> mds = new ArrayList<>(typeNames.length);
            for (String typeName : typeNames) {
                SimpleFeatureSource fs = ds.getFeatureSource(typeName);
                DatasetMetadata md = describe(fs);
                mds.add(md);
            }
            return mds;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ds.dispose();
        }
    }

    private DatasetMetadata describe(SimpleFeatureSource fs) throws IOException {
        DatasetMetadata md = new DatasetMetadata();
        md.setTypeName(fs.getName().getLocalPart());
        md.setNativeBounds(nativeBounds(fs));
        md.setFeatureCount(featureCount(fs));

        Optional<SimpleFeature> sampleFeature = sampleFeature(fs);
        if (sampleFeature.isPresent()) {
            SimpleFeature feature = sampleFeature.get();
            Geometry sampleGeometry;
            Map<String, Object> sampleProperties;

            sampleGeometry = (Geometry) feature.getDefaultGeometry();
            sampleProperties = feature.getProperties().stream()//
                    .filter(p -> !(p instanceof GeometryAttribute))//
                    .collect(Collectors.toMap(p -> p.getName().getLocalPart(), Property::getValue));

            md.setSampleGeometry(sampleGeometry);
            md.setSampleProperties(sampleProperties);
        }
        return md;
    }

    private @Nullable BoundingBoxMetadata nativeBounds(SimpleFeatureSource fs) throws IOException {
        ReferencedEnvelope bounds = fs.getBounds();
        if (bounds == null)
            return null;

        BoundingBoxMetadata bb = new BoundingBoxMetadata();
        bb.setCrs(crs(bounds.getCoordinateReferenceSystem()));
        bb.setMinx(bounds.getMinX());
        bb.setMaxx(bounds.getMaxX());
        bb.setMiny(bounds.getMinY());
        bb.setMaxy(bounds.getMaxY());

        return bb;
    }

    private int featureCount(SimpleFeatureSource fs) throws IOException {
        return fs.getCount(Query.ALL);
    }

    private @Nullable CoordinateReferenceSystemMetadata crs(@Nullable CoordinateReferenceSystem crs) {
        if (crs == null)
            return null;

        String srs = null;
        // potentially slow, but doesn't matter for the sake of this analysis process
        final boolean fullScan = true;
        try {
            srs = CRS.lookupIdentifier(crs, fullScan);
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        String wkt = toSingleLineWKT(crs);

        CoordinateReferenceSystemMetadata md = new CoordinateReferenceSystemMetadata();
        md.setSrs(srs);
        md.setWKT(wkt);
        return md;
    }

    private String toSingleLineWKT(@NonNull CoordinateReferenceSystem crs) {
        if (crs instanceof Formattable) {
            try {
                return ((Formattable) crs).toWKT(Formattable.SINGLE_LINE, false);
            } catch (RuntimeException e) {
                log.warn("Error formatting CRS to single-line WKT", e);
            }
        }
        return crs.toWKT();
    }

    private Optional<SimpleFeature> sampleFeature(SimpleFeatureSource fs) throws IOException {
        Query query = new Query(fs.getName().getLocalPart());
        query.setMaxFeatures(1);
        SimpleFeatureCollection collection = fs.getFeatures(query);
        try (SimpleFeatureIterator it = collection.features()) {
            if (it.hasNext()) {
                return Optional.of(it.next());
            }
        }
        return Optional.empty();
    }

    public @NonNull DataStore loadDataStore(@NonNull Path path) throws IOException {
        Map<Object, Object> params = resolveConnectionParameters(path);
        DataStore ds = DataStoreFinder.getDataStore(params);
        if (ds == null) {
            throw new IOException("Unable to resolve dataset " + path.getFileName());
        }
        return ds;
    }

    private @NonNull Map<Object, Object> resolveConnectionParameters(@NonNull Path path) throws IOException {
        // TODO support other file types than shapefile
        Map<Object, Object> params = new HashMap<>();
        if (path.getFileName().toString().toLowerCase().endsWith(".shp")) {
            URL url = path.toUri().toURL();
            params.put(ShapefileDataStoreFactory.URLP.key, url);
            params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX, Boolean.FALSE);
        }
        return params;
    }

}
