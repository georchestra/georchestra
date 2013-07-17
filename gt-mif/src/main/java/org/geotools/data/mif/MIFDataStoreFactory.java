/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 * 
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.mif;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Builds a MIFDataStore. Required parameters are:
 * 
 * <ul>
 * <li>
 * MIFDataStore.PARAM_DBTYPE: String, must be "mif"
 * </li>
 * <li>
 * MIFDataStore.PARAM_PATH: String, full path of the directory containing MIF
 * files, or path of a single mif file (with or without .mif extension)
 * </li>
 * </ul>
 * 
 * <p>
 * For a full description of creation parameters, see MIFDataStore().
 * </p>
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/data/mif/MIFDataStoreFactory.java $
 * @version $Id: MIFDataStoreFactory.java 30702 2008-06-13 14:57:03Z acuster $
 *
 * @see MIFDataStore#MIFDataStore(String, HashMap)
 */
public class MIFDataStoreFactory implements DataStoreFactorySpi {
    // DataStore - specific parameters
    public static final Param PARAM_PATH = new Param("path", File.class,
            "Full path of directory containing mifs or single mif file", true,
            "c:/data/mifpath/");
    public static final Param PARAM_NAMESPACE = new Param("namespace",
            URI.class,
            "URI of the namespace prefix for FeatureTypes returned by this DataStore",
            false);

    // Options
    public static final Param PARAM_FIELDCASE = new Param(MIFDataStore.PARAM_FIELDCASE,
            String.class,
            "Field name case transformation, can be \"\" (no transform), \"upper\" (to uppercase) or \"lower\" (to lowercase).",
            false, "upper");
    public static final Param PARAM_GEOMNAME = new Param(MIFDataStore.PARAM_GEOMNAME,
            String.class,
            "Name of the geometry field, if not specified defaults to \"the_geom\".",
            false, "the_geom");
    public static final Param PARAM_GEOMFACTORY = new Param(MIFDataStore.PARAM_GEOMFACTORY,
            GeometryFactory.class,
            "GeometryFactory object used for building geometries", false,
            "new GeometryFactory()");
    public static final Param PARAM_GEOMTYPE = new Param(MIFDataStore.PARAM_GEOMTYPE,
            String.class, "Can be typed, untyped or multi (implies typed).",
            false, "untyped");
    public static final Param PARAM_SRID = new Param(MIFDataStore.PARAM_SRID,
            Integer.class,
            "SRID code for Geometry, use as alternative for GEOMFACTORY",
            false, new Integer(26591));

    // Header clauses
    public static final Param PARAM_COORDSYS = new Param(MIFDataStore.HCLAUSE_COORDSYS,
            String.class, "CoordSys clause for new files", false,
            "CoordSys Earth Projection 8, 87, \"m\", 9, 0, 0.9996, 1500000, 0 Bounds (-6746230.6469, -9998287.38389) (9746230.6469, 9998287.38389)");
    public static final Param PARAM_CHARSET = new Param(MIFDataStore.HCLAUSE_CHARSET,
            String.class, "Charset clause", false, "WindowsLatin1");
    public static final Param PARAM_DELIMITER = new Param(MIFDataStore.HCLAUSE_DELIMITER,
            String.class, "Delimiter to be used in output MID files", false, ";");
    public static final Param PARAM_INDEX = new Param(MIFDataStore.HCLAUSE_INDEX,
            String.class,
            "Index clasue (comma separated list of indexed field numbers",
            false, "1,2,4");
    public static final Param PARAM_TRANSFORM = new Param(MIFDataStore.HCLAUSE_TRANSFORM,
            String.class, "Transform clause to be uised for output", false,
            "0.5,0.5,0,0");
    public static final Param PARAM_UNIQUE = new Param(MIFDataStore.HCLAUSE_UNIQUE,
            String.class,
            "Unique clause - comma separated list of field numbers forming unique values.",
            false, "1,2");
    public static final Param PARAM_VERSION = new Param(MIFDataStore.HCLAUSE_VERSION,
            String.class, "Version ID", false, "410");

