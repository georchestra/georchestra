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

package org.georchestra.mapfishapp.ws;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Main controller that display the extractor home page
 * 
 * @author bruno.binet@camptocamp.com
 */

@Controller
@RequestMapping("/home")
public class HomeController extends AbstractController {

    @Autowired
    private GeorchestraConfiguration georConfig;

    /**
     * POST entry point.
     * 
     * @param request
     *            . Must contains information from the layers and services to be
     *            extracted
     * @param response
     *            .
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String str = getPostData(request);

        Map<String, Object> model = createModelFromString(request, str);

        return new ModelAndView("index", "c", model);
    }

    /**
     * GET entry point.
     * 
     * @param request
     *            .
     * @param response
     *            .
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        String str = request.getParameter("data");

        Map<String, Object> model = createModelFromString(request, str);

        return new ModelAndView("index", "c", model);
    }

}
