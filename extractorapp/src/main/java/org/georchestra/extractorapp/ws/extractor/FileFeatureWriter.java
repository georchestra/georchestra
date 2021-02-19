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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
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

    protected SimpleFeatureType schema;
    protected File basedir;
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> features;

    /**
     * Sets the strategy parameters
     * 
     * @param schema   output schema
     * @param basedir  output base folder
     * @param features the input set of features to write
     */
    public FileFeatureWriter(SimpleFeatureType schema, File basedir, SimpleFeatureCollection features) {

        this.schema = schema;
        this.basedir = basedir;
        this.features = features;

    }
}
