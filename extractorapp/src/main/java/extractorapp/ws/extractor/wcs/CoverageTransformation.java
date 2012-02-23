package extractorapp.ws.extractor.wcs;

import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;

import extractorapp.ws.ExtractorException;

/**
 * Class to assist in reading a coverage from a file and transforming the file in some way
 * 
 * @author jeichar
 *
 * @param <T> The resulting object from the transformation
 */
@SuppressWarnings("deprecation")
abstract class CoverageTransformation<T> {
	/**
	 * Entry point for performing a transformation
	 * 
	 * @param in the file to read the coverage from
	 * @param transform the transformation to apply
	 * 
	 * @return result from the transform object
	 */
	public static <T> T perform(File in, CoverageTransformation<T> transform) throws IOException {
            GeneralParameterValue[] params = new GeneralParameterValue[0];
            AbstractGridFormat gridFormat = lookupFormat(in);
            GridCoverage coverage = gridFormat.getReader (in).read (params);
            try {
				return transform.transform(coverage);
			} catch (FactoryException e) {
				throw new ExtractorException(e);
			}
	}
	
	/**
	 * just a helper method for obtaining a format object
	 */
	public static AbstractGridFormat lookupFormat (File in) {
        Format format = GridFormatFinder.findFormat (in);
        
        if (format instanceof AbstractGridFormat && UnknownFormat.class != format.getClass ()) {
            return (AbstractGridFormat) format;
        } else {
            throw new IllegalArgumentException ("Current configuration is unable to read coverage of " + in
                    + " format.  " + "Check that you have the correct geotools plugins");
        }		
	}

	/**
	 * The method called by perform to do the actual transformation on the coverage
	 */
	public abstract T transform(GridCoverage coverage) throws IOException, FactoryException;
}
