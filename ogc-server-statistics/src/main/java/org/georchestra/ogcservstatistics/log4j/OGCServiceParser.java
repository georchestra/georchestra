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

package org.georchestra.ogcservstatistics.log4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This parse recognizes an OGC service taking into account the syntax convention 
 * implemented by {@link OGCServiceMessageFormatter}.
 * 
 * @author Mauricio Pazos
 *
 */
public final class OGCServiceParser {

	// Key for hashmap that holds logs information
	public final static String DATE_COLUMN = "date";
	public final static String USER_COLUMN = "user_name";
	public final static String SERVICE_COLUMN = "service";
	public final static String LAYER_COLUMN = "layer";
	public final static String REQUEST_COLUMN = "request";
	public final static String ORG_COLUMN = "org";
	public final static String SECROLE_COLUMN = "roles";

	private static final String SERVICE_KEYWORD = "SERVICE=";
	private static final String REQUEST_KEYWORD = "REQUEST=";
			
	// service types
	private static final String WFS = "WFS";
	private static final String WMS = "WMS";
	private static final String WCS = "WCS";
	private static final String WMTS = "WMTS";
	private static final String[] SERVICE_TYPE_PATTERNS = {
		SERVICE_KEYWORD + WFS,
		SERVICE_KEYWORD + WCS,
		SERVICE_KEYWORD + WMS,
		SERVICE_KEYWORD + WMTS,
		SERVICE_KEYWORD + "\"" + WFS + "\"",
		SERVICE_KEYWORD + "\"" + WCS + "\"",
		SERVICE_KEYWORD + "\"" + WMS + "\"",
		SERVICE_KEYWORD + "\"" + WMTS + "\""
	};
	
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

	private static final String[] OPERATION_NAME_PATTERNS = {
		REQUEST_KEYWORD + GETCAPABILITIES,
		REQUEST_KEYWORD + GETMAP,
		REQUEST_KEYWORD + GETLEGENDGRAPHIC,
		REQUEST_KEYWORD + GETFEATUREINFO,
		REQUEST_KEYWORD + DESCRIBELAYER,
		REQUEST_KEYWORD + GETFEATURE,
		REQUEST_KEYWORD + DESCRIBEFEATURETYPE,
		REQUEST_KEYWORD + GETCOVERAGE,
		REQUEST_KEYWORD + DESCRIBECOVERAGE,
		REQUEST_KEYWORD + GETTILE,
		REQUEST_KEYWORD + GETSTYLES,
		// WFS2
		REQUEST_KEYWORD + GETPROPERTYVALUE,
		REQUEST_KEYWORD + LOCKFEATURE,
		REQUEST_KEYWORD + GETFEATUREWITHLOCK,
		REQUEST_KEYWORD + LISTSTOREDQUERIES,
		REQUEST_KEYWORD + DESCRIBESTOREDQUERIES,
		REQUEST_KEYWORD + CREATESTOREDQUERY,
		REQUEST_KEYWORD + DROPSTOREDQUERY
	};
	
	private static final String[] LAYER_KEYWORD = {
		"LAYERS=", "LAYER=","TYPENAME=", "QUERY_LAYERS=", "COVERAGEID="
	};

	private static final String COMMA = ",";
	private static final char QUOTE = '\"';
	private static final char[]  DELIMITER = {'&', ' ',  '\r', '\t', '>' };
	private static final String OGC_MSG_SPLITTER = "[" + OGCServiceMessageFormatter.SEPARATOR + "]";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(OGCServiceMessageFormatter.DATE_FORMAT);

	private static final List<String> HAS_TO_CREATE_A_LOG_WITHOUT_LAYER = Arrays.asList(new String [] {""});

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
	public static List<Map<String, Object>> parseLog(final String message) throws ParseException, UnsupportedEncodingException {
		List<Map<String, Object>> logList = new LinkedList<Map<String,Object>>();

		String[] splittedMessage = message.split(OGC_MSG_SPLITTER);
		if(splittedMessage.length < 3){
			throw new ParseException("the message has not be recognized. Use OGCServiceMessageFormatter.format(...) to build the message", 0);
		}

		// parses service and layer from request
		String request = URLDecoder.decode(splittedMessage[2], "UTF-8").toUpperCase();
		String service = parseService(request);
		String ogcReq = parseOperationName(request).toLowerCase();

		boolean undefinedService = "".equals(service);
		if (undefinedService) return logList;

		// extracts user 
		String user=  splittedMessage[0];
		
		// extracts date
		Date date = DATE_FORMAT.parse(splittedMessage[1]);
		

		// parses org (it is optional) and sec roles
		String org;
		String roles;
		if(splittedMessage.length == 5){
			org = splittedMessage[3];
			roles = splittedMessage[4];
		} else { // missing case
			org = "";
			roles = "";
		}
		

		// for each layer adds a log to the list
		List<String> layerList = parseLayer(request);

		for(String layer : layerList){
			Map<String, Object>  log = new HashMap<String, Object>(6);

			log.put(USER_COLUMN, user );
			log.put(DATE_COLUMN, date);
			log.put(SERVICE_COLUMN, service );
			log.put(LAYER_COLUMN, layer.toLowerCase() );
			log.put(REQUEST_COLUMN, ogcReq );
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
	private static String parseService(final String message){
		// checks if it is an ogc service
		for (String pattern : SERVICE_TYPE_PATTERNS) {
			if (message.contains(pattern)) {

				String service = pattern.substring(SERVICE_KEYWORD.length());
				return removeQuoteAndTrim(service);
			}
		}
		// Particular case: the following does not contain the WMS service key
		if(message.contains(GETLEGENDGRAPHIC)){
			return WMS;
		}
		return "";
	}

	private static String parseOperationName(final String message){
		for (String pattern : OPERATION_NAME_PATTERNS) {
			if (message.contains(pattern)) {

				String request = pattern.substring(REQUEST_KEYWORD.length());
				return removeQuoteAndTrim(request);
			}
		}
		return "";
	}

	/**
	 * Parses the layer name
	 * 
	 * @param request
	 * 
	 * @return a list of layer names
	 */
	private static List<String> parseLayer(final String request) {
		List<String> layerList = HAS_TO_CREATE_A_LOG_WITHOUT_LAYER;
		for (String layerKeyword : LAYER_KEYWORD) {
			if (request.contains(layerKeyword)) {

				int begin = request.indexOf(layerKeyword);
				begin = begin + layerKeyword.length();
				String layers = request.substring(begin);
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
		for(int i = 0; i < layer.length(); i ++){
			
			char current = layer.charAt(i);
			if(Arrays.binarySearch(DELIMITER, current) >= 0){
				return i;
			}
		}
		if( end == -1){
			end = layer.length();
		}
		return end;
	}

	/**
	 * Extract the layer name from a list like
	 * layer1, layer2, ...., layerN
	 * 
	 * @param strLayerList
	 * 
	 * @return List of layers
	 */
	private static List<String> buildLayerList(final String strLayerList) {
		List<String> layers = new LinkedList<String>();
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
