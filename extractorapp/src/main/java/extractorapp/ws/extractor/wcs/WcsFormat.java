package extractorapp.ws.extractor.wcs;

import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Create {@link WcsCoverageReader}s
 * 
 * See {@link Format} for details, and don't be fooled by the deprecated non-sense.
 * that is a consequence of working with Martin.  There are no replacements for this
 * API so we have to sue them.
 * 
 * @author jeichar
 */
@SuppressWarnings("deprecation")
public class WcsFormat extends AbstractGridFormat {
    private final long maxCoverageExtractionSize;

    public WcsFormat(long maxCoverageExtractionSize) {
        this.maxCoverageExtractionSize = maxCoverageExtractionSize;
    }

    @Override
    public boolean accepts (Object input) {
        try {
            return toURL (input) != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public WcsCoverageReader getReader (Object source) {
        testSource (source);
        try {
            return new WcsCoverageReader (toURL (source), maxCoverageExtractionSize);
        } catch (MalformedURLException e) {
            throw new RuntimeException (e);
        }
    }
    
    /*-------------------------  Support methods  --------------------*/
    private void testSource (Object source) {
        if (!accepts (source)) {
            throw new IllegalArgumentException (source + " is not a supported source for a WCS");
        }
    }

    private URL toURL (Object input) throws MalformedURLException {
        if (input instanceof String) {
            return new URL ((String) input);
        } else if (input instanceof URL) {
            return (URL) input;
        }
        return null;
    }

    /*-------------------------  Unsupported methods--------------------*/
    @Override
    public ParameterValueGroup getReadParameters () {
        throw new UnsupportedOperationException ("Does not need to be implemented for geobretagne");
    }

    /*-------------------------  write support is not planned  --------------------*/
    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters () {
        throw new UnsupportedOperationException ("Read only support");
    }

    @Override
    public WcsCoverageReader getReader (Object source, Hints hints) {
        return getReader (source);
    }

    @Override
    public GridCoverageWriter getWriter (Object destination) {
        throw new UnsupportedOperationException ("Read only support");
    }

}
