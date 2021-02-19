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
import java.io.IOException;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Creates the a file that contains a bounding box geometry related with the
 * extracted features.
 * 
 * @author Mauricio Pazos
 *
 */
public class BBoxWriter {

    // Properties of the bbox FeatureType
    private static final String GEOMETRY_PROPERTY = "the_geom";
    private static final String ID_PROPERTY = "id";

    private ReferencedEnvelope bbox;
    private File baseDir;
    private FileFormat fileFormat;
    private CoordinateReferenceSystem requestedCRS;

    /**
     * New instance of BBoxWriter
     * 
     * @param bbox         the bbox used to create the polygon
     * @param baseDir      where the file is created
     * @param format
     * @param requestedCRS CRS used to project the polygon associated to the bbox
     */
    public BBoxWriter(ReferencedEnvelope bbox, File baseDir, FileFormat format,
            CoordinateReferenceSystem requestedCRS) {
        assert bbox != null;
        assert baseDir != null;
        assert format != null;
        assert requestedCRS != null;

        this.bbox = bbox;
        this.baseDir = baseDir;
        this.fileFormat = format;
        this.requestedCRS = requestedCRS;
    }

    /**
     * Writes the bbox files in the required format
     * 
     * @return the set of {@link File} created
     * 
     * @throws IOException
     */
    public File[] generateFiles() throws IOException {

        // create the feature type for the bbox geometry
        SimpleFeatureType type = createFeatureType();

        // sets bbox feature the the attributes
        Geometry geom = createBBoxGeometry(this.bbox, this.requestedCRS);
        SimpleFeature bboxFeature = createFeature(geom, type);

        // writes the file
        SimpleFeatureCollection features = DataUtilities.collection(bboxFeature);

        // bbox in shapefile format
        FeatureWriterStrategy writer = new ShpFeatureWriter(features.getSchema(), this.baseDir, features);
        return writer.generateFiles();
    }

    /**
     * Creates the feature type for the bbox feature
     * 
     * @return
     * @throws IOException
     */
    private SimpleFeatureType createFeatureType() throws IOException {

        try {
            Integer epsgCode = CRS.lookupEpsgCode(this.requestedCRS, false);

            SimpleFeatureType type = DataUtilities.createType("bounding",
                    GEOMETRY_PROPERTY + ":Polygon:srid=" + epsgCode + "," + ID_PROPERTY + ":Integer");

            return type;

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Creates a feature with the polygon
     * 
     * @param geom
     * @param type
     * @return {@link SimpleFeature}
     */
    private SimpleFeature createFeature(Geometry geom, SimpleFeatureType type) {

        SimpleFeature feature = DataUtilities.template(type);

        feature.setAttribute(ID_PROPERTY, 1);
        feature.setAttribute(GEOMETRY_PROPERTY, geom);

        return feature;
    }

    /**
     * Creates a polygon or multipolygon geometry using the bbox as reference. The
     * new polygon will be in the target crs.
     * 
     * @param envelope
     * @param geomClass required geometry
     * @param epsgCode
     * @return a Polygon or MultiPolygon geometry
     * @throws IOException
     */
    private Polygon createBBoxGeometry(ReferencedEnvelope envelope, CoordinateReferenceSystem targetCrs)
            throws IOException {

        try {

            Polygon polygon = JTS.toGeometry(envelope.toBounds(targetCrs));

            polygon.setSRID(CRS.lookupEpsgCode(targetCrs, false));

            return polygon;

        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
