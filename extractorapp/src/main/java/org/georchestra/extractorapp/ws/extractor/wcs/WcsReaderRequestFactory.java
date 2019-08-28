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

import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.BBOX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.COVERAGE;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.CRS;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.EXTENT;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.FORMAT;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MAXX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MAXY;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MINX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.MINY;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.PASSWORD;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.REMOTE_REPROJECT;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RES;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RESULT_IMAGE_PARAMS;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RESX;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.RESY;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.SIZE;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.TIME;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.USERNAME;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.USE_COMMANDLINE_GDAL;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.USE_POST;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsParameters.VERSION;
import static org.georchestra.extractorapp.ws.extractor.wcs.WcsReaderRequest.DEFAULT_VERSION;

import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Factory methods for creating WcsReaderRequest objects
 *
 * @author jeichar
 */
public class WcsReaderRequestFactory {

    // ---- Factory methods with explicit resolution and height ----- //

    /**
     * Create a new instance of WcsReaderRequest
     *
     * @param version         WCS version to use. Can use {@link #DEFAULT_VERSION}
     * @param coverage        the layer/coverage name of the coverage to access
     * @param minx            envelope param
     * @param miny            envelope param
     * @param maxx            envelope param
     * @param maxy            envelope param
     * @param requestCRS      request CRS to use. Can use {@link #DEFAULT_CRS}
     * @param resolution      resolution of the returned image
     * @param height          height of returned image
     * @param format          format of the image
     * @param usePost         if true post will be used for making requests
     * @param remoteReproject if true reprojection will be done on WCS side if
     *                        possible
     */
    public static WcsReaderRequest create(String version, String coverage, double minx, double miny, double maxx,
            double maxy, CoordinateReferenceSystem requestCRS, CoordinateReferenceSystem responseCRS, double resolution,
            String format, boolean usePost, boolean remoteReproject, boolean useCommandLineGDAL, String username,
            String password) {
        return new WcsReaderRequest(version, coverage, new ReferencedEnvelope(minx, maxx, miny, maxy, requestCRS),
                responseCRS, resolution, format, usePost, remoteReproject, useCommandLineGDAL, username, password);
    }

    /**
     * Use {@link WcsReaderRequestFactory} to create instances of
     * {@link WcsReaderRequestFactory}
     *
     * @param version         WCS version to use. Can use {@link #DEFAULT_VERSION}
     * @param coverage        the layer/coverage name of the coverage to access
     * @param bbox            the bbox of the desired area in the coverage
     * @param resolution      resolution of the returned image
     * @param format          format of the image
     * @param usePost         if true post will be used for making requests
     * @param remoteReproject if true reprojection will be done on WCS side if
     *                        possible
     */
    public static WcsReaderRequest create(String version, String coverage, ReferencedEnvelope bbox,
            CoordinateReferenceSystem responseCRS, double resolution, String format, boolean usePost,
            boolean remoteReproject, boolean useCommandLineGDAL, String username, String password) {
        return new WcsReaderRequest(version, coverage, bbox, responseCRS, resolution, format, usePost, remoteReproject,
                useCommandLineGDAL, username, password);
    }

