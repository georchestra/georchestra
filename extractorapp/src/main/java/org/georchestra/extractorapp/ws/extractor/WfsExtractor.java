package org.georchestra.extractorapp.ws.extractor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ReTypingFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.GeoTools;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.google.common.collect.ImmutableSet;

/**
 * Obtains data from a WFS and write the data out to the filesystem
 *
 * @author jeichar
 */
public class WfsExtractor {

    protected static final Log LOG = LogFactory.getLog(WcsExtractor.class.getPackage().getName());

    private static final FilterFactory2 FILTER_FACTORY = CommonFactoryFinder
            .getFilterFactory2(GeoTools.getDefaultHints());
    private static final Set<String> SUPPORTED_FORMATS = ImmutableSet.of("shp", "mif", "tab", "kml");

    private final File _basedir;
    private final String _adminUsername;
    private final String _adminPassword;
    private final String _secureHost;
    private String userAgent;

    /**
     *
     * Should only be used by tests
     *
     */
    public WfsExtractor(File basedir) throws IOException {
        this(basedir, "", "", "localhost", null);
    }

    /**
     *
     * @param basedir       the directory that the extracted files will be written
     *                      in
     * @param adminUsername username that give admin access to geoserver
     * @param adminPassword password the the admin user
     * @param secureHost
     */
    public WfsExtractor(File basedir, String adminUsername, String adminPassword, String secureHost, String userAgent) {
        this._basedir = basedir;
        this._adminPassword = adminPassword;
        this._adminUsername = adminUsername;
        this._secureHost = secureHost;
        this.userAgent = userAgent;
    }

    public void checkPermission(ExtractorLayerRequest request, String secureHost, String username, String roles)
            throws IOException {
        URL capabilitiesURL = request.capabilitiesURL("WFS", "1.0.0");

        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setUserAgent(this.userAgent);

        HttpClientContext localContext = HttpClientContext.create();
        final HttpHost httpHost = new HttpHost(capabilitiesURL.getHost(), capabilitiesURL.getPort(),
                capabilitiesURL.getProtocol());

        HttpGet get = new HttpGet(capabilitiesURL.toExternalForm());
        if (username != null && (secureHost.equalsIgnoreCase(request._url.getHost())
                || "127.0.0.1".equalsIgnoreCase(request._url.getHost())
                || "localhost".equalsIgnoreCase(request._url.getHost()))) {
            LOG.debug(
                    "WfsExtractor.checkPermission - Secured Server: adding username header and role headers to request for checkPermission");

            addImpersonateUserHeaders(username, roles, get);

            enablePreemptiveBasicAuth(capabilitiesURL, httpClientBuilder, localContext, httpHost, _adminUsername,
                    _adminPassword);
        } else {
            // use a user agent that does *not* trigger basic auth on remote server
            httpClientBuilder.setUserAgent("Apache-HttpClient");
            LOG.debug("WfsExtractor.checkPermission - Non Secured Server");
        }

        final CloseableHttpClient httpclient = httpClientBuilder.build();
        String capabilities = FileUtils
                .asString(httpclient.execute(httpHost, get, localContext).getEntity().getContent());
        Pattern regex = Pattern.compile("(?m)<FeatureType[^>]*>(\\\\n|\\s)*<Name>\\s*(\\w*:)?"
                + Pattern.quote(request._layerName) + "\\s*</Name>");
        boolean permitted = regex.matcher(capabilities).find();

        if (!permitted) {
            throw new SecurityException("User does not have sufficient privileges to access the Layer: "
                    + request._layerName + ". \n\nCapabilities:  " + capabilities);
        }
    }

    public static void addImpersonateUserHeaders(String username, String roles, HttpGet get) {
        get.addHeader("imp-username", username);
        if (roles != null)
            get.addHeader("imp-roles", roles);
    }

