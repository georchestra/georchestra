/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service.geonetwork;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import lombok.NonNull;

public interface GeoNetworkClient {

    void setApiUrl(URL apiUrl);

    void setBasicAuth(String username, String password);

    void setHeadersAuth(Map<String, String> authHeaders);

    void checkServiceAvailable() throws IOException;

    GeoNetworkResponse putXmlRecord(@NonNull String metadataId, @NonNull String xmlRecord, String groupName);

    String getXmlRecord(@NonNull String recordId);

}
