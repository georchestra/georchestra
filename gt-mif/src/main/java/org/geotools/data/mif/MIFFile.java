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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.logging.Logger;

import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.PrjFileReader;
import org.geotools.data.Query;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.AttributeTypes;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.ParseException;


/**
 * <p>
 * MIFFile class allows sequential reading and writing of Features in MapInfo
 * MIF/MID text file format with a  FeatureReader<SimpleFeatureType, SimpleFeature> and FeatureWriter.
 * </p>
 * 
 * <p>
 * This class has been developed starting from MapInfoDataSource.
 * </p>
 * 
 * <p>
 * Open issues:
 * </p>
 * 
 * <ul>
 * <li>
 * CoordSys clause parsing is still not supported
 * </li>
 * </ul>
 * 
 *
 * @author Luca S. Percich, AMA-MI
 * @author Paolo Rizzi, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/data/mif/MIFFile.java $
 * @version $Id: MIFFile.java 30921 2008-07-05 07:51:23Z jgarnett $
 */
public class MIFFile {
    private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(MIFFile.class.getName());

    // Geometry type identifier constants 
    private static final String TYPE_NONE = "none";
    private static final String TYPE_POINT = "point";
    private static final String TYPE_LINE = "line";
    private static final String TYPE_PLINE = "pline";
    private static final String TYPE_REGION = "region";
    private static final String TYPE_TEXT = "text";

    // The following object types are still not supported
    private static final String TYPE_ARC = "arc";
    private static final String TYPE_RECT = "rect"; // could be converted to polygon
    private static final String TYPE_ROUNDRECT = "roundrect";
    private static final String TYPE_ELLIPSE = "ellipse";

    // New types introduced after version 6.0, still not supported
    private static final String TYPE_MULTIPOINT = "multipoint";
    private static final String TYPE_COLLECTION = "collection";

    // String Style Constants  
    private static final String CLAUSE_SYMBOL = "symbol";
    private static final String CLAUSE_PEN = "pen";
    private static final String CLAUSE_SMOOTH = "smooth";
    private static final String CLAUSE_CENTER = "center";
    private static final String CLAUSE_BRUSH = "brush";
    private static final String CLAUSE_FONT = "font";
    private static final String CLAUSE_ANGLE = "angle";
    private static final String CLAUSE_JUSTIFY = "justify";
    private static final String CLAUSE_SPACING = "spacing";
    private static final String CLAUSE_RIGHT = "right";
    private static final String CLAUSE_LABEL = "label";

    // Header parse Constants (& parameter names) 
    private static final String CLAUSE_COLUMNS = "columns";
    public static final int MAX_STRING_LEN = 255; // Max length for MapInfo Char() fields

    // Some (by now useless) default values
    private static final String DEFAULT_PEN = "Pen (1,2,0)";
    private static final String DEFAULT_BRUSH = "Brush (2,16777215,16777215)";
    private static final String DEFAULT_SYMBOL = "Symbol (34,0,12)";

    // Header information
    private HashMap<String, Object> header = new HashMap<String, Object>();

    // File IO Variables
    private File mifFile = null;

    // File IO Variables
    private File midFile = null;

    // File IO Variables
    private File mifFileOut = null;

    // File IO Variables
    private File midFileOut = null;

    private File prjFile;
	private MIFProjReader prjReader;

    private Object[] featureDefaults = null;
    private char chDelimiter = '\t'; // TAB is the default delimiter if not specified in header

    // Schema variables
    private SimpleFeatureType featureType = null;
    private int numAttribs = 0;
    private int geomFieldIndex = -1;
    private URI namespace = null;

    // Parameters for coordinate transformation during file i/o
    private boolean useTransform = false;
    private float multX = 1;
    private float multY = 1;
    private float sumX = 0;
    private float sumY = 0;

    // Options & parameters
    private GeometryFactory geomFactory = null;
    private Integer SRID = new Integer(0);
    private CoordinateReferenceSystem crs;
    private String fieldNameCase;
    private String geometryName;
    private String geometryClass;
    private boolean toGeometryCollection = false;

    /**
     * <p>
     * This constructor opens an existing MIF/MID file, and creates the
     * corresponding schema from the file header
     * </p>
     * 
     * <p>
     * Allowed parameters in params Map:
     * </p>
     * 
     * <ul>
     * <li>
     * "namespace" = URI of the namespace prefix for FeatureTypes
     * </li>
     * <li>
     * PARAM_GEOMFACTORY = GeometryFactory object to be used for creating
     * geometries; alternatively, use PARAM_SRID;
     * </li>
     * <li>
     * PARAM_SRID = SRID to be used for creating geometries;
     * </li>
     * <li>
     * PARAM_FIELDCASE = field names tranformation: "upper" to uppercase |
     * "lower" to lowercase | "" none;
     * </li>
     * <li>
     * PARAM_GEOMNAME = &lt;String&gt, name of the geometry field (defaults to
     * "the_geom");
     * </li>
     * <li>
     * PARAM_GEOMTYPE = geometry type handling: "untyped" uses Geometry class |
     * "typed" force geometry to the type of the first valid geometry found in
     * file | "multi" like typed, but forces LineString to MultilineString and
     * Polygon to MultiPolygon; | "Point" | "LineString" | "MultiLineString" |
     * "Polygon" | "MultiPolygon" | "Text" forces Geometry to Point and
     * creates a MIF_TEXT String field in the schema
     * </li>
     * </ul>
     * 
     * <p>
     * Header clauses values can also be set in the params Map, but they might
     * be overridden by values read from MIF header.
     * </p>
     * 
     * <p>
     * Basic usage:
     * </p>
     * <pre><code>
     *   HashMap params = new HashMap();
     *   // params.put(MIFFile.PARAM_GEOMFACTORY, new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING_SINGLE), SRID));
     *   params.put(MIFFile.PARAM_SRID, new Integer(SRID));
     *   params.put(MIFFile.PARAM_FIELDCASE, "upper");
     *   params.put(MIFFile.PARAM_GEOMNAME, "GEOM");
     *   params.put(MIFFile.PARAM_GEOMTYPE, "typed");
     *   MIFFile mf = new MIFFile("c:/some_path/file.mif",params);
     *   FeatureType ft = mf.getSchema();
     *    FeatureReader<SimpleFeatureType, SimpleFeature> fr = mf.getFeatureReader();	
     *   while (fr.hasNext()) {
     *   	Feature in = fr.next();
     *   	doSomethingWithFeature(in);
     *   }
     *   fr.close(); // closes file resources
     * </code></pre>
     *
     * @param path Full pathName of the mif file, can be specified without the
     *        .mif extension
     * @param params Parameters map
     *
     * @throws IOException If the specified mif file could not be opened
     */
    public MIFFile(final String path, final Map<Param, Object> params) throws IOException {
        // TODO use url instead of String
        super();

        parseParams(params);

        initFiles(path, true);

        MIFFileTokenizer mifTokenizer = new MIFFileTokenizer(new BufferedReader(
                    new FileReader(mifFile)));

        
        if(prjFile!=null && SRID != null) {

            if(prjFile.exists() ){
                FileInputStream in = new FileInputStream(prjFile);
                try {
                    PrjFileReader reader = new PrjFileReader(in.getChannel());
                    crs = reader.getCoordinateReferenceSystem();
                    SRID = CRS.lookupEpsgCode(crs,false);
                } catch (FactoryException e) {
                    throw new RuntimeException(e);
                } finally {
                    in.close();
                }
            } else {
            	
                // retrieves the crs from mif file
        		FileInputStream in = new FileInputStream(this.mifFile);
        		try {
        			SRID = retrieveSRID(in.getChannel());
        			crs = CRS.decode("EPSG:" + SRID);
        			
        		} catch (Exception e) {
        			e.printStackTrace();
        			throw new RuntimeException(e);
        		} finally {
        			in.close();
        		}
            }
        } 

        try {
            readMifHeader(false, mifTokenizer);
        } catch (Exception e) {
            throw new IOException("Can't read MIF header: " + e.toString());
        } finally {
            try {
                mifTokenizer.close();
            } catch (Exception e) {
            }
        }
    }

