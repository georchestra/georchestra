/*
 * Copyright (C) 2021 by the geOrchestra PSC
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

public class ShpFeatureWriterTest {

    public @Rule TemporaryFolder tmp = new TemporaryFolder();

    public @Test void testShapefileAttributeNames() throws IOException {
        File basedir = tmp.getRoot();
        SimpleFeatureCollection features = buildEmptyCollectionWithNames("TypeWithLongNames", "long_geometry",
                "long_property_name_1", "long_property_name_2", "long_property_name_3");

        File shpFile = generateAndReturnShapefiles(basedir, features).get(0);

        String typeName = FilenameUtils.getBaseName(shpFile.getName());
        DataStore ds = loadShapefile(shpFile);
        try {
            SimpleFeatureType schema = ds.getSchema(typeName);

            List<String> expected = Arrays.asList("the_geom", "long_prope", "long_prop0", "long_prop1");
            List<String> actual = schema.getAttributeDescriptors().stream().map(AttributeDescriptor::getName)
                    .map(Name::getLocalPart).collect(Collectors.toList());
            assertEquals(expected, actual);
        } finally {
            ds.dispose();
        }
    }

    public @Test void testMultipleGeometryTypesGoToSeparateShapefiles() throws IOException, SchemaException {
        File basedir = tmp.getRoot();
        SimpleFeatureCollection features = buildCollectionWithMixedGeometryTypes();
        final String origName = features.getSchema().getTypeName();

        List<File> shpFiles = generateAndReturnShapefiles(basedir, features);

        Set<String> expected = new HashSet<>(Arrays.asList(//
                origName + "Point.shp", //
                origName + "MPoint.shp", //
                origName + "Line.shp", //
                origName + "Polygon.shp"));

        Set<String> actual = shpFiles.stream().map(f -> f.getName()).collect(Collectors.toSet());
        assertEquals(expected, actual);
        assertEquals(Point.class, getGeometryType(shpFiles, origName + "Point.shp"));
        assertEquals(MultiPoint.class, getGeometryType(shpFiles, origName + "MPoint.shp"));
        assertEquals(MultiLineString.class, getGeometryType(shpFiles, origName + "Line.shp"));
        assertEquals(MultiPolygon.class, getGeometryType(shpFiles, origName + "Polygon.shp"));
    }

    private Class<?> getGeometryType(List<File> shpFiles, String fileName) throws IOException {
        return getSchema(shpFiles, fileName).getGeometryDescriptor().getType().getBinding();
    }

    private SimpleFeatureType getSchema(List<File> files, String name) throws IOException {
        File file = files.stream().filter(f -> f.getName().equals(name)).findFirst()
                .orElseThrow(() -> new NoSuchElementException(name));
        DataStore ds = loadShapefile(file);
        try {
            return ds.getSchema(ds.getTypeNames()[0]);
        } finally {
            ds.dispose();
        }
    }

    private List<File> generateAndReturnShapefiles(File basedir, SimpleFeatureCollection features) throws IOException {
        ShpFeatureWriter writer = new ShpFeatureWriter(basedir, features);
        List<File> files = writer.generateFiles();
        return files.stream().filter(f -> f.getName().endsWith(".shp")).collect(Collectors.toList());
    }

    private DataStore loadShapefile(File shpFile) throws MalformedURLException {
        ShapefileDataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
        return store;
    }

    private SimpleFeatureCollection buildCollectionWithMixedGeometryTypes() throws SchemaException {
        String typeSpec = "name:String,geom:Geometry:srid=4326";
        SimpleFeatureType schema = DataUtilities.createType("MixedGeoms", typeSpec);
        ListFeatureCollection list = new ListFeatureCollection(schema);

        SimpleFeatureBuilder b = new SimpleFeatureBuilder(schema);
        list.add(feature(b, "1", "point", geom("POINT(0 1)")));
        list.add(feature(b, "2", "mpoint", geom("MULTIPOINT(1 1, 2 2)")));
        list.add(feature(b, "3", "line", geom("LINESTRING(-180 0, 180 0)")));
        list.add(feature(b, "4", "mline", geom("MULTILINESTRING ((10 10, 20 20),(40 40, 30 30))")));
        list.add(feature(b, "5", "poly", geom("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))")));
        list.add(feature(b, "5", "mpoly", geom(//
                "MULTIPOLYGON ( ((40 40, 20 45, 45 30, 40 40)),((20 35, 10 30, 10 10, 30 5, 45 20, 20 35)) )"//
        )));
        return list;
    }

    private SimpleFeatureCollection buildEmptyCollectionWithNames(String typeName, String... attribtues) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(typeName);
        b.setSRS("EPSG:4326");
        b.add(attribtues[0], MultiPolygon.class);

        for (int i = 1; i < attribtues.length; i++) {
            b.add(attribtues[i], String.class);
        }
        SimpleFeatureType type = b.buildFeatureType();
        return new ListFeatureCollection(type);
    }

    private SimpleFeature feature(SimpleFeatureBuilder b, String id, Object... values) {
        b.reset();
        for (int i = 0; i < values.length; i++) {
            b.set(i, values[i]);
        }
        return b.buildFeature(id);
    }

    private Geometry geom(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
