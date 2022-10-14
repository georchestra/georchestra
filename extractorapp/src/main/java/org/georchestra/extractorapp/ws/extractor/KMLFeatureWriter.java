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

package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xsd.Encoder;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * This class implements the KML file writing strategy
 *
 * @author Florent Gravin
 *
 */
final class KMLFeatureWriter extends FileFeatureWriter {

    /**
     * New instance of {@link OGRFeatureWriter}
     *
     * @param basedir  output folder
     * @param features input the set of Features to write
     */
    public KMLFeatureWriter(File basedir, SimpleFeatureCollection features) {

        super(basedir, features);

    }

    /**
     *
     * @return Format file extension
     */
    protected String extension() {
        return "kml";
    }

    /**
     * Generates a vector files in the specified format
     *
     * @throws IOException
     */
    @Override
    public List<File> generateFiles() throws IOException {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        SimpleFeatureType schema = features.getSchema();
        builder.setName(schema.getName());

        File file = new File(basedir, builder.getName() + "." + extension());
        try (FileOutputStream fop = new FileOutputStream(file)) {
            Encoder encoder = new Encoder(new KMLConfiguration());
            encoder.setIndenting(true);
            encoder.encode(features, KML.kml, fop);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Generated file: " + file.getAbsolutePath());
            }
            fop.flush();
            return Collections.singletonList(file);
        } catch (IOException e) {
            final String message = "Failed generation: " + schema.getName() + " - " + e.getMessage();
            LOG.error(message);
            throw e;
        }
    }

}
