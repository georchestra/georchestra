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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.DataSourceMetadata.DataSourceType;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Formattable;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.lang.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

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

    public List<String> getTypeNames(@NonNull Path path) {
        final Map<String, String> parameters = resolveConnectionParameters(path);
        DataStore ds = null;
        try {
            ds = loadDataStore(parameters);
            return Arrays.asList(ds.getTypeNames());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (ds != null)
                ds.dispose();
        }
    }

    public DatasetMetadata describe(@NonNull Path path, @NonNull String typeName) {
        Map<String, String> params = resolveConnectionParameters(path);
        DataStore ds = loadDataStore(params);
        try {
            SimpleFeatureSource fs = ds.getFeatureSource(typeName);
            DatasetMetadata md = describe(fs);
            if (isShapefile(path)) {
                String charset = params.get(ShapefileDataStoreFactory.DBFCHARSET.key);
                md.setEncoding(charset == null ? DEFAULT_SHAPEFILE_ENCODING : charset);
            }
            return md;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ds.dispose();
        }
    }

    public BoundingBoxMetadata getBounds(Path path, @NonNull String typeName, String srs, boolean reproject) {
        DataStore ds = loadDataStore(path);
        try {
            SimpleFeatureSource fs = ds.getFeatureSource(typeName);
            Query query = new Query();
            if (srs != null) {
                CoordinateReferenceSystem crs = CRS.decode(srs);
                if (reproject) {
                    query.setCoordinateSystemReproject(crs);
                } else {
                    query.setCoordinateSystem(crs);
                }
            }
            ReferencedEnvelope bounds = fs.getBounds(query);
            return toBoundingBoxMetadata(bounds);
        } catch (FactoryException | IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            ds.dispose();
        }
    }

    public SimpleFeature getFeature(@NonNull Path path, @NonNull String typeName, Charset encoding, int featureIndex,
            String srs, boolean srsReproject) {

        DataStore ds = loadDataStore(path, encoding);
        try {
            SimpleFeatureSource fs = ds.getFeatureSource(typeName);
            Query query = new Query();
            query.setStartIndex(featureIndex);
            query.setMaxFeatures(1);
            if (srs != null) {
                CoordinateReferenceSystem crs = CRS.decode(srs);
                if (srsReproject) {
                    query.setCoordinateSystemReproject(crs);
                } else {
                    query.setCoordinateSystem(crs);
                }
            }
            SimpleFeatureCollection collection = fs.getFeatures(query);
            try (SimpleFeatureIterator it = collection.features()) {
                if (it.hasNext()) {
                    return it.next();
                }
            }
            throw new IllegalArgumentException("Requested feature index is outside feature count");
        } catch (FactoryException | IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            ds.dispose();
        }
    }

    private DatasetMetadata describe(SimpleFeatureSource fs) throws IOException {
        DatasetMetadata md = new DatasetMetadata();
        md.setTypeName(fs.getName().getLocalPart());

        // compute native bounds
        md.setNativeBounds(nativeBounds(fs));

        // compute feature count
        md.setFeatureCount(featureCount(fs));

        // compute sample feature for properties and geometry
        Optional<SimpleFeature> sampleFeature = sampleFeature(fs);
        if (sampleFeature.isPresent()) {
            SimpleFeature feature = sampleFeature.get();
            Geometry sampleGeometry;
            Map<String, Object> sampleProperties;

            sampleGeometry = (Geometry) feature.getDefaultGeometry();
            sampleProperties = feature.getProperties().stream()//
                    .filter(p -> !(p instanceof GeometryAttribute))//
                    .collect(Collectors.toMap(p -> p.getName().getLocalPart(),
                            p -> Optional.ofNullable(p.getValue()).orElse("")));

            md.setSampleGeometry(sampleGeometry);
            md.setSampleProperties(sampleProperties);
        }
        return md;
    }

    public DataSourceMetadata describe(@NonNull Path path) {
        final Map<String, String> parameters = resolveConnectionParameters(path);
        DataSourceType dataSourceType = resolveDataSourceType(path, parameters);
        DataStore ds = loadDataStore(parameters);

        List<DatasetMetadata> mds = new ArrayList<>();
        try {
            String[] typeNames = ds.getTypeNames();
            for (String typeName : typeNames) {
                SimpleFeatureSource fs = ds.getFeatureSource(typeName);
                DatasetMetadata md = describe(fs);
                if (isShapefile(path)) {
                    String charset = parameters.get(ShapefileDataStoreFactory.DBFCHARSET.key);
                    md.setEncoding(charset == null ? DEFAULT_SHAPEFILE_ENCODING : charset);
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

    private @Nullable BoundingBoxMetadata nativeBounds(SimpleFeatureSource fs) throws IOException {
        ReferencedEnvelope bounds = fs.getBounds();
        return toBoundingBoxMetadata(bounds);
    }

    private BoundingBoxMetadata toBoundingBoxMetadata(ReferencedEnvelope bounds) {
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

    final @VisibleForTesting @NonNull DataStore loadDataStore(@NonNull Path path) {
        Map<String, String> params = resolveConnectionParameters(path);
        return loadDataStore(params);
    }

    private @NonNull DataStore loadDataStore(@NonNull Path path, @Nullable Charset encoding) {
        Map<String, String> params = resolveConnectionParameters(path);
        if (encoding != null)
            params.put(ShapefileDataStoreFactory.DBFCHARSET.key, encoding.name());
        return loadDataStore(params);
    }

    public @VisibleForTesting @NonNull DataStore loadDataStore(Map<String, String> params) {
        DataStore ds;
        try {
            ds = DataStoreFinder.getDataStore(params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (ds == null) {
            throw new IllegalArgumentException("Unable to resolve dataset with parameters " + params);
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
            String codePage = loadCharsetFromCodePageSideCarFile(path);
            if (codePage != null) {
                params.put(ShapefileDataStoreFactory.DBFCHARSET.key, codePage);
            }
        }
        return params;
    }

    private String loadCharsetFromCodePageSideCarFile(@NonNull Path shapefilePath) {
        String shpName = shapefilePath.getFileName().toString();
        shpName = shpName.substring(0, shpName.length() - ".shp".length());
        Path codepageFile = shapefilePath.resolveSibling(shpName + ".cpg");
        String charset = null;
        if (Files.isRegularFile(codepageFile)) {
            try {
                String codePage = com.google.common.io.Files.asCharSource(codepageFile.toFile(), StandardCharsets.UTF_8)
                        .readFirstLine();
                Charset.forName(codePage);
                charset = codePage;
            } catch (IllegalArgumentException e) {
                log.warn("Error obtaining charset from shapefile's .cpg side-car file {}", codepageFile, e);
            } catch (IOException e) {
                log.warn("Unable to read shapefile's .cpg side-car file {}", codepageFile, e);
            }
        }
        return charset;
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

    public void createDataStore(Map<String, String> connectionParams) throws IOException {

        DataStore ds = DataStoreFinder.getDataStore(connectionParams);
        if (ds == null) {
            throw new IOException("Unable to create datastore " + connectionParams);
        }
    }

    public void importDataset(@NonNull DatasetUploadState d, @NonNull Map<String, String> connectionParams)
            throws IOException {
        requireNonNull(d.getPublishing());
        requireNonNull(d.getPublishing().getImportedName(), "imported type name not provided");

        DataStore sourceDs = resolveSourceDataStore(d);
        try {
            SimpleFeatureSource source = resolveFeatureSource(sourceDs, d);
            SimpleFeatureType sourceType = source.getSchema();
            String targetTypeName = d.getPublishing().getImportedName();
            SimpleFeatureType targetType;
            {
                SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                ftb.init(sourceType);
                ftb.setName(targetTypeName);
                targetType = ftb.buildFeatureType();
            }
            DataStore targetds = loadDataStore(connectionParams);
            try {
                createSchema(targetType, targetds);
                SimpleFeatureStore target = (SimpleFeatureStore) targetds.getFeatureSource(targetTypeName);
                importData(source, target);
            } finally {
                targetds.dispose();
            }
        } finally {
            sourceDs.dispose();
        }
    }

    private void importData(SimpleFeatureSource source, SimpleFeatureStore target) throws IOException {
        log.info("Uploading data to {}", target.getSchema().getTypeName());
        final String typeName = target.getSchema().getTypeName();
        final Stopwatch sw = Stopwatch.createStarted();

        Transaction gtTx = new DefaultTransaction();
        try {
            target.setTransaction(gtTx);
            target.addFeatures(source.getFeatures());
            gtTx.commit();
            log.info("Data imported to {} in {}", typeName, sw.stop());
        } catch (IOException e) {
            log.error("Error importing data to {}", typeName, e);
            gtTx.rollback();
            throw e;
        }
    }

    private void createSchema(SimpleFeatureType featureType, DataStore targetds) throws IOException {
        log.info("Creating FeatureType " + featureType.getTypeName());
        try {
            targetds.createSchema(featureType);
            log.info("FeatureType " + featureType.getTypeName() + " created successfully");
        } catch (IOException e) {
            log.error("Error creating FeatureType {}", featureType.getTypeName(), e);
            throw e;
        }
    }

    public @VisibleForTesting DataStore resolveSourceDataStore(@NonNull DatasetUploadState d) {
        PublishSettings publishing = d.getPublishing();
        requireNonNull(publishing, "Dataset 'publishing' settings is null");
        requireNonNull(d.getAbsolutePath(), "Dataset file absolutePath not provided");

        Path path = Paths.get(d.getAbsolutePath());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Dataset absolutePath is not a file: " + path);
        }

        String encoding = publishing.getEncoding() == null ? d.getEncoding() : publishing.getEncoding();
        Charset charset = encoding == null ? null : Charset.forName(encoding);
        return loadDataStore(path, charset);
    }

    private SimpleFeatureSource resolveFeatureSource(DataStore store, DatasetUploadState d) throws IOException {
        final String sourceNativeName = d.getName();
        {
            String[] typeNames = store.getTypeNames();
            if (!Arrays.asList(typeNames).contains(sourceNativeName)) {
                throw new IllegalArgumentException("Dataset name '" + sourceNativeName
                        + "' does not exist. Type names in provided file: " + Arrays.toString(typeNames));
            }
        }
        SimpleFeatureSource orig = store.getFeatureSource(sourceNativeName);
        return orig;
    }
}
