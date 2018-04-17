/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.DelegationEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class LogController {

	private static final Log LOG = LogFactory.getLog(LogController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/admin_logs";
	private static GrantedAuthority ROLE_SUPERUSER = new SimpleGrantedAuthority("ROLE_SUPERUSER");

	@Autowired
	private AdminLogDao logDao;

	@Autowired
	private DelegationDao delegationDao;
	@Autowired
	private OrgsDao orgsDao;
	@Autowired
	private AdvancedDelegationDao advancedDelegationDao;

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
	@RequestMapping(value=REQUEST_MAPPING + "/{target}/{limit}/{page}", method=RequestMethod.GET,
			produces = "application/json; charset=utf-8")
	@ResponseBody
	public List<AdminLogEntry> find(@PathVariable String target, @PathVariable int limit, @PathVariable int page){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Filter logs by orgs users if user is not SUPERUSER
		if(!auth.getAuthorities().contains(ROLE_SUPERUSER)){
			List<String> users = new ArrayList<String>();
			DelegationEntry delegation = this.delegationDao.findOne(auth.getName());
			String[] orgs = delegation.getOrgs();
			for(String org: orgs)
				users.addAll(this.orgsDao.findByCommonName(org).getMembers());
			if(!users.contains(target))
				throw new AccessDeniedException("User not under delegation");
		}

		return this.logDao.findByTarget(target, new PageRequest(page, limit, new Sort(Sort.Direction.DESC, "date")));
	}


	@RequestMapping(value=REQUEST_MAPPING + "/{limit}/{page}", method=RequestMethod.GET,
			produces = "application/json; charset=utf-8")
	@ResponseBody
	public List<AdminLogEntry> find( HttpServletRequest request, @PathVariable int limit, @PathVariable int page){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Filter logs by orgs users if user is not SUPERUSER
		if(!auth.getAuthorities().contains(ROLE_SUPERUSER)){
			Set<String> users = this.advancedDelegationDao.findUsersUnderDelegation(auth.getName());
			return this.logDao.myFindByTargets(users,
					new PageRequest(page, limit, new Sort(Sort.Direction.DESC, "date")));
		} else {
			return this.logDao.findAll(new PageRequest(page, limit, new Sort(Sort.Direction.DESC, "date"))).getContent();
		}

	}

}
