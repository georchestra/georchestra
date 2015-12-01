package org.georchestra.analytics;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatisticsController {

	public static enum GRANULARITY { HOUR, DAY, WEEK, MONTH }

	@RequestMapping(value="/combinedRequests", method=RequestMethod.POST, produces= "application/json; charset=utf-8")
	@ResponseBody
	public String combinedRequests(@RequestBody String payload, HttpServletResponse response) throws JSONException {
		return new JSONObject().put("status", "helloworld ééé").toString(4);
	}
}


