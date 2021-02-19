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

package org.georchestra.extractorapp.ws.extractor;

import static java.util.Objects.requireNonNull;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * This abstract class defines the template strategy required to write different
 * sort of vector files.
 * 
 * @author Mauricio Pazos
 *
 */
abstract class FileFeatureWriter implements FeatureWriterStrategy {

    protected static final Log LOG = LogFactory.getLog(FileFeatureWriter.class.getPackage().getName());

    protected final SimpleFeatureType schema;
    protected final File basedir;
    protected final SimpleFeatureCollection features;

    /**
     * Sets the strategy parameters
     * 
     * @param schema   output schema
     * @param basedir  output base folder
     * @param features the input set of features to write
     */
    public FileFeatureWriter(SimpleFeatureType schema, File basedir, SimpleFeatureCollection features) {
        requireNonNull(schema, "schema is null");
        requireNonNull(basedir, "basedir is null");
        requireNonNull(features, "features is null");
        if (!basedir.isDirectory())
            throw new IllegalArgumentException("basedir is not a directory: " + basedir);
        this.schema = schema;
        this.basedir = basedir;
        this.features = features;
    }
}