    /**
     * Create a request object from a set of parameters. The parameters expected are
     * those in {@link WcsParameters}
     */
    public static WcsReaderRequest create(GeneralParameterValue[] params)
            throws NoSuchAuthorityCodeException, FactoryException {
        String format, requestEpsg, responseEpsg, coverage, version, username, password;
        boolean usePost = true;
        boolean remoteReproject = true;
        boolean useCommandLineGDAL = false;
        format = coverage = null;
        responseEpsg = WcsReaderRequest.DEFAULT_CRS;
        requestEpsg = WcsReaderRequest.DEFAULT_CRS;
        version = WcsReaderRequest.DEFAULT_VERSION;
        username = null;
        password = null;

        List<GeneralParameterValue> extent, imgParams, bbox, resolution;
        extent = imgParams = bbox = resolution = new java.util.ArrayList<GeneralParameterValue>();

        Double minx, maxx, miny, maxy, resx;

        minx = maxx = miny = maxy = resx = null;

        // find top level
        for (GeneralParameterValue param : params) {
            if (param.getDescriptor().getName().equals(USE_POST.getName())) {
                usePost = WcsReaderRequest.getValue(param, Boolean.class);
            } else if (param.getDescriptor().getName().equals(REMOTE_REPROJECT.getName())) {
                remoteReproject = WcsReaderRequest.getValue(param, Boolean.class);
            } else if (param.getDescriptor().getName().equals(USE_COMMANDLINE_GDAL.getName())) {
                useCommandLineGDAL = WcsReaderRequest.getValue(param, Boolean.class);
            } else if (param.getDescriptor().getName().equals(FORMAT.getName())) {
                format = WcsReaderRequest.getValue(param, String.class);
            } else if (param.getDescriptor().getName().equals(CRS.getName())) {
                responseEpsg = WcsReaderRequest.getValue(param, String.class);
            } else if (param.getDescriptor().getName().equals(COVERAGE.getName())) {
                coverage = WcsReaderRequest.getValue(param, String.class);
            } else if (param.getDescriptor().getName().equals(VERSION.getName())) {
                version = WcsReaderRequest.getValue(param, String.class);
            } else if (param.getDescriptor().getName().equals(EXTENT.getName())) {
                extent = ((ParameterValueGroup) param).values();
            } else if (param.getDescriptor().getName().equals(RESULT_IMAGE_PARAMS.getName())) {
                imgParams = ((ParameterValueGroup) param).values();
            } else if (param.getDescriptor().getName().equals(USERNAME.getName())) {
                username = WcsReaderRequest.getValue(param, String.class);
            } else if (param.getDescriptor().getName().equals(PASSWORD.getName())) {
                password = WcsReaderRequest.getValue(param, String.class);
            } else {
                throw new IllegalArgumentException(param + "is not a recognized parameter");
            }
        }

        for (GeneralParameterValue param : extent) {
            if (param.getDescriptor().getName().equals(BBOX.getName())) {
                bbox = ((ParameterValueGroup) param).values();
            } else if (param.getDescriptor().getName().equals(TIME.getName())) {
                throw new IllegalArgumentException(param + " is not currently supported");
                // time = ((ParameterValueGroup)param).values ();
            } else {
                throw new IllegalArgumentException(param + "is not a recognized parameter");
            }
        }

        for (GeneralParameterValue param : imgParams) {
            if (param.getDescriptor().getName().equals(SIZE.getName())) {
                throw new IllegalArgumentException(param + " is not currently supported");
                // res = ((ParameterValueGroup)param).values ();
            } else if (param.getDescriptor().getName().equals(RES.getName())) {
                resolution = ((ParameterValueGroup) param).values();
            } else {
                throw new IllegalArgumentException(param + "is not a recognized parameter");
            }
        }

        for (GeneralParameterValue param : bbox) {
            if (param.getDescriptor().getName().equals(MINX.getName())) {
                minx = WcsReaderRequest.getValue(param, Double.class);
            } else if (param.getDescriptor().getName().equals(MAXX.getName())) {
                maxx = WcsReaderRequest.getValue(param, Double.class);
            } else if (param.getDescriptor().getName().equals(MINY.getName())) {
                miny = WcsReaderRequest.getValue(param, Double.class);
            } else if (param.getDescriptor().getName().equals(MAXY.getName())) {
                maxy = WcsReaderRequest.getValue(param, Double.class);
            } else if (param.getDescriptor().getName().equals(CRS.getName())) {
                requestEpsg = WcsReaderRequest.getValue(param, String.class);
            } else {
                throw new IllegalArgumentException(param + "is not a recognized parameter");
            }
        }

        for (GeneralParameterValue param : resolution) {
            if (param.getDescriptor().getName().equals(RESX.getName())) {
                resx = WcsReaderRequest.getValue(param, Double.class);
            } else if (param.getDescriptor().getName().equals(RESY.getName())) {
                // currently we are assuming resx and resy are equal
//                resx = WcsReaderRequest.getValue(param, Double.class);
            } else {
                throw new IllegalArgumentException(param + "is not a recognized parameter");
            }
        }

        WcsReaderRequest.assertNotNull("minx", minx);
        WcsReaderRequest.assertNotNull("maxx", maxx);
        WcsReaderRequest.assertNotNull("miny", miny);
        WcsReaderRequest.assertNotNull("maxy", maxy);
        WcsReaderRequest.assertNotNull("width", resolution);
        WcsReaderRequest.assertNotNull("height", resx);
        WcsReaderRequest.assertNotNull("coverage", coverage);
        WcsReaderRequest.assertNotNull("format", format);

        CoordinateReferenceSystem requestCrs = org.geotools.referencing.CRS.decode(requestEpsg);
        CoordinateReferenceSystem responseCrs = org.geotools.referencing.CRS.decode(responseEpsg);

        return WcsReaderRequestFactory.create(version, coverage, minx, miny, maxx, maxy, requestCrs, responseCrs, resx,
                format, usePost, remoteReproject, useCommandLineGDAL, username, password);
    }
}
