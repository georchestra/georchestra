/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.mapfishapp.ws.upload;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONString;
import org.json.JSONWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONStreamAware;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.base.Preconditions;

/**
 * This is a workaround to fix the problem found in the CRS generation. For more
 * details about this fix see
 * {@link #writeFeatureCollection(FeatureCollection, Object)}
 *
 * @author Mauricio Pazos
 */
final class FeatureJSON2 extends FeatureJSON {

    private static final Log LOG = LogFactory.getLog(FeatureJSON2.class.getPackage().getName());

    private GeometryJSON gjson;

    public FeatureJSON2() {
        this(new GeometryJSON());
    }

    public FeatureJSON2(GeometryJSON gjson) {
        super(gjson);
        this.gjson = gjson;
    }

    /**
     * Override to encode the CRS from the feature collection schema instead of its
     * bounds, in case bounds is {@code null}, but the CRS still needs to be encoded
     *
     * @param features The feature collection.
     * @param output   The output. See {@link GeoJSONUtil#toWriter(Object)} for
     *                 details.
     */
    @SuppressWarnings("rawtypes")
    public @Override void writeFeatureCollection(FeatureCollection features, Object output) throws IOException {
        Preconditions.checkArgument(features instanceof SimpleFeatureCollection);
        LinkedHashMap<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("type", "FeatureCollection");
        final ReferencedEnvelope bounds = features.getBounds();

        if (bounds != null && isEncodeFeatureCollectionBounds()) {
            obj.put("bbox", new JSONStreamAware() {
                public void writeJSONString(Writer out) throws IOException {
                    JSONArray.writeJSONString(
                            Arrays.asList(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()), out);
                }
            });
        }
        CoordinateReferenceSystem crs = features.getSchema().getCoordinateReferenceSystem();
        if (crs != null && (isEncodeFeatureCollectionCRS() || isStandardCRS(crs))) {
            obj.put("crs", toMap(crs));
        }

        obj.put("features", new FeatureCollectionEncoder((SimpleFeatureCollection) features, gjson));
        GeoJSONUtil.encode(obj, output);
    }

