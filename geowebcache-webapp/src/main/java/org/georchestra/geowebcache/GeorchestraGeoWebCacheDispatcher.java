/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.geowebcache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.geowebcache.GeoWebCacheDispatcher;
import org.geowebcache.GeoWebCacheException;
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
public class GeorchestraGeoWebCacheDispatcher extends GeoWebCacheDispatcher implements InitializingBean {

    private final TileLayerDispatcher tileLayerDispatcher;
    private final GridSetBroker gridSetBroker;

    private String instanceName;

    private boolean useLegacyHeader;
    private String headerUrl;
    private String headerHeight;
    private String headerScript;
    private String logoUrl;
    private String headerConfigFile;

    /**
     * custom georchestra CSS stylsheet: optional, can be null See
     * https://github.com/georchestra/datadir/blob/master/default.properties#L40-L43
     */
    private String georchestraStylesheet = "";

    private String newGeorHeaderInclude = "<html>\n" + "  <head>\n"
            + "    <title>GeoWebCache - @instanceName@</title>\n" + "    <script src=\"@headerScript@\"></script>\n"
            + "    <link rel=\"stylesheet\" type=\"text/css\" href=\"@georchestraStylesheet@\" />\n"
            + "  </head>\n" + "  <body>\n"
            + "    <geor-header  active-app=\"geowebcache\" config-file=\"@headerConfigFile@\" legacy-header=\"@useLegacyHeader@\" legacy-url=\"@headerUrl@\" logo-url=\"@logoUrl@\" stylesheet=\"@georchestraStylesheet@\" height=\"@headerHeight@\"></geor-header>";

    /** Should be invoked through Spring. */
    public GeorchestraGeoWebCacheDispatcher(TileLayerDispatcher tileLayerDispatcher, GridSetBroker gridSetBroker,
            StorageBroker storageBroker, BlobStoreAggregator blobStoreAggregator, ServerConfiguration mainConfiguration,
            RuntimeStats runtimeStats) throws IOException {
        super(tileLayerDispatcher, gridSetBroker, storageBroker, blobStoreAggregator, mainConfiguration, runtimeStats);
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

    public void setUseLegacyHeader(boolean useLegacyHeader) {
        this.useLegacyHeader = useLegacyHeader;
    }

    public void setHeaderScript(String headerScript) {
        this.headerScript = headerScript;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void setHeaderConfigFile(String headerConfigFile) {
        this.headerConfigFile = headerConfigFile;
    }

    public void setGeorchestraStylesheet(String georchestraStylesheet) {
        this.georchestraStylesheet = georchestraStylesheet;
    }

    public @Override void afterPropertiesSet() throws IOException {
        Objects.requireNonNull(this.instanceName, "property 'instanceName' not initialized");
        Objects.requireNonNull(this.headerScript, "property 'headerScript' not initialized");
        Objects.requireNonNull(this.headerHeight, "property 'headerHeight' not initialized");
        Objects.requireNonNull(this.logoUrl, "property 'logoUrl' not initialized");

        if (this.useLegacyHeader) {
            Objects.requireNonNull(this.headerUrl, "property 'headerUrl' not initialized, but useLegacyHeader is true");
        }

        newGeorHeaderInclude = newGeorHeaderInclude.replace("@instanceName@", this.instanceName);
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@headerScript@", this.headerScript);
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@useLegacyHeader@", String.valueOf(this.useLegacyHeader));
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@headerUrl@", this.headerUrl);
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@headerHeight@", this.headerHeight);
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@logoUrl@", this.logoUrl);
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@georchestraStylesheet@", this.georchestraStylesheet);
        newGeorHeaderInclude = newGeorHeaderInclude.replace("@headerConfigFile@", this.headerConfigFile);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.warn("handling " + request.getRequestURI());

        // Break the request into components, {type, service name}
        String[] requestComps = null;
        try {
            String normalizedURI = request.getRequestURI().replaceFirst(request.getContextPath(), "");

            if (getServletPrefix() != null) {
                normalizedURI = normalizedURI.replaceFirst(getServletPrefix(), ""); // getRequestURI().replaceFirst(request
                // .getContextPath()+,
                // "");
            }
            requestComps = parseRequest(normalizedURI);
            // requestComps = parseRequest(request.getRequestURI());
        } catch (GeoWebCacheException gwce) {
            // superclass will handle this case as well
        }

        if (requestComps == null || requestComps[0].equalsIgnoreCase(TYPE_HOME)
                || requestComps[0].equalsIgnoreCase(TYPE_DEMO) || requestComps[0].equalsIgnoreCase(TYPE_DEMO + "s")) {
            handleDemoRequest(requestComps, request, response);
        } else {
            return super.handleRequestInternal(request, response);
        }

        return null;
    }

    private void handleDemoRequest(String[] requestComps, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final HttpServletResponse httpServletResponse = new HttpServletResponseWrapper(response) {
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
                Demo.makeMap(this.tileLayerDispatcher, this.gridSetBroker, requestComps[1], request,
                        httpServletResponse);
            }
        } finally {
            out.close();
        }

        final String html = out.toString("UTF-8");

        final String bodyTag = "<body>";
        final String htmlBody = html.substring(html.indexOf(bodyTag) + bodyTag.length());

        StringBuilder builder = new StringBuilder(newGeorHeaderInclude);

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
