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

package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Maintains useful file information about the uploaded file.
 *
 * @author Mauricio Pazos
 */
public class FileDescriptor {

    /** file name of upload file */
    public String       originalFileName;

    /** upload file's extension */
    public String       originalFileExt;

    /** upload file */
    public File         savedFile;

    /** the geofile format */
    public FileFormat   geoFileType;

    /** list of the geo file extensions */
    public List<String> listOfExtensions = new ArrayList<String>();

    /** list of files contained in the zip file */
    public List<String> listOfFiles      = new LinkedList<String>();

    public FileDescriptor(final String fileName) {

        assert fileName != null;

        originalFileName = fileName;
        originalFileExt = FilenameUtils.getExtension(fileName);
    }

    public boolean isValidFormat() {
        assert originalFileExt != null;
        return "zip".equalsIgnoreCase(originalFileExt)
                || "kml".equalsIgnoreCase(originalFileExt)
                || "gml".equalsIgnoreCase(originalFileExt);
    }

    public boolean isZipFile() {
        return "zip".equalsIgnoreCase(originalFileExt);
    }

}
