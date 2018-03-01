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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configurations
 *
 * <p>
 * This class maintains the shared values in this application.
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
public final class Configuration {

	protected static final Log LOG = LogFactory.getLog(Configuration.class.getName());

	private String publicContextPath;

	@Autowired
	private GeorchestraConfiguration georConfig;

	public String getPublicContextPath() {

	    if ((georConfig != null) && (georConfig.activated())) {
	        LOG.debug("GeorchestraConfiguration activated, using publicContextPath from the geOrchestra datadir.");
	        return georConfig.getProperty("publicContextPath");
	    }

	    // Falls back on the original behaviour
		checkConfiguration();
		return this.publicContextPath;
	}

	private void checkConfiguration() {

		if (StringUtils.isEmpty(this.publicContextPath)) {
			LOG.warn("password recovery context was not configured.");
			return;
		}

		LOG.info("password recovery context was configured: "+ this.publicContextPath);
	}

	public void setPublicContextPath(String publicContextPath) {
		this.publicContextPath = publicContextPath;
	}



}
