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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * This class is responsible to maintain the metadata values in a file.
 *
 * @author Mauricio Pazos
 *
 */
final class MetadataEntity {

    protected static final Log LOG = LogFactory.getLog(MetadataEntity.class.getPackage().getName());

    private CSWRequest request;

    /**
     * a new instance of {@link MetadataEntity}.
     *
     * @param cswRequest where the metadata is available
     */
    private MetadataEntity(CSWRequest cswRequest) {

        this.request = cswRequest;
    }

    /**
     * Crates a new instance of {@link MetadataEntity}. Its values will be retrieved
     * from the Catalog service specified in the request parameter.
     *
     * @param cswRequest where the metadata is available
     */
    public static MetadataEntity create(final CSWRequest cswRequest) {

        return new MetadataEntity(cswRequest);

    }

    /**
     * Stores the metadata retrieved from CSW using the request value.
     *
     * @param fileName file name where the metadata must be saved.
     *
     * @throws IOException
     */
    public void save(final String fileName) throws IOException {

        InputStream content = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");

            HttpGet get = new HttpGet(this.request.buildURI());
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpClientContext localContext = HttpClientContext.create();

            // if credentials are actually provided, use them to configure
            // the HttpClient object.
            try {
                if (this.request.getUser() != null && request.getPassword() != null) {
                    Credentials credentials = new UsernamePasswordCredentials(request.getUser(), request.getPassword());
                    AuthScope authScope = new AuthScope(get.getURI().getHost(), get.getURI().getPort());
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(authScope, credentials);
                    localContext.setCredentialsProvider(credentialsProvider);
                }
            } catch (Exception e) {
                LOG.error("Unable to set basic-auth on http client to get the Metadata remotely, trying without ...",
                        e);
            }
            content = httpclient.execute(get, localContext).getEntity().getContent();
            reader = new BufferedReader(new InputStreamReader(content));

            String line = reader.readLine();
            while (line != null) {

                writer.println(line);

                line = reader.readLine();
            }

        } catch (Exception e) {

            final String msg = "The metadata could not be extracted";
            LOG.error(msg, e);

            throw new IOException(e);

        } finally {

            if (writer != null)
                writer.close();

            if (reader != null)
                reader.close();

            if (content != null)
                content.close();
        }
    }

}
