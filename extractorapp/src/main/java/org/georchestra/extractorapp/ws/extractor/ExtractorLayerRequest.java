/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Encapsulates all the parameters of an extractor request for one layer
 * 
 * @author jeichar
 */
public class ExtractorLayerRequest {
    static final String EMAILS_KEY = "emails";
    static final String LAYERS_KEY = "layers";
    static final String NAMESPACE_KEY = "namespace";
    static final String GLOBAL_PROPS_KEY = "globalProperties";
    public static final String URL_KEY = "owsUrl";
    public static final String TYPE_KEY = "owsType";
    public static final String LAYER_NAME_KEY = "layerName";
    public static final String PROJECTION_KEY = "projection";
    public static final String FORMAT_KEY = "format";
    static final String VECTOR_FORMAT_KEY = "vectorFormat";
    static final String RASTER_FORMAT_KEY = "rasterFormat";
    public static final String BBOX_KEY = "bbox";
    public static final String BBOX_SRS_KEY = "srs";
    public static final String BBOX_VALUE_KEY = "value";
    static final String RESOLUTION_KEY = "resolution";
    public static final String ISO_METADATA_URL_KEY = "isoMetadataURL";

    public final String[] _emails;
    public final URL _url;
    public final CoordinateReferenceSystem _projection;
    public final OWSType _owsType;
    public final String _format;
    public final ReferencedEnvelope _bbox;
    public final String _epsg;
    public final String _layerName;
    public final String _namespace;
    public final double _resolution;
    public final URL _isoMetadataURL;

    private final JSONObject _layerJson;
    private final JSONObject _globalJson;
    private String _wfsName;
    private Long dbLogId;

    public ExtractorLayerRequest(JSONObject layerJson, JSONObject globalJson, JSONArray emails)
            throws NoSuchAuthorityCodeException, FactoryException, MalformedURLException, JSONException {
        _layerJson = layerJson;
        _globalJson = globalJson;
        _emails = parseEmails(emails);
        _url = parseURL();
        _epsg = parseProjection();
        _projection = CRS.decode(_epsg);
        _owsType = parseType();
        _format = parseFormat();
        _bbox = parseBbox();
        _resolution = parseResolution();
        _layerName = parseLayerName();
        _namespace = parseNameSpace();
        _isoMetadataURL = parseIsoMetadataURL();
    }

    /**
     * Convert the _url to a capabilities url
     * 
     * @param service the type of service to access
     * @param version the version of the ows
     */
    public URL capabilitiesURL(String service, String version) throws MalformedURLException {

        String externalForm = _url.toExternalForm();

        String versionParam = version == null ? "" : "&VERSION=" + version;

        String query = "REQUEST=GETCAPABILITIES&SERVICE=" + service + versionParam;

        if (externalForm.endsWith("?")) {
            return new URL(externalForm + query);
        } else if (externalForm.contains("?")) {
            return new URL(externalForm + "&" + query);
        } else {
            return new URL(externalForm + "?" + query);
        }
    }

    /**
     * Create the directory to extract this layer to
     */
    public File createContainingDir(File basedir) {
        String dirname = FileUtils.toSafeFileName(_url.getHost() + "_" + _layerName);
        File dir = new File(basedir, dirname);

        for (int i = 1; dir.exists(); i++) {
            dir = new File(basedir, dirname + "_" + i);
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Unable to create " + basedir.getAbsolutePath());
        }
        return dir;
    }

    /** --------------- Factory methods ------------------ */
    /**
     * Create a list of requests from jsonData. One per layer requested
     * 
     * @param jsonData
     */
    public static List<ExtractorLayerRequest> parseJson(String jsonData)
            throws JSONException, NoSuchAuthorityCodeException, FactoryException, MalformedURLException {
        JSONObject jsonObject = parseStringToJSon(jsonData);
        JSONArray emails = jsonObject.getJSONArray(EMAILS_KEY);
        JSONArray jsonLayers = jsonObject.getJSONArray(LAYERS_KEY);
        JSONObject globalProps = jsonObject.getJSONObject(GLOBAL_PROPS_KEY);

        ArrayList<ExtractorLayerRequest> layers = new ArrayList<ExtractorLayerRequest>();
        for (int i = 0; i < jsonLayers.length(); i++) {
            ExtractorLayerRequest layer = new ExtractorLayerRequest(jsonLayers.getJSONObject(i), globalProps, emails);
            layers.add(layer);
        }
        return layers;
    }

