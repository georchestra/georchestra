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

package org.georchestra.ogcservstatistics.log4j;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.DATE_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.LAYER_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.ORG_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.REQUEST_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.SECROLE_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.SERVICE_COLUMN;
import static org.georchestra.ogcservstatistics.dataservices.LogColumns.USER_COLUMN;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This parse recognizes an OGC service taking into account the syntax
 * convention implemented by {@link OGCServiceMessageFormatter}.
 *
 * @author Mauricio Pazos
 *
 */
public final class OGCServiceParser {

    // service types
    private static final String WFS = "WFS";
    private static final String WMS = "WMS";
    private static final String WCS = "WCS";
    private static final String WMTS = "WMTS";

    private static Map<Pattern, String> SERVICE_PATTERNS = new HashMap<>();
    static {
        SERVICE_PATTERNS.put(compile(".*\\bservice=wms\\b.*", CASE_INSENSITIVE), WMS);
        SERVICE_PATTERNS.put(compile(".*\\bWMS\\?\\n*[\\s\\S]*", CASE_INSENSITIVE), WMS);
        SERVICE_PATTERNS.put(compile(".*\\bservice=wfs\\b.*", CASE_INSENSITIVE), WFS);
        SERVICE_PATTERNS.put(compile(".*\\bWFS\\?\\n*[\\s\\S]*", CASE_INSENSITIVE), WFS);
        SERVICE_PATTERNS.put(compile(".*\\bWFSDISPATCHER\\?\\n*[\\s\\S]*", CASE_INSENSITIVE), WFS);
        SERVICE_PATTERNS.put(compile(".*\\bservice=wcs\\b.*", CASE_INSENSITIVE), WCS);
        SERVICE_PATTERNS.put(compile(".*\\bWCS\\?\\n*[\\s\\S]*", CASE_INSENSITIVE), WCS);
        SERVICE_PATTERNS.put(compile(".*\\bservice=wmts\\b.*", CASE_INSENSITIVE), WMTS);
    }

    // request type
    private static final String GETCAPABILITIES = "GETCAPABILITIES";
    private static final String GETMAP = "GETMAP";
    private static final String GETLEGENDGRAPHIC = "GETLEGENDGRAPHIC";
    private static final String GETFEATUREINFO = "GETFEATUREINFO";
    private static final String DESCRIBELAYER = "DESCRIBELAYER";
    private static final String GETFEATURE = "GETFEATURE";
    private static final String DESCRIBEFEATURETYPE = "DESCRIBEFEATURETYPE";
    private static final String GETCOVERAGE = "GETCOVERAGE";
    private static final String DESCRIBECOVERAGE = "DESCRIBECOVERAGE";
    private static final String GETTILE = "GETTILE";
    private static final String GETSTYLES = "GETSTYLES";

    // WFS2 support
    private static final String GETPROPERTYVALUE = "GETPROPERTYVALUE";
    private static final String LOCKFEATURE = "LOCKFEATURE";
    private static final String GETFEATUREWITHLOCK = "GETFEATUREWITHLOCK";
    private static final String LISTSTOREDQUERIES = "LISTSTOREDQUERIES";
    private static final String DESCRIBESTOREDQUERIES = "DESCRIBESTOREDQUERIES";
    private static final String CREATESTOREDQUERY = "CREATESTOREDQUERY";
    private static final String DROPSTOREDQUERY = "DROPSTOREDQUERY";