    public static void enablePreemptiveBasicAuth(URL capabilitiesURL, HttpClientBuilder httpClientBuilder,
            HttpClientContext localContext, HttpHost httpHost, String adminUsername, String adminPassword) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(capabilitiesURL.getHost(), capabilitiesURL.getPort()),
                new UsernamePasswordCredentials(adminUsername, adminPassword));
        httpClientBuilder.setDefaultCredentialsProvider(credsProvider);

        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(httpHost, basicAuth);

        // Add AuthCache to the execution context
        localContext.setAuthCache(authCache);
    }

    /**
     * Extract the data as defined in the request object.
     *
     * @return the directory that contains the extracted file
     */
    public File extract(ExtractorLayerRequest request) throws IOException, TransformException, FactoryException {
        checkNotNull(request);
        checkNotNull(request._bbox, "Bounding box not specified");
        checkNotNull(request.getWFSName(), "WFS layer name not specified");
        checkArgument(request._owsType == OWSType.WFS, request._owsType + "must be WFS for the WfsExtractor");
        checkArgument(request._format != null && SUPPORTED_FORMATS.contains(request._format.toLowerCase()),
                "%s is not a recognized vector format", request._format);

        final File basedir = request.createContainingDir(_basedir);

        // TODO: dispose datastore
        DataStore sourceDs = resolveDataStore(request);
        try {
            SimpleFeatureSource featureSource = resolveFeatureSource(sourceDs, request);
            SimpleFeatureCollection features = getFeatures(request, featureSource);

            FeatureWriterStrategy featuresWriter = resolveFeatureWriter(request._format, basedir, features);
            BBoxWriter bboxWriter = new BBoxWriter(request._bbox, basedir, request._projection);

            // generates the feature files and bbox file
            featuresWriter.generateFiles();
            bboxWriter.generateFiles();
        } finally {
            sourceDs.dispose();
        }
        return basedir;
    }

    private FeatureWriterStrategy resolveFeatureWriter(String format, File basedir, SimpleFeatureCollection features) {
        switch (format.toLowerCase()) {
        case "shp":
            return new ShpFeatureWriter(basedir, features);
        case "kml":
            return new KMLFeatureWriter(basedir, features);
        default:
            throw new IllegalStateException("Shouldn't happen, aldready checked format is in SUPPORTED_FORMATS");
        }
    }

    private SimpleFeatureCollection removeTypeNamePrefix(SimpleFeatureCollection features) {
        SimpleFeatureType origSchema = features.getSchema();
        String typeName = origSchema.getTypeName();
        checkArgument(typeName.contains(":"));
        SimpleFeatureType renamedType;
        try {
            typeName = typeName.substring(typeName.indexOf(':') + 1);
            String[] allProperties = origSchema.getTypes().stream().map(AttributeType::getName).map(Name::getLocalPart)
                    .toArray(String[]::new);
            CoordinateReferenceSystem override = null;
            URI namespace = null;
            renamedType = DataUtilities.createSubType(origSchema, allProperties, override, typeName, namespace);
        } catch (SchemaException e) {
            throw new IllegalStateException(e);
        }
        return new ReTypingFeatureCollection(features, renamedType);
    }

    private SimpleFeatureSource resolveFeatureSource(DataStore sourceDs, ExtractorLayerRequest request)
            throws IOException {
        String requestTypeName = request.getWFSName();
        SimpleFeatureSource featureSource = null;
        // prefixed typeName
        if (requestTypeName.contains(":")) {
            featureSource = sourceDs.getFeatureSource(requestTypeName);
        } else {
            // Not prefixed one (mapserver ?)
            String[] typeNames = sourceDs.getTypeNames();
            for (String typeName : typeNames) {
                if (typeName.contains(requestTypeName)) {
                    featureSource = sourceDs.getFeatureSource(typeName);
                    break;
                }
            }
            if (featureSource == null) {
                throw new IOException("Unable to find the remote layer " + requestTypeName);
            }
        }
        return featureSource;
    }

    private DataStore resolveDataStore(ExtractorLayerRequest request) throws IOException {
        final Map<String, Serializable> params = buildWFSConnectionParameters(request);

        String requestedTypeName = request.getWFSName();
        DataStore sourceDs = null;
        // prefixed typeName
        if (requestedTypeName.contains(":")) {
            sourceDs = DataStoreFinder.getDataStore(params);
        } else {
            // Not prefixed one (mapserver ?)
            // Recreating the datastore forcing wfs 1.1.0, so that (presuming
            // the remote server is actually powered by MapServer), we would
            // have a typename prefixed with the same convention as before.
            params.put(WFSDataStoreFactory.URL.key, request.capabilitiesURL("WFS", "1.1.0"));
            // params.put(WFSDataStoreFactory.WFS_STRATEGY.key, "mapserver");
            sourceDs = DataStoreFinder.getDataStore(params);
        }
        if (sourceDs == null) {
            throw new IllegalStateException("Unable to connect to WFS " + params.get(WFSDataStoreFactory.URL.key));
        }
        return sourceDs;
    }

    private Map<String, Serializable> buildWFSConnectionParameters(ExtractorLayerRequest request) {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        try {
            params.put(WFSDataStoreFactory.URL.key, request.capabilitiesURL("WFS", "1.0.0"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        params.put(WFSDataStoreFactory.LENIENT.key, true);
        params.put(WFSDataStoreFactory.PROTOCOL.key, true);
        params.put(WFSDataStoreFactory.TIMEOUT.key, Integer.valueOf(60000));
        params.put(WFSDataStoreFactory.MAXFEATURES.key, Integer.valueOf(0));

        // HACK I want unrestricted access to layers.
        // Security check takes place in ExtractorThread
        if (_secureHost.equalsIgnoreCase(request._url.getHost()) || "127.0.0.1".equalsIgnoreCase(request._url.getHost())
                || "localhost".equalsIgnoreCase(request._url.getHost())) {
            LOG.debug("WfsExtractor.extract - Secured Server: Adding extractionUserName to connection params");
            if (_adminUsername != null)
                params.put(WFSDataStoreFactory.USERNAME.key, _adminUsername);
            if (_adminPassword != null)
                params.put(WFSDataStoreFactory.PASSWORD.key, _adminPassword);
        } else {
            LOG.debug("WfsExtractor.extract - Non Secured Server");
        }
        return params;
    }

    private SimpleFeatureCollection getFeatures(ExtractorLayerRequest request, SimpleFeatureSource featureSource)
            throws IOException, TransformException, FactoryException {

        Query query = createQuery(request, featureSource.getSchema());
        SimpleFeatureCollection features = featureSource.getFeatures(query);

        CoordinateReferenceSystem returnedCrs = features.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem targetCrs = request._projection;
        // current version (9.2) of WFS datastore does not perform reprojection
        if (!CRS.equalsIgnoreMetadata(targetCrs, returnedCrs)) {
            features = new ReprojectingFeatureCollection(features, targetCrs);
        }

        if (features.getSchema().getTypeName().contains(":")) {
            features = removeTypeNamePrefix(features);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of features returned : " + features.size());
        }
        return features;
    }

    private Query createQuery(ExtractorLayerRequest request, SimpleFeatureType schema)
            throws TransformException, FactoryException {

        final Filter filter;
        final String[] properties;
        if (null == schema.getGeometryDescriptor()) {
            filter = Filter.EXCLUDE;
            properties = Query.ALL_NAMES;
        } else {
            final CoordinateReferenceSystem nativeCrs = schema.getCoordinateReferenceSystem();
            GeometryDescriptor defGeom = schema.getGeometryDescriptor();
            PropertyName propertyName = FILTER_FACTORY.property(defGeom.getLocalName());
            ReferencedEnvelope bbox = request._bbox;
            // bbox may not be in the same projection as the data so it sometimes necessary
            // to reproject the request BBOX
            if (!CRS.equalsIgnoreMetadata(nativeCrs, bbox.getCoordinateReferenceSystem())) {
                bbox = bbox.transform(nativeCrs, true, 10);
            }
            Polygon bboxGeom = JTS.toGeometry(bbox);
            filter = FILTER_FACTORY.intersects(propertyName, FILTER_FACTORY.literal(bboxGeom));
            properties = schema.getDescriptors().stream()//
                    // shapefiles can only have one geometry so skip any
                    // geometry descriptor that is not the default
                    .filter(d -> d instanceof GeometryDescriptor ? d.equals(defGeom) : true)//
                    .map(d -> d.getName().getLocalPart())//
                    .toArray(String[]::new);
        }
        Query query = new Query(schema.getTypeName(), filter, properties);

        return query;
    }
}
