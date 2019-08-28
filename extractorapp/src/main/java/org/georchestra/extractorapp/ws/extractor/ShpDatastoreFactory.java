package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeatureType;

public class ShpDatastoreFactory implements DatastoreFactory {

    @Override
    public DataStore create(File shpfile, SimpleFeatureType schema) throws IOException {
        ShapefileDataStore ds = new ShapefileDataStore(shpfile.toURI().toURL());
        if (!shpfile.exists()) {
            ds.createSchema(schema);
        }

        return ds;
    }

    @Override
    public String extension() {
        return "shp";
    }

}
