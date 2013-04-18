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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.*;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * <p>
 * MIFDataStore gives read and write access to MapInfo MIF files.  It can be
 * instantiated either on a single .mif file, or on a directory (thus exposing
 * all the mif files within).
 * </p>
 * 
 * <p>
 * MIFDataStore is a replacement for the MapInfoDataStore, which was based on
 * the legacy MapInfoDataSource.
 * </p>
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/data/mif/MIFDataStore.java $
 * @version $Id: MIFDataStore.java 30702 2008-06-13 14:57:03Z acuster $
 */
public class MIFDataStore extends AbstractDataStore {
    // MIF Header clause names
    public static final String HCLAUSE_VERSION = "version";
    public static final String HCLAUSE_CHARSET = "charset";
    public static final String HCLAUSE_DELIMITER = "delimiter";
    public static final String HCLAUSE_UNIQUE = "unique";
    public static final String HCLAUSE_INDEX = "index";
    public static final String HCLAUSE_COORDSYS = "coordsys";
    public static final String HCLAUSE_TRANSFORM = "transform";

    // Config parameter names
    public static final String PARAM_FIELDCASE = "fieldCase";
    public static final String PARAM_GEOMFACTORY = "geometryFactory";
    public static final String PARAM_GEOMNAME = "geometryFieldName";
    public static final String PARAM_GEOMTYPE = "geometryType";
    public static final String PARAM_SRID = "SRID";

    /** The path in which MIF/MIDs are being stored, or the single MIF file */ 
    private File filePath;

    /** The parameter maps to pass to MIFFile constructors*/
    private HashMap<Param,Object> params = null;

    // A map of MIFFileHolders, indexed by FeatureType name
    private HashMap<String, MIFFileHolder> mifFileHolders = new HashMap<String, MIFFileHolder> ();

    /**
     * <p>
     * Builds a new MIFDataStore given a mif file or directory path.
     * </p>
     * 
     * <p>
     * Each feature type is represented by a MIFFile object
     * </p>
     *
     * @param path location (directory) of the mif files to read, or full path
     *        of a single mif file. If a directory is given, the headers of
     *        all the mif files in it are read.
     * @param params The MIFFile parameters map, see MIFFile for a full
     *        description.
     *
     * @throws IOException Path does not exists, or error accessing files
     *
     * @see MIFFile#MIFFile(String, Map)
     */
    public MIFDataStore(final String path, final HashMap<Param,Object> params) throws IOException {
        super(true); // Is writable

        this.params = (params != null) ? params : new HashMap<Param,Object>();

        filePath = new File(String.valueOf(path));

        if (filePath.isDirectory()) {
            scanFiles(filePath);
        } else {
            // Try to access a single .mif file - might have been specified with no extension
            registerMIF(filePath.getAbsolutePath());
        }
    }

    /**
     * <p>
     * Looks for all the .mif files in the given Path
     * </p>
     *
     * @param filePath
     *
     * @return the number of mif files found
     *
     * @throws IOException
     */
    private int scanFiles(File filePath) throws IOException {
        if (!filePath.isDirectory()) {
            return 0;
        }

        File[] files = filePath.listFiles();

        int found = 0;

        for (int i = 0; i < files.length; i++) {
            String fName = files[i].getName();

            if ((fName.length() > 4)
                    && (fName.toLowerCase().indexOf(".mif") == (fName.length()
                    - 4))) {
                fName = fName.substring(0, fName.length() - 4);

                if (mifFileHolders.get(fName) == null) {
                    registerMIF(files[i].getAbsolutePath());
                    found++;
                }
            }
        }

        return found;
    }

    /**
     * <p>
     * Given a FeatureType, creates the corresponding MIFFile object in the
     * current directory
     * </p>
     * .
     *
     * @param featureType The FeatureType
     *
     * @throws IOException if init path is not a directory or a MIFFile object
     *         cannot be created
     */
    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        if (!filePath.isDirectory()) {
            throw new IOException(
                "Can't create schema on a MIF DataStore instantiated from a single MIF file");
        }

