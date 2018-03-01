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

package org.georchestra.console;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Configuration interceptor
 *
 * <p>
 * This class adds the configuration parameters to model before calling Controllers.
 * </p>
 *
 * @author Sylvain Lesage
 *
 */
public class ConfigurationInterceptor extends HandlerInterceptorAdapter{

	@Autowired
	private Configuration config;

	@Autowired
	private GeorchestraConfiguration georConfig;
	   
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws Exception {

		HttpSession currentSession = request.getSession();
		currentSession.setAttribute("publicContextPath", config.getPublicContextPath());

		if ((georConfig != null) && (georConfig.activated())) {
		  currentSession.setAttribute("headerHeight", georConfig.getProperty("headerHeight"));
		} else {
		  currentSession.setAttribute("headerHeight", "90");
		}
		return true;
	}

}
