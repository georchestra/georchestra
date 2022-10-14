/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor.csw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.georchestra.extractorapp.ws.extractor.ExtractorLayerRequest;
import org.georchestra.extractorapp.ws.extractor.FileUtils;
import org.georchestra.extractorapp.ws.extractor.WfsExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Extracts the metadata document
 *
 * <p>
 * It is responsible of request the metadata associated to a layer the from
 * Catalog Service and saves it in the temporal directory which will be used to
 * build the zip file.
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
public class CSWExtractor {

    protected static final Log LOG = LogFactory.getLog(CSWExtractor.class.getPackage().getName());

    private File _basedir;
    private String _adminPassword;
    private String _secureHost;
    private String _adminUserName;
    private String userAgent;

    /**
     * CSWExtractor
     *
     * @param layerDirectory directory that contains the extracted layer
     * @param adminUserName
     * @param adminPassword
     * @param secureHost
     */
    public CSWExtractor(final File layerDirectory, final String adminUserName, final String adminPassword,
            final String secureHost, String userAgent) {
        this._basedir = layerDirectory;
        this._adminPassword = adminPassword;
        this._adminUserName = adminUserName;
        this._secureHost = secureHost;
        this.userAgent = userAgent;
    }

    /**
     * checks the permissions to access to the CSW
     *
     * @param request
     * @param username request user name
     * @param roles
     *
     * @throws IOException
     */
    public void checkPermission(ExtractorLayerRequest request, String username, String roles) throws IOException {

        InputStream content = null;
        boolean isMetadata = false;
        try {
            final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            httpClientBuilder.setUserAgent(this.userAgent);

            HttpClientContext localContext = HttpClientContext.create();
            final HttpHost httpHost = new HttpHost(request._isoMetadataURL.getHost(),
                    request._isoMetadataURL.getPort());

            HttpGet get = new HttpGet(request._isoMetadataURL.toURI());

            if (username != null && (_secureHost.equalsIgnoreCase(request._isoMetadataURL.getHost())
                    || "127.0.0.1".equalsIgnoreCase(request._isoMetadataURL.getHost())
                    || "localhost".equalsIgnoreCase(request._isoMetadataURL.getHost()))) {
                LOG.debug(getClass().getName()
                        + ".checkPermission - Secured Server: adding username header and role headers to request for checkPermission");
                WfsExtractor.addImpersonateUserHeaders(username, roles, get);

                WfsExtractor.enablePreemptiveBasicAuth(request._isoMetadataURL, httpClientBuilder, localContext,
                        httpHost, _adminUserName, _adminPassword);

            } else {
                LOG.debug("WcsExtractor.checkPermission - Non Secured Server");
            }

            // checks whether it is a metadata

            final CloseableHttpClient httpclient = httpClientBuilder.build();
            content = httpclient.execute(httpHost, get, localContext).getEntity().getContent();

            String metadata = FileUtils.asString(content);
            Pattern regex = Pattern.compile("<(gmd:)?MD_Metadata*");

            isMetadata = regex.matcher(metadata).find();

        } catch (Exception e) {

            throw new IOException(e);

        } finally {

            if (content != null)
                content.close();
        }

        if (!isMetadata) {
            throw new SecurityException("The metadata is not available: " + request._isoMetadataURL);
        }

    }

    /**
     * Requests to the CSW for a metadata the content will be stored in the temporl
     * dir as xml document.
     */
    public void extract(final URL metadataURL) throws IOException {
        assert metadataURL != null : metadataURL + "must be provided";

        CSWRequest cswRequest = new CSWRequest();
        cswRequest.setURL(metadataURL);
        cswRequest.setTimeout(Integer.valueOf(60000));

        if (_secureHost.equalsIgnoreCase(metadataURL.getHost()) || "127.0.0.1".equalsIgnoreCase(metadataURL.getHost())
                || "localhost".equalsIgnoreCase(metadataURL.getHost())) {
            LOG.debug("CswExtractor.extract - Secured Server: Adding extractionUserName to connection params");

            // to access the secure host it uses the administrator credential
            cswRequest.setUser(_adminUserName);
            cswRequest.setPassword(_adminPassword);
        } else {
            LOG.debug("Non Secured Server");
        }

        MetadataEntity metadata = MetadataEntity.create(cswRequest);

        metadata.save(this._basedir.getAbsolutePath() + File.separatorChar + "metadata.xml");

    }

}
