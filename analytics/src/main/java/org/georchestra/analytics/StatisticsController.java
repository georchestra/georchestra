/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.analytics;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.analytics.dao.StatsRepo;
import org.georchestra.analytics.util.DBConnection;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller defines the entry point to return statistics based on user or groups, for a given
 * date period.
 *
 * The entry point "/combinedRequests" receives a plain JSON object defined as follows:
 *
 * - search by user:
 * <pre>
 * {
 *   "user": "user",
 *   "startDate": "YYY-mm-dd",
 *   "endDate": "YY-mm-dd"
 * }
 * </pre>
 *
 * - search by roles:
 * <pre>
 * {
 *   "group": "group",
 *   "startDate": "YYY-mm-dd",
 *   "endDate": "YY-mm-dd"
 * }
 * </pre>
 *
 * It will return a JSON object which follows the current format:
 *
 * <pre>
 * {
 *   "granularity": "GRANULARITY",
 *   "results": [
 *     {
 *       "count": int,
 *       "date": "YYY-mm-dd"
 *     }, ...
 *    ]
 * }
 * </pre>
 * 
 * If neither user nor group is set, global statistics are returned.
 *
 * where granularity will depend on the submitted date, following the algorithm:
 *  if datediff < 2 days   then granularity by hour
 *  if datediff < 1 week   then granularity by day
 *  if datediff < 1 month  then granularity by day
 *  if datediff < 3 months then granularity by week
 *  if datediff < 1 year   then granularity by month
 *
 * - The entry point "/layersUsage" receives a JSON object as follows:
 * <pre>
 * {
 *   "user"|"group": "user|group",
 *   "limit": integer,
 *   "startDate": "YYYY-mm-dd",
 *   "endDate": "YYYY-mm-dd"
 * }
 * </pre>
 * User, group and limit are optional parameters.
 * 
 * The returned JSON object will follow the pattern:
 * <pre>
 * { "results": [
 *   {
 *       "count": 831,
 *       "layer": "layername1"
 *   },
 *   {
 *       "count": 257,
 *       "layer": "layername2"
 *   }, ...
 *   ]
 * }
 * </pre>
 *
 * - the entry point "/distinctUsers" receives a JSON object as follows:
 *  
 * <pre>
 * {
 *   "group": "group",
 *   "startDate": "YYYY-mm-dd",
 *   "endDate": "YYY-mm-dd"
 * }
 * </pre>
 *
 * group is optional. If not set, global statistics are returned.
 *
 * The returned object will follow the pattern:
 * <pre>
 * {
 *   "results": [
 *     { "user": "user1", "nb_requests": 10, "organization": "truite" },
 *     { "user": "user1", "nb_requests": 10, "organization": "truite" },
 *     ...
 *   ]
 * }
 * </pre>
 *
 * @author pmauduit
 * @since 15.12
 */

@Controller
@Api(name = "Statistics API", description = "Methods to get several statistics "
        + "related to users and groups, and their use of the infrastructure.")
public class StatisticsController {

    @Autowired
    private StatsRepo statsRepository;

	@Autowired
	private GeorchestraConfiguration georConfig;

	private DBConnection db;

	private DateTimeFormatter localInputFormatter;
	private DateTimeFormatter dbOutputFormatter;

	private DateTimeFormatter dbHourInputFormatter;
	private DateTimeFormatter dbHourOutputFormatter;
	private DateTimeFormatter dbDayOutputFormatter;
	private DateTimeFormatter dbWeekInputFormatter;
	private DateTimeFormatter dbWeekOutputFormatter;
	private DateTimeFormatter dbMonthInputFormatter;
	private DateTimeFormatter dbMonthOutputFormatter;
	private DateTimeFormatter dbDayInputFormatter;

	private static enum FORMAT { JSON, CSV }
	private static enum REQUEST_TYPE { USAGE, EXTRACTION }

