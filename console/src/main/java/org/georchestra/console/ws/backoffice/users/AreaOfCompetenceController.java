/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.users;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.console.bs.areas.AreasService;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @see AreasService
 */
@Controller
public class AreaOfCompetenceController {

    private AccountDao accountDao;
    private AreasService areas;
    private ObjectMapper objectMapper;

    @VisibleForTesting
    @Setter(value = AccessLevel.PACKAGE)
    private Supplier<String> usernameResolver = () -> SecurityContextHolder.getContext().getAuthentication().getName();

    @Autowired
    public AreaOfCompetenceController(@NonNull AccountDao accountDao, @NonNull AreasService areas) {
        this.accountDao = accountDao;
        this.areas = areas;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JtsModule());
    }

    /**
     * Writes the area of competence for the calling user to the response as a
     * GeoJSON geometry.
     * <p>
     * Expected response body is:
     * <ul>
     * <li>{@code null} if the user has no geographic restrictions
     * <li>An empty geometry if the user does not have access to any region
     * <li>A GeoJSON geometry (usually a {@literal MultiPolygon}) with the union of
     * all the user organization's areas.
     * </ul>
     * <p>
     * The area of competence is computed as the geometry union of the
     * {@link Org#getCities() cities} allowed to the organization the calling user
     * belongs to.
     *
     * @throws DataServiceException
     * @throws IOException
     */
    @GetMapping(value = "/account/areaofcompetence")
    public void getCurrentUserAreaOfCompetence(HttpServletResponse response) throws DataServiceException, IOException {
        final String accountId = usernameResolver.get();
        final Account account = accountDao.findByUID(accountId);
        Geometry areaOfCompetence = areas.getAreaOfCompetence(account);
        response.setContentType("application/json");
        OutputStream out = response.getOutputStream();
        this.objectMapper.writeValue(out, areaOfCompetence);
        out.flush();
    }
}
