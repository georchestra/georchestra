package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.mif.MIFDataStore;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

public class MifDatastoreFactory implements DatastoreFactory {
    private static final Log       LOG = LogFactory.getLog(MifDatastoreFactory.class.getPackage().getName());

    @Override
    public DataStore create(File filename, SimpleFeatureType schema) throws IOException{
        HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        try {
            Integer crs = CRS.lookupEpsgCode(schema.getCoordinateReferenceSystem(), true);
            params.put(MIFDataStore.PARAM_SRID, crs );
        } catch (FactoryException e) {
            LOG.warn("unable to convert "+schema.getCoordinateReferenceSystem()+" to a EPSG code", e);
        }
        params.put(MIFDataStore.PARAM_GEOMTYPE, "untyped");
        MIFDataStore ds = new MIFDataStore(filename.getParentFile().getPath(), params);
        ds.createSchema(schema);
            
        return ds;
    }

    @Override
    public String extension() {
        return "mif";
    }


}
