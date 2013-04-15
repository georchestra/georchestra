/**
 * 
 */
package org.geotools.data.mif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import org.opengis.referencing.crs.CoordinateReferenceSystem;



/**
 * <p>
 * MIFProjReader class enables CoordSys clause parsing support for writing of Features in MapInfo
 * MIF/MID text file format.
 * </p>
 * 
 * <p>
 * Open issues:
 * </p>
 * 
 * <ul>
 * <li>
 * CoordSys clause parsing is still not supported for reading MapInfo MIF/MID files.
 * </li>
 * </ul>
 * 
 * @author mcoudert
 */
public class MIFProjReader {
    
    /** The logger for the mif module. */
    protected static final Logger LOGGER = Logger.getLogger(MIFProjReader.class.getName());
    
    // Constants
    private static final String PRJ_NAME = "MAPINFOW.PRJ";
    private static final String SRID_PATTERN = "\\p";
    private static final String QUOTE = "\"";
    
    private static ConcurrentHashMap<Integer, String> mapEpsgToMifCoordSys = null;
    private static ConcurrentHashMap<String, Integer> mapMifCoordSysToEpsg = null;
    
    // File Input Variable : MIF Projection
    private static File prjFile = null;
    
    /**
     * Constructor
     */
    public MIFProjReader(String path) throws IOException {
        // TODO use url instead of String
        super();
        checkFileName(path);        
    }

    /**
     * New instance of MIFProjReader. This constructor will try to retrieve the crs from MAPINFOW.PRJ (in resource folder), it it is not
     * present in the directory an IOException will be thrown.
     * 
     * @param path
     * @throws IOException
     */
    public MIFProjReader() throws IOException {
        super();
        
		try {
	        URL resource = this.getClass().getResource("MAPINFOW.PRJ");
			
			String path = resource.toURI().getPath();
			
	        checkFileName(path);
	        
		} catch (URISyntaxException e) {
			
			LOGGER.log(Level.SEVERE, e.getMessage());
			throw new IOException(e);
		}
    }
    
    /**
     * Check the path name of the PRJ file
     *
     * @param path The full path of the .mif file, with or without extension
     *
     * @throws FileNotFoundException
     */
    private void checkFileName(String path)
        throws FileNotFoundException {
        File file = new File(path);

        if (file.isDirectory()) {
            throw new FileNotFoundException(path + " is a directory");
        }

        if (!file.getName().equals(PRJ_NAME)) {
            throw new FileNotFoundException(" Unexpected file " + path + " for MapInfo Projection.");
        }
        
        prjFile = file; 
    }

    
    /**
     * Reads PRJ file stream tokenizer in order to set the maps which maintain the bidirectional relation epsg 1 <---> 1 mifCoordSys
     *
     * @param prj
     * @param mapMifCoordSysToEpsg 
     * @param mapEpsgToMifCoordSys
     *
     * @throws IOException
     */
	private static void readPrjFile(MIFFileTokenizer prj, ConcurrentHashMap<Integer, String> mapEpsgToMifCoordSys, ConcurrentHashMap<String, Integer> mapMifCoordSysToEpsg)
        throws IOException {
        try {

        	while (prj.readLine()) {
            
        		String line = prj.getLine();
                if (line.contains(SRID_PATTERN)) {
                    String strEpsg = line.subSequence(line.indexOf(SRID_PATTERN)+2,line.lastIndexOf(QUOTE)).toString();
                    Integer epsg = Integer.parseInt(strEpsg);
                    String mifCoordSys = line.substring(line.lastIndexOf(QUOTE)+2);

                    if (!mapEpsgToMifCoordSys.containsKey( epsg )) {
                        mapEpsgToMifCoordSys.put(epsg, mifCoordSys); 
                    }
                    if (!mapMifCoordSysToEpsg.containsKey(mifCoordSys)) {
                    	mapMifCoordSysToEpsg.put(mifCoordSys, epsg); 
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("IOException reading PRJ file, line "
                + prj.getLineNumber() + ": " + e.getMessage());
        }
    }
    

    /**
     * This method initializes prjMaps   
     * 
     * @throws IOException
     * 
     * @return java.util.concurrent.ConcurrentHashMap
     *  
     */
    private ConcurrentHashMap<Integer, String> getMapEpsgToMifCoordSys() throws IOException {
        if (mapEpsgToMifCoordSys == null) {
        	initCoordSysMaps();
        }
        return mapEpsgToMifCoordSys ; 
    }
    

    /**
     * This method initializes prjMaps   
     * 
     * @throws IOException
     * 
     * @return java.util.concurrent.ConcurrentHashMap
     *  
     */
    private ConcurrentHashMap<String, Integer> getMapMifCoordSysToEpsg() throws IOException{
    	
        if (mapMifCoordSysToEpsg == null) {
            
        	initCoordSysMaps();
        }
        return mapMifCoordSysToEpsg ; 
    }

    
    private static void initCoordSysMaps() throws IOException {
    	
    	MIFFileTokenizer prjTokenizer = null;
        try {
        	LOGGER.info("Readding MAPINFO.PRJ file, mapping under construction");

        	mapMifCoordSysToEpsg = new ConcurrentHashMap<String, Integer>();
        	mapEpsgToMifCoordSys = new ConcurrentHashMap<Integer, String>();
        
        	prjTokenizer = new MIFFileTokenizer(
        						new BufferedReader(
        								new FileReader(prjFile)));  
            readPrjFile(prjTokenizer, mapEpsgToMifCoordSys, MIFProjReader.mapMifCoordSysToEpsg);
        } catch (Exception e) {
            throw new IOException("Can't read PRJ file : " + e.toString());
        } finally {
            try {
                prjTokenizer.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
	}

	/**
     * This method checks whether SRID exists in MapInfo projection file.
     * 
     * @param crs Coordinate Reference System 
     * 
     * @throws IOException
     */
    public String checkSRID(CoordinateReferenceSystem crs) throws IOException {
        String coordsys = "";
        String code = "";
        
        if (crs != null) {
            try {
                Set ident = crs.getIdentifiers();
                if ((ident == null || ident.isEmpty()) && crs == DefaultGeographicCRS.WGS84) {
                    code = "4326";
                } else {
                    code = ((NamedIdentifier) ident.toArray()[0]).getCode();
                }
            } catch (Exception e) {
                LOGGER.warning("EPSG code could not be determined");
                code = "-1";
            }
        } else {
            code = "-1";
        }
        
        LOGGER.info("Looking for epsg code : " + code);
        
        if (getMapEpsgToMifCoordSys().containsKey(code)){
            coordsys = (String) getMapEpsgToMifCoordSys().get(code);
            LOGGER.info("MapInfo  equivalent projection is : "+ coordsys);
        } else {
            LOGGER.warning("No MapInfo projection related to your EPSG code : " + code);
        }
        
        return coordsys;    
    }
    
    /**
     * Looks up the EPSG associated to the mif coordinate system provided as parameter.
     * 
     * @param mifCoordSys
     * @return the EPSG code
     * @throws IOException 
     */
	public int toCRS(String mifCoordSys) throws IOException {
		
		Integer epsg = getMapMifCoordSysToEpsg().get(mifCoordSys);
		
		if(epsg == null){
			throw new IOException("the mif coordinate system code wasn't found: " + mifCoordSys);
		}
		
		return epsg;
	}

	public String toMifCoordSys(int epsg) throws IOException {
		
		String mifCoordSys = getMapEpsgToMifCoordSys().get(epsg);
		
		if(mifCoordSys == null){
			throw new IOException("the EPSG wasn't found: " + epsg);
		}
		
		return mifCoordSys;
	}
    
}
