package org.geowebcache;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.config.Configuration;
import org.geowebcache.demo.Demo;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.storage.StorageBroker;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author Jesse on 4/25/2014.
 */
public class GeorchestraGeoWebCacheDispatcher extends GeoWebCacheDispatcher {
    private static Log log = LogFactory.getLog(GeorchestraGeoWebCacheDispatcher.class);

    private final RuntimeStats runtimeStats;
    private final String header;
    private final TileLayerDispatcher tileLayerDispatcher;
    private final GridSetBroker gridSetBroker;

    /**
     * Should be invoked through Spring.
     *
     * @param tileLayerDispatcher
     * @param gridSetBroker
     * @param storageBroker
     * @param mainConfiguration
     * @param runtimeStats
     */
    public GeorchestraGeoWebCacheDispatcher(
            TileLayerDispatcher tileLayerDispatcher,
            GridSetBroker gridSetBroker,
            StorageBroker storageBroker,
            Configuration mainConfiguration,
            RuntimeStats runtimeStats) throws IOException {
        super(tileLayerDispatcher, gridSetBroker, storageBroker, mainConfiguration, runtimeStats);
        this.tileLayerDispatcher = tileLayerDispatcher;
        this.gridSetBroker = gridSetBroker;
        this.runtimeStats = runtimeStats;
        this.header = IOUtils.toString(GeorchestraGeoWebCacheDispatcher.class.getResourceAsStream("/georchestraHeader.html"));
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Break the request into components, {type, service name}
        String[] requestComps = null;
        try {
            String normalizedURI = request.getRequestURI().replaceFirst(request.getContextPath(),
                    "");

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
            || requestComps[0].equalsIgnoreCase(TYPE_DEMO)
            || requestComps[0].equalsIgnoreCase(TYPE_DEMO + "s")) {
            handleDemoRequest(requestComps, request, response);
        } else {
            return super.handleRequestInternal(request, response);
        }

        return null;
    }


    private void handleDemoRequest(String[] requestComps, HttpServletRequest request,
                                   HttpServletResponse response) throws Exception {
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
                    };
                }
            };
            if (requestComps == null || requestComps[0].equalsIgnoreCase(TYPE_HOME)) {
                super.handleRequestInternal(request, httpServletResponse);
            } else {
                Demo.makeMap(this.tileLayerDispatcher, this.gridSetBroker, requestComps[1], request, httpServletResponse);
            }
        } finally {
            out.close();
        }

        final String html = out.toString("UTF-8");

        final String bodyTag = "<body>";
        final String htmlBody = html.substring(html.indexOf(bodyTag) + bodyTag.length());
        String username = "";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        }

        StringBuilder builder = new StringBuilder(this.header.replaceAll("@@username@@", username));
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