    /**
     * Check for GeoJSON default (EPSG:4326 in easting/northing order).
     *
     * @return true if crs is the default for GeoJSON
     * @throws NoSuchAuthorityCodeException
     * @throws FactoryException
     */
    private boolean isStandardCRS(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return true;
        }
        try {
            boolean longitudeFirst = true;
            CoordinateReferenceSystem standardCRS = CRS.decode("EPSG:4326", longitudeFirst);
            return CRS.equalsIgnoreMetadata(crs, standardCRS);
        } catch (Exception unexpected) {
            return false; // no way to tell
        }
    }

    /**
     * Create a properties map for the provided crs.
     *
     * @param crs CoordinateReferenceSystem or null for default
     * @return properties map naming crs identifier
     * @throws IOException
     */
    private Map<String, Object> toMap(CoordinateReferenceSystem crs) throws IOException {
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("type", "name");

        Map<String, Object> props = new LinkedHashMap<String, Object>();
        if (crs == null) {
            props.put("name", "EPSG:4326");
        } else {
            try {
                String identifier = CRS.lookupIdentifier(crs, true);
                props.put("name", identifier);
            } catch (FactoryException e) {
                throw (IOException) new IOException("Error looking up crs identifier").initCause(e);
            }
        }
        obj.put("properties", props);
        return obj;
    }

    class FeatureEncoder implements JSONStreamAware {

        SimpleFeatureType featureType;
        SimpleFeature feature;

        public FeatureEncoder(SimpleFeature feature) {
            this(feature.getType());
            this.feature = feature;
        }

        public FeatureEncoder(SimpleFeatureType featureType) {
            this.featureType = featureType;
        }

        public FeatureEncoder feature(SimpleFeature feature) {
            this.feature = feature;
            return this;
        }

        public @Override void writeJSONString(Writer out) throws IOException {
            try {
                writeJSON(new JSONWriter(out));
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }

        public void writeJSON(JSONWriter writer) throws IOException, JSONException {
            writer.object();
            writer.key("type").value("Feature");
            writer.key("id").value(feature.getID());
            writeCrs(writer);
            writeBounds(writer);
            writeGeometry(writer);
            writeProperties(writer);
            writer.endObject();
        }

        private void writeProperties(JSONWriter writer) throws JSONException {
            writer.key("properties");
            writer.object();

            GeometryDescriptor defaultGeometry = featureType.getGeometryDescriptor();
            Predicate<Property> predicate = p -> !p.getDescriptor().getName().equals(defaultGeometry.getName());

            for (Property p : feature.getProperties()) {
                if (predicate.test(p)) {
                    writeProperty(writer, p);
                }
            }

            writer.endObject();
        }

        private void writeProperty(JSONWriter writer, Property p) throws JSONException {
            final Object value = p.getValue();
            if (value == null && !!isEncodeNullValues()) {
                return;
            }

            final PropertyDescriptor descriptor = p.getDescriptor();
            final String propertyName = descriptor.getName().getLocalPart();

            // handle special types separately, everything else as a string or literal (is
            // it?)
            if (value instanceof Envelope) {
                writeVerbatimValue(writer, propertyName, gjson.toString((Envelope) value));
            } else if (value instanceof BoundingBox) {
                writeVerbatimValue(writer, propertyName, gjson.toString((BoundingBox) value));
            } else if (value instanceof Geometry) {
                writeVerbatimValue(writer, propertyName, gjson.toString((Geometry) value));
            } else if (value != null || isEncodeNullValues()) {
                writer.key(propertyName).value(value);
            }
        }

        private void writeGeometry(JSONWriter writer) throws JSONException {
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            // note: this is still too much for streaming, gjson.toString(geometry) builds a
            // Map and then converts to String
            String value = geometry == null ? null : gjson.toString(geometry);
            writeVerbatimValue(writer, "geometry", value);
        }

        private void writeBounds(JSONWriter writer) throws JSONException {
            if (isEncodeFeatureBounds()) {
                BoundingBox bbox = feature.getBounds();
                String value = bbox == null ? null : gjson.toString(bbox);
                writeVerbatimValue(writer, "bbox", value);
            }
        }

        private void writeCrs(JSONWriter writer) throws IOException, JSONException {
            if (isEncodeFeatureCRS()) {
                CoordinateReferenceSystem crs = featureType.getCoordinateReferenceSystem();
                writeVerbatimValue(writer, "crs", crs == null ? null : FeatureJSON2.this.toString(crs));
            }
        }

        private void writeVerbatimValue(JSONWriter writer, String key, @Nullable String value) throws JSONException {
            if (value != null) {
                JSONString verbatim = () -> value;
                writer.key(key).value(verbatim);
            } else if (isEncodeNullValues()) {
                writer.key(key).value(null);
            }
        }
    }

    class FeatureCollectionEncoder implements JSONStreamAware {

        SimpleFeatureCollection features;
        GeometryJSON gjson;

        public FeatureCollectionEncoder(SimpleFeatureCollection features, GeometryJSON gjson) {
            this.features = features;
            this.gjson = gjson;
        }

        public @Override void writeJSONString(Writer out) throws IOException {
            SimpleFeatureType ft = (SimpleFeatureType) features.getSchema();
            FeatureEncoder featureEncoder = new FeatureEncoder(ft);
            JSONWriter writer = new JSONWriter(out);
            try (FeatureIterator<SimpleFeature> featureIterator = features.features()) {
                writer.array();
                while (featureIterator.hasNext()) {
                    featureEncoder.feature(featureIterator.next()).writeJSON(writer);
                }
                writer.endArray();
            } catch (JSONException e) {
                String msg = "Unable to generate JSON: " + e.getMessage();
                LOG.error(msg);
                throw new IOException(msg, e);
            }
        }
    }

}
