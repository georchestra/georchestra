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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.analytics.dao.StatsRepo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class StatisticsController {

    @Autowired
    private StatsRepo statsRepository;

	private SimpleDateFormat utcTimezone;
	private SimpleDateFormat localTimezone;
	private SimpleDateFormat uiInputFormatter;
	private SimpleDateFormat dbHourInputFormatter;
	private SimpleDateFormat dbHourOutputFormatter;
	private SimpleDateFormat dbDayOutputFormatter;
	private SimpleDateFormat dbWeekInputFormatter;
	private SimpleDateFormat dbWeekOutputFormatter;
	private SimpleDateFormat dbMonthInputFormatter;
	private SimpleDateFormat dbMonthOutputFormatter;
	private SimpleDateFormat dbDayInputFormatter;

	private static final int USAGE_TYPE = 0;
	private static final int EXTRACTION_TYPE = 1;

	public StatisticsController(String localTimezone) {

		// Used to convert from one timezone to another
		this.localTimezone = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		this.localTimezone.setTimeZone(TimeZone.getTimeZone(localTimezone));
		this.utcTimezone = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		this.utcTimezone.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Used to parse UI date
		this.uiInputFormatter = new SimpleDateFormat("yyyy-MM-dd");
		this.uiInputFormatter.setTimeZone(TimeZone.getTimeZone(localTimezone));

		// Used to parse date from DB based on granularity
		this.dbHourInputFormatter = new SimpleDateFormat("y-M-d H");
		this.dbHourOutputFormatter = new SimpleDateFormat("yyyy-MM-dd HH");
		this.dbDayInputFormatter = new SimpleDateFormat("y-M-d");
		this.dbDayOutputFormatter = new SimpleDateFormat("yyyy-MM-dd");
		this.dbWeekInputFormatter = new SimpleDateFormat("y-w");
		this.dbWeekOutputFormatter = new SimpleDateFormat("yyyy-ww");
		this.dbMonthInputFormatter = new SimpleDateFormat("y-M");
		this.dbMonthOutputFormatter = new SimpleDateFormat("yyyy-MM");

	}

	/**
     * Setter used mainly for testing purposes.
     * @param statsRepository
     */
    public void setStatsRepository(StatsRepo statsRepository) {
		this.statsRepository = statsRepository;
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
	public String combinedRequests(@RequestBody String payload, HttpServletResponse response) throws JSONException, ParseException {
		JSONObject input = null;
		String userId  = null, groupId = null;
		Date startDate, endDate;
		try {
			input = new JSONObject(payload);
			if (!input.has("startDate") || !input.has("endDate")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}

			startDate = this.convertLocalDateToUTC(input.getString("startDate"));
			endDate = this.convertLocalDateToUTC(input.getString("endDate"));
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (input.has("user")) {
			userId = input.getString("user");
		}
		if (input.has("group")) {
			groupId = "ROLE_" + input.getString("group");
		}
		// not both group and user can be defined at the same time
		if ((userId != null) && (groupId != null)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		List<Object> lst = new ArrayList() ;
		GRANULARITY g = guessGranularity(startDate, endDate);
		if (userId != null) {
			switch (g) {
			case HOUR:
				lst = statsRepository.getRequestCountForUserBetweenStartDateAndEndDateByHour(userId, startDate,
						endDate);
				break;
			case DAY:
				lst = statsRepository.getRequestCountForUserBetweenStartDateAndEndDateByDay(userId, startDate, endDate);
				break;
			case WEEK:
				lst = statsRepository.getRequestCountForUserBetweenStartDateAndEndDateByWeek(userId, startDate,
						endDate);
				break;
			case MONTH:
				lst = statsRepository.getRequestCountForUserBetweenStartDateAndEndDateByMonth(userId, startDate,
						endDate);
				break;
			}
		} else if (groupId != null){
			switch (g) {
			case HOUR:
				lst = statsRepository.getRequestCountForGroupBetweenStartDateAndEndDateByHour(groupId, startDate,
						endDate);
				break;
			case DAY:
				lst = statsRepository.getRequestCountForGroupBetweenStartDateAndEndDateByDay(groupId, startDate, endDate);
				break;
			case WEEK:
				lst = statsRepository.getRequestCountForGroupBetweenStartDateAndEndDateByWeek(groupId, startDate,
						endDate);
				break;
			case MONTH:
				lst = statsRepository.getRequestCountForGroupBetweenStartDateAndEndDateByMonth(groupId, startDate,
						endDate);
				break;
			}
		} else {
			switch (g) {
			case HOUR:
				lst = statsRepository.getRequestCountBetweenStartDateAndEndDateByHour(startDate, endDate);
				break;
			case DAY:
				lst = statsRepository.getRequestCountBetweenStartDateAndEndDateByDay(startDate, endDate);
				break;
			case WEEK:
				lst = statsRepository.getRequestCountBetweenStartDateAndEndDateByWeek(startDate, endDate);
				break;
			case MONTH:
				lst = statsRepository.getRequestCountBetweenStartDateAndEndDateByMonth(startDate, endDate);
				break;
			}
		}
		JSONArray results = new JSONArray();
		for (Object o : lst) {
			Object[] row = (Object[]) o;
			String date = (String) row[1];
			date = this.convertUTCDateToLocal(date, g);
			results.put(new JSONObject().put("count", row[0]).put("date", date));
		}
		return new JSONObject().put("results", results)
				.put("granularity", g)
				.toString(4);
	}

	/**
	 * Gets statistics for layers consumption. May be filtered by a user or a group and limited.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a JSON string containing the requested aggregated statistics.
	 *
	 * @throws JSONException 
	 */
	@RequestMapping(value="/layersUsage", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String layersUsage(@RequestBody String payload, HttpServletResponse response) throws JSONException {
		return this.generateStats(payload, USAGE_TYPE, response);
	}

	/**
	 * Gets statistics for layers extraction. May be filtered by a user or a group and limited.
	 *
	 * @param payload the JSON object containing the input parameters
	 * @param response the HttpServletResponse object.
	 * @return a JSON string containing the requested aggregated statistics.
	 *
	 * @throws JSONException
	 */
	@RequestMapping(value="/layersExtraction", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String layersExtraction(@RequestBody String payload, HttpServletResponse response) throws JSONException {
		return this.generateStats(payload, EXTRACTION_TYPE, response);
	}

	private String generateStats(String payload, int type, HttpServletResponse response) throws JSONException {

		JSONObject input;
		String userId, groupId;
		Date startDate, endDate;
		Integer limit;

		try {
			input = new JSONObject(payload);
			if (this.getStartDate(input) == null || this.getEndDate(input) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}

			startDate = this.getStartDate(input);
			endDate = this.getEndDate(input);
			limit = this.getLimit(input);
			userId = this.getUser(input);
			groupId = this.getGroup(input);

		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		List lst = null;
		if (userId != null) {
			if (limit != null)
				switch (type) {
					case EXTRACTION_TYPE:
						lst = statsRepository.getLayersExtractionForUserLimit(userId, startDate, endDate, limit); break;
					case USAGE_TYPE:
						lst = statsRepository.getLayersStatisticsForUserLimit(userId, startDate, endDate, limit); break;
				}
			else
				switch (type) {
					case EXTRACTION_TYPE:
						lst = statsRepository.getLayersExtractionForUser(userId, startDate, endDate); break;
					case USAGE_TYPE:
						lst = statsRepository.getLayersStatisticsForUser(userId, startDate, endDate); break;
				}
		} else if (groupId != null) {
			if (limit != null)
				switch (type) {
					case EXTRACTION_TYPE:
						lst = statsRepository.getLayersExtractionForGroupLimit(groupId, startDate, endDate, limit); break;
					case USAGE_TYPE:
						lst = statsRepository.getLayersStatisticsForGroupLimit(groupId, startDate, endDate, limit); break;
				}
			else
				switch (type) {
					case EXTRACTION_TYPE:
						lst = statsRepository.getLayersExtractionForGroup(groupId, startDate, endDate); break;
					case USAGE_TYPE:
						lst = statsRepository.getLayersStatisticsForGroup(groupId, startDate, endDate); break;
				}
		} else {
			if (limit != null)
				switch (type) {
					case EXTRACTION_TYPE:
						lst = statsRepository.getLayersExtractionLimit(startDate, endDate, limit); break;
					case USAGE_TYPE:
						lst = statsRepository.getLayersStatisticsLimit(startDate, endDate, limit); break;
				}
			else
				switch (type) {
					case EXTRACTION_TYPE:
						lst = statsRepository.getLayersExtraction(startDate, endDate); break;
					case USAGE_TYPE:
						lst = statsRepository.getLayersStatistics(startDate, endDate); break;
				}
		}
		JSONArray results = new JSONArray();
		for (Object o : lst) {
			Object[] row = (Object[]) o;
			results.put(new JSONObject().put("layer", row[0]).put("count", row[1]));
		}
		return new JSONObject().put("results", results)
				.toString(4);
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
	@RequestMapping(value="/distinctUsers", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String distinctUsers(@RequestBody String payload, HttpServletResponse response) throws JSONException {
		JSONObject input = null;
		String groupId = null;
		Date startDate = null, endDate = null;

		try {
			input = new JSONObject(payload);
			if (!input.has("startDate") || !input.has("endDate")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}
			startDate = this.convertLocalDateToUTC(input.getString("startDate"));
			endDate = this.convertLocalDateToUTC(input.getString("endDate"));
			if (input.has("group")) {
				groupId = "ROLE_" + input.getString("group");
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		List lst = null;
		if (groupId != null) {
			lst = statsRepository.getDistinctUsersByGroup(groupId, startDate,
					endDate);
		} else {
			lst = statsRepository.getDistinctUsers(startDate, endDate);
		}
		JSONArray results = new JSONArray();
		for (Object o : lst) {
			Object[] r = (Object[]) o;
			JSONObject row = new JSONObject();
			row.put("user", r[0]);
			row.put("organization", r[1]);
			row.put("nb_requests", r[2]);
			results.put(row);
		}
		return new JSONObject().put("results", results)
				.toString(4);
	}
	
	/**
	 * Calculates the appropriate granularity given the begin date and the end date.
	 *
	 * @param beginDate the begin date.
	 * @param endDate the end date.
	 * @return the most relevant GRANULARITY.
	 */
	private GRANULARITY guessGranularity(Date beginDate, Date endDate) {
		long diff = endDate.getTime() - beginDate.getTime();
		long numdays = TimeUnit.MILLISECONDS.toDays(diff);
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
	 * to date with same timezone as database records.
	 *
	 * @param rawDate Date to convert, should looks like : 2016-02-12
	 * @return Date instance with date and time converted but with wrong timezone, please don't use timezone part of
	 * 		   result
	 * @throws ParseException if input date is not parsable
	 */

	private Date convertLocalDateToUTC(String rawDate) throws ParseException {
		Date date = this.uiInputFormatter.parse(rawDate);
		return this.localTimezone.parse(this.utcTimezone.format(date));
	}

	/**
	 * Convert date from UTC to local configured timezone. This method is used to convert dates returns by database.
	 * @param rawDate raw date from database with format : "2016-02-12 23" or "2016-02-12" or "2016-06" or "2016-02"
	 * @return date in local timezone with hour
	 * @throws ParseException if input date is not parsable
	 */
	private String convertUTCDateToLocal(String rawDate, GRANULARITY granularity) throws ParseException {
		SimpleDateFormat inputFormatter = null, outputFormatter = null;
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

		Date date = inputFormatter.parse(rawDate);

		date = this.utcTimezone.parse(this.localTimezone.format(date));
		return outputFormatter.format(date);

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

	private Date getDateField(JSONObject payload, String field) throws JSONException, ParseException {
		if(payload.has(field))
			return this.convertLocalDateToUTC(payload.getString(field));
		else
			return null;
	}

	private Date getStartDate(JSONObject payload) throws JSONException, ParseException {
		return this.getDateField(payload, "startDate");
	}

	private Date getEndDate(JSONObject payload) throws JSONException, ParseException {
		return this.getDateField(payload, "endDate");
	}

}


