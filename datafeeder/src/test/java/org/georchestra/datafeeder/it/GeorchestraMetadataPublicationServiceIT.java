/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.it;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.Envelope;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraMetadataPublicationService;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = { //
        DataFeederApplicationConfiguration.class, //
        IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
@Slf4j
public class GeorchestraMetadataPublicationServiceIT {

    public @Autowired @Rule IntegrationTestSupport support;

    private @Autowired GeorchestraMetadataPublicationService service;
    private @Autowired GeoNetworkRemoteService geonetwork;

    private @Autowired DataFeederConfigurationProperties configProperties;

    private DatasetUploadState shpDataset;

    private static final String NATIVE_LAYERNAME = "public_layer";
    private static final String PULISHED_LAYERNAME = "PublicLayer";

    public @Before void setup() {
        shpDataset = buildShapefileDatasetFromDefaultGeorchestraDataDirectory();

        // replace the configured geoserver datastore connection parameters by a
        // "directory of shapefiles" set of parameters
        Map<String, String> params = configProperties.getPublishing().getBackend().getGeoserver();
        params.clear();
        params.put(ShapefileDataStoreFactory.FILE_TYPE.key, "shapefile");
        params.put(ShapefileDataStoreFactory.URLP.key, "file:data/automated_tests");
    }

    private DatasetUploadState buildShapefileDatasetFromDefaultGeorchestraDataDirectory() {
        DataUploadJob job = new DataUploadJob();

        DatasetUploadState dset = new DatasetUploadState();
        dset.setJob(job);
        dset.setName(NATIVE_LAYERNAME);

        dset.setNativeBounds(new BoundingBoxMetadata());
        dset.getNativeBounds().setCrs(new CoordinateReferenceSystemMetadata());
        dset.getNativeBounds().getCrs().setSrs("EPSG:4326");
        dset.getNativeBounds().setMinx(-86d);
        dset.getNativeBounds().setMaxx(77d);
        dset.getNativeBounds().setMiny(-17d);
        dset.getNativeBounds().setMaxy(51d);

        PublishSettings publishing = new PublishSettings();
        dset.setPublishing(publishing);
        publishing.setPublishedName(PULISHED_LAYERNAME);
        publishing.setKeywords(Arrays.asList("tag1", "tag 2"));
        publishing.setSrs("EPSG:4326");
        return dset;
    }

    @Test
    public void testPublishSingleShapefile() {

        PublishSettings publishing = shpDataset.getPublishing();
        publishing.setMetadataRecordId(null);
        publishing.setTitle("Test Title");
        publishing.setAbstract("Test abstract");
        final LocalDate datasetCreationDate = LocalDate.now();
        publishing.setDatasetCreationDate(datasetCreationDate);
        publishing.setDatasetCreationProcessDescription("Test process description");
        publishing.setEncoding("ISO-8859-1");
        publishing.setScale(50_000);

        Envelope bounds = new Envelope();
        bounds.setMinx(-180d);
        bounds.setMaxx(180d);
        bounds.setMiny(-90d);
        bounds.setMaxy(90d);
        publishing.setGeographicBoundingBox(bounds);

        publishing.setKeywords(Arrays.asList("keyword 1", "key2"));
        publishing.setPublishedName(PULISHED_LAYERNAME);
        publishing.setSrs("EPSG:4326");

        service.publish(shpDataset, support.user());

        final String createdMdId = publishing.getMetadataRecordId();
        assertNotNull(createdMdId);

        final String publishedRecord = geonetwork.getRecordById(createdMdId);
        assertNotNull(publishedRecord);
        log.info("published record returned from GN: {}", publishedRecord);

        Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(publishedRecord)));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Published record not returned as valid XML");
            return;
        }

        // metadata uuid, computed
        assertXpath(dom, "MD_Metadata/fileIdentifier/CharacterString[text() = '%s']", createdMdId);
        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/title/CharacterString[text() = 'Test Title']");
        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/abstract/CharacterString[text() = 'Test abstract']");

        String keyword = "MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords/MD_Keywords/keyword/CharacterString[text()='%s']";
        for (String kw : publishing.getKeywords()) {
            assertXpath(dom, keyword, kw);
        }

        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date/CI_Date/date/Date[text() = '%s']",
                datasetCreationDate);
        assertXpath(dom,
                "MD_Metadata/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/statement/CharacterString[text() = 'Test process description']");

        assertXpath(dom, "MD_Metadata/hierarchyLevel/MD_ScopeCode[@codeListValue='series']");

        final String title = publishing.getTitle();
        assertOnlineResource(dom, "OGC:WMS", PULISHED_LAYERNAME, title + " - WMS");
        assertOnlineResource(dom, "OGC:WFS", PULISHED_LAYERNAME, title + " - WFS");
        assertOnlineResource(dom, "WWW:DOWNLOAD-1.0-http--download", PULISHED_LAYERNAME, title + " - WWW");

        URL publicUrl = configProperties.getPublishing().getGeonetwork().getPublicUrl();
        final String uniqueResourceIdentifier = URI.create(publicUrl + "?uuid=" + createdMdId).normalize().toString();

        assertXpath(dom,
                "//MD_DataIdentification/citation/CI_Citation/identifier/MD_Identifier/code/CharacterString[text()='%s']",
                uniqueResourceIdentifier);

        // TODO:
        // /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString

        // TODO (file format encoding (not char encoding !)): ? to be decided
        // /gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format

        // charset encoding, User input, computed from input file (UTF-8 if not found),
        // Note ISO-8859-1 shall be translated to 8859part1
        assertXpath(dom, "MD_Metadata/characterSet/MD_CharacterSetCode[@codeListValue='8859part1']");

        // spatial representation, provided by metadata template
        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/spatialRepresentationType/MD_SpatialRepresentationTypeCode[@codeListValue='vector']");

        // topic category, ? to be decided
        // /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode

        // data extent, computed
        String bbox = "MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement/EX_GeographicBoundingBox/%s/Decimal[text()='%s']";
        assertXpath(dom, bbox, "westBoundLongitude", "-180.0");
        assertXpath(dom, bbox, "eastBoundLongitude", "180.0");
        assertXpath(dom, bbox, "southBoundLatitude", "-90.0");
        assertXpath(dom, bbox, "northBoundLatitude", "90.0");

        // coordinate reference system, User input|computed from input file
        // REVISIT: shouldn't it be <gmx:Anchor
        // xlink:href="http://www.opengis.net/def/crs/EPSG/0/4258">EPSG:4258</gmx:Anchor>
        String crs = "MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier/code/CharacterString[text()='%s']";
        assertXpath(dom, crs, "EPSG:4326");

        final String dataIdentXpath = "MD_Metadata/identificationInfo/MD_DataIdentification";
        // metadata publication date, computed from datafeeder form submit time|now()
        final LocalDate today = LocalDate.now();
        final String dateBase = dataIdentXpath + "/citation/CI_Citation";
        assertXpath(dom, dateBase + "/date[1]/CI_Date/date/Date[text()='%s']", today);
        assertXpath(dom, dateBase + "/date[1]/CI_Date/dateType/CI_DateTypeCode[@codeListValue='creation']");
        assertXpath(dom, dateBase + "/date[2]/CI_Date/date/Date[text()='%s']", today);
        assertXpath(dom, dateBase + "/date[2]/CI_Date/dateType/CI_DateTypeCode[@codeListValue='publication']");

        // spatial resolution, User input, default 1 : 25000
        String spatialRes = dataIdentXpath
                + "/spatialResolution/MD_Resolution/equivalentScale/MD_RepresentativeFraction/denominator/Integer[text()='%s']";
        assertXpath(dom, spatialRes, publishing.getScale());

        // use limitation, user input, default: CONFIG (typically "ODBL")
        // REVISIT assertXpath(dom,
        // dataIdentXpath+"/resourceConstraints/MD_LegalConstraints/useLimitation");

        // access constraints, provided by metadata template,
        // codeListValue="otherRestrictions"
        String legalXpath = dataIdentXpath + "/resourceConstraints/MD_LegalConstraints";
        assertXpath(dom, legalXpath + "/accessConstraints/MD_RestrictionCode[@codeListValue='otherRestrictions']");

        // use constraints, provided by metadata template, codeListValue="license"
        assertXpath(dom, legalXpath + "/useConstraints/MD_RestrictionCode[@codeListValue='license']");

        // metadata timestamp, computed, now()::ISO8601 (REVISIT?)
        assertXpath(dom, "MD_Metadata/dateStamp/DateTime");

        // metadata language, provided by metadata template, typically "eng" or "fre"
        assertXpath(dom, "MD_Metadata/language/LanguageCode[@codeListValue='eng']");

        // graphic overview, provided by metadata template for the first iteration
        assertXpath(dom, dataIdentXpath + "/graphicOverview");

        // update frequency, provided by metadata template, codeListValue="asNeeded"
        assertXpath(dom, dataIdentXpath
                + "/resourceMaintenance/MD_MaintenanceInformation/maintenanceAndUpdateFrequency/MD_MaintenanceFrequencyCode[@codeListValue='asNeeded']");

        assertResponsibleParty(dom);
        assertPointOfContact(dom);
    }

    // /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact
    // computed from LDAP / sec-* headers
    // individualName = sec-firstname + ' ' + sec-lastname
    // organisationName = sec-orgname
    // adress = postalAddress from LDAP (to be discussed)
    // email = sec-email
    // linkage = labeledURI from LDAP
    // protocol = URL
    // name = sec-orgname
    // as in
    // https://geobretagne.fr/geonetwork/srv/api/records/633f2882-2a90-4f98-9739-472a72d31b64/formatters/xml
    private void assertResponsibleParty(Document dom) {
        final String rp = "MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty";
        UserInfo user = support.user();
        String individualName = user.getFirstName() + " " + user.getLastName();
        String organizationName = user.getOrganization().getName();
        String email = user.getEmail();
        String address = user.getOrganization().getPostalAddress();
        assertPointOfContact(dom, rp, individualName, organizationName, email, address);
    }

    // /gmd:MD_Metadata/gmd:contact
    // computed from LDAP / sec-* headers
    // individualName = sec-firstname + ' ' + sec-lastname
    // organisationName = sec-orgname
    // adress = postalAddress from LDAP (to be discussed)
    // email = sec-email
    private void assertPointOfContact(Document dom) {
        final String poc = "MD_Metadata/contact/CI_ResponsibleParty";
        UserInfo user = support.user();
        String individualName = user.getFirstName() + " " + user.getLastName();
        String organizationName = user.getOrganization().getName();
        String email = user.getEmail();
        String postalAddress = user.getPostalAddress();

        assertPointOfContact(dom, poc, individualName, organizationName, email, postalAddress);
    }

    private void assertPointOfContact(Document dom, String CI_ResponsiblePartyXPath, String individualName,
            String organizationName, String email, String address) {
        final String base = CI_ResponsiblePartyXPath;

        assertXpath(dom, base + "/individualName/CharacterString[text()='%s']", individualName);
        assertXpath(dom, base + "/organisationName/CharacterString[text()='%s']", organizationName);

        String addrXpath = base + "/contactInfo/CI_Contact/address/CI_Address/%s/CharacterString[text()='%s']";
        assertXpath(dom, addrXpath, "electronicMailAddress", email);
        assertXpath(dom, addrXpath, "deliveryPoint", address);
    }

    private void assertOnlineResource(Document dom, String protocol, String name, String description) {
        final String onlineResource = "MD_Metadata/distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource/%s/CharacterString[text() = '%s']";

        assertXpath(dom, onlineResource, "protocol", protocol);
        assertXpath(dom, onlineResource, "name", name);
        assertXpath(dom, onlineResource, "description", description);
    }

    private void assertXpath(Document dom, String xpath, Object... args) {
        String finalXpath = format(xpath, args);
        assertThat(dom, hasXPath(finalXpath));
    }

}
