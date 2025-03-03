/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.console.ws;

import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.console.bs.ExpiredTokenManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Displays the home page, also intercepts some assets.
 *
 *
 * @author Mauricio Pazos, Pierre Mauduit
 *
 */
@Controller
public class HomeController {

    private static final Log LOG = LogFactory.getLog(HomeController.class.getName());
    private ExpiredTokenManagement tokenManagement;

    @Value("${publicContextPath:/console}")
    private String publicContextPath;

    @Autowired
    private ServletContext context;

    public HomeController(ExpiredTokenManagement tokenManagment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("home controller initialization");
        }

        this.tokenManagement = tokenManagment;
        this.tokenManagement.start();
    }

    @RequestMapping(value = "/")
    public void root(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String roles = SecurityHeaders.decode(request.getHeader(SEC_ROLES));

        if (roles != null) {
            String redirectUrl;
            List<String> rolesList = Arrays.asList(roles.split(";"));

            if (rolesList.contains("ROLE_SUPERUSER") || rolesList.contains("ROLE_ORGADMIN")) {
                redirectUrl = "/manager/";
            } else {
                redirectUrl = "/account/userdetails";
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("root page request -> redirection to " + publicContextPath + redirectUrl);
            }
            response.sendRedirect(publicContextPath + redirectUrl);
        } else {
            // redirect to CAS
            response.sendRedirect(publicContextPath + "/account/userdetails?login");
        }
    }

    @RequestMapping(value = "/manager/")
    public String consoleHome(HttpServletRequest request) throws IOException {
        return "managerUi";
    }

    public void setPublicContextPath(String publicContextPath) {
        this.publicContextPath = publicContextPath;
    }
}
