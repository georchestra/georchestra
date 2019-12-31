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

package org.geowebcache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.geowebcache.config.ServerConfiguration;
import org.geowebcache.demo.Demo;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.storage.BlobStoreAggregator;
import org.geowebcache.storage.StorageBroker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;

/** @author Jesse on 4/25/2014. */
public class GeorchestraGeoWebCacheDispatcher extends GeoWebCacheDispatcher
        implements InitializingBean {

    private final TileLayerDispatcher tileLayerDispatcher;
    private final GridSetBroker gridSetBroker;

    private String instanceName;
    private String headerUrl;
    private String headerHeight;

    private String georHeaderInclude =
            "<html>"
                    + "  <head>"
                    + "    <title>GeoWebCache - @instanceName@</title>"
                    + "    <style type=\"text/css\">"
                    + "      body, td {"
                    + "        font-family: Verdana,Arial,'Bitstream Vera Sans',Helvetica,sans-serif;"
                    + "        font-size: 0.85em;"
                    + "        vertical-align: top;"
                    + "      }"
                    + "      a#logo {"
                    + "        display:none;"
                    + "      }"
                    + "    </style>"
                    + "  </head>"
                    + "  <body>"
                    + "    <!-- geOrchestra header -->"
                    + "    <div id=\"go_head\">"
                    + "      <iframe src=\"@headerUrl@?active=geowebcache\" style=\"width:100%;height:@headerHeight@px;border:none;overflow:hidden;\" scrolling=\"no\" frameborder=\"0\"></iframe>"
                    + "    </div>"
                    + "    <!-- end of geOrchestra header -->";

    /** Should be invoked through Spring. */
    public GeorchestraGeoWebCacheDispatcher(
            TileLayerDispatcher tileLayerDispatcher,
            GridSetBroker gridSetBroker,
            StorageBroker storageBroker,
            BlobStoreAggregator blobStoreAggregator,
            ServerConfiguration mainConfiguration,
            RuntimeStats runtimeStats)
            throws IOException {
        super(
                tileLayerDispatcher,
                gridSetBroker,
                storageBroker,
                blobStoreAggregator,
                mainConfiguration,
                runtimeStats);
        this.tileLayerDispatcher = tileLayerDispatcher;
        this.gridSetBroker = gridSetBroker;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setHeaderUrl(String headerUrl) {
        this.headerUrl = headerUrl;
    }

    public void setHeaderHeight(String headerHeight) {
        this.headerHeight = headerHeight;
    }

    public void afterPropertiesSet() throws IOException {
        Objects.requireNonNull(this.instanceName, "property 'instanceName' not initialized");
        Objects.requireNonNull(this.headerUrl, "property 'headerUrl' not initialized");
        Objects.requireNonNull(this.headerHeight, "property 'headerHeight' not initialized");

        georHeaderInclude = georHeaderInclude.replace("@instanceName@", this.instanceName);
        georHeaderInclude = georHeaderInclude.replace("@headerHeight@", this.headerHeight);
        georHeaderInclude = georHeaderInclude.replace("@headerUrl@", this.headerUrl);
    }

    @Override
    protected ModelAndView handleRequestInternal(
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Break the request into components, {type, service name}
        String[] requestComps = null;
        try {
            String normalizedURI =
                    request.getRequestURI().replaceFirst(request.getContextPath(), "");

            if (getServletPrefix() != null) {
                normalizedURI =
                        normalizedURI.replaceFirst(
                                getServletPrefix(), ""); // getRequestURI().replaceFirst(request
                // .getContextPath()+,
                // "");
            }
            requestComps = parseRequest(normalizedURI);
            // requestComps = parseRequest(request.getRequestURI());
        } catch (GeoWebCacheException gwce) {
            // superclass will handle this case as well
        }

        if (requestComps == null
                || requestComps[0].equalsIgnoreCase(TYPE_HOME)
                || requestComps[0].equalsIgnoreCase(TYPE_DEMO)
                || requestComps[0].equalsIgnoreCase(TYPE_DEMO + "s")) {
            handleDemoRequest(requestComps, request, response);
        } else {
            return super.handleRequestInternal(request, response);
        }

        return null;
    }

    private void handleDemoRequest(
            String[] requestComps, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final HttpServletResponse httpServletResponse =
                    new HttpServletResponseWrapper(response) {
                        @Override
                        public ServletOutputStream getOutputStream() throws IOException {
                            return new ServletOutputStream() {

                                @Override
                                public void write(int b) throws IOException {
                                    out.write(b);
                                }

                                @Override
                                public void write(byte[] b) throws IOException {
                                    out.write(b);
                                }

                                @Override
                                public void write(byte[] b, int off, int len) throws IOException {
                                    out.write(b, off, len);
                                }

                                @Override
                                public boolean isReady() {
                                    return true;
                                }

                                @Override
                                public void setWriteListener(WriteListener writeListener) {
                                    throw new UnsupportedOperationException(
                                            "setWriteListener not implemented, call not expected");
                                }
                            };
                        }
                    };
            if (requestComps == null || requestComps[0].equalsIgnoreCase(TYPE_HOME)) {
                super.handleRequestInternal(request, httpServletResponse);
            } else {
                Demo.makeMap(
                        this.tileLayerDispatcher,
                        this.gridSetBroker,
                        requestComps[1],
                        request,
                        httpServletResponse);
            }
        } finally {
            out.close();
        }

        final String html = out.toString("UTF-8");

        final String bodyTag = "<body>";
        final String htmlBody = html.substring(html.indexOf(bodyTag) + bodyTag.length());

        StringBuilder builder = new StringBuilder(georHeaderInclude);
        builder.append(htmlBody);
        final byte[] bytes = builder.toString().getBytes("UTF-8");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private String[] parseRequest(String servletPath) throws GeoWebCacheException {
        String[] retStrs = new String[2];
        String[] splitStr = servletPath.split("/");

        if (splitStr == null || splitStr.length < 2) {
            return null;
        }

        retStrs[0] = splitStr[1];
        if (splitStr.length > 2) {
            retStrs[1] = splitStr[2];
        }
        return retStrs;
    }
}
