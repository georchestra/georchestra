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

package org.georchestra.ldapadmin.ws.backoffice.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.dao.AdminLogDao;
import org.georchestra.ldapadmin.model.AdminLogEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class LogController {

	private static final Log LOG = LogFactory.getLog(LogController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/admin_logs";

	@Autowired
	private AdminLogDao logDao;

	/**
	 * Returns array of logs using json syntax.
	 * <pre>
	 *     {"logs": [
	 *		{
	 *			"admin": "testadmin",
 	 *			"date": "2016-03-22T15:26:21.087+0100",
	 *			"target": "testeditor",
	 *			"type": "Email sent"
	 *		},
	 *		{
	 *			"admin": "testadmin",
	 *			"date": "2016-03-21T17:50:09.258+0100",
	 *			"target": "joe",
	 *			"type": "Email sent"
	 *		},
	 *		{
	 *			"admin": "testadmin",
	 *			"date": "2016-03-21T17:50:09.258+0100",
	 *			"target": "marie",
	 *			"type": "Email sent"
	 *		}
	 *	]}
	 * </pre>
	 *
	 */
	@RequestMapping(value=REQUEST_MAPPING + "/{target}/{limit}/{page}", method=RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String find( HttpServletRequest request, @PathVariable String target, @PathVariable int limit, @PathVariable int page) throws JSONException {

		List<AdminLogEntry> logs = this.logDao.findByTarget(target, new PageRequest(page, limit, new Sort("date")));

		JSONArray res = new JSONArray();
		for(AdminLogEntry log: logs)
			res.put(log.toJSON());

		return new JSONObject().put("logs", res).toString(4);
	}


	@RequestMapping(value=REQUEST_MAPPING + "/{limit}/{page}", method=RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String find( HttpServletRequest request, @PathVariable int limit, @PathVariable int page) throws JSONException {

		Page<AdminLogEntry> logs = this.logDao.findAll(new PageRequest(page, limit, new Sort("date")));

		JSONArray res = new JSONArray();
		for(AdminLogEntry log: logs)
			res.put(log.toJSON());

		return new JSONObject().put("logs", res).toString(4);
	}
}
