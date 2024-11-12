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
package org.georchestra.datafeeder.api;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.georchestra.datafeeder.api.mapper.DataPublishingResponseMapper;
import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.config.PostgisSchemasConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.DataPublishingService;
import org.georchestra.datafeeder.service.DataUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;

@Controller
@RolesAllowed({ "ROLE_USER", "ROLE_ADMINISTRATOR" })
@Api(tags = { "Data Publishing" }) // hides the empty data-publishing-api-controller entry in swagger-ui.html
public class DataPublishingApiController implements DataPublishingApi {

    private @Autowired AuthorizationService authorizationService;
    private @Autowired DataPublishingService dataPublishingService;
    private @Autowired DataUploadService uploadService;
    private @Autowired DataPublishingResponseMapper mapper;
    private @Autowired DataFeederConfigurationProperties props;
    private @Autowired GeorchestraNameNormalizer nameResolver;
    private @Autowired(required = false) PostgisSchemasConfiguration postgisSchemasConfiguration;

    @Override
    public ResponseEntity<PublishJobStatus> getPublishingStatus(@PathVariable("jobId") UUID jobId) {
        authorizationService.checkAccessRights(jobId);

        PublishJobStatus status = mapper.toApi(getOrNotFound(jobId));
        status.add(linkTo(methodOn(DataPublishingApiController.class).getPublishingStatus(jobId)).withSelfRel());

        status.getDatasets().forEach(this::addLinks);
        return ResponseEntity.ok().body(status);
    }

    private void addLinks(DatasetPublishingStatus dataset) {

        String gsUrl = props.getPublishing().getGeoserver().getPublicUrl().toString();
        String gnUrl = props.getPublishing().getGeonetwork().getPublicUrl().toString();
        String ogcApiUrl = props.getPublishing().getOgcfeatures().getPublicUrl() == null ? null
                : props.getPublishing().getOgcfeatures().getPublicUrl().toString();

        if (dataset.getStatus() == PublishStatusEnum.DONE) {
            {// WMS entry point
                String wmsUrl = String.format("%s/%s/wms?", gsUrl, dataset.getPublishedWorkspace());
                wmsUrl = URI.create(wmsUrl).normalize().toString();
                dataset.add(Link.of(wmsUrl, IanaLinkRelations.SERVICE).withName("WMS")
                        .withTitle("Web Map Service entry point where the layer is published"));
            }
            {// WFS entry point
                String wfsUrl = String.format("%s/%s/wfs?", gsUrl, dataset.getPublishedWorkspace());
                wfsUrl = URI.create(wfsUrl).normalize().toString();
                dataset.add(Link.of(wfsUrl, IanaLinkRelations.SERVICE).withName("WFS")
                        .withTitle("Web Feature Service entry point where the layer is published"));
            }
            {// OpenLayers map preview TODO don't set it if non geo dataset
                String layerPreviewUrl = String.format(
                        "%s/%s/wms/reflect?LAYERS=%s&width=800&format=application/openlayers", gsUrl,
                        dataset.getPublishedWorkspace(), dataset.getPublishedName());
                layerPreviewUrl = URI.create(layerPreviewUrl).normalize().toString();
                dataset.add(Link.of(layerPreviewUrl, IanaLinkRelations.PREVIEW).withName("openlayers")
                        .withType("application/openlayers")
                        .withTitle("OpenLayers preview page for the layer published in GeoServer"));
            }
            {// XML Metadata record
             // e.g.
             // http://localhost:28080/geonetwork/srv/api/0.1/records/3276d285-f458-4a38-8040-1b14ea5afc9e/formatters/xml
                String xmlMdRecord = String.format("%s/srv/api/0.1/records/%s/formatters/xml", gnUrl,
                        dataset.getMetadataRecordId());
                xmlMdRecord = URI.create(xmlMdRecord).normalize().toString();
                dataset.add(Link.of(xmlMdRecord, IanaLinkRelations.DESCRIBED_BY).withName("metadata")
                        .withType("application/xml").withTitle("Metadata record XML representation"));
            }
            {// HTML metadata page
             // e.g.
             // http://localhost:28080/geonetwork/srv/eng/catalog.search#/metadata/3276d285-f458-4a38-8040-1b14ea5afc9e
                String htmlMdRecord = String.format("%s/srv/eng/catalog.search#/metadata/%s", gnUrl,
                        dataset.getMetadataRecordId());
                htmlMdRecord = URI.create(htmlMdRecord).normalize().toString();
                dataset.add(Link.of(htmlMdRecord, IanaLinkRelations.DESCRIBED_BY).withName("metadata")
                        .withType("text/html").withTitle("Metadata record web page"));
            }
            if (!StringUtils.isEmpty(ogcApiUrl)) {// OGC API features page TODO update link relation
                                                  // in app.yml
                                                  // e.g.
                                                  // http://localhost/data/ogcapi/collections/3276d285-f458-4a38-8040-1b14ea5afc9e/items
                String schema = nameResolver
                        .resolveDatabaseSchemaName(authorizationService.getUserInfo().getOrganization().getShortName());
                String ogcApiFeature;
                if (postgisSchemasConfiguration != null) {
                    ogcApiFeature = String.format("%s/collections/%s/items", ogcApiUrl,
                            postgisSchemasConfiguration.prefix(schema) + dataset.getPublishedName());
                } else {
                    ogcApiFeature = String.format("%s/collections/%s/items", ogcApiUrl, dataset.getPublishedName());
                }
                ogcApiFeature = URI.create(ogcApiFeature).normalize().toString();
                dataset.add(Link.of(ogcApiFeature, IanaLinkRelations.HOSTS).withName("features")
                        .withType("application/json").withTitle("OGC API Feature JSON web page"));
            }
        }
    }

    private DataUploadJob getOrNotFound(UUID jobId) {
        DataUploadJob upload = this.uploadService.findJob(jobId)
                .orElseThrow(() -> ApiException.notFound("upload %s does not exist", jobId));
        return upload;
    }

    @Override
    public ResponseEntity<PublishJobStatus> publish(@PathVariable("jobId") UUID jobId,
            @RequestBody(required = false) PublishRequest publishRequest) {

        authorizationService.checkAccessRights(jobId);

        UserInfo user = authorizationService.getUserInfo();
        // launch the async job
        dataPublishingService.publish(jobId, publishRequest, user);

        DataUploadJob upload = getOrNotFound(jobId);
        PublishJobStatus status = mapper.toApi(upload);
        status.add(linkTo(methodOn(DataPublishingApiController.class).getPublishingStatus(jobId)).withSelfRel());

        return ResponseEntity.accepted().body(status);
    }

}
