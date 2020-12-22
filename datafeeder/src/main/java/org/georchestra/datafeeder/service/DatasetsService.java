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

import java.io.IOException;
import java.net.MalformedURLException;
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
import org.georchestra.datafeeder.service.DataSourceMetadata.DataSourceType;
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

    /**
     * Default encoding of shapefiles' dbf by spec
     */
    private static final String DEFAULT_SHAPEFILE_ENCODING = "ISO-8859-1";

    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    public DataSourceMetadata describe(@NonNull Path path) {
        final Map<String, String> parameters = resolveConnectionParameters(path);
        DataSourceType dataSourceType = resolveDataSourceType(path, parameters);
        DataStore ds;
        try {
            ds = loadDataStore(parameters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<DatasetMetadata> mds = new ArrayList<>();
        try {
            String[] typeNames = ds.getTypeNames();
            for (String typeName : typeNames) {
                SimpleFeatureSource fs = ds.getFeatureSource(typeName);
                DatasetMetadata md = describe(fs);
                if (isShapefile(path)) {
                    md.setEncoding(DEFAULT_SHAPEFILE_ENCODING);
                }
                mds.add(md);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ds.dispose();
        }
        DataSourceMetadata dsm = new DataSourceMetadata();
        dsm.setConnectionParameters(parameters);
        dsm.setDatasets(mds);
        dsm.setType(dataSourceType);
        return dsm;
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
        final boolean fullScan = false;
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
        Map<String, String> params = resolveConnectionParameters(path);
        return loadDataStore(params);
    }

    public @NonNull DataStore loadDataStore(Map<String, String> params) throws IOException {
        DataStore ds = DataStoreFinder.getDataStore(params);
        if (ds == null) {
            throw new IOException("Unable to resolve dataset");
        }
        return ds;
    }

    private @NonNull Map<String, String> resolveConnectionParameters(@NonNull Path path) {
        // TODO support other file types than shapefile
        Map<String, String> params = new HashMap<>();
        if (isShapefile(path)) {
            URL url;
            try {
                url = path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            params.put(ShapefileDataStoreFactory.FILE_TYPE.key, "shapefile");
            params.put(ShapefileDataStoreFactory.URLP.key, url.toString());
            params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, "false");
        }
        return params;
    }

    private boolean isShapefile(@NonNull Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".shp");
    }

    private DataSourceType resolveDataSourceType(@NonNull Path path, Map<String, String> parameters) {
        if (isShapefile(path)) {
            return DataSourceType.SHAPEFILE;
        }
        throw new UnsupportedOperationException("Only shapefiles are supported so far");
    }

}
