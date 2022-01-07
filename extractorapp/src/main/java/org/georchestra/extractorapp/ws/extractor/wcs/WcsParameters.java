/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 * The {@link Format} / {@link GridCoverageReader} API requires that there are
 * ParameterObjects defined that can be used for configuring read requests with
 * {@link GridCoverageReader}. This class contains all the parameters that
 * possible
 * 
 * This should not need to be used directory, instead the
 * {@link WcsReaderRequest} class should be used as it can translate to and from
 * these parameters in a easier way.
 * 
 * Read the description parameter of the parameters for documentation on what
 * they do.
 * 
 * @author jeichar
 */
class WcsParameters {
    public static final DefaultParameterDescriptor<String> FORMAT = DefaultParameterDescriptor.create("format",
            "Output format of map, as stated in the GetCapabilities", String.class, "geotiff", false);
    public static final DefaultParameterDescriptor<String> CRS = DefaultParameterDescriptor.create("crs",
            "Coordinate Reference System in which the request is expressed", String.class, "EPSG:4326", false);
    public static final DefaultParameterDescriptor<String> COVERAGE = DefaultParameterDescriptor.create("coverage",
            "Name of an available coverage, as stated in the GetCapabilities", String.class, null, true);
    public static final DefaultParameterDescriptor<String> VERSION = DefaultParameterDescriptor.create("version",
            "Request version", String.class, "1.0.0", false);
    public static final DefaultParameterDescriptor<String> USERNAME = DefaultParameterDescriptor.create("username",
            "Request version", String.class, null, false);
    public static final DefaultParameterDescriptor<String> PASSWORD = DefaultParameterDescriptor.create("password",
            "Request version", String.class, null, false);

    public static final DefaultParameterDescriptor<Double> RESX = DefaultParameterDescriptor.create("resx",
            "When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution. One of WIDTH/HEIGHT or RESX/Y is required.",
            Double.class, null, false);
    public static final DefaultParameterDescriptor<Double> RESY = DefaultParameterDescriptor.create("resy",
            "When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution. One of WIDTH/HEIGHT or RESX/Y is required.",
            Double.class, null, false);

    public static final DefaultParameterDescriptor<Integer> WIDTH = DefaultParameterDescriptor.create("width",
            "Width in pixels of map picture. One of WIDTH/HEIGHT or RESX/Y is required.", Integer.class, null, false);
    public static final DefaultParameterDescriptor<Integer> HEIGHT = DefaultParameterDescriptor.create("height",
            "Height in pixels of map picture. One of WIDTH/HEIGHT or RESX/Y is required.", Integer.class, null, false);

    public static final DefaultParameterDescriptor<Double> TIME1 = DefaultParameterDescriptor.create("time1",
            "start time", Double.class, null, false);
    public static final DefaultParameterDescriptor<Double> TIME2 = DefaultParameterDescriptor.create("time2",
            "end time", Double.class, null, false);

    public static final DefaultParameterDescriptor<Double> MINX = DefaultParameterDescriptor.create("minx",
            "minx of the bbox", Double.class, null, false);
    public static final DefaultParameterDescriptor<Double> MINY = DefaultParameterDescriptor.create("miny",
            "miny of the bbox", Double.class, null, false);
    public static final DefaultParameterDescriptor<Double> MAXX = DefaultParameterDescriptor.create("maxx",
            "maxx of the bbox", Double.class, null, false);
    public static final DefaultParameterDescriptor<Double> MAXY = DefaultParameterDescriptor.create("maxy",
            "maxy of the bbox", Double.class, null, false);

    public static final DefaultParameterDescriptor<Boolean> USE_POST = DefaultParameterDescriptor.create("usePost",
            "Whether Post should be used", Boolean.class, true, false);
    public static final DefaultParameterDescriptor<Boolean> REMOTE_REPROJECT = DefaultParameterDescriptor.create(
            "remoteReproject", "Whether the reprojection should be done on the WCS side or in the extractorapp",
            Boolean.class, true, false);
    public static final DefaultParameterDescriptor<Boolean> USE_COMMANDLINE_GDAL = DefaultParameterDescriptor.create(
            "commandLineGDAL", "Whether use a system process to invoke the commandline gdal application", Boolean.class,
            false, false);

    public static final ParameterDescriptorGroup BBOX = new WcsParameterDescriptorGroup("bbox",
            new GeneralParameterDescriptor[] { MINX, MINY, MAXX, MAXY, CRS });
    public static final ParameterDescriptorGroup TIME = new WcsParameterDescriptorGroup("time",
            new GeneralParameterDescriptor[] { TIME1, TIME2, });
    public static final ParameterDescriptorGroup EXTENT = new WcsParameterDescriptorGroup("extent",
            new GeneralParameterDescriptor[] { BBOX, TIME });

    public static final ParameterDescriptorGroup SIZE = new WcsParameterDescriptorGroup("size",
            new GeneralParameterDescriptor[] { WIDTH, HEIGHT, });
    public static final ParameterDescriptorGroup RES = new WcsParameterDescriptorGroup("res",
            new GeneralParameterDescriptor[] { RESX, RESY, });
    public static final ParameterDescriptorGroup RESULT_IMAGE_PARAMS = new WcsParameterDescriptorGroup("imgParams",
            new GeneralParameterDescriptor[] { SIZE, RES });

    public static final ParameterDescriptorGroup ROOT = new WcsParameterDescriptorGroup("rootGroup",
            new GeneralParameterDescriptor[] { VERSION, COVERAGE, CRS, EXTENT, RESULT_IMAGE_PARAMS, FORMAT, USE_POST });

    public static class WcsParameterDescriptorGroup extends DefaultParameterDescriptorGroup {

        private static final long serialVersionUID = 3520228230801030405L;

        public WcsParameterDescriptorGroup(String name, GeneralParameterDescriptor[] contained) {
            super(name, contained);
        }

        @Override
        public int getMinimumOccurs() {
            return 0;
        }
    }
}
