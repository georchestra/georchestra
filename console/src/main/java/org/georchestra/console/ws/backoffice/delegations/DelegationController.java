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

package org.georchestra.console.ws.backoffice.delegations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class DelegationController {

	private static final Log LOG = LogFactory.getLog(DelegationController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/delegation";

	@Autowired
	private DelegationDao delegationDao;
	@Autowired
	private RoleDao roleDao;

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String handleException(Exception e, HttpServletResponse response) throws IOException {
		LOG.error(e.getMessage());
		ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		throw new IOException(e);
	}

	@RequestMapping(value=REQUEST_MAPPING + "/delegations", method= RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String findAll() throws JSONException {

		// TODO filter if request came from delegated admin
		Iterable<DelegationEntry> delegations = this.delegationDao.findAll();

		JSONArray res = new JSONArray();
		for(DelegationEntry delegation: delegations)
			res.put(delegation.toJSON());

		return new JSONObject().put("delegations", res).toString();
	}

	@RequestMapping(value=REQUEST_MAPPING + "/{uid}", method= RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String findByUid(@PathVariable String uid) throws JSONException {

		// TODO test if uid correspond to connected user if request came from delegated admin
		return this.delegationDao.findOne(uid).toJSON().toString();
	}

	@RequestMapping(value=REQUEST_MAPPING + "/{uid}", method= RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String add(HttpServletRequest request, @PathVariable String uid) throws JSONException, IOException, DataServiceException {

		// TODO deny if request came from delegated admin
		// Parse Json
		JSONObject json = new JSONObject(FileUtils.asString(request.getInputStream()));

		DelegationEntry delegation = new DelegationEntry();
		delegation.setUid(uid);
		delegation.setOrgs(json.getJSONArray("orgs").join(",").split(","));
		delegation.setRoles(json.getJSONArray("roles").join(",").split(","));
		this.delegationDao.save(delegation);
		this.roleDao.addUser("ORGADMIN", uid, request.getHeader("sec-username"));

		return new JSONObject().put("result", "ok").toString();
	}

	@RequestMapping(value=REQUEST_MAPPING + "/{uid}", method= RequestMethod.DELETE, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String delete(HttpServletRequest request, @PathVariable String uid) throws JSONException, IOException, DataServiceException {

		// TODO deny if request came from delegated admin
		this.delegationDao.delete(uid);
		this.roleDao.deleteUser("ORGADMIN", uid, request.getHeader("sec-username"));
		return new JSONObject().put("result", "ok").toString();
	}

}
