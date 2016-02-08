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

package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStore;
import org.geotools.data.mif.MIFDataStore;
import org.geotools.data.mif.MIFDataStoreFactory;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

public class MifDatastoreFactory implements DatastoreFactory {
    private static final Log       LOG = LogFactory.getLog(MifDatastoreFactory.class.getPackage().getName());

    @Override
    public DataStore create(File filename, SimpleFeatureType schema) throws IOException{
        HashMap<Param, Object> params = new HashMap<Param, Object>();
        try {
            Integer crs = CRS.lookupEpsgCode(schema.getCoordinateReferenceSystem(), true);
            params.put(MIFDataStoreFactory.PARAM_SRID, crs );
        } catch (FactoryException e) {
            LOG.warn("unable to convert "+schema.getCoordinateReferenceSystem()+" to a EPSG code", e);
        }
        params.put(MIFDataStoreFactory.PARAM_GEOMTYPE, "untyped");
        
		MIFDataStore ds = new MIFDataStore(filename.getParentFile().getPath(), params);

        ds.createSchema(schema);
            
        return ds;
    }

    @Override
    public String extension() {
        return "mif";
    }


}
