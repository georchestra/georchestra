package extractorapp.ws.extractor.wcs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverageio.gdal.ecw.ECWFormat;
import org.geotools.coverageio.gdal.jp2ecw.JP2ECWFormat;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.image.WorldImageFormat;

public class Formats {
    // If the requested format is not available on the server. Then these
    // are the order of formats to try next. (Once downloaded
    // in one of these formats then the result will be converted to the
    // requested format)
    private static Map<String, AbstractGridFormat> gtFormats = new HashMap<String, AbstractGridFormat>();
    // formats that are best consumed by the extractorapp
    public static final Set<String> preferredFormats;
    // formats that contain the CRS information and do not need 
    public static final Set<String> embeddedCrsFormats; 
    static {
        
        gtFormats.put("png", new WorldImageFormat());
        gtFormats.put("gif", new WorldImageFormat());
        gtFormats.put("jpeg", new WorldImageFormat());
        gtFormats.put("geotiff", new GeoTiffFormat());
        gtFormats.put("jp2ecw", new JP2ECWFormat());
        gtFormats.put("ecw", new ECWFormat());
        String[] formats = { "png", "geotiff", "gif", "jpeg", "jp2ecw", "ecw" };
        preferredFormats = Collections.unmodifiableSet(gtFormats.keySet());
        
        formats = new String[]{ "geotiff","jp2ecw", "ecw" };
        embeddedCrsFormats = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(formats)));
        
    }
    public static AbstractGridFormat getFormat(String format) {
        return gtFormats.get(format.toLowerCase());
    }
}
