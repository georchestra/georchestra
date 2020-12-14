package org.georchestra.datafeeder.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.api.BoundingBox;
import org.georchestra.datafeeder.api.CRS;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import lombok.NonNull;

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
        SimpleFeature sampleFeature = sampleFeature(fs);

        Geometry sampleGeometry = (Geometry) sampleFeature.getDefaultGeometry();
        Map<String, Object> sampleProperties = sampleFeature.getProperties().stream()//
                .filter(p -> !(p instanceof GeometryAttribute))//
                .collect(Collectors.toMap(p -> p.getName().getLocalPart(), Property::getValue));

        md.setSampleGeometry(sampleGeometry);
        md.setSampleProperties(sampleProperties);
        return md;
    }

    private BoundingBox nativeBounds(SimpleFeatureSource fs) throws IOException {
        ReferencedEnvelope bounds = fs.getBounds();
        if (bounds == null)
            return null;

        BoundingBox bb = new BoundingBox();
        bb.setCrs(crs(bounds.getCoordinateReferenceSystem()));
        bb.setMinx(bounds.getMinX());
        bb.setMaxx(bounds.getMaxX());
        bb.setMiny(bounds.getMinY());
        bb.setMaxy(bounds.getMaxY());

        return bb;
    }

    private CRS crs(CoordinateReferenceSystem coordinateReferenceSystem) {
        // TODO Auto-generated method stub
        return null;
    }

    private SimpleFeature sampleFeature(SimpleFeatureSource fs) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataStore loadDataStore(@NonNull Path path) throws IOException {
        Map<Object, Object> params = resolveConnectionParameters(path);
        return DataStoreFinder.getDataStore(params);
    }

    private Map<Object, Object> resolveConnectionParameters(@NonNull Path path) {
        // TODO Auto-generated method stub
        return null;
    }

}