        try {
            File newFile = new File(filePath, featureType.getTypeName()
                    + ".mif");
            MIFFile mf = new MIFFile(newFile.getAbsolutePath(), featureType, params);
            MIFFileHolder mfh = new MIFFileHolder(mf);
            mifFileHolders.put(mf.getSchema().getTypeName(), mfh);
        } catch (Exception e) {
            throw new IOException("Unable to create MIFFile object: "
                + e.getMessage());
        }
    }

    /**
     * <p>
     * Returns the list of type names (mif files)
     * </p>
     *
     * @return The list of type names
     *
     * @throws IOException Couldn't scan path for files
     */
    @Override
	public String[] getTypeNames() throws IOException {
        scanFiles(filePath); // re-scans path just in case some file was added

        String[] names = new String[mifFileHolders.size()];
        int index = 0;
        
        Iterator<String> i = mifFileHolders.keySet().iterator();
        while (i.hasNext()){
            names[index++] = (String) i.next();
        }
        return names;
    }

    /**
     * <p>
     * Returns the schema given a type name
     * </p>
     *
     * @param typeName
     *
     *
     * @throws IOException
     */
    @Override
	public SimpleFeatureType getSchema(String typeName) throws IOException {
        return getMIFFile(typeName).getSchema();
    }

    /**
     * Gets a  FeatureReader<SimpleFeatureType, SimpleFeature> from a MIFFile object
     *
     * @param typeName name of the FeatureType
     *
     * @return The FeatureReader
     *
     * @throws IOException
     */
    @Override
	protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName)
        throws IOException {
        return getMIFFile(typeName).getFeatureReader();
    }
    
    @Override
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName, Query query)
    throws IOException
    {
      return getMIFFile(typeName).getFeatureReader( query);
    }
    

    /**
     * Gets a FeatureWriter from a MIFFile object
     *
     * @param typeName
     *
     *
     * @throws IOException
     */
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName)
        throws IOException {
        return getMIFFile(typeName).getFeatureWriter();
    }

    /**
     * <p>
     * Loads a MIF file header and create the corresponding schema.
     * </p>
     *
     * @param path path of single .MIF file.
     *
     * @throws IOException
     */
    private void registerMIF(String path) throws IOException {
        MIFFile mf = new MIFFile(path, params);
        MIFFileHolder mfh = new MIFFileHolder(mf);
        SimpleFeatureType ft = mf.getSchema();
        mifFileHolders.put(ft.getTypeName(), mfh);
    }

    @Override
    protected ReferencedEnvelope getBounds(Query query) throws IOException {
    	
        FeatureSource<SimpleFeatureType,SimpleFeature> fs = getFeatureSource(query.getTypeName());
        FeatureIterator<SimpleFeature> iter = fs.getFeatures(query).features();
        
        ReferencedEnvelope bounds = null;
        try {
            while(iter.hasNext()) {
                SimpleFeature feature = iter.next();
                
				BoundingBox feautreBounds = feature.getBounds();
				if(bounds == null){
					CoordinateReferenceSystem crs = feature.getType().getCoordinateReferenceSystem();
					bounds = new ReferencedEnvelope(crs);
				} else {
					bounds.expandToInclude(new ReferencedEnvelope(feautreBounds));
				}
				
            }
        } finally {
            iter.close();
        }
        return bounds;
    }

    /**
     * <p>
     * Returns a MIFFile object given its type name.
     * </p>
     *
     * @param typeName
     *
     */
    private MIFFile getMIFFile(String typeName) {
        MIFFileHolder mifHolder = (MIFFileHolder) mifFileHolders.get(typeName);

        if (mifHolder != null) {
            return mifHolder.mifFile;
        }

        try {
            if (scanFiles(filePath) == 0) {
                return null; // no more file read 
            }
        } catch (IOException e) {
        }

        mifHolder = (MIFFileHolder) mifFileHolders.get(typeName);

        if (mifHolder != null) {
            return mifHolder.mifFile;
        }

        return null;
    }

    // Utility class for holding MIFFile objects
    private class MIFFileHolder {
        private MIFFile mifFile = null;

        //private boolean modified = false;
        //private boolean inSync = true;
        private MIFFileHolder(MIFFile mifFile) {
            this.mifFile = mifFile;
        }
    }
}
