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
 * where granularity will depend on the submitted date, following the algorithm:
 *  if datediff < 1 day    then granularity by hour
 *  if datediff < 1 week   then granularity by day
 *  if datediff < 1 month  then granularity by day
 *  if datediff < 3 months then granularity by week
 *  if datediff < 1 year   then granularity by month
 *
 * @author pmauduit
 * @since 15.12
 */

@Controller
public class StatisticsController {
    @Autowired
    private StatsRepo statsRepository;

	public static enum GRANULARITY { HOUR, DAY, WEEK, MONTH }

	/**
	 * Testing onto users:
	 *
	 *  curl -XPOST --data-binary '{"user": "testadmin", "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
	   -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/combinedRequests -i
	 *
	 * Testing onto groups:
	 *
	   curl -XPOST --data-binary '{"group": "ADMINISTRATOR", "startDate": "2015-10-01", "endDate": "2015-11-01" }' \
           -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/combinedRequests -i
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
		// Either group or user is mandatory, but not both
		if (((userId == null) && (groupId == null)
				|| ((userId != null) && (groupId != null)))) {
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
		} else {
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
		}
		JSONArray results = new JSONArray();
		for (Object o : lst) {
			Object[] row = (Object[]) o;
			// o[0] is a Long
			// o[1] is a String
			results.put(new JSONObject().put("count", row[0]).put("date", row[1]));
		}
		return new JSONObject().put("results", results)
				.put("granularity", g)
				.toString(4);
	}

	/**
	 * Calculates the appropriate granularity given the begin date and end date.
	 *
	 * @param beginDate
	 * @param endDate
	 * @return the most relevant GRANULARITY
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


