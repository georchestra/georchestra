/**
 * 
 */
package org.georchestra.ogcservstatistics.log4j;

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

import org.apache.log4j.spi.LoggingEvent;

/**
 * This parse recognizes an OGC service taking into account the syntax convention 
 * implemented by {@link OGCServiceMessageFormatter}.
 * 
 * @author Mauricio Pazos
 *
 */
final class OGCServiceParser {

	private static final String SERVICE_KEYWORD = "SERVICE=";
	private static final String REQUEST_KEYWORD = "REQUEST=";
			
	// service types
	private static final String WFS = "WFS";
	private static final String WMS = "WMS";
	private static final String WCS = "WCS";
	private static final String[] SERVICE_TYPE = 
		{ 	SERVICE_KEYWORD+WFS,SERVICE_KEYWORD+WCS, SERVICE_KEYWORD+WMS, 
			SERVICE_KEYWORD+ "WMTS", 
			SERVICE_KEYWORD+"\"WFS\"", SERVICE_KEYWORD+"\"WCS\"", SERVICE_KEYWORD+"\"WMTS\"" };
	
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
	
	
	
	private static final String[] REQUEST_TYPE = 
		{ 	REQUEST_KEYWORD+GETCAPABILITIES,
			REQUEST_KEYWORD+GETMAP,
			REQUEST_KEYWORD+GETLEGENDGRAPHIC,
			REQUEST_KEYWORD+GETFEATUREINFO,
			REQUEST_KEYWORD+DESCRIBELAYER,
			REQUEST_KEYWORD+GETFEATURE,
			REQUEST_KEYWORD+DESCRIBEFEATURETYPE,
			REQUEST_KEYWORD+GETCOVERAGE,
			REQUEST_KEYWORD+DESCRIBECOVERAGE,
			REQUEST_KEYWORD+GETTILE,
			REQUEST_KEYWORD+GETSTYLES,
			// WFS2
			REQUEST_KEYWORD+GETPROPERTYVALUE,
			REQUEST_KEYWORD+LOCKFEATURE,
			REQUEST_KEYWORD+GETFEATUREWITHLOCK,
			REQUEST_KEYWORD+LISTSTOREDQUERIES,
			REQUEST_KEYWORD+DESCRIBESTOREDQUERIES,
			REQUEST_KEYWORD+CREATESTOREDQUERY,
			REQUEST_KEYWORD+DROPSTOREDQUERY
		};
	
	private static final String[] LAYER_KEYWORD = {"LAYERS=", "LAYER=","TYPENAME=", "QUERY_LAYERS="};

	private static final String OPERATION_GET_LEGEND_GRAPHIC = "GETLEGENDGRAPHIC";
	private static final char COMMA = ',';
	private static final char QUOTE = '\"';
	
	private static final char[]  DELIMITER = {'&', ' ',  '\r', '\t', '>' };
	static{
		// sorts the delimiters to allow binary search
		Arrays.sort(DELIMITER);
	}
	
	private OGCServiceParser(){
		// utility class
	}

	public  static boolean isOGCService(LoggingEvent event) {
		
		String service = parseService(event.getMessage().toString());
		
		return !"".equals(service);
	}
	/**
	 * Parses the OGC service.
	 * 
	 * @param message
	 * 
	 * @return an OGC service symbol, "" in other case.
	 */
	private static String parseService(final String message){
		
		String msg = new String(message); // defensive copy 
		msg = msg.toUpperCase();
		// checks if it is an ogc service
		for (int i = 0; i < SERVICE_TYPE.length; i++) {
			if (msg.contains(SERVICE_TYPE[i])) {
				
				String service = SERVICE_TYPE[i].substring(SERVICE_KEYWORD.length());
				return removeQuote(service);
			}
		}
		// Particular case: the following does not contain the WMS service key 
		if(msg.contains(OPERATION_GET_LEGEND_GRAPHIC)){
			return WMS;
		}
		return "";
	}
	
	private static String parseRequest(final String message){
		
		String msg = new String(message); // defensive copy 
		msg = msg.toUpperCase();
		// checks if it is an ogc service
		for (int i = 0; i < REQUEST_TYPE.length; i++) {
			if (msg.contains(REQUEST_TYPE[i])) {
				
				String request = REQUEST_TYPE[i].substring(REQUEST_KEYWORD.length());
				return removeQuote(request);
			}
		}
		return "";
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

		String work = new String(message);
		String[] splittedMessage = work.split("["+OGCServiceMessageFormatter.SEPARATOR+"]");
		if(splittedMessage.length < 3){
			throw new ParseException("the message has not be recognized. Use OGCServiceMessageFormatter.format(...) to build the message", 0);
		}

		// extracts user 
		final String user=  splittedMessage[0];
		
		// extracts date
		DateFormat format = new SimpleDateFormat(OGCServiceMessageFormatter.DATE_FORMAT);
		Date date = format.parse(splittedMessage[1] );
		
		// parses service and layer from request
		String request = URLDecoder.decode(splittedMessage[2], "UTF-8");
		String service = parseService(request);
		String ogcReq = parseRequest(request).toLowerCase();
		
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
		List<Map<String, Object>> logList = new LinkedList<Map<String,Object>>(); 
		List<String> layerList = parseLayer(request);
		if(layerList.isEmpty() ){
			// create a log without layer
			Map<String, Object>  log = new HashMap<String, Object>(6);
			
			log.put("user_name", user );
			log.put("date", date);
			log.put("service", service );
			log.put("layer", "" );
			log.put("request", ogcReq );
			log.put("org", org);
			log.put("secrole", roles);
			
			logList.add(log);
		} else{ // there are one ore more layers
			
			for(String layer : layerList){
				Map<String, Object>  log = new HashMap<String, Object>(6);
				
				log.put("user_name", user );
				log.put("date", date);
				log.put("service", service );
				log.put("layer", layer.toLowerCase() );
				log.put("request", ogcReq );
				log.put("org", org);
				log.put("secrole", roles);
				
				logList.add(log);
			}
		}
		return logList;
	}

	/**
	 * Parses the layer name
	 * 
	 * @param request
	 * 
	 * @return a list of layer names
	 */
	private static List<String> parseLayer(final String request) {

		String msg = new String(request); // defensive copy
		msg = msg.toUpperCase();

		List<String> layerList = Collections.emptyList();
		for (int i = 0; i < LAYER_KEYWORD.length; i++) {
			if (msg.contains(LAYER_KEYWORD[i])) {

				int begin = msg.indexOf(LAYER_KEYWORD[i]);
				begin = begin + LAYER_KEYWORD[i].length();
				String layers = msg.substring(begin);
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
			end = layer.length() - 1;
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
		
		List<String> layerList = new LinkedList<String>();
		
		StringBuilder currentLayer = new StringBuilder(strLayerList.length());
		currentLayer.append("");
		for(int i = 0; i < strLayerList.length(); i++){

			if(strLayerList.charAt(i) == COMMA){

				layerList.add(removeQuote(currentLayer.toString()));
				
				int capacity = strLayerList.length() - currentLayer.length();
				currentLayer = new StringBuilder(capacity);
			} else {
				currentLayer.append(strLayerList.charAt(i));
			}
		}
		if( !"".equals(currentLayer) ){
			
			layerList.add(removeQuote(currentLayer.toString()));
		}
		
		return layerList;
	}

	/**
	 * Remove quotes from string
	 * @param string
	 * @return string without string
	 */
	private static String removeQuote(String string) {

		string = string.replace(QUOTE, ' ');
		
		return string.trim();
	}

}