    private static Map<Pattern, String> OPERATION_NAME_PATTERNS = new HashMap<>();
    static {
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=getcapabilities\\b.*", CASE_INSENSITIVE), GETCAPABILITIES);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=getmap\\b.*", CASE_INSENSITIVE), GETMAP);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETLEGENDGRAPHIC\\b.*", CASE_INSENSITIVE), GETLEGENDGRAPHIC);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETFEATUREINFO\\b.*", CASE_INSENSITIVE), GETFEATUREINFO);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=DESCRIBELAYER\\b.*", CASE_INSENSITIVE), DESCRIBELAYER);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETFEATURE\\b.*", CASE_INSENSITIVE), GETFEATURE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=DESCRIBEFEATURETYPE\\b.*", CASE_INSENSITIVE),
                DESCRIBEFEATURETYPE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETCOVERAGE\\b.*", CASE_INSENSITIVE), GETCOVERAGE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=DESCRIBECOVERAGE\\b.*", CASE_INSENSITIVE), DESCRIBECOVERAGE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETTILE\\b.*", CASE_INSENSITIVE), GETTILE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETSTYLES\\b.*", CASE_INSENSITIVE), GETSTYLES);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETPROPERTYVALUE\\b.*", CASE_INSENSITIVE), GETPROPERTYVALUE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=LOCKFEATURE\\b.*", CASE_INSENSITIVE), LOCKFEATURE);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=GETFEATUREWITHLOCK\\b.*", CASE_INSENSITIVE),
                GETFEATUREWITHLOCK);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=LISTSTOREDQUERIES\\b.*", CASE_INSENSITIVE),
                LISTSTOREDQUERIES);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=DESCRIBESTOREDQUERIES\\b.*", CASE_INSENSITIVE),
                DESCRIBESTOREDQUERIES);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=CREATESTOREDQUERY.*", CASE_INSENSITIVE), CREATESTOREDQUERY);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=DROPSTOREDQUERY.*", CASE_INSENSITIVE), DROPSTOREDQUERY);
        OPERATION_NAME_PATTERNS.put(compile(".*\\brequest=getmap.*", CASE_INSENSITIVE), GETMAP);
    };

    private static final String[] LAYER_KEYWORD = { "LAYERS=", "LAYER=", "TYPENAME=", "QUERY_LAYERS=", "COVERAGEID=" };

    private static final String COMMA = ",";
    private static final char QUOTE = '\"';
    private static final char[] DELIMITER = { '&', ' ', '\r', '\t', '>' };
    private static final String OGC_MSG_SPLITTER = "[" + OGCServiceMessageFormatter.SEPARATOR + "]";

    private static final List<String> HAS_TO_CREATE_A_LOG_WITHOUT_LAYER = Collections.singletonList("");

    static {
        // sorts the delimiters to allow binary search
        Arrays.sort(DELIMITER);
    }

    private OGCServiceParser() {
        // utility class
    }

    /**
     * Parses the request string in order to extract service, layer, user, date
     *
     * @param message
     * @return list of logs
     *
     * @throws ParseException
     * @throws UnsupportedEncodingException
     */
    public static List<Map<String, Object>> parseLog(final String message)
            throws ParseException, UnsupportedEncodingException {
        List<Map<String, Object>> logList = new LinkedList<>();

        String[] splittedMessage = message.split(OGC_MSG_SPLITTER);
        if (splittedMessage.length < 3) {
            throw new ParseException(
                    "the message has not be recognized. Use OGCServiceMessageFormatter.format(...) to build the message",
                    0);
        }

        // parses service and layer from request
        String request = URLDecoder.decode(splittedMessage[2], "UTF-8");
        String service = parseService(request);
        String ogcReq = parseOperationName(request).toLowerCase();

        boolean undefinedService = "".equals(service);
        if (undefinedService)
            return logList;

        // extracts user
        String user = splittedMessage[0];

        // extracts date
        DateFormat formatter = new SimpleDateFormat(OGCServiceMessageFormatter.DATE_FORMAT);
        Date date = formatter.parse(splittedMessage[1]);

        // parses org (it is optional) and sec roles
        String org;
        String roles;
        if (splittedMessage.length == 5) {
            org = splittedMessage[3];
            roles = splittedMessage[4];
        } else { // missing case
            org = "";
            roles = "";
        }

        // for each layer adds a log to the list
        List<String> layerList = parseLayer(request);

        for (String layer : layerList) {
            Map<String, Object> log = new HashMap<>(6);

            log.put(USER_COLUMN, user);
            log.put(DATE_COLUMN, date);
            log.put(SERVICE_COLUMN, service);
            log.put(LAYER_COLUMN, layer.toLowerCase());
            log.put(REQUEST_COLUMN, ogcReq);
            log.put(ORG_COLUMN, org);
            log.put(SECROLE_COLUMN, roles);

            logList.add(log);
        }
        return logList;
    }

    /**
     * Parses the OGC service.
     *
     * @param message
     *
     * @return an OGC service symbol, "" in other case.
     */
    private static String parseService(final String message) {
        // checks if it is an ogc service
        String service = SERVICE_PATTERNS.entrySet().stream().filter(e -> e.getKey().matcher(message).matches())
                .map(Entry<Pattern, String>::getValue).findFirst().orElse("");

        // Particular case: the following does not contain the WMS service key
        if (service.isEmpty() && message.contains(GETLEGENDGRAPHIC)) {
            service = WMS;
        }
        return service;
    }

    private static String parseOperationName(final String message) {
        Optional<String> operationName = OPERATION_NAME_PATTERNS.entrySet().stream().parallel()
                .filter(e -> e.getKey().matcher(message).matches()).map(Entry<Pattern, String>::getValue).findFirst();
        return operationName.orElse("");
    }

    /**
     * Parses the layer name
     *
     * @param request
     *
     * @return a list of layer names
     */
    private static List<String> parseLayer(final String request) {
        // only convert to upper case the request URL, in case it has a large POST body
        final int newlineIndex = request.indexOf('\n');
        String matchLayersStr = newlineIndex == -1 ? request : request.substring(0, newlineIndex);
        matchLayersStr = matchLayersStr.toUpperCase();

        List<String> layerList = HAS_TO_CREATE_A_LOG_WITHOUT_LAYER;
        for (String layerKeyword : LAYER_KEYWORD) {
            if (matchLayersStr.contains(layerKeyword)) {

                int begin = matchLayersStr.indexOf(layerKeyword);
                begin = begin + layerKeyword.length();
                String layers = matchLayersStr.substring(begin);
                int end = searchEndOfLayerValue(layers);
                layers = layers.substring(0, end);

                layerList = buildLayerList(layers);
            }
        }
        return layerList;
    }

    /**
     * Index of the end of list of the layer names.
     *
     * @param layer
     * @return index
     */
    private static int searchEndOfLayerValue(String layer) {

        int end = -1;
        // search the delimiter the layer element
        for (int i = 0; i < layer.length(); i++) {

            char current = layer.charAt(i);
            if (Arrays.binarySearch(DELIMITER, current) >= 0) {
                return i;
            }
        }
        if (end == -1) {
            end = layer.length();
        }
        return end;
    }

    /**
     * Extract the layer name from a list like layer1, layer2, ...., layerN
     *
     * @param strLayerList
     *
     * @return List of layers
     */
    private static List<String> buildLayerList(final String strLayerList) {
        List<String> layers = new LinkedList<>();
        String[] layersToBeautify = strLayerList.split(COMMA);
        for (String layerToBeautify : layersToBeautify) {
            layers.add(removeQuoteAndTrim(layerToBeautify));
        }
        return layers;
    }

    private static String removeQuoteAndTrim(String string) {
        return string.replace(QUOTE, ' ').trim();
    }
}
