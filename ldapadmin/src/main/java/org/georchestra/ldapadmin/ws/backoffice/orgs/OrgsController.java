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

package org.georchestra.ldapadmin.ws.backoffice.orgs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.OrgsDao;
import org.georchestra.ldapadmin.dto.Org;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Controller
public class OrgsController {

    private static final Log LOG = LogFactory.getLog(OrgsDao.class.getName());


    private static final String BASE_MAPPING = "/private";
    private static final String BASE_RESOURCE = "orgs";
    private static final String REQUEST_MAPPING = BASE_MAPPING + "/" + BASE_RESOURCE;

    @Autowired
    private OrgsDao orgDao;

    @Autowired
    public OrgsController(OrgsDao dao) {
        this.orgDao = dao;
    }

    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.GET)
    public void findAll(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            List<Org> orgs = this.orgDao.findAll();
            JSONArray res = new JSONArray();
            for (Org org : orgs)
                res.put(org.toJson());

            ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            throw new IOException(e);
        }


    }

}