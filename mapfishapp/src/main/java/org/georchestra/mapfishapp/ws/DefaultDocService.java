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

import javax.sql.DataSource;

/**
 * This is a convenient class that provides a basic DocService.
 * It means that the features of the abstract base class {@link A_DocService} is enough to do the job.
 * It is useful for creating new doc services that do not need specific behaviors
 * and it also prevent the multiplication of classes that inherits from {@link A_DocService}.
 * @author yoann.buch@gmail.com
 */

public class DefaultDocService extends A_DocService {


	/**
     * This constructor is set private. 
     * It forces user to use {@link DefaultDocService#DefaultDocService(String, String)}
     * 
	 * @param tempDir
	 */
	@SuppressWarnings("unused")
    private DefaultDocService(final String tempDir, DataSource pgpool) {
        super("", "", tempDir, pgpool);
    }
    
    /**
     * Creates a new Doc Service using all the features of its abstract base class 
     * {@link A_DocService}
     * 
     * @param fileExtension
     * @param MIMEType
     * @param tempDir
     */
    public DefaultDocService(final String fileExtension, final String MIMEType, final String tempDir, DataSource pgpool ) {
        super(fileExtension, MIMEType, tempDir, pgpool);
    }

}
