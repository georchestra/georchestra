package org.georchestra.analytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

@Controller
public class StatisticsController {
    @Autowired
    private StatsRepo statsRepository;

	public static enum GRANULARITY { HOUR, DAY, WEEK, MONTH }

	/**
	 * Testing onto users:
	 *  curl -XPOST --data-binary '{"user": "testadmin", "startDate": "2015-01-01", "endDate": "2015-12-01" }' \
	   -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/combinedRequests -i
	 * Testing onto groups:
	   curl -XPOST --data-binary '{"group": "ADMINISTRATOR", "startDate": "2015-10-01", "endDate": "2015-11-01" }' \
           -H'Content-Type: application/json'   http://localhost:8280/analytics/ws/combinedRequests -i
	   
	 * @param payload
	 * @param response
	 * @return
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
		List<Object> lst ;
		if (userId != null) {
			lst = statsRepository.getRequestCountForUserBetweenStartDateAndEndDate(userId,
					startDate, endDate);
		} else {
			lst = statsRepository.getRequestCountForGroupBetweenStartDateAndEndDate(groupId,
					startDate, endDate);
		}
		JSONArray results = new JSONArray();
		for (Object o : lst) {
			Object[] row = (Object[]) o;
			// o[0] is a Long
			// o[1] is a date
			results.put(new JSONObject().put("count", row[0]).put("date", row[1]));
		}
		return new JSONObject().put("results", results)
				.put("granularity", GRANULARITY.DAY)
				.toString(4);
	}
}