    private Integer retrieveSRID(FileChannel channel) throws IOException {
		// create the ByteBuffer
		FileChannel fc = (FileChannel) channel;
		ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		buffer.position((int) fc.position());

		// The entire file is in little endian
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		CharBuffer charBuffer = CharBuffer.allocate(8 * 1024);
		Charset chars = Charset.forName("ISO-8859-1");
		CharsetDecoder decoder = chars.newDecoder();
		
		decoder.decode(buffer, charBuffer, true);
		buffer.limit(buffer.capacity());
		charBuffer.flip();
		
		String str = charBuffer.toString();
		String crsKey = "CoordSys Earth Projection"; // TODO Mauricio: it is necessary take into account other syntax (have a look at mif specification)
		int i = str.indexOf(crsKey);
		if(i != -1){
			String mifCoordSys = str.substring(i + crsKey.length() + 1, str.indexOf("Column") -1);
			
			int srid = this.prjReader.toCRS(mifCoordSys); 
			
			
			return srid;
		}
		
		return -1;
    }

	/**
     * <p>
     * This constructor creates a a new MIF/MID file given schema and path.  If
     * a .mif/.mid file pair already exists, it will be overwritten.
     * </p>
     * 
     * <p>
     * Basic usage:
     * </p>
     * <pre><code>
     *   HashMap params = new HashMap();
     *   params.put(MIFFile.MIFDataStore.HCLAUSE_COORDSYS, "Nonearth \"m\"");
     * 
     *   MIFFile mf = new MIFFile("c:/some_path/", ft, params);
     * 
     * 
     *   FeatureWriter fw = mf.getFeatureWriter();
     * 
     *   while(...) {
     * 	    Feature f = fw.next();
     * 			f.setAttribute(...,...);
     * 			fw.write();
     * 	 }
     * 
     *   fw.close();
     * </code></pre>
     *
     * @param path Full path & file name of the MIF file to create, can be
     *        specified without the .mif extension
     * @param featureType
     * @param params Parameter map
     *
     * @throws IOException Couldn't open the specified mif file for writing
     *         header
     * @throws SchemaException Error setting the given FeatureType as the MIF
     *         schema
     */
    public MIFFile(String path, SimpleFeatureType featureType, HashMap<Param, Object> params)
        throws IOException, SchemaException {
        super();

        parseParams(params);

        setSchema(featureType);
        initFiles(path, false);

        PrintStream outMif = new PrintStream(new FileOutputStream(mifFile, false));
        PrintStream outMid = new PrintStream(new FileOutputStream(midFile, false));

        // writes out header
        outMif.println(exportHeader());

        outMif.close();
        outMid.close();
    }

