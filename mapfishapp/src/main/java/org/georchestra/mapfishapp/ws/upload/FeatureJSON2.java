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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONStreamAware;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
     * REDEFINED Writes a feature collection as GeoJSON.
     *
     * @param features The feature collection.
     * @param output   The output. See {@link GeoJSONUtil#toWriter(Object)} for
     *                 details.
     */
    public @Override void writeFeatureCollection(@SuppressWarnings("rawtypes") FeatureCollection features,
            Object output) throws IOException {
        LinkedHashMap<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("type", "FeatureCollection");
        if (isEncodeFeatureCollectionBounds() || isEncodeFeatureCollectionCRS()) {

            final ReferencedEnvelope bounds = features.getBounds();

            if (isEncodeFeatureCollectionBounds() && bounds != null) {

                obj.put("bbox", new JSONStreamAware() {

                    public void writeJSONString(Writer out) throws IOException {
                        JSONArray.writeJSONString(
                                Arrays.asList(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()),
                                out);
                    }
                });
            }
//         This return the crs present in the store it is necessary the crs present in the feature collection's schema
//          if (encodeFeatureCollectionCRS) {
//              obj.put("crs", createCRS(bounds.getCoordinateReferenceSystem()));
//          }
            if (isEncodeFeatureCollectionCRS()) {
                CoordinateReferenceSystem coordinateReferenceSystem = features.getSchema()
                        .getCoordinateReferenceSystem();
                if (coordinateReferenceSystem != null) // FIXME have a look at this
                    obj.put("crs", toMap(coordinateReferenceSystem));
            }

        }
        obj.put("features", new FeatureCollectionEncoder(features, gjson));
        GeoJSONUtil.encode(obj, output);
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

    class FeatureEncoder implements JSONAware {

        SimpleFeatureType featureType;
        SimpleFeature feature;

        public FeatureEncoder(SimpleFeature feature) {
            this(feature.getType());
            this.feature = feature;
        }

        public FeatureEncoder(SimpleFeatureType featureType) {
            this.featureType = featureType;
        }

        public String toJSONString(SimpleFeature feature) {
            try {
                JSONObject ret = new JSONObject();
                ret.put("type", "Feature");
                // crs
                if (isEncodeFeatureCRS()) {
                    CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
                    if (crs != null) {
                        try {
                            ret.put("crs", FeatureJSON2.this.toString(crs));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // bounding box
                if (isEncodeFeatureBounds()) {
                    BoundingBox bbox = feature.getBounds();
                    ret.put("bbox", gjson.toString(bbox));
                }

                // geometry
                if (feature.getDefaultGeometry() != null) {
                    JSONObject geom = new JSONObject(gjson.toString((Geometry) feature.getDefaultGeometry()));
                    ret.put("geometry", geom);
                }

                // properties
                int gindex = featureType.getGeometryDescriptor() != null
                        ? featureType.indexOf(featureType.getGeometryDescriptor().getLocalName())
                        : -1;

                JSONObject jsProp = new JSONObject();

                for (int i = 0; i < featureType.getAttributeCount(); i++) {
                    AttributeDescriptor ad = featureType.getDescriptor(i);

                    // skip the default geometry, it's already encoded
                    if (i == gindex) {
                        continue;
                    }

                    Object value = feature.getAttribute(i);

                    if (!isEncodeNullValues() && value == null) {
                        // skip
                        continue;
                    }

                    // handle special types separately, everything else as a string or literal
                    if (value instanceof Envelope) {
                        jsProp.put(ad.getLocalName(), gjson.toString((Envelope) value));
                    } else if (value instanceof BoundingBox) {
                        jsProp.put(ad.getLocalName(), gjson.toString((BoundingBox) value));
                    } else if (value instanceof Geometry) {
                        jsProp.put(ad.getLocalName(), gjson.toString((Geometry) value));
                    } else {
                        jsProp.put(ad.getLocalName(), value);
                    }
                }

                ret.put("properties", jsProp);

                ret.put("id", feature.getID());

                return ret.toString(4);

            } catch (JSONException e) {
                LOG.error("Unable to encode the feature into GeoJSON, returning an empty object.");
                return "{}";
            }
        }

        public String toJSONString() {
            return toJSONString(feature);
        }
    }

    @SuppressWarnings("rawtypes")
    class FeatureCollectionEncoder implements JSONStreamAware {

        FeatureCollection features;
        GeometryJSON gjson;

        public FeatureCollectionEncoder(FeatureCollection features, GeometryJSON gjson) {
            this.features = features;
            this.gjson = gjson;
        }

        public void writeJSONString(Writer out) throws IOException {
            SimpleFeatureType ft = (SimpleFeatureType) features.getSchema();
            FeatureEncoder featureEncoder = new FeatureEncoder(ft);
            JSONWriter jsRet = new JSONWriter(out);
            try {
                jsRet.array();
                FeatureIterator i = features.features();
                try {
                    if (i == null) {
                        jsRet.endArray();
                        return;
                    }
                    while (i.hasNext()) {
                        Feature f = i.next();
                        try {
                            if (f instanceof SimpleFeature) {
                                jsRet.value(new JSONObject(featureEncoder.toJSONString((SimpleFeature) f)));
                            }
                        } catch (NullPointerException e) {
                            LOG.error("Unable to convert feature into JSON, skipping it. " + e.getMessage());
                        }
                    }
                } catch (Throwable e) {
                    LOG.error("Unable to convert the featurecollection into JSON: " + e.getMessage());
                    LOG.error("Ignoring ...");
                } finally {
                    if (i != null) {
                        i.close();
                    }
                    jsRet.endArray();
                }

            } catch (JSONException e) {
                LOG.error("Unable to generate JSON: " + e.getMessage());
            }
        }
    }

}