	public StatisticsController(String localTimezone) throws PropertyVetoException, SQLException {
		// Parser to convert from local time to DB time (UTC)
		this.localInputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
				.withZone(DateTimeZone.forID(localTimezone));
		this.dbOutputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
				.withZone(DateTimeZone.forID("UTC"));

		// Used to parse date from DB based on granularity
		this.dbHourInputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH")
				.withZone(DateTimeZone.forID("UTC"));
		this.dbHourOutputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH")
				.withZone(DateTimeZone.forID(localTimezone));

		this.dbDayInputFormatter = DateTimeFormat.forPattern("y-M-d")
				.withZone(DateTimeZone.forID("UTC"));
		this.dbDayOutputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
				.withZone(DateTimeZone.forID(localTimezone));

		this.dbWeekInputFormatter = DateTimeFormat.forPattern("y-w")
				.withZone(DateTimeZone.forID("UTC"));
		this.dbWeekOutputFormatter = DateTimeFormat.forPattern("yyyy-ww")
				.withZone(DateTimeZone.forID(localTimezone));

		this.dbMonthInputFormatter = DateTimeFormat.forPattern("y-M")
				.withZone(DateTimeZone.forID("UTC"));
		this.dbMonthOutputFormatter = DateTimeFormat.forPattern("yyyy-MM")
				.withZone(DateTimeZone.forID(localTimezone));
	}

	@PostConstruct
	public void init() throws PropertyVetoException, SQLException {
		this.db = new DBConnection(georConfig.getProperty("dlJdbcUrlOGC"));
	}

	/**
     * Setter used mainly for testing purposes.
     * @param statsRepository
     */
    public void setStatsRepository(StatsRepo statsRepository) {
		this.statsRepository = statsRepository;
	}

	public GeorchestraConfiguration getGeorConfig() {
		return georConfig;
	}

	public void setGeorConfig(GeorchestraConfiguration georConfig) {
		this.georConfig = georConfig;
	}

	/** Granularity used for the returned date type in combined requests statistics */
	public static enum GRANULARITY { HOUR, DAY, WEEK, MONTH }