    /**
     * Parses the parameters map into fields:
     *
     * @param params
     *
     * @throws IOException Error getting parameters from the specified map
     */
    private void parseParams(Map<Param, Object> params) throws IOException {
        if (params == null) {
            params = new HashMap<Param, Object>();
        }

        // Sets defaults for header
        setHeaderClause(MIFDataStore.HCLAUSE_VERSION,
            (String) getParam(MIFDataStore.HCLAUSE_VERSION, "300", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_CHARSET,
            (String) getParam(MIFDataStore.HCLAUSE_CHARSET, "WindowsLatin1",
                false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_DELIMITER,
            (String) getParam(MIFDataStore.HCLAUSE_DELIMITER,
                String.valueOf(chDelimiter), false, params));
        chDelimiter = getHeaderClause(MIFDataStore.HCLAUSE_DELIMITER).charAt(0);

        setHeaderClause(MIFDataStore.HCLAUSE_UNIQUE,
            (String) getParam(MIFDataStore.HCLAUSE_UNIQUE, "", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_INDEX,
            (String) getParam(MIFDataStore.HCLAUSE_INDEX, "", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_COORDSYS,
            (String) getParam(MIFDataStore.HCLAUSE_COORDSYS, "", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_TRANSFORM,
            (String) getParam(MIFDataStore.HCLAUSE_TRANSFORM, "", false, params));

        SRID = (Integer) getParam(MIFDataStore.PARAM_SRID, new Integer(0),
                false, params);
        if(SRID!=null && SRID != 0) {
            try {
                crs = CRS.decode("epsg:"+SRID);
            } catch (FactoryException e) {
                throw new RuntimeException(e);
            }
        }

        geomFactory = (GeometryFactory) getParam(MIFDataStore.PARAM_GEOMFACTORY,
                null, false, params);

        if (geomFactory == null) {
            geomFactory = new GeometryFactory(new PrecisionModel(
                        PrecisionModel.FLOATING), SRID.intValue());
        }

        geometryName = (String) getParam(MIFDataStore.PARAM_GEOMNAME,
                "the_geom", false, params);
        fieldNameCase = ((String) getParam(MIFDataStore.PARAM_FIELDCASE, "",
                false, params)).toLowerCase();

        geometryClass = ((String) getParam(MIFDataStore.PARAM_GEOMTYPE,
                "untyped", false, params)).toLowerCase();

        namespace = (URI) getParam("namespace", FeatureTypes.DEFAULT_NAMESPACE, false,
                params);
    }

    /**
     * Returns a parameter value from the parameters map
     *
     * @param name
     * @param defa
     * @param required
     * @param params
     *
     *
     * @throws IOException if required parameter is missing
     */
    private Object getParam(final String name, final Object defa, final boolean required, final Map<Param, Object> params) throws IOException {
        Object result;

        Param param = new Param(name);
        try {
            result = params.get(param);
        } catch (Exception e) {
            result = null;
        }

        if (result == null) {
            if (required) {
                throw new IOException("MIFFile: parameter " + name
                    + " is required");
            }

            result = defa;
        }

        return result;
    }

    /**
     * <p>
     * Sets the value for a Header Clause. Possible values are:
     * </p>
     * 
     * <ul>
     * <li>
     * MIFDataStore.HCLAUSE_VERSION = Version number ("310")
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_CHARSET = Charset name ("WindowsLatin1")
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_UNIQUE = Comma-separated list of field indexes
     * (1..numFields) corresponding to unique values (i.e. street names for
     * street segments)
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_INDEX = Comma-separated list of field indexes
     * (1..numFields) indicating which fields have to be indexed in MapInfo
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_COORDSYS = MapInfo CoordSys clause
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_TRANSFORM = Comma-separated list of four
     * transformation parameters ("1000, 1000, 0, 0")
     * </li>
     * </ul>
     * 
     *
     * @param clause Name of the Header Clause
     * @param value Value for the Header Clause
     *
     * @throws IOException Bad delimiter was specified
     */
    private void setHeaderClause(String clause, String value)
        throws IOException {
        if (value == null) {
            value = "";
        }

        if (clause.equals(MIFDataStore.HCLAUSE_DELIMITER)
                && (value.equals("") || value.equals("\""))) {
            throw new IOException("Bad delimiter specified");
        }

        header.put(clause, value);
    }

    /**
     * Gets the value for an header clause
     *
     * @param clause
     *
     */
    public String getHeaderClause(final String clause) {
        try {
            //return (String) getParam(clause, "", false, header);
            Object value = header.get(clause);
            if (value == null) {
                value = "";
            }

            return value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * <p>
     * Opens the MIF file for input and returns a  FeatureReader<SimpleFeatureType, SimpleFeature> for accessing
     * the features.
     * </p>
     * 
     * <p>
     * TODO Concurrent file access is still not handled. MUST LOCK FILE and
     * return an error if another  FeatureReader<SimpleFeatureType, SimpleFeature> is open - Handle concurrent
     * access with synchronized(mif) / or Filesystem locking is enough?
     * </p>
     *
     * @return A  FeatureReader<SimpleFeatureType, SimpleFeature> for reading features from MIF/MID file
     *
     * @throws IOException
     */
    public  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader() throws IOException {
    	
    	return createReader(null);
    }


	public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query) throws IOException {
		
		return createReader(query);
	}

	private Reader createReader(Query query) throws IOException{
		
        MIFFileTokenizer mifTokenizer = null;
        MIFFileTokenizer midTokenizer = null;

        // if exists outMIF throw new IOException("File is being accessed in write mode");
        try {
            mifTokenizer = new MIFFileTokenizer(new BufferedReader(
                        new FileReader(mifFile)));
            midTokenizer = new MIFFileTokenizer(new BufferedReader(
                        new FileReader(midFile)));
            readMifHeader(true, mifTokenizer); // skips header

            return new Reader(mifTokenizer, midTokenizer);
            
        } catch (Exception e) {
            if (mifTokenizer != null) {
                mifTokenizer.close();
            }

            if (midTokenizer != null) {
                midTokenizer.close();
            }

            throw new IOException("Error initializing reader: " + e.toString());
        }
    	
    }
    

    /**
     * Returns a FeatureWriter for writing features to the MIF/MID file.
     *
     * @return A featureWriter for this file
     *
     * @throws IOException
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter() throws IOException {
        return getFeatureWriter(false);
    }

    /**
     * <p>
     * Private FeatureWriter in append mode, could be called by
     * DataStore.getFeatureWriterAppend(); not implemented yet
     * </p>
     *
     * @param append
     *
     *
     * @throws IOException
     */
    private FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(boolean append)
        throws IOException {
        if (append) {
            // copy inMif to OutMIf
        } else {
            // WriteHeader
        }

        PrintStream outMif = new PrintStream(new FileOutputStream(mifFileOut,
                    append));
        PrintStream outMid = new PrintStream(new FileOutputStream(midFileOut,
                    append));

        return new Writer(outMif, outMid, append);
    }

    /**
     * Creates the MIF file header
     *
     * @return the Header as a String
     *
     * @throws SchemaException A required header clause is missing.
     */
    private String exportHeader() throws SchemaException {
        // Header tags passed in parameters are overridden by the tags read from mif file 
        String header = exportClause(MIFDataStore.HCLAUSE_VERSION, true, false)
            + exportClause(MIFDataStore.HCLAUSE_CHARSET, true, true) // TODO Charset clause support should imply character conversion????
            + exportClause(MIFDataStore.HCLAUSE_DELIMITER, true, true)
            + exportClause(MIFDataStore.HCLAUSE_UNIQUE, false, false)
            + exportClause(MIFDataStore.HCLAUSE_INDEX, false, false)
            + exportClause(MIFDataStore.HCLAUSE_COORDSYS, false, false)
            + exportClause(MIFDataStore.HCLAUSE_TRANSFORM, false, false);

        header += ("Columns " + (numAttribs - 1) + "\n");

        for (int i = 1; i < numAttribs; i++) {
            AttributeDescriptor at = featureType.getDescriptor(i);
            header += ("  " + at.getLocalName() + " " + getMapInfoAttrType(at)
            + "\n");
        }

        header += "Data\n";

        return header;
    }

    private String exportClause(String clause, boolean required, boolean quote)
        throws SchemaException {
        String result = getHeaderClause(clause);

        if (!result.equals("")) {
            if (quote) {
                result = MIFStringTokenizer.strQuote(result);
            }

            return clause + " " + result + "\n";
        }

        if (required) {
            throw new SchemaException("Header clause " + clause
                + " is required.");
        }

        return "";
    }

    /**
     * Maps an AttributeType to a MapInfo field type
     *
     * @param at Attribute Type
     *
     * @return the String description of the MapInfo Type
     */
    private String getMapInfoAttrType(AttributeDescriptor at) {
        if (at.getType().getBinding() == String.class) {
        	
            int l = AttributeTypes.getFieldLength(at, MAX_STRING_LEN);

            if (l <= 0) {
                l = MAX_STRING_LEN;
            }

            return "Char(" + l + ")";
        } else if (at.getType().getBinding() == Integer.class) {
            return "Integer";
        } else if ((at.getType().getBinding() == Double.class)
                || (at.getType().getBinding() == Float.class)) {
            return "Float";
        } else if (at.getType().getBinding() == Boolean.class) {
            return "Logical";
        } else if (at.getType().getBinding() == Date.class) {
            return "Date";
        } else {
            return "Char(" + MAX_STRING_LEN + ")"; // TODO Should it raise an exception here (UnsupportedSchema) ?
        }
    }

    /**
     * Sets the path name of the MIF and MID files
     *
     * @param path The full path of the .mif file, with or without extension
     * @param mustExist True if opening file for reading
     * @throws IOException 
     */
    private void initFiles(String path, boolean mustExist)
        throws IOException {
        File file = new File(path);

        if (file.isDirectory()) {
            throw new FileNotFoundException(path + " is a directory");
        }

        String fName = getMifName(file.getName());
        file = file.getParentFile();

        mifFile = getFileHandler(file, fName, ".mif", mustExist);
        midFile = getFileHandler(file, fName, ".mid", mustExist);
        prjFile = getFileHandler(file, fName, ".prj", false); // FIXME it is necessary to check whether this format actually requires a prj file

        mifFileOut = getFileHandler(file, fName, ".mif.out", false);
        midFileOut = getFileHandler(file, fName, ".mid.out", false);
        
		this.prjReader = new MIFProjReader();

    }

    /**
     * Returns the name of a .mif file without extension
     *
     * @param fName The file name, possibly with .mif extension
     *
     * @return The name with no extension
     *
     * @throws FileNotFoundException if extension was other than "mif"
     */
    protected static String getMifName(String fName)
        throws FileNotFoundException {
        int ext = fName.lastIndexOf(".");

        if (ext > 0) {
            String theExt = fName.substring(ext + 1).toLowerCase();

            if (!(theExt.equals("mif"))) {
                throw new FileNotFoundException(
                    "Please specify a .mif file extension.");
            }

            fName = fName.substring(0, ext);
        }

        return fName;
    }

    /**
     * Utility function for initFiles - returns a File given a parent path, the
     * file name without extension and the extension Tests different extension
     * case for case-sensitive filesystems
     *
     * @param path Directory containing the file
     * @param fileName Name of the file with no extension
     * @param ext extension with trailing "."
     * @param mustExist If true, raises an excaption if the file does not exist
     *
     * @return The File object
     *
     * @throws FileNotFoundException
     */
    protected static File getFileHandler(File path, String fileName,
        String ext, boolean mustExist) throws FileNotFoundException {
        File file = new File(path, fileName + ext);

        if (file.exists() || !mustExist) {
            return file;
        }

        file = new File(path, fileName + ext.toUpperCase());

        if (file.exists()) {
            return file;
        }

        throw new FileNotFoundException("Can't find file: " + file.getName());
    }

    /**
     * Reads the header from the given MIF file stream tokenizer
     *
     * @param skipRead Skip the header, just to get to the data section
     * @param mif
     *
     * @throws IOException
     * @throws SchemaException Error reading header information
     */
    private void readMifHeader(boolean skipRead, MIFFileTokenizer mif)
        throws IOException, SchemaException {
        try {
            String tok;
            boolean hasMifText = false;
            AttributeDescriptor[] columns = null;

            while (mif.readLine()) {
                tok = mif.getToken().toLowerCase();

                // "data" might be a field name, in this case the type name would follow on the same line 
                if (tok.equals("data") && mif.getLine().equals("")) {
                    break;
                }

                if (skipRead) {
                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_VERSION)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_VERSION, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_CHARSET)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_CHARSET,
                        mif.getToken(' ', false, true));

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_DELIMITER)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_DELIMITER,
                        mif.getToken(' ', false, true));
                    chDelimiter = getHeaderClause(MIFDataStore.HCLAUSE_DELIMITER)
                                      .charAt(0);

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_UNIQUE)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_UNIQUE, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_COORDSYS)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_COORDSYS, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_INDEX)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_INDEX, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_TRANSFORM)) {
                    useTransform = true;
                    multX = Float.parseFloat("0" + mif.getToken(','));
                    multY = Float.parseFloat("0" + mif.getToken(','));
                    sumX = Float.parseFloat("0" + mif.getToken(','));
                    sumY = Float.parseFloat("0" + mif.getToken(','));

                    if (multX == 0) {
                        multX = 1;
                    }

                    if (multY == 0) {
                        multY = 1;
                    }

                    continue;
                }

