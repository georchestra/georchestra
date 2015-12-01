package org.georchestra.mapfishapp.ws.buffer;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.vividsolutions.jts.geom.Geometry;

@Controller
public class BufferController {

	@RequestMapping(value = "/buffer/{bufferValue}",
			method = RequestMethod.POST,
			produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String computeBuffer(@PathVariable double bufferValue,
			@RequestBody String wktgeom,
			HttpServletResponse response) throws JSONException {

		JSONObject js = new JSONObject();
		try {

			WKTReader w = new WKTReader();
			Geometry g = w.read(wktgeom);
			g = g.buffer(bufferValue);
			js.put("geometry", g.toString());

		} catch (ParseException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new JSONObject().put("error", e.getMessage()).toString();
		}

		return js.toString();
	}
}