    /**
     * Takes a string and parses it to a JSON object structure
     */
    public static JSONObject parseStringToJSon(String jsonData) throws JSONException {
        final String trimmed = jsonData.trim();
        final JSONTokener tokener = new JSONTokener(trimmed);
        JSONObject jsonObject = new JSONObject(tokener);
        return jsonObject;
    }

    /* --------------- Private methods ------------------ */

    private String[] parseEmails(JSONArray jsonEmails) throws JSONException {
        String[] emails = new String[jsonEmails.length()];
        for (int i = 0; i < emails.length; i++) {
            emails[i] = jsonEmails.getString(i);
        }
        return emails;
    }

    private URL parseURL() throws MalformedURLException, JSONException {
        String url = _layerJson.getString(URL_KEY);
        return new URL(url);
    }

    private URL parseIsoMetadataURL() throws MalformedURLException {

        try {
            String metadataURL = _layerJson.getString(ISO_METADATA_URL_KEY);

            return new URL(metadataURL);

        } catch (JSONException e) {

            return null;
        }

    }

    private String parseProjection() throws JSONException {
        String projection = get(PROJECTION_KEY, PROJECTION_KEY, false).toString();
        return projection;
    }

    private Object get(String layerKey, String globalKey, boolean optional) throws JSONException {
        Object obj = _layerJson.opt(layerKey);
        if (obj == null || "null".equalsIgnoreCase(obj.toString())) {
            if (optional) {
                obj = _globalJson.opt(globalKey);
            } else {
                obj = _globalJson.get(globalKey);
            }
        }

        return obj;
    }

    private OWSType parseType() throws JSONException {
        return OWSType.valueOf(_layerJson.getString(TYPE_KEY));
    }

    private String parseFormat() throws JSONException {
        String format;
        switch (_owsType) {
        case WFS:
            format = get(FORMAT_KEY, VECTOR_FORMAT_KEY, false).toString();
            break;
        case WCS:
            format = get(FORMAT_KEY, RASTER_FORMAT_KEY, false).toString();
            break;
        default:
            throw new Error("UNSUPPORTED OWS TYPE:" + _owsType);
        }
        return format;
    }

    private double parseResolution() throws JSONException {
        Object value = (Object) get(RESOLUTION_KEY, RESOLUTION_KEY, true);
        if (value == null) {
            return -1;
        } else if (value instanceof Integer) {
            Integer i = (Integer) value;
            return i.doubleValue();
        } else if (value instanceof Float) {
            Float f = (Float) value;
            return f.doubleValue();
        } else {
            return (Double) value;
        }
    }

    private ReferencedEnvelope parseBbox() throws JSONException, NoSuchAuthorityCodeException, FactoryException {
        JSONObject bbox = (JSONObject) get(BBOX_KEY, BBOX_KEY, false);

        String srs = bbox.getString(BBOX_SRS_KEY);
        CoordinateReferenceSystem crs = CRS.decode(srs);

        JSONArray values = bbox.getJSONArray(BBOX_VALUE_KEY);
        double minx = values.getDouble(0);
        double miny = values.getDouble(1);
        double maxx = values.getDouble(2);
        double maxy = values.getDouble(3);
        Envelope env = new Envelope(minx, maxx, miny, maxy);
        return new ReferencedEnvelope(env, crs);
    }

    private String parseLayerName() throws JSONException {
        return _layerJson.getString(LAYER_NAME_KEY);
    }

    private String parseNameSpace() {
        try {
            return _layerJson.getString(NAMESPACE_KEY);
        } catch (JSONException e) {
            return null;
        }
    }

    public void setWFSName(String name) {
        this._wfsName = name;
    }

    public String getWFSName() {
        return _wfsName == null ? _layerName : _wfsName;
    }

    public void setDbLogId(Long dbLogId) {
        this.dbLogId = dbLogId;
    }

    public Long getDbLogId() {
        return dbLogId;
    }
}