    /**
     * Creates a new MIFDataStoreFactory object.
     */
    public MIFDataStoreFactory() {
        super();
    }

    /**
     */
    public String getDisplayName() {
        return "MIFDataStore";
    }

    /**
     * <p>
     * As the creation of new MIF files is simply achieved by createSchema()
     * calls, this method simply calls {@link #createDataStore(Map)}. 
     * </p>
     *
     * @param params The parameter map
     *
     * @return the MIFDataStore instance returned by createDataStore(params)
     *
     * @throws IOException
     *
     * 
     */
	public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
		return createDataStore(params);
	}

	/**
	 * Makes a {@link MIFDataStore} based on the parameters.
	 * 
	 * @param params
	 * 
	 */
	public DataStore createDataStore(Map<String, Serializable> params)	throws IOException {
        if (!processParams(params)) {
            throw new IOException("The parameters map isn't correct.");
        }

        MIFDataStore mif = null;

        try {
            HashMap<Param,Object> parameters = new HashMap<Param,Object> ();

            File path = (File) PARAM_PATH.lookUp(params);

            addParamToMap(PARAM_NAMESPACE, params, parameters, null);

            // Options
            addParamToMap(PARAM_FIELDCASE, params, parameters, null);
            addParamToMap(PARAM_GEOMNAME, params, parameters, null);
            addParamToMap(PARAM_GEOMTYPE, params, parameters, null);
            addParamToMap(PARAM_GEOMFACTORY, params, parameters, null);
            addParamToMap(PARAM_SRID, params, parameters, null);

            // Header
            addParamToMap(PARAM_COORDSYS, params, parameters, null);
            addParamToMap(PARAM_CHARSET, params, parameters, null);
            addParamToMap(PARAM_DELIMITER, params, parameters, null);
            addParamToMap(PARAM_INDEX, params, parameters, null);
            addParamToMap(PARAM_TRANSFORM, params, parameters, null);
            addParamToMap(PARAM_UNIQUE, params, parameters, null);
            addParamToMap(PARAM_VERSION, params, parameters, null);

            mif = new MIFDataStore(path.getPath(), parameters);

            return mif;
            
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void addParamToMap(Param param, Map<String, Serializable> params, HashMap<Param, Object> map, Object defa) {
        Object val = null;

        try {
            val = param.lookUp(params);
        } catch (Exception e) {
        }

        if (val != null) {
            map.put(param, val);
        } else if (defa != null) {
            map.put(param, defa);
        }
    }


    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "MapInfo MIF/MID format datastore";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        Param[] params = {
                PARAM_PATH, PARAM_NAMESPACE,
                
                PARAM_FIELDCASE, PARAM_GEOMNAME, PARAM_GEOMFACTORY,
                PARAM_GEOMTYPE, PARAM_SRID,
                
                PARAM_COORDSYS, PARAM_CHARSET, PARAM_DELIMITER, PARAM_INDEX,
                PARAM_TRANSFORM, PARAM_UNIQUE, PARAM_VERSION
            };

        return params;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
	public boolean canProcess(Map<String, Serializable> params) {
        try {
            return processParams(params);
        } catch (Exception e) {
            return false;
        }
	}

    
    

    /*
     * Utility function for processing params
     */
    private boolean processParams(Map<String,Serializable> params) throws IOException {

        String path = String.valueOf(PARAM_PATH.lookUp(params));
        File file = new File(path);

        if (!file.isDirectory()) {
            // Try to build a File object pointing to a .mif file
            // Will throw an exception if the file cannot be found
            MIFFile.getFileHandler(file.getParentFile(), MIFFile.getMifName(file.getName()), ".mif", true);
        }

        return true;
    }

    /**
     * <p>
     * This method always returns true, because no specific libraries are
     * required by MIFDataStore.
     * </p>
     *
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * <p>
     * Always return Collections#EMPTY_MAP, because no hints are available for
     * now.
     * </p>
     *
     * @see org.geotools.factory.Factory#getImplementationHints()
     */
    public Map getImplementationHints() {
        // TODO Check possible use of hints for GeometryFactory, SRID.
        return Collections.EMPTY_MAP;
    }


}
