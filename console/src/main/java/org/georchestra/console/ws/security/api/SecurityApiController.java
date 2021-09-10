/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.console.ws.security.api;

import java.util.List;

import org.georchestra.ds.security.AccountsService;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/internal", produces = "application/json; charset=utf-8")
public class SecurityApiController {

    private @Autowired AccountsService service;

    /**
     * Return a list of available users as a json array
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<GeorchestraUser> findAllUsers() {
        return service.findAllUsers();
    }

    /**
     * Return a list of available users as a json array
     */
    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<Organization> findAllOrganizations() {
        return service.findAllOrganizations();
    }
}
