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

package org.georchestra.mapfishapp.ws;

import org.georchestra.mapfishapp.model.ConnectionPool;

/**
 * This service handles the storage and the loading of a Filter Encoding file
 *
 * @author yoann buch  - yoann.buch@gmail.com
 * @author jean-denis giguère - jdenisgiguere@gmail.com
 *
 */

public class FEDocService extends A_DocService {

    public static final String FILE_EXTENSION = ".xml";
    public static final String MIME_TYPE = "application/xml";

    public FEDocService(final String tempDir, ConnectionPool pgpool) {
        super(FILE_EXTENSION, MIME_TYPE, tempDir, pgpool);
    }

    /**
     * Called before saving the content
     * @throws DocServiceException
     */
    @Override
    protected void preSave() throws DocServiceException {
        //We do not perform any action before saving the file.
    }

    /**
     * Called right after the loading of the file content
     * @throws DocServiceException
     */
    @Override
    protected void postLoad() throws DocServiceException {
        //We do not perform any action on file once loaded in memory.
    }

}
