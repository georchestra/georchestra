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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import lombok.NonNull;

public class DefaultGeoNetworkClient implements GeoNetworkClient {

	@Override
	public GeoNetworkResponse putXmlRecord(@NonNull String url, @NonNull HttpHeaders additionalRequestHeaders,
			@NonNull String xmlRecord) {

		HttpEntity<String> request = createRequestEntity(xmlRecord, additionalRequestHeaders);

		RestTemplate template = new RestTemplate();
		RequestCallback requestCallback = template.httpEntityCallback(request);

		ResponseExtractor<GeoNetworkResponse> responseExtractor = r -> {
			GeoNetworkResponse gnr = new GeoNetworkResponse();
			gnr.setStatus(r.getStatusCode());
			gnr.setStatusText(r.getStatusText());
			gnr.setHeaders(r.getHeaders());
			return gnr;
		};

		GeoNetworkResponse response;
		try {
			ResponseEntity<String> exchange = template.exchange(url, HttpMethod.PUT, request, String.class);
			response = template.execute(url, HttpMethod.PUT, requestCallback, responseExtractor);
		} catch (HttpClientErrorException ex) {
			response = new GeoNetworkResponse();
			response.setStatus(ex.getStatusCode());
			response.setStatusText(ex.getStatusText());
			response.setHeaders(ex.getResponseHeaders());
			response.setStatusText(ex.getStatusText());
			String responseBodyAsString = ex.getResponseBodyAsString();
			response.setErrorResponseBody(responseBodyAsString);
		}
		return response;
	}

	private HttpEntity<String> createRequestEntity(@NonNull String xmlRecord,
			@NonNull HttpHeaders additionalRequestHeaders) {

		HttpHeaders headers = new HttpHeaders(additionalRequestHeaders);
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/xml");
		headers.set("user-agent", "Mozilla/5.0 Firefox/26.0");
		
		return new HttpEntity<String>(xmlRecord, headers);
	}

}
