/**
 * 
 */
package org.geotools.data.mif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;



/**
 * <p>
 * MIFProjReader class enables CoordSys clause parsing support for writing of Features in MapInfo
 * MIF/MID text file format.
 * </p>
 * 
 * @author mcoudert
 */
public class MIFProjReader {
    
    /** The logger for the mif module. */
    protected static final Logger LOGGER = Logger.getLogger(MIFProjReader.class.getName());

    /** maintains the relation Epsg 1<---->1 Mif Coord Sys */ 
    private static ConcurrentHashMap<Integer, String> mapEpsgToMifCoordSys = null;
    private static ConcurrentHashMap<String, Integer> mapMifCoordSysToEpsg = null;
    
    /** Mapinfo world projection */
    private static final String PRJ_NAME = "MAPINFOW.PRJ";
    private static final String SRID_PATTERN = "\\p";
    private static final String QUOTE = "\"";
    
    /** File Input Variable : MIF Projection */
    private static InputStreamReader PRJ_FILE;
    static{
    	//  Retrieves the CRSs from MAPINFOW.PRJ (in resource folder).
		InputStream is= MIFProjReader.class.getResourceAsStream("MAPINFOW.PRJ");
		PRJ_FILE = new InputStreamReader(is);
    }
    
    /**
     * New instance of {@link MIFProjReader}. 
     * 
     * @param path
     * @throws IOException
     */
    public MIFProjReader() throws IOException {
        super();
        
    }

    /**
     * Constructor
     * 
     * @param projectionFile file which contains the CRS <---> MIF Coord Sys relation
     */
    public MIFProjReader(final String projectionFile ) throws IOException {
        super();

        File file = new File(projectionFile);
        InputStreamReader is = new FileReader(file );
        
        checkFileName(file);
        
        PRJ_FILE = is; 
    }

    
    /**
     * Check the path name of the PRJ file
     *
     * @param path 
     *
     * @throws FileNotFoundException
     */
    private void checkFileName(File file)
        throws FileNotFoundException {

        if (file.isDirectory()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " is a directory");
        }

        if (!file.getName().equals(PRJ_NAME)) {
            throw new FileNotFoundException(" Unexpected file " + file.getAbsolutePath() + " for MapInfo Projection.");
        }
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
	private void readPrjFile(MIFFileTokenizer prj, ConcurrentHashMap<Integer, String> mapEpsgToMifCoordSys, ConcurrentHashMap<String, Integer> mapMifCoordSysToEpsg)
        throws IOException {
        try {

        	while (prj.readLine()) {
            
        		String line = prj.getLine();
                if (line.contains(SRID_PATTERN)) {
                    String strEpsg = line.subSequence(line.indexOf(SRID_PATTERN)+2,line.lastIndexOf(QUOTE)).toString();
                    Integer epsg = Integer.parseInt(strEpsg);
                    String mifCoordSys = line.substring(line.lastIndexOf(QUOTE)+2);
                    mifCoordSys = mifCoordSys.replaceAll("\\s", "");
                    
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

    private void initCoordSysMaps() throws IOException {
    	
    	MIFFileTokenizer prjTokenizer = null;
        try {
        	LOGGER.info("Readding MAPINFO.PRJ file, mapping under construction");

        	mapMifCoordSysToEpsg = new ConcurrentHashMap<String, Integer>();
        	mapEpsgToMifCoordSys = new ConcurrentHashMap<Integer, String>();
        
        	prjTokenizer = new MIFFileTokenizer(
        						new BufferedReader(PRJ_FILE));
        	
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
                Set<ReferenceIdentifier> ident = crs.getIdentifiers();
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
	public int toCRS(final String mifCoordSys) throws IOException {
		
		// removes spaces from mifCoordSys
		String cleanedCoordSys = mifCoordSys.replaceAll("\\s", "");
				
		Integer epsg = getMapMifCoordSysToEpsg().get(cleanedCoordSys);
		
		if(epsg == null){
			throw new IOException("The EPSG code wasn't found for the mif coordinate system: " + mifCoordSys);
		}
		
		return epsg;
	}

	/**
	 * Looks up the Mif coordinate system code associated to the EPSG code, provided as parameter.
	 * 
	 * @param epsg
	 * @return the mif coordinate system code
	 * 
	 * @throws IOException
	 */
	public String toMifCoordSys(final int epsg) throws IOException {
		
		String mifCoordSys = getMapEpsgToMifCoordSys().get(epsg);
		
		if(mifCoordSys == null){
			throw new IOException("the EPSG wasn't found: " + epsg);
		}
		return mifCoordSys;
	}
}
