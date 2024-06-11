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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.DataSourceMetadata.DataSourceType;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.crs.ForceCoordinateSystemFeatureReader;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.csv.CSVDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.wkt.Formattable;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
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
        DataStore ds = null;
        try {
            ds = loadDataStore(params);
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
            if (ds != null)
                ds.dispose();
        }
    }

    /**
     *
     * @param path              location of file to get dataset bounds from
     * @param typeName          name of the dataset to get bounds from, that belongs
     *                          to the file
     * @param targetSrs         target CRS code (e.g. 'EPSG:900903') to return the
     *                          bounds reprojected to, from the native, or
     *                          {@code nativeSrsOverride} CRS if provided.
     * @param nativeSrsOverride allows to override the native CRS, most useful when
     *                          the dataset provides no native CRS information.
     * @return
     * @throws IOException
     */
    public BoundingBoxMetadata getBounds(Path path, @NonNull String typeName, String targetSrs,
            String nativeSrsOverride) throws IOException {
        DataStore ds = loadDataStore(path);
        try {
            SimpleFeatureSource fs = ds.getFeatureSource(typeName);
            Query query = buildQuery(fs, nativeSrsOverride, targetSrs);

            ReferencedEnvelope bounds = fs.getBounds(query);
            BoundingBoxMetadata bbm = toBoundingBoxMetadata(bounds);

            boolean reprojected = query.getCoordinateSystemReproject() != null;
            CoordinateReferenceSystem sourceCrs = query.getCoordinateSystem();

            bbm.setReprojected(reprojected);
            bbm.setNativeCrs(crs(sourceCrs));
            return bbm;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            ds.dispose();
        }
    }

    private Query buildQuery(@NonNull SimpleFeatureSource fs, final String nativeSrsOverride, final String targetSrs) {
        final @Nullable CoordinateReferenceSystem nativeCrs = fs.getSchema().getCoordinateReferenceSystem();
        final @Nullable CoordinateReferenceSystem sourceCrs = resolveCrs(nativeCrs, nativeSrsOverride);
        final @Nullable CoordinateReferenceSystem targetCrs = resolveCrs(sourceCrs, targetSrs);

        if (targetSrs != null && sourceCrs == null) {
            throw new IllegalArgumentException(String.format(
                    "Unable to reproject, dataset %s doesn't declare a native CRS and no native SRS override was provided",
                    fs.getSchema().getName()));
        }

        final boolean reproject = targetCrs != null && !CRS.equalsIgnoreMetadata(sourceCrs, targetCrs);

        Query query = new Query();
        // overrides the source CRS, may be equal to the native CRS, or even null
        query.setCoordinateSystem(sourceCrs);
        if (reproject) {
            // and reproject to the target CRS
            query.setCoordinateSystemReproject(targetCrs);
        }
        return query;
    }

    private CoordinateReferenceSystem resolveCrs(CoordinateReferenceSystem crs, String srs) {
        if (srs == null)
            return crs;
        CoordinateReferenceSystem overriding;
        try {
            overriding = CRS.decode(srs);
        } catch (FactoryException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return overriding;
    }

    /**
     *
     * @param path              location of file to get the sample dataset feature
     *                          from
     * @param typeName          name of the dataset to get the sample feature from,
     *                          that belongs to the file
     * @param encoding          allows to declare the source data character encoding
     *                          when reading the dataset
     * @param featureIndex      which feature to return, starting from zero
     * @param targetSrs         target CRS code (e.g. 'EPSG:900903') to return the
     *                          feature geometry reprojected to.
     * @param nativeSrsOverride allows to override the native CRS, most useful when
     *                          the dataset provides no native CRS information.
     * @return
     * @throws IOException
     */
    public FeatureResult getFeature(@NonNull Path path, @NonNull String typeName, Charset encoding, int featureIndex,
            String targetSrs, String nativeSrsOverride) throws IOException {

        DataStore ds = loadDataStore(path, encoding);
        try {
            SimpleFeatureSource fs = ds.getFeatureSource(typeName);
            Query query = buildQuery(fs, nativeSrsOverride, targetSrs);
            query.setStartIndex(featureIndex);
            query.setMaxFeatures(1);
            SimpleFeatureCollection collection = fs.getFeatures(query);

            SimpleFeature feature = getFirst(collection);
            boolean reprojected = query.getCoordinateSystemReproject() != null;
            CoordinateReferenceSystem sourceCrs = query.getCoordinateSystem();
            CoordinateReferenceSystem targetCrs = feature.getFeatureType().getCoordinateReferenceSystem();
            return new FeatureResult(feature, reprojected, crs(sourceCrs), crs(targetCrs));
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            ds.dispose();
        }
    }

    private SimpleFeature getFirst(SimpleFeatureCollection collection) {
        try (SimpleFeatureIterator it = collection.features()) {
            if (it.hasNext()) {
                return it.next();
            }
        }
        throw new IllegalArgumentException("Requested feature index is outside feature count");
    }

    public static @Value class FeatureResult {
        SimpleFeature feature;
        boolean reprojected;
        CoordinateReferenceSystemMetadata nativeCrs;
        CoordinateReferenceSystemMetadata crs;
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
        DataStore ds = null;

        List<DatasetMetadata> mds = new ArrayList<>();
        try {
            ds = loadDataStore(parameters);
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
            if (ds != null)
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
            log.error("Unable to lookup CRS", e);
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

    final @VisibleForTesting @NonNull DataStore loadDataStore(@NonNull Path path) throws IOException {
        Map<String, String> params = resolveConnectionParameters(path);
        return loadDataStore(params);
    }

    private @NonNull DataStore loadDataStore(@NonNull Path path, @Nullable Charset encoding) throws IOException {
        Map<String, String> params = resolveConnectionParameters(path);
        if (encoding != null)
            params.put(ShapefileDataStoreFactory.DBFCHARSET.key, encoding.name());
        return loadDataStore(params);
    }

    public @VisibleForTesting @NonNull DataStore loadDataStore(Map<String, String> params) throws IOException {
        DataStore ds = DataStoreFinder.getDataStore(params);
        if (ds == null) {
            throw new IOException("Unable to resolve dataset with parameters " + params);
        }
        return ds;
    }

    @NonNull
    Map<String, String> resolveConnectionParameters(@NonNull Path path) {
        Map<String, String> params = new HashMap<>();
        URL url;
        try {
            // white space handling. We don't want "file name.shp" to be "file%20name.shp",
            // or the "native type name" will also be "file%20name"
            Path parent = path.getParent();
            Path fileName = path.getFileName();
            url = new URL(String.format("file://%s/%s", parent.toAbsolutePath(), fileName.toString()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (isShapefile(path)) {
            params.put(ShapefileDataStoreFactory.FILE_TYPE.key, "shapefile");
            params.put(ShapefileDataStoreFactory.URLP.key, url.toString());
            params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, "false");
            String codePage = loadCharsetFromCodePageSideCarFile(path);
            if (codePage != null) {
                params.put(ShapefileDataStoreFactory.DBFCHARSET.key, codePage);
            }
        } else if (isCsv(path)) {
            params.put(CSVDataStoreFactory.STRATEGYP.key, "AttributesOnly");
            params.put(CSVDataStoreFactory.URL_PARAM.key, url.toString());
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

    public static boolean isShapefile(@NonNull Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".shp");
    }

    public static boolean isCsv(@NonNull Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".csv");
    }

    private DataSourceType resolveDataSourceType(@NonNull Path path, Map<String, String> parameters) {
        if (isShapefile(path)) {
            return DataSourceType.SHAPEFILE;
        } else if (isCsv(path)) {
            return DataSourceType.CSV;
        }
        throw new UnsupportedOperationException("Only shapefiles are supported so far");
    }

    public void createDataStore(Map<String, String> connectionParams) throws IOException {

        DataStore ds = DataStoreFinder.getDataStore(connectionParams);
        if (ds == null) {
            throw new IOException("Unable to create datastore " + connectionParams);
        }
    }

    public void importDataset(@NonNull DatasetUploadState d, @NonNull Map<String, String> connectionParams,
            ProgressListener listener) throws IOException {
        final PublishSettings publishing = d.getPublishing();
        requireNonNull(publishing);
        requireNonNull(publishing.getImportedName(), "imported type name not provided");
        CoordinateReferenceSystem targetCRS = null;
        if (d.getFormat() != DataSourceType.CSV) {
            requireNonNull(publishing.getSrs(), "Dataset publish settings must provide the dataset's SRS");
            targetCRS = decodeCRS(publishing.getSrs());
        } else {
            // TODO: might need to be revisited and let
            // the user provide a custom CRS.
            targetCRS = DefaultGeographicCRS.WGS84;
        }

        final DataStore sourceStore = resolveSourceDataStore(d);
        final DataStore targetStore;
        try {
            targetStore = loadDataStore(connectionParams);
        } catch (IOException e) {
            sourceStore.dispose();
            throw e;
        }

        try {
            final String sourceNativeName = resolveTypeName(sourceStore, d.getName());
            final int featureCount = featureCount(sourceStore.getFeatureSource(sourceNativeName));
            try (FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(sourceStore, sourceNativeName,
                    targetCRS, publishing.getSrsReproject())) {

                final SimpleFeatureType sourceType = reader.getFeatureType();
                final String targetTypeName = publishing.getImportedName();
                SimpleFeatureStore target = createTarget(targetStore, targetTypeName, sourceType);
                importData(reader, target, featureCount, listener);
            }
        } finally {
            sourceStore.dispose();
            targetStore.dispose();
        }
    }

    private SimpleFeatureStore createTarget(DataStore targetStore, String targetTypeName, SimpleFeatureType sourceType)
            throws IOException {

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.init(sourceType);
        ftb.setName(targetTypeName);
        SimpleFeatureType targetType = ftb.buildFeatureType();

        createSchema(targetType, targetStore);
        return (SimpleFeatureStore) targetStore.getFeatureSource(targetTypeName);
    }

    private FeatureReader<SimpleFeatureType, SimpleFeature> getReader(DataStore sourceStore, String sourceNativeName,
            CoordinateReferenceSystem targetCRS, Boolean srsReproject) throws IOException {

        Query query = new Query(sourceNativeName);
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = sourceStore.getFeatureReader(query,
                Transaction.AUTO_COMMIT);
        final CoordinateReferenceSystem sourceCRS = reader.getFeatureType().getCoordinateReferenceSystem();

        if (!CRS.equalsIgnoreMetadata(targetCRS, sourceCRS)) {
            if (Boolean.TRUE.equals(srsReproject)) {
                throw new UnsupportedOperationException("Reprojection is not yet implemented");
            }
            try {
                reader = new ForceCoordinateSystemFeatureReader(reader, targetCRS);
            } catch (SchemaException e) {
                throw new IOException(e);
            }
        }
        return reader;
    }

    private CoordinateReferenceSystem decodeCRS(String srs) throws IOException {
        final CoordinateReferenceSystem targetCRS;
        try {
            targetCRS = CRS.decode(srs);
        } catch (FactoryException e) {
            throw new IOException(e);
        }
        return targetCRS;
    }

    private void importData(FeatureReader<SimpleFeatureType, SimpleFeature> source, SimpleFeatureStore target,
            int featureCount, ProgressListener listener) throws IOException {

        log.info("Uploading data to {}", target.getSchema().getTypeName());
        final String typeName = target.getSchema().getTypeName();
        final Stopwatch sw = Stopwatch.createStarted();

        source = new ProgressReportingSimpleFeatureReader(source, featureCount, listener);
        Transaction gtTx = new DefaultTransaction();
        try {
            target.setTransaction(gtTx);
            target.setFeatures(source);
            gtTx.commit();
            log.info("Data imported to {} in {}", typeName, sw.stop());
        } catch (IOException e) {
            log.error("Error importing data to {}", typeName, e);
            gtTx.rollback();
            throw e;
        } finally {
            gtTx.close();
        }
    }

    /**
     * {@link FeatureReader} decorator the reports progress to a
     * {@link ProgressListener} at {@link FeatureReader#next()}
     */
    @RequiredArgsConstructor
    static class ProgressReportingSimpleFeatureReader implements SimpleFeatureReader {

        private final FeatureReader<SimpleFeatureType, SimpleFeature> source;
        private final int featureCount;
        private final ProgressListener listener;

        private int count;
        private SimpleFeature last;

        public @Override SimpleFeatureType getFeatureType() {
            return source.getFeatureType();
        }

        public @Override SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
            if (last == null) {
                listener.started();
            }
            SimpleFeature f = source.next();
            if (featureCount > 0) {
                count++;
                float percent = (count * 100f) / featureCount;
                listener.progress(percent);
            }
            return f;
        }

        public @Override boolean hasNext() throws IOException {
            boolean hasNext = source.hasNext();
            if (!hasNext) {
                listener.progress(100f);
            }
            return hasNext;
        }

        public @Override void close() throws IOException {
            listener.complete();
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

    public @VisibleForTesting DataStore resolveSourceDataStore(@NonNull DatasetUploadState dataset) throws IOException {
        PublishSettings publishing = dataset.getPublishing();
        requireNonNull(publishing, "Dataset 'publishing' settings is null");
        requireNonNull(dataset.getAbsolutePath(), "Dataset file absolutePath not provided");

        Path path = Paths.get(dataset.getAbsolutePath());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Dataset absolutePath is not a file: " + path);
        }

        String encoding = "UTF-8";
        if (publishing.getEncoding() != null) {
            encoding = publishing.getEncoding();
            log.info("Loading source data from {} using requested character encoding {}", path, encoding);
        } else if (dataset.getEncoding() != null) {
            encoding = dataset.getEncoding();
            log.info("Loading source data from {} using native character encoding {}", path, encoding);
        } else {
            log.info("Loading source data from {} default fallback character encoding UTF-8", path);
        }
        Charset charset = encoding == null ? null : Charset.forName(encoding);

        Map params = resolveConnectionParameters(path);
        if (dataset.getFormat().equals(DataSourceType.SHAPEFILE)) {
            if (charset != null) {
                params.put(ShapefileDataStoreFactory.DBFCHARSET.key, charset.name());
            }
        } else if (dataset.getFormat().equals(DataSourceType.CSV)) {
            // https://docs.geotools.org/stable/userguide/library/data/csv.html
            params.put(CSVDataStoreFactory.STRATEGYP.key, CSVDataStoreFactory.ATTRIBUTES_ONLY_STRATEGY);
            Map providedParams = dataset.getPublishing().getOptions();
            if (providedParams != null) {
                if (providedParams.containsKey("latField") && providedParams.containsKey("lngField")) {
                    params.put(CSVDataStoreFactory.STRATEGYP.key, CSVDataStoreFactory.SPECIFC_STRATEGY);
                    params.put(CSVDataStoreFactory.LATFIELDP.key, providedParams.get("latField"));
                    params.put(CSVDataStoreFactory.LnGFIELDP.key, providedParams.get("lngField"));
                }
                String quoteChar = (String) providedParams.get("quoteChar");
                String sep = (String) providedParams.get("delimiter");
                if (quoteChar != null) {
                    params.put(CSVDataStoreFactory.QUOTECHAR.key, quoteChar.charAt(0));
                }
                if (sep != null) {
                    params.put(CSVDataStoreFactory.SEPERATORCHAR.key, sep.charAt(0));
                }
            }
        } else {
            throw new RuntimeException(
                    String.format("Format '%s' not managed by the Datafeeder.", dataset.getFormat()));
        }

        return loadDataStore(params);
    }

    private String resolveTypeName(DataStore store, final String sourceNativeName) throws IOException {
        {
            String[] typeNames = store.getTypeNames();
            if (!Arrays.asList(typeNames).contains(sourceNativeName)) {
                throw new IllegalArgumentException("Dataset name '" + sourceNativeName
                        + "' does not exist. Type names in provided file: " + Arrays.toString(typeNames));
            }
        }
        return sourceNativeName;
    }
}
