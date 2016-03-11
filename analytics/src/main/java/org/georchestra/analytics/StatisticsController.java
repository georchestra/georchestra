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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
 *  if datediff < 1 day    then granularity by hour
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
	public String combinedRequests(@RequestBody String payload, HttpServletResponse response) throws JSONException {
		JSONObject input = null;
		String userId  = null, groupId = null;
		Date startDate = null, endDate = null;
		try {
			input = new JSONObject(payload);
			if (!input.has("startDate") || !input.has("endDate")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			startDate = sdf.parse(input.getString("startDate"));
			endDate = sdf.parse(input.getString("endDate"));
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (input.has("user")) {
			userId = input.getString("user");
		}
		if (input.has("group")) {
			groupId = input.getString("group");
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
			results.put(new JSONObject().put("count", row[0]).put("date", row[1]));
		}
		return new JSONObject().put("results", results)
				.put("granularity", g)
				.toString(4);
	}

	/**
	 * Gets statistics for layers consumption. May be filtered by a user or a group.
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
		JSONObject input = null;
		String userId  = null, groupId = null;
		Date startDate = null, endDate = null;
		int limit = -1;

		try {
			input = new JSONObject(payload);
			if (!input.has("startDate") || !input.has("endDate")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			startDate = sdf.parse(input.getString("startDate"));
			endDate = sdf.parse(input.getString("endDate"));
			if (input.has("limit")) {
				limit = input.getInt("limit");
			}
			if (input.has("user")) {
				userId = input.getString("user");
			}
			if (input.has("group")) {
				groupId = input.getString("group");
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		List lst = null;
		if (userId != null) {
			if (limit > 0)
				lst = statsRepository.getLayersStatisticsForUserLimit(userId, startDate, endDate, limit);
			else
				lst = statsRepository.getLayersStatisticsForUser(userId, startDate, endDate);				
		} else if (groupId != null) {
			if (limit > 0)
				lst = statsRepository.getLayersStatisticsForGroupLimit(groupId, startDate, endDate, limit);
			else
				lst = statsRepository.getLayersStatisticsForGroup(groupId, startDate, endDate);				
		} else {
			if (limit > 0)
				lst = statsRepository.getLayersStatisticsLimit(startDate, endDate, limit);
			else
				lst = statsRepository.getLayersStatistics(startDate, endDate);							
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
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			startDate = sdf.parse(input.getString("startDate"));
			endDate = sdf.parse(input.getString("endDate"));
			if (input.has("group")) {
				groupId = input.getString("group");
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
		if (numdays < 1) {
			return GRANULARITY.HOUR;
		} else if (numdays < 90) {
			return GRANULARITY.DAY;
		} else if (numdays < 365) {
			return GRANULARITY.WEEK;
		} else {
			return GRANULARITY.MONTH;
		}
	}
}


