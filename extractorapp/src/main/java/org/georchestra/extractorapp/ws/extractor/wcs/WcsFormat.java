/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor.wcs;

import java.io.File;
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
public class WcsFormat extends AbstractGridFormat {
    private final long maxCoverageExtractionSize;

    public WcsFormat(long maxCoverageExtractionSize) {
        this.maxCoverageExtractionSize = maxCoverageExtractionSize;
    }

    public boolean accepts (Object input, Hints hints ) {
        return accepts(input);
    }
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
        throw new UnsupportedOperationException ("Does not need to be implemented for geOrchestra");
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
    
    public GridCoverageWriter getWriter (Object destination, Hints hints) {
    	throw new UnsupportedOperationException ("Read only support");
    }

}