	/*
	 * Test examples :
	 *
	 *  combinedRequests with user:
	 *	 curl -XPOST --data-binary '{"user": "testadmin", "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
	   -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/combinedRequests -i
	 *
	 *  combinedRequests with group:
	 *   curl -XPOST --data-binary '{"group": "ADMINISTRATOR", "startDate": "2015-10-01", "endDate": "2015-11-01" }' \
           -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/combinedRequests -i
	 *
	 *  layersUsage with user:
	 *    curl -XPOST --data-binary '{"user": "testadmin", "limit": 10, "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
           -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/layersUsage -i
	 *
	 *  layersUsage with group:
	 *    curl -XPOST --data-binary '{"group": "ADMINISTRATOR", "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
           -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/layersUsage -i
	 *
	 *  layersUsage without filter:
	 *    curl -XPOST --data-binary '{"limit": 10, "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
           -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/layersUsage -i
	 *
	 *  distinctUsers :
	 *    curl -XPOST --data-binary '{"group": "ADMINISTRATOR", "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
	   -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/distinctUsers -i
	 */
	/**
	 * Total combined requests count group by time interval (hour, day, week or month). May be filtered by a user or a
	 * group.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a JSON string containing the requested aggregated statistics.
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/combinedRequests", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
    @ApiMethod(description="Returns the Total combined requests count group by time interval "
	            + "(hour, day, week or month). It must be filtered by either a user or a group. "
	            + "User or group is mandatory, a startDate and an endDate must be specified, ie:"
	            + "<br/><code>"
	            + "{ user: testadmin, startDate: 2015-01-01, endDate: 2015-12-01 }"
	            + "</code><br/>or<br/>"
	            + "<code>"
	            + "{ group: ADMINISTRATOR, startDate: 2015-10-01, endDate: 2015-11-01 }"
	            + "</code><br/>"
	            + "is a valid request."
	            + "")
	public String combinedRequests(@RequestBody String payload, HttpServletResponse response) throws JSONException, ParseException, SQLException {
		JSONObject input = null;

		// Parse Input
		try {
			input = new JSONObject(payload);
			if (!input.has("startDate") || !input.has("endDate")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		// Fetch date, user and group filters
		Map<String, Object> sqlValues = new HashMap<String, Object>();
		sqlValues.put("startDate", this.convertLocalDateToUTC(input.getString("startDate")));
		sqlValues.put("endDate", this.convertLocalDateToUTC(input.getString("endDate")));

		// not both group and user can be defined at the same time
		if (input.has("user") && input.has("group")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (input.has("user"))
			sqlValues.put("user", input.getString("user"));
		if (input.has("group"))
			sqlValues.put("group", "ROLE_" + input.getString("group"));

		// Compute expression to aggregate dates
		GRANULARITY g = guessGranularity((String) sqlValues.get("startDate"),(String) sqlValues.get("endDate"));
		String aggregateDate;
		switch (g) {
			case HOUR:
				aggregateDate = "YYYY-mm-dd HH24";
				break;
			case DAY:
				aggregateDate = "YYYY-mm-dd";
				break;
			case WEEK:
				aggregateDate = "YYYY-IW";
				break;
			case MONTH:
				aggregateDate = "YYYY-mm";
				break;
			default:
				throw new IllegalArgumentException("Invalid value for granularity");
		}
		sqlValues.put("aggregateDateExpression", aggregateDate);

		// Generate SQL query
		String sql = "SELECT CAST(COUNT(*) AS integer) AS count," +
				"            to_char(date, {aggregateDateExpression}) AS aggregate_date " +
				"     FROM ogcstatistics.ogc_services_log " +
				"     WHERE date >= CAST({startDate} AS timestamp without time zone) " +
				"     AND date < CAST({endDate} AS timestamp without time zone) ";

		// Handle user and group
		if (input.has("user"))
			sql += " AND user_name = {user} ";
		if (input.has("group"))
			sql += " AND {group} = ANY (roles) ";

		sql += "GROUP BY to_char(date, {aggregateDateExpression}) " +
				"ORDER BY to_char(date, {aggregateDateExpression})";

		// Fetch and format results
		ResultSet res = db.execute(db.generateQuery(sql,sqlValues));
		JSONArray results = new JSONArray();
		while(res.next()) {
			String date =  this.convertUTCDateToLocal(res.getString("aggregate_date"), g);
			int count = res.getInt("count");
			results.put(new JSONObject().put("count", count).put("date", date));
		}
		return new JSONObject().put("results", results)
				.put("granularity", g)
				.toString(4);
	}

	/**
	 * Gets statistics for layers consumption in JSON format. May be filtered by a user or a group and limited.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a JSON string containing the requested aggregated statistics.
	 *
	 * @throws JSONException 
	 */
	@RequestMapping(value="/layersUsage.json", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String layersUsageJson(@RequestBody String payload, HttpServletResponse response) throws JSONException, SQLException {
		return this.generateStats(payload, REQUEST_TYPE.USAGE, response, FORMAT.JSON);
	}

	/**
	 * Gets statistics for layers consumption in CSV format. May be filtered by a user or a group and limited.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a CSV string containing the requested aggregated statistics.
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/layersUsage.csv", method=RequestMethod.POST, produces= "application/csv; charset=utf-8")
	@ResponseBody
	public String layersUsage(@RequestBody String payload, HttpServletResponse response) throws JSONException, SQLException {
		return this.generateStats(payload, REQUEST_TYPE.USAGE, response, FORMAT.CSV);
	}

	/**
	 * Gets statistics for layers extraction in JSON format. May be filtered by a user or a group and limited.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a JSON string containing the requested aggregated statistics.
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/layersExtraction.json", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String layersExtractionJson(@RequestBody String payload, HttpServletResponse response) throws JSONException, SQLException {
		return this.generateStats(payload, REQUEST_TYPE.EXTRACTION, response, FORMAT.JSON);
	}

	/**
	 * Gets full statistics for layers extraction in JSON format. Compared to previous method, this method will not
	 * aggregate records and it will contains several new informations : organization, start date, end date, duration ...
	 *
	 * @param startDate minimum date for stats
	 * @param endDate maximum date for stats
	 * @param response the HttpServletResponse object.
	 * @return a JSON string containing the requested statistics.
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/fullLayersExtraction.csv", method=RequestMethod.GET, produces= "application/csv; charset=utf-8")
	@ResponseBody
	public String fullLayersExtractionStats(@RequestParam String startDate, @RequestParam String endDate, HttpServletResponse response) throws JSONException {

		try {
			if (startDate == null || endDate == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		response.setHeader("Content-Disposition", "attachment; filename=data.csv");
		response.setContentType("application/csv; charset=utf-8");

		List lst = statsRepository.getFullLayersExtraction(startDate, endDate);
		StringBuilder res = new StringBuilder("username;organization;creation_date;duration;end_date;layer_name;is_successful;bbox;area_km2\n");
		for (Object o : lst) {
			Object[] row = (Object[]) o;
			for(int i=0; i<8; i++)
				res.append(row[i] + ";");
			res.append(row[8] + "\n");
		}
		return res.toString();
	}

	/**
	 * Gets statistics for layers extraction in CSV format. May be filtered by a user or a group and limited.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a CSV string containing the requested aggregated statistics.
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/layersExtraction.csv", method=RequestMethod.POST, produces= "application/csv; charset=utf-8")
	@ResponseBody
	public String layersExtractionCsv(@RequestBody String payload, HttpServletResponse response) throws JSONException, SQLException {
		return this.generateStats(payload, REQUEST_TYPE.EXTRACTION, response, FORMAT.CSV);
	}

	/**
	 *  This method generates stats for layer usage or extraction and return results in CSV or JSON format
	 * @param payload JSON payload, should contain 'startDate', 'endDate', 'limit', 'group'
	 * @param type either layer usage 'USAGE' or layer extraction 'EXTRACTION'
	 * @param response response
	 * @param format
	 * @return
	 * @throws JSONException
	 */
	private String generateStats(String payload, REQUEST_TYPE type, HttpServletResponse response, FORMAT format) throws JSONException,SQLException {

		JSONObject input;
		String userId, groupId;
		String startDate;
		String endDate;
		Integer limit;
		Map<String, Object> sqlValues = new HashMap<String, Object>();

		try {
			input = new JSONObject(payload);
			sqlValues.put("startDate", this.getStartDate(input));
			sqlValues.put("endDate", this.getEndDate(input));
			limit = this.getLimit(input);
			userId = this.getUser(input);
			groupId = this.getGroup(input);
			sqlValues.put("group", groupId);
			sqlValues.put("user", userId);
			sqlValues.put("limit", limit);

			if (sqlValues.get("startDate") == null || sqlValues.get("endDate") == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}

		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		String sql;

		if (type == REQUEST_TYPE.USAGE) {
			sql = "SELECT layer, CAST(COUNT(*) AS integer) AS count " +
				  "FROM ogcstatistics.ogc_services_log " +
				  "WHERE date >= CAST({startDate} AS timestamp without time zone) AND date < CAST({endDate} AS timestamp without time zone) " +
				  "AND layer != '' ";
		} else if (type == REQUEST_TYPE.EXTRACTION){
			sql = "SELECT layer_name AS layer, CAST(COUNT(*) AS integer) AS count " +
				  "FROM extractorapp.extractor_layer_log " +
				  "LEFT JOIN extractorapp.extractor_log " +
				  "	ON (extractorapp.extractor_log.id = extractorapp.extractor_layer_log.extractor_log_id) " +
			      "WHERE creation_date >= CAST({startDate} AS timestamp without time zone) AND creation_date < CAST({endDate} AS timestamp without time zone) " +
				  "AND is_successful ";
		} else {
			throw new IllegalArgumentException("Invalid request type : " + type);
		}

		if(groupId != null)
			sql += " AND {group} = ANY(roles) ";
		if(userId != null)
			sql += " AND username = {user} ";

		if (type == REQUEST_TYPE.USAGE) {
			sql += " GROUP BY layer " +
				   "ORDER BY COUNT(*) DESC " ;
		} else if (type == REQUEST_TYPE.EXTRACTION){
			sql += " GROUP BY layer_name " +
				   "ORDER BY COUNT(*) DESC";
		} else {
			throw new IllegalArgumentException("Invalid request type : " + type);
		}

		if(limit != null){
			sql += " LIMIT {limit}";

		}

		ResultSet sqlRes = db.execute(db.generateQuery(sql,sqlValues));

		switch (format){
			case JSON:
				JSONArray results = new JSONArray();
				while(sqlRes.next())
					results.put(new JSONObject().put("layer", sqlRes.getString("layer")).put("count", sqlRes.getInt("count")));
				return new JSONObject().put("results", results)
						.toString(4);
			case CSV:
				StringBuilder res = new StringBuilder("layer,count\n");
				while(sqlRes.next())
					res.append(sqlRes.getString("layer") + "," + sqlRes.getInt("count") + "\n");
				return res.toString();
			default:
				throw new JSONException("Invalid format " + format);
		}
	}




	/**
	 * Gets the statistics by distinct users (number of requests between
	 * beginDate and endDate).
	 *
	 * @param payload
	 *            the JSON object containing the parameters
	 * @param response
	 *            the HTTP Servlet Response object, used to set the 40x HTTP
	 *            code in case of errors.
	 *
	 * @return A string representing a JSON object with the requested datas. The
	 *         output JSON has the following form:
	 * 
	 * <pre>
	 *    { "results": [
	 *	    {
	 *        "nb_requests": 3895,
	 *        "organization": "geOrchestra",
	 *        "user": "testadmin"
	 *      }, [...]
	 *     ]
	 *    }
	 * </pre>
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/distinctUsers", method=RequestMethod.POST)
        @ApiMethod(description="Returns the distinct active users for a given period. A group can be provided in the query "
            + "to limit the results to a given group.<br/>"
            + "Here are 2 valid examples (with and without a group):<br/>"
            + "<code>"
            + "{ group: ADMINISTRATOR, startDate: 2015-01-01, endDate: 2015-12-01 }"
            + "</code><br/>"
            + "or:<br/>"
            + "<code>"
            + "{ startDate: 2015-01-01, endDate: 2015-12-01 }"
            + "</code>")
	public void distinctUsers(@RequestBody String payload, HttpServletResponse response) throws JSONException, IOException, InvocationTargetException, SQLException, IllegalAccessException, NoSuchMethodException {
		JSONObject input;
		String groupId = null;
		String startDate;
		String endDate;

		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> sqlValues = new HashMap<String, Object>();

		// Parse input
		try {
			input = new JSONObject(payload);
			if (!input.has("startDate") || !input.has("endDate")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			sqlValues.put("startDate", this.convertLocalDateToUTC(input.getString("startDate")));
			sqlValues.put("endDate", this.convertLocalDateToUTC(input.getString("endDate")));

			if (input.has("group")) {
				sqlValues.put("group", "ROLE_" + input.getString("group"));
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// construct SQL query
		String sql = "SELECT user_name, org, CAST(COUNT(*) AS integer) AS count " +
				"FROM ogcstatistics.ogc_services_log " +
				"WHERE date >= CAST({startDate} AS timestamp without time zone) AND date < CAST({endDate} AS timestamp without time zone) ";

		if (groupId != null)
			sql += " AND {group} = ANY (roles) ";

		sql += "GROUP BY user_name, org " +
			   "ORDER BY COUNT(*) DESC";

		// Extract list of user to ignore in stats
		Set<String> excluded_users = new HashSet<String>();
		excluded_users.add("anonymousUser");
		for (int i = 1; true; i++) {
			String user = this.georConfig.getProperty("excludedUser.uid" + i);
			if (user != null)
				excluded_users.add(user);
			else
				break;
		}

		// Fetch and format results
		ResultSet res = this.db.execute(this.db.generateQuery(sql, sqlValues));
		JSONArray results = new JSONArray();
		while (res.next()) {
			if (excluded_users.contains(res.getString("user_name")))
				continue;
			JSONObject row = new JSONObject();
			row.put("user", res.getString("user_name"));
			row.put("organization", res.getString("org"));
			row.put("nb_requests", res.getString("count"));
			results.put(row);
		}
		String jsonOutput = new JSONObject().put("results", results)
				.toString(4);

		PrintWriter writer = response.getWriter();
		writer.print(jsonOutput);
		writer.close();
	}
	
	/**
	 * Calculates the appropriate granularity given the begin date and the end date.
	 *
	 * @param beginDate the begin date.
	 * @param endDate the end date.
	 * @return the most relevant GRANULARITY.
	 */
	private GRANULARITY guessGranularity(String beginDate, String endDate) {
		DateTime from = DateTime.parse(beginDate, this.dbOutputFormatter);
		DateTime to = DateTime.parse(endDate, this.dbOutputFormatter);

		Duration duration = new Duration(from, to);
		long numdays = duration.getStandardDays();
		if (numdays < 2) {
			return GRANULARITY.HOUR;
		} else if (numdays < 90) {
			return GRANULARITY.DAY;
		} else if (numdays < 365) {
			return GRANULARITY.WEEK;
		} else {
			return GRANULARITY.MONTH;
		}
	}

	/**
	 * Convert Date (with time) from configured local timezone to UTC. This method is used to convert date sent by UI
	 * to date with same timezone as database records. Ex : "2016-11-15" will be convert to "2016-11-14 23:00:00" if
	 * your local timezone is Europe/Paris (+01:00)
	 *
	 * @param rawDate Date to convert, should looks like : 2016-02-12
	 * @return String representation of datatime convert to UTC timezone with following format : 2016-11-14 23:00:00
	 * @throws ParseException if input date is not parsable
	 */

	private String convertLocalDateToUTC(String rawDate) {
		DateTime localDatetime = this.localInputFormatter.parseDateTime(rawDate);
		return this.dbOutputFormatter.print(localDatetime.toInstant());
	}

	/**
	 * Convert date from UTC to local configured timezone. This method is used to convert dates returns by database.
	 * @param rawDate raw date from database with format : "2016-02-12 23" or "2016-02-12" or "2016-06" or "2016-02"
	 * @return date in local timezone with hour
	 * @throws ParseException if input date is not parsable
	 */
	private String convertUTCDateToLocal(String rawDate, GRANULARITY granularity) throws ParseException {
		DateTimeFormatter inputFormatter = null;
		DateTimeFormatter outputFormatter = null;
		switch (granularity){
			case HOUR:
				inputFormatter = this.dbHourInputFormatter;
				outputFormatter = this.dbHourOutputFormatter;
				break;
			case DAY:
				inputFormatter = this.dbDayInputFormatter;
				outputFormatter = this.dbDayOutputFormatter;
				break;
			case WEEK:
				inputFormatter = this.dbWeekInputFormatter;
				outputFormatter = this.dbWeekOutputFormatter;
				break;
			case MONTH:
				inputFormatter = this.dbMonthInputFormatter;
				outputFormatter = this.dbMonthOutputFormatter;
				break;
		}
		DateTime localDatetime = inputFormatter.parseDateTime(rawDate);
		return outputFormatter.print(localDatetime.toInstant());
	}

	private String getGroup(JSONObject payload) throws JSONException {
		if(payload.has("group"))
			return "ROLE_" + payload.getString("group");
		else
			return null;
	}

	private String getUser(JSONObject payload) throws JSONException {
		if(payload.has("user"))
			return payload.getString("user");
		else
			return null;
	}

	private Integer getLimit(JSONObject payload) throws JSONException {
		if(payload.has("limit"))
			return payload.getInt("limit");
		else
			return null;
	}

	private String getDateField(JSONObject payload, String field) throws JSONException, ParseException {
		if(payload.has(field))
			return this.convertLocalDateToUTC(payload.getString(field));
		else
			return null;
	}

	private String getStartDate(JSONObject payload) throws JSONException, ParseException {
		return this.getDateField(payload, "startDate");
	}

	private String getEndDate(JSONObject payload) throws JSONException, ParseException {
		return this.getDateField(payload, "endDate");
	}

}