                if (tok.equals(CLAUSE_COLUMNS)) {
                    int cols;

                    try {
                        cols = Integer.parseInt(mif.getLine());
                    } catch (NumberFormatException nfexp) {
                        throw new IOException("bad number of colums: "
                            + mif.getLine());
                    }

                    // Columns <n> does not take into account the geometry column, so we increment
                    columns = new AttributeDescriptor[++cols];

                    String name;
                    String type;
                    Object defa;
                    Class typeClass;
                    int size;

                    for (int i = 1; i < cols; i++) {
                        if (!mif.readLine()) {
                            throw new IOException("Expected column definition");
                        }

                        name = mif.getToken();

                        if (fieldNameCase.equalsIgnoreCase("upper")) {
                            name = name.toUpperCase();
                        } else if (fieldNameCase.equalsIgnoreCase("lower")) {
                            name = name.toLowerCase();
                        }

                        type = mif.getToken('(').toLowerCase();
                        defa = null;
                        typeClass = null;
                        size = 4;

                        if (type.equals("float") || type.equals("decimal")) {
                            typeClass = Double.class;
                            size = 8;
                            defa = new Double(0.0);

                            // TODO: check precision?
                        } else if (type.startsWith("char")) {
                            typeClass = String.class;
                            size = Integer.parseInt(mif.getToken(')'));
                            defa = "";
                        } else if (type.equals("integer")
                                || type.equals("smallint")) {
                            typeClass = Integer.class;
                            defa = new Integer(0);

                            // TODO: apply a restriction for Smallint (value between -32768 and +32767)
                        } else if (type.equals("logical")) {
                            typeClass = Boolean.class;
                            size = 2; // ???
                            defa = new Boolean(false);
                        } else if (type.equals("date")) {
                            typeClass = Date.class; // MapInfo format: yyyymmdd
                            size = 4; // ???
                            defa = null; // Dates are "nillable" (like Strings can be empty)
                        } else {
                            LOGGER.fine("unknown type in mif/mid read " + type
                                + " storing as String");
                            typeClass = String.class;
                            size = 254;
                            defa = "";
                        }

                        
                        AttributeTypeBuilder b = new AttributeTypeBuilder();
                        b.setName(name);
                        b.setBinding(typeClass);
                        b.setNillable(defa == null);
                        b.setDefaultValue(defa);
                        
                        
                        // Apart from Geometry, MapInfo table fields cannot be null, so Nillable is always false and default value must always be provided!
                        columns[i] = b.buildDescriptor(name);
                    }
                }
            }

            // Builds schema if not in skip mode...
            if (!skipRead) {
                Class geomType = null;

                String geomClass = geometryClass.toLowerCase();

                if (geomClass.equals("untyped")) {
                    geomType = Geometry.class;
                } else if (geomClass.equals("typed")) {
                    toGeometryCollection = false;
                } else if (geomClass.equals("multi")) {
                    toGeometryCollection = true;
                } else if (geomClass.equals("point")) {
                    geomType = Point.class;
                } else if (geomClass.equals("text")) {
                    geomType = Point.class;
                    hasMifText = true;
                } else if (geomClass.equals("linestring")) {
                    geomType = LineString.class;
                } else if (geomClass.equals("multilinestring")) {
                    geomType = MultiLineString.class;
                    toGeometryCollection = true;
                } else if (geomClass.equals("polygon")) {
                    geomType = Polygon.class;
                } else if (geomClass.equals("multipolygon")) {
                    geomType = MultiPolygon.class;
                    toGeometryCollection = true;
                } else {
                    throw new SchemaException("Bad geometry type option: "
                        + geomClass);
                }

                // Determine geometry type from the first non-null geometry read from mif file
                if (geomType == null) {
                    Reader reader = new Reader(mif, null);
                    Geometry geom = null;

                    while (!reader.mifEOF) {
                        geom = reader.readGeometry();
                        hasMifText = (!reader.mifText.equals(""));

                        if (geom != null) {
                            geomType = geom.getClass();

                            if (toGeometryCollection) {
                                if (geomType.isAssignableFrom(Polygon.class)) {
                                    geomType = MultiPolygon.class;
                                } else if (geomType.isAssignableFrom(
                                            LineString.class)) {
                                    geomType = MultiLineString.class;
                                }
                            }

                            break;
                        }
                    }

                    reader.close();
                    reader = null;
                }

                if (geomType == null) {
                    throw new SchemaException(
                        "Unable to determine geometry type from mif file");
                }

                AttributeTypeBuilder b = new AttributeTypeBuilder();
                b.setName(geometryName);
                b.setBinding(geomType);
                b.setNillable(true);
                b.setCRS(crs);
                
                
                columns[0] = b.buildDescriptor(geometryName);

                try {
                    String typeName = mifFile.getName();
                    typeName = typeName.substring(0, typeName.indexOf("."));

                    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                    builder.setName(typeName);

                    builder.setNamespaceURI(namespace);

                    for (int i = 0; i < columns.length; i++)
                        builder.add(columns[i]);

                    if (hasMifText) {
                    	b = new AttributeTypeBuilder();
                    	b.setName("MIF_TEXT");
                    	b.setBinding(String.class);
                    	b.setNillable(true);
                    	
                        builder.add(b.buildDescriptor("MIF_TEXT"));
                    }

                    builder.setCRS(crs);
                    SimpleFeatureType type = builder.buildFeatureType();
					setSchema(type);
                } catch (SchemaException schexp) {
                    throw new SchemaException(
                        "Exception creating feature type from MIF header: "
                        + schexp.toString());
                }
            }
        } catch (Exception e) {
            throw new IOException("IOException reading MIF header, line "
                + mif.getLineNumber() + ": " + e.getMessage());
        }
    }

    /**
     * Returns the MIF schema
     *
     * @return the current FeatureType associated with the MIF file
     */
    public SimpleFeatureType getSchema() {
        return featureType;
    }

    /**
     * Sets the schema (FeatureType) and creates value setters and IO object
     * buffer
     *
     * @param ft
     *
     * @throws SchemaException The given FeatureType is not compatible with
     *         MapInfo format
     */
    private void setSchema(SimpleFeatureType ft) throws SchemaException {
        featureType = ft;

        numAttribs = featureType.getAttributeCount();
        geomFieldIndex = -1;

        // Creates the input buffer for reading MID file
        featureDefaults = new Object[numAttribs];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeDescriptor at = featureType.getDescriptor(i);

            Class atc = at.getType().getBinding();

            if (Geometry.class.isAssignableFrom(atc)) {
                if (geomFieldIndex >= 0) {
                    throw new SchemaException(
                        "Feature Types with more than one geometric attribute are not supported.");
                }

                if (i > 0) {
                    throw new SchemaException(
                        "Geometry must be the first attribute in schema.");
                }

                geomFieldIndex = i; // = 0
            }
        }

        MIFValueSetter[] tmp = getValueSetters();

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            if (i != geomFieldIndex) {
                tmp[i].setString("");
                featureDefaults[i] = tmp[i].getValue();
            }
        }
    }

    /**
     * Gets the ValueSetters
     *
     * @return An array of valueSetters to be used for IO operations
     *
     * @throws SchemaException An attribute of an unsupported type was found.
     */
    private MIFValueSetter[] getValueSetters() throws SchemaException {
        MIFValueSetter[] fieldValueSetters = new MIFValueSetter[numAttribs];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeDescriptor at = featureType.getDescriptor(i);
            Class atc = at.getType().getBinding();

            if (i == geomFieldIndex) {
                fieldValueSetters[i] = null;
            } else if (atc == Integer.class) {
                fieldValueSetters[i] = new MIFValueSetter("0") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Integer(strValue);
                            }
                        };
            } else if (atc == Double.class) {
                fieldValueSetters[i] = new MIFValueSetter("0") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Double(strValue);
                            }

                            protected void valueToString() {
                                // TODO use DecimalFormat class!!!
                                super.valueToString();
                            }
                        };
            } else if (atc == Float.class) {
                fieldValueSetters[i] = new MIFValueSetter("0") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Float(strValue);
                            }

                            protected void valueToString() {
                                // TODO use DecimalFormat class!!!
                                super.valueToString();
                            }
                        };
            } else if (atc == Boolean.class) {
                fieldValueSetters[i] = new MIFValueSetter("false") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Boolean("T".equalsIgnoreCase(
                                            strValue) ? "true"
                                                      : ("F".equalsIgnoreCase(
                                            strValue) ? "false" : strValue));
                            }

                            protected void valueToString() {
                                if ((objValue == null)
                                        || (((Boolean) objValue).booleanValue() == false)) {
                                    strValue = "F";
                                } else {
                                    strValue = "T";
                                }
                            }
                        };
            } else if (Date.class.isAssignableFrom( atc ) ) {
                // TODO Check conversion of date values - switch to java.sql.Date
                fieldValueSetters[i] = new MIFValueSetter("") {
                            protected SimpleDateFormat dateFormat = new SimpleDateFormat(
                                    "yyyyMMdd");

                            protected void stringToValue()
                                throws Exception {
                                if ((strValue != null) && !strValue.equals("")) {
                                    objValue = dateFormat.parse(strValue);
                                } else {
                                    objValue = null;
                                }

                                // Date.valueOf(strValue.substring(0, 4) + "-" + strValue.substring(4, 6) + "-" + strValue.substring(6));
                            }

                            protected void valueToString() {
                                if (objValue == null) {
                                    strValue = "";
                                } else {
                                    strValue = dateFormat.format(objValue);

                                    // strValue = ((Date) objValue).getYear() + "" + ((Date) objValue).getMonth() + "" + ((Date) objValue).getDay();
                                }
                            }
                        };
            } else if (atc == String.class) {
                fieldValueSetters[i] = new MIFValueSetter("") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new String(strValue);
                            }

                            // Quotes the string
                            protected void valueToString() {
                                strValue = new String("\""
                                        + objValue.toString().replaceAll("\"",
                                            "\"\"") + "\"");
                            }
                        };
            } else {
                throw new SchemaException("Unsupported attribute type: "
                    + atc.getName());
            }
        }

        return fieldValueSetters;
    }

    /**
     * Utility function for copying or moving files
     *
     * @param in Source file
     * @param out Destination file
     * @param deleteIn If true, source will be deleted upon successfull copy
     *
     * @throws IOException
     */
    protected static void copyFileAndDelete(File in, File out, boolean deleteIn)
        throws IOException {
        try {
            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out)
                .getChannel();
            destinationChannel.transferFrom( sourceChannel, 0, sourceChannel.size() );
            if (deleteIn) {
                in.delete();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * <p>
     * Private  FeatureReader<SimpleFeatureType, SimpleFeature> inner class for reading Features from the MIF file
     * </p>
     */
    private class Reader implements  FeatureReader<SimpleFeatureType, SimpleFeature> {
    	
        private MIFFileTokenizer mif = null;
        private MIFFileTokenizer mid = null;
        private boolean mifEOF = false;
        private String mifText = ""; // caption for text objects 
        private SimpleFeature inputFeature = null;
        private Object[] inputBuffer = null;
        private MIFValueSetter[] fieldValueSetters;
		private Query query = null;

        private Reader(MIFFileTokenizer mifTokenizer, MIFFileTokenizer midTokenizer) throws IOException {
            inputBuffer = new Object[numAttribs];

            mif = mifTokenizer;
            mid = midTokenizer;

            // numAttribs == 0 when Reader is called from within readMifHeader for determining geometry Type
            if (numAttribs > 0) {
                try {
                    fieldValueSetters = getValueSetters();
                } catch (SchemaException e) {
                    throw new IOException(e.getMessage());
                }
                inputFeature = readFeature();
            }
        }

		public boolean hasNext() {
            return (inputFeature != null);
        }

        // Reads the next feature and returns the last one
        public SimpleFeature next() throws NoSuchElementException {
            if (inputFeature == null) {
                throw new NoSuchElementException("Reached the end of MIF file");
            }

            SimpleFeature temp = inputFeature;

            try {
                inputFeature = readFeature();
            } catch (Exception e) {
                throw new NoSuchElementException(
                    "Error retrieving next feature: " + e.toString());
            }

            return temp;
        }

        public SimpleFeatureType getFeatureType() {
            return featureType;
        }

        public void close() {
            try {
                if (mif != null) {
                    mif.close();
                }

                if (mid != null) {
                    mid.close();
                }
            } finally {
                mif = null;
                mid = null;
            }
        }

        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }

        /**
         * Reads a single MIF Object (Point, Line, Region, etc.) as a Feature
         *
         * @return The feature, or null if the end of file was reached
         *
         * @throws IOException
         */
        private SimpleFeature readFeature() throws IOException {
        	
            SimpleFeature feature = null;
            Geometry geom = readGeometry();

            if (mifEOF) {
                return null;
            }

            if (!mid.readLine()) {
                // TODO According to MapInfo spec., MID file is optional... in this case we should return the default values for the feature
                if (geom != null) {
                    throw new IOException("Unexpected end of MID file.");
                }

                return null;
            }

            // Reads data from mid file
            // Assumes that geomFieldIndex == 0
            try {
                String tok = "";
                int col = 0;

                while (!mid.isEmpty()) {
                    tok = mid.getToken(chDelimiter, false, true);

                    if (!fieldValueSetters[++col].setString(tok)) {
                        LOGGER.severe("Bad value:"
                            + fieldValueSetters[col].getError());
                    }

                    inputBuffer[col] = fieldValueSetters[col].getValue();
                }

                if (!mifText.equals("")) {
                    // MIF_TEXT MUST BE the LAST Field for now
                    inputBuffer[++col] = mifText;

                    // a better approach could be using a separate array of value setters for MIF_ fields
                    // (TEXT, ANGLE...)
                }

                if (col != (numAttribs - 1)) {
                    throw new Exception(
                        "Bad number of attributes read on MID row "
                        + mid.getLineNumber() + ": found " + col
                        + ", expecting " + numAttribs);
                }
            } catch (Exception e) {
                throw new IOException("Error reading MID file, line "
                    + mid.getLineNumber() + ": " + e.getMessage());
            }

            // Now add geometry and build the feature
            try {
                inputBuffer[0] = geom;
                feature = SimpleFeatureBuilder.build(featureType, inputBuffer, null);
            } catch (Exception e) {
                throw new IOException("Exception building feature: "
                    + e.getMessage());
            }

            return feature;
        }

        /**
         * Reads one geometric object from the MIF file
         *
         * @return The geometry object
         *
         * @throws IOException Error retrieving geometry from input MIF stream
         */
        private Geometry readGeometry() throws IOException {
            mifText = "";

            if (!mif.readLine()) {
                mifEOF = true;

                return null;
            }

            Geometry geom = null;

            try {
                // First of all reads geometry
                String objType = mif.getToken().toLowerCase();

                if (objType.equals(TYPE_NONE)) {
                    geom = null;
                } else if (objType.equals(TYPE_POINT)) {
                    geom = readPointObject();
                } else if (objType.equals(TYPE_LINE)) {
                    geom = readLineObject();
                } else if (objType.equals(TYPE_PLINE)) {
                    geom = readPLineObject();
                } else if (objType.equals(TYPE_REGION)) {
                    geom = readRegionObject();
                } else if (objType.equals(TYPE_TEXT)) {
                    geom = readTextObject();
                } else if (objType.equals(CLAUSE_PEN)
                        || objType.equals(CLAUSE_SYMBOL)
                        || objType.equals(CLAUSE_SMOOTH)
                        || objType.equals(CLAUSE_CENTER)
                        || objType.equals(CLAUSE_BRUSH)
                        || objType.equals(CLAUSE_FONT)
                        || objType.equals(CLAUSE_ANGLE)
                        || objType.equals(CLAUSE_JUSTIFY)
                        || objType.equals(CLAUSE_SPACING)
                        || objType.equals(CLAUSE_LABEL)) {
                    // Symply ignores styling clauses, so let's read the next lines
                    geom = readGeometry();
                } else {
                    // TODO add MultiPoint & Collection!!!
                    throw new IOException(
                        "Unknown or unsupported object in mif file:" + objType);
                }
            } catch (Exception e) {
                throw new IOException("File " + mifFile.getName() + ", line "
                    + mif.getLineNumber() + ": " + e.getMessage());
            }

            return geom;
        }

        /**
         * Reads Multi-Line (PLine) information from the MIF stream
         *
         * @return The (MULTI)LINESTRING object read
         *
         * @throws IOException Error retrieving geometry from input MIF stream
         */
        private Geometry readPLineObject() throws IOException {
            try {
                String tmp = mif.getToken(' ', true);
                int numsections = 1;
                int numpoints = 0;

                if (tmp.equalsIgnoreCase("MULTIPLE")) {
                    numsections = Integer.parseInt(mif.getToken(' ', true)); //read the number of sections
                    numpoints = Integer.parseInt(mif.getToken(' ', true)); //read the number of points
                } else {
                    // already got the number of points, simply parse it
                    numpoints = Integer.parseInt(tmp);
                }

                LineString[] lineStrings = new LineString[numsections];

                // Read each polyline
                for (int i = 0; i < lineStrings.length; i++) {
                    if (numpoints == 0) {
                        numpoints = Integer.parseInt(mif.getToken(' ', true));
                    }

                    Coordinate[] coords = new Coordinate[numpoints];

                    // Read each point
                    for (int p = 0; p < coords.length; p++) {
                        coords[p] = readMIFCoordinate();
                    }

                    numpoints = 0;

                    lineStrings[i] = geomFactory.createLineString(coords);
                }

                if ((numsections == 1) && !toGeometryCollection) {
                    return (Geometry) lineStrings[0];
                }

                return (Geometry) geomFactory.createMultiLineString(lineStrings);
            } catch (Exception e) {
                throw new IOException(
                    "Exception reading PLine data from MIF file : "
                    + e.toString());
            }
        }

        /**
         * Reads Region (Polygon) information from the MIF stream
         *
         * @return The (MULTI)POLYGON object
         *
         * @throws IOException Error retrieving geometry from input MIF stream
         */
        private Geometry readRegionObject() throws IOException {
            try {
                int numpolygons = Integer.parseInt(mif.getToken(' ', true));

                Vector polygons = new Vector();

                LinearRing tmpRing = null;
                Polygon shell = null;
                LinearRing shellRing = null;
                Vector holes = null;
                boolean savePolygon;

                // Read all linearrings;
                for (int i = 0; i < numpolygons; i++) {
                    // Read coordinates & create ring
                    int numpoints = Integer.parseInt(mif.getToken(' ', true));
                    Coordinate[] coords = new Coordinate[numpoints + 1];

                    for (int p = 0; p < numpoints; p++) {
                        coords[p] = readMIFCoordinate();
                    }

                    coords[coords.length - 1] = coords[0];
                    tmpRing = geomFactory.createLinearRing(coords);

                    /*
                     * In MIF format a polygon is described as a list of rings, with no info wether
                     * a ring is a hole or a shell, so we have to determine it by checking if
                     * a ring in contained in the previously defined shell
                     */
                    if ((shell != null) && shell.contains(tmpRing)) {
                        holes.add(tmpRing);
                        tmpRing = null; // mark as done
                        savePolygon = (i == (numpolygons - 1));
                    } else {
                        // New polygon, must save previous if it's not the first ring
                        savePolygon = (i > 0);
                    }

                    if (savePolygon) {
                        LinearRing[] h = null;

                        if (holes.size() > 0) {
                            h = new LinearRing[holes.size()];

                            for (int hole = 0; hole < holes.size(); hole++) {
                                h[hole] = (LinearRing) holes.get(hole);
                            }
                        }

                        polygons.add(geomFactory.createPolygon(shellRing, h));

                        shellRing = null;
                    }

                    // Build the polygon needed for testing holes
                    if (tmpRing != null) {
                        shellRing = tmpRing;
                        shell = geomFactory.createPolygon(shellRing, null);
                        holes = new Vector();
                    }
                }

                if (shellRing != null) {
                    polygons.add(geomFactory.createPolygon(shellRing, null));
                }

                try {
                    if ((polygons.size() == 1) && !toGeometryCollection) {
                        return (Polygon) polygons.get(0);
                    }

                    Polygon[] polys = new Polygon[polygons.size()];

                    for (int i = 0; i < polygons.size(); i++) {
                        polys[i] = (Polygon) polygons.get(i);
                    }

                    return geomFactory.createMultiPolygon(polys);
                } catch (TopologyException topexp) {
                    throw new TopologyException(
                        "TopologyException reading Region polygon : "
                        + topexp.toString());
                }
            } catch (Exception e) {
                throw new IOException(
                    "Exception reading Region data from MIF file : "
                    + e.toString());
            }
        }

        /**
         * Reads a couple of coordinates (x,y) from input stream, applying the
         * transform factor if required.
         *
         * @return A Coordinate object, or null if error encountered
         *
         * @throws IOException if couldn't build a valid Coordinate object
         */
        private Coordinate readMIFCoordinate() throws IOException {
            String x;
            String y;

            try {
                x = mif.getToken(' ', true);
                y = mif.getToken();

                if (x.equals("") || y.equals("")) {
                    throw new IOException("End of file.");
                }

                Coordinate result = new Coordinate(Double.parseDouble(x),
                        Double.parseDouble(y));

                if (useTransform) {
                    result.x = (result.x * multX) + sumX;
                    result.y = (result.y * multY) + sumY;
                }

                return result;
            } catch (Exception e) {
                throw new IOException("Error getting coordinates: "
                    + e.toString());
            }
        }

        /**
         * Reads Point information from the MIF stream
         *
         * @return The next POINT object read
         *
         * @throws IOException Error retrieving geometry from input MIF stream
         */
        private Geometry readPointObject() throws IOException {
            return geomFactory.createPoint(readMIFCoordinate());
        }

        /**
         * Reads Line information from the MIF stream
         *
         * @return a LINESTRING object
         *
         * @throws IOException Error retrieving geometry from input MIF stream
         */
        private Geometry readLineObject() throws IOException {
            Coordinate[] cPoints = new Coordinate[2];
            cPoints[0] = readMIFCoordinate();
            cPoints[1] = readMIFCoordinate();

            LineString[] result = { geomFactory.createLineString(cPoints) };

            if (toGeometryCollection) {
                return geomFactory.createMultiLineString(result);
            }

            return result[0];
        }

        private Geometry readTextObject() throws IOException {
            try {
                mifText = mif.getToken(' ', true, true);
            } catch (ParseException e) {
                throw new IOException(e.getMessage());
            }

            Coordinate c1 = readMIFCoordinate();
            Coordinate c2 = readMIFCoordinate();
            Coordinate p = new Coordinate((c1.x + c2.x) / 2, (c1.y + c2.y) / 2);

            return geomFactory.createPoint(p);
        }
    }

    /**
     * <p>
     * MIF FeatureWriter
     * </p>
     */
    private class Writer implements FeatureWriter<SimpleFeatureType, SimpleFeature> {
        private PrintStream outMif = null;
        private PrintStream outMid = null;
        private  FeatureReader<SimpleFeatureType, SimpleFeature> innerReader = null;
        private MIFValueSetter[] fieldValueSetters;
        private SimpleFeature editFeature = null;
        private SimpleFeature originalFeature = null;

        private Writer(PrintStream mif, PrintStream mid, boolean append)
            throws IOException {
            innerReader = getFeatureReader();

            try {
                fieldValueSetters = getValueSetters();
            } catch (SchemaException e) {
                throw new IOException(e.getMessage());
            }

            outMif = mif;
            outMid = mid;

            try {
                if (!append) {
                    outMif.println(exportHeader());
                }
            } catch (Exception e) {
                outMif = null;
                outMid = null;
                throw new IOException(e.getMessage());
            }
        }

        public SimpleFeatureType getFeatureType() {
            return featureType;
        }

        public SimpleFeature next() throws IOException {
            try {
                if (originalFeature != null) {
                    writeFeature(originalFeature); // keep the original
                }

                if (innerReader.hasNext()) {
                    originalFeature = innerReader.next(); // ;
                    editFeature = SimpleFeatureBuilder.copy(originalFeature);
                    } else {
                    originalFeature = null;
                    editFeature = SimpleFeatureBuilder.build(featureType, featureDefaults, null); 
                }

                return editFeature;
            } catch (Exception e) {
                throw new IOException(e.toString());
            }
        }

        public void remove() throws IOException {
            if (editFeature == null) {
                throw new IOException("Current feature is null");
            }

            editFeature = null;
            originalFeature = null;
        }

        public void write() throws IOException {
            if (editFeature == null) {
                throw new IOException("Current feature is null");
            }

            try {
                writeFeature(editFeature);
            } catch (Exception e) {
                editFeature = null;
                throw new IOException("Can't write feature: " + e.toString());
            }

            editFeature = null;
            originalFeature = null;
        }

        public boolean hasNext() throws IOException {
            return innerReader.hasNext();
        }

        public void close() throws IOException {
            while (hasNext())
                next();

            try {
                if (originalFeature != null) {
                    writeFeature(originalFeature); // keep the original
                }
            } catch (Exception e) {
            }

            innerReader.close();
            innerReader = null;

            try {
                if (outMif != null) {
                    outMif.close();
                }

                if (outMid != null) {
                    outMid.close();
                }

                copyFileAndDelete(mifFileOut, mifFile, true);
                copyFileAndDelete(midFileOut, midFile, true);
            } catch (IOException e) {
            } finally {
                outMid = null;
                outMif = null;
            }
        }

        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }

        /**
         * Writes the given Feature to file
         *
         * @param f The feature to write
         *
         * @throws IOException if cannot access file for reading
         * @throws SchemaException if given Feature is not compatible with
         *         MIFFile FeatureType. TODO: private
         */
        public void writeFeature(SimpleFeature f) throws IOException, SchemaException {
            if ((outMif == null) || (outMid == null)) {
                throw new IOException(
                    "Output stream has not been opened for writing.");
            }

            Geometry theGeom = (geomFieldIndex >= 0)
                ? (Geometry) f.getAttribute(geomFieldIndex) : null;
            String outGeom = exportGeometry(theGeom);

            if (outGeom.equals("")) {
                throw new SchemaException("Unsupported geometry type: "
                    + theGeom.getClass().getName());
            }

            outMif.println(outGeom);

            int col;
            String outBuf = "";

            try {
                for (col = 1; col < numAttribs; col++) {
                    fieldValueSetters[col].setValue(f.getAttribute(col));

                    if (col > 1) {
                        outBuf += chDelimiter;
                    }

                    outBuf += fieldValueSetters[col].getString();
                }
            } catch (Exception e) {
                throw new IOException("Error writing MID file: "
                    + e.getMessage());
            }

            outMid.println(outBuf);
        }

        private String exportGeometry(Geometry geom) {
            // Style information is optional, so we will not export the default styles
            if ((geom == null) || (geom.isEmpty())) {
                return TYPE_NONE.toUpperCase();
            }

            if (geom instanceof Point) {
                return TYPE_POINT + " "
                + exportCoord(((Point) geom).getCoordinate());
            }

            if (geom instanceof LineString) {
                Coordinate[] coords = geom.getCoordinates();

                return TYPE_PLINE + " " + exportCoords(coords, false);
            }

            if (geom instanceof MultiPolygon) {
                MultiPolygon mpoly = (MultiPolygon) geom;

                int nPol = mpoly.getNumGeometries();
                int nRings = nPol;

                for (int i = 0; i < nPol; i++) {
                    nRings += ((Polygon) mpoly.getGeometryN(i))
                    .getNumInteriorRing();
                }

                String buf = TYPE_REGION + " " + (nRings);

                for (int i = 0; i < nPol; i++) {
                    Polygon poly = (Polygon) mpoly.getGeometryN(i);

                    buf += ("\n"
                    + exportCoords(poly.getExteriorRing().getCoordinates(), true));

                    for (int inner = 0; inner < poly.getNumInteriorRing();
                            inner++) {
                        buf += ("\n"
                        + exportCoords(poly.getInteriorRingN(inner)
                                           .getCoordinates(), true));
                    }
                }

                return buf;
            }

            if (geom instanceof Polygon) {
                Polygon poly = (Polygon) geom;
                int nRings = poly.getNumInteriorRing();
                String buf = TYPE_REGION + " " + (1 + nRings) + "\n";
                buf += exportCoords(poly.getExteriorRing().getCoordinates(),
                    true);

                for (int i = 0; i < nRings; i++) {
                    buf += ("\n"
                    + exportCoords(poly.getInteriorRingN(i).getCoordinates(),
                        true));
                }

                return buf;
            }

            if (geom instanceof MultiLineString) {
                MultiLineString multi = (MultiLineString) geom;
                String buf = TYPE_PLINE + " Multiple "
                    + multi.getNumGeometries();

                for (int i = 0; i < multi.getNumGeometries(); i++) {
                    buf += ("\n"
                    + exportCoords(((LineString) multi.getGeometryN(i))
                        .getCoordinates(), false));
                }

                return buf;
            }

            return "";
        }

        /**
         * Renders a single coordinate
         *
         * @param coord The Coordinate object
         *
         * @return The coordinate as string
         */
        private String exportCoord(Coordinate coord) {
            return coord.x + " " + coord.y;
        }

        /**
         * Renders a coordinate list, prefixing it with the number of points
         * SkipLast is used for Polygons (in Mapinfo the last vertex of a
         * polygon is not the clone of first one)
         *
         * @param coords The coordinates to render
         * @param skipLast if true, a polygon coordinate list will be rendered
         *
         * @return the coordinate list as string
         */
        private String exportCoords(Coordinate[] coords, boolean skipLast) {
            int len = (skipLast) ? (coords.length - 1) : coords.length;

            String buf = String.valueOf(len);

            for (int i = 0; i < len; i++) {
                buf += ("\n" + exportCoord(coords[i]));
            }

            return buf;
        }
    }
}
