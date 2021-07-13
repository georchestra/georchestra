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
package org.georchestra.datafeeder.service.publish.impl;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilderFactory;

import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.Address;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.OnlineResource;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemplateMapperTest {

    private TemplateMapper mapper;

    public @Before void before() {
        mapper = new TemplateMapper();
    }

    @Test
    public void testApplyTemplate() {
        final MetadataRecordProperties mdprops = new MetadataPropertiesTestSupport().createTestProps();

        Supplier<String> recordSupplier = mapper.apply(mdprops);
        assertNotNull(recordSupplier);
        String record = recordSupplier.get();
        assertNotNull(record);
        log.info(record);
        Document dom = parse(record);

        // metadata uuid, computed
        assertXpath(dom, "MD_Metadata/fileIdentifier/CharacterString[text() = '%s']", mdprops.getMetadataId());

        assertDataIdentification(dom, mdprops);

        assertMetadataPointOfContact(dom, mdprops);

        // lineage, User input
        assertXpath(dom,
                "MD_Metadata/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/statement/CharacterString[text() = '%s']",
                mdprops.getLineage());

        // resource type, provided by metadata template
        assertXpath(dom, "MD_Metadata/hierarchyLevel/MD_ScopeCode[@codeListValue='series']");

        // online resource, computed
        for (OnlineResource ol : mdprops.getOnlineResources()) {
            assertOnlineResource(dom, ol.getLinkage(), ol.getProtocol(), ol.getName(), ol.getDescription());
        }

        // TODO (file format encoding (not char encoding !)): ? to be decided
        // /gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format

        // charset encoding, User input, computed from input file (UTF-8 if not found),
        // Note ISO-8859-1 shall be translated to 8859part1
        assertXpath(dom, "MD_Metadata/characterSet/MD_CharacterSetCode[@codeListValue='%s']",
                mdprops.getCharsetEncoding());

        // coordinate reference system, User input|computed from input file
        // REVISIT: shouldn't it be <gmx:Anchor
        // xlink:href="http://www.opengis.net/def/crs/EPSG/0/4258">EPSG:4258</gmx:Anchor>
        String crs = "MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier/code/CharacterString[text()='%s']";
        assertXpath(dom, crs, mdprops.getCoordinateReferenceSystem());

        // metadata timestamp, computed, now()::ISO8601
        // note: using starts-with to avoid false failures due to trailing zero in
        // mdprops.getMetadataTimestamp() (e.g. text() can be '2021-05-03T16:49:48.850'
        // and mdprops.getMetadataTimestamp() can be '2021-05-03T16:49:48.85')
        assertXpath(dom, "MD_Metadata/dateStamp/DateTime[starts-with(text(), '%s')]", mdprops.getMetadataTimestamp());

        // metadata language, provided by metadata template, typically "eng" or "fre"
        String lang = mdprops.getMetadataLanguage();
        assertXpath(dom, "MD_Metadata/language/LanguageCode[@codeListValue='%s']", lang);

    }

    private Document parse(String record) {
        Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(record)));
        } catch (Exception e) {
            throw new IllegalStateException("Published record not returned as valid XML", e);
        }
        return dom;
    }

    private void assertDataIdentification(Document dom, MetadataRecordProperties mdprops) {
        // title, User input|computed from input file : uploaded file name without file
        // extension
        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/title/CharacterString[text() = '%s']",
                mdprops.getTitle());
        // description, User input
        assertXpath(dom, "MD_Metadata/identificationInfo/MD_DataIdentification/abstract/CharacterString[text() = '%s']",
                mdprops.getAbstract());

        // keywords, User input restricted to the "GEMET – INSPIRE themes" thesaurus
        // entries.
        String keyword = "MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords/MD_Keywords/keyword/CharacterString[text()='%s']";
        for (String kw : mdprops.getKeywords()) {
            assertXpath(dom, keyword, kw);
        }

        // data creation date, User input
        final String citation = "MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation";

        LocalDate creationDate = mdprops.getCreationDate();
        assertXpath(dom, citation + "/date[1]/CI_Date/date/Date[text()='%s']", creationDate);
        assertXpath(dom, citation + "/date[1]/CI_Date/dateType/CI_DateTypeCode[@codeListValue='creation']");

//		assertXpath(dom,
//				"MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date/CI_Date/date/Date[text() = '%s']",
//				creationDate);

        // metadata publication date, computed from datafeeder form submit time|now()
        LocalDate publicationDate = mdprops.getMetadataPublicationDate();
        assertXpath(dom, citation + "/date[2]/CI_Date/date/Date[text()='%s']", publicationDate);
        assertXpath(dom, citation + "/date[2]/CI_Date/dateType/CI_DateTypeCode[@codeListValue='publication']");

        // unique resource identifier, computed
        final String uniqueResourceIdentifier = mdprops.getDataIdentifier().toString();
        assertXpath(dom, citation + "/identifier/MD_Identifier/code/CharacterString[text() = '%s']",
                uniqueResourceIdentifier);

        // dataset language, User input, restricted to a subset of iso639-2 (from
        // CONFIG)
        // TODO:
        // /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString

        // topic category: see testInspireTopicCategoriesAddedFromKeywords()

        // data extent, computed
        String bbox = "MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement/EX_GeographicBoundingBox/%s/Decimal[text()='%s']";
        assertXpath(dom, bbox, "westBoundLongitude", mdprops.getWestBoundLongitude());
        assertXpath(dom, bbox, "eastBoundLongitude", mdprops.getEastBoundLongitude());
        assertXpath(dom, bbox, "southBoundLatitude", mdprops.getSouthBoundLatitude());
        assertXpath(dom, bbox, "northBoundLatitude", mdprops.getNorthBoundLatitude());

        // spatial representation, provided by metadata template
        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/spatialRepresentationType/MD_SpatialRepresentationTypeCode[@codeListValue='%s']",
                mdprops.getSpatialRepresentation());

        // spatial resolution, User input, default 1 : 25000
        String spatialRes = "MD_Metadata/identificationInfo/MD_DataIdentification/spatialResolution/MD_Resolution/equivalentScale/MD_RepresentativeFraction/denominator/Integer[text()='%s']";
        assertXpath(dom, spatialRes, mdprops.getSpatialResolution());

        // access constraints, provided by metadata template,
        // codeListValue="otherRestrictions"
        String legalXpath = "MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/MD_LegalConstraints";
        assertXpath(dom, legalXpath + "/accessConstraints/MD_RestrictionCode[@codeListValue='otherRestrictions']");

        // use limitation, user input, default: CONFIG (typically "ODBL")
        // assertXpath(dom,
        // dataIdentXpath+"/resourceConstraints/MD_LegalConstraints/useLimitation");

        // use constraints, provided by metadata template, codeListValue="license"
        assertXpath(dom, legalXpath + "/useConstraints/MD_RestrictionCode[@codeListValue='license']");

        // graphic overview, provided by metadata template for the first iteration
        assertXpath(dom, "MD_Metadata/identificationInfo/MD_DataIdentification/graphicOverview");

        // update frequency, provided by metadata template, codeListValue="asNeeded"
        assertXpath(dom,
                "MD_Metadata/identificationInfo/MD_DataIdentification/resourceMaintenance/MD_MaintenanceInformation/maintenanceAndUpdateFrequency/MD_MaintenanceFrequencyCode[@codeListValue='asNeeded']");
        assertResponsibleParty(dom, mdprops);
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
    private void assertResponsibleParty(Document dom, MetadataRecordProperties mdprops) {
        final String rp = "MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty";
        assertPointOfContact(dom, rp, mdprops.getDatasetResponsibleParty());
    }

    // /gmd:MD_Metadata/gmd:contact
    // computed from LDAP / sec-* headers
    // individualName = sec-firstname + ' ' + sec-lastname
    // organisationName = sec-orgname
    // adress = postalAddress from LDAP (to be discussed)
    // email = sec-email
    private void assertMetadataPointOfContact(Document dom, MetadataRecordProperties mdprops) {
        final String poc = "MD_Metadata/contact/CI_ResponsibleParty";
        assertPointOfContact(dom, poc, mdprops.getMetadataResponsibleParty());
    }

    private void assertPointOfContact(Document dom, String CI_ResponsiblePartyXPath,
            MetadataRecordProperties.ContactInfo c) {
        final String base = CI_ResponsiblePartyXPath;

        assertXpath(dom, base + "/individualName/CharacterString[text()='%s']", c.getIndividualName());
        assertXpath(dom, base + "/organisationName/CharacterString[text()='%s']", c.getOrganizationName());

        Address addr = c.getAddress();
        String addrXpath = base + "/contactInfo/CI_Contact/address/CI_Address/%s/CharacterString[text()='%s']";
        assertXpath(dom, addrXpath, "deliveryPoint", addr.getDeliveryPoint());
        assertXpath(dom, addrXpath, "city", addr.getCity());
        assertXpath(dom, addrXpath, "postalCode", addr.getPostalCode());
        assertXpath(dom, addrXpath, "electronicMailAddress", c.getEmail());
    }

    private void assertOnlineResource(Document dom, URI linkage, String protocol, String name, String description) {
        final String ci_olr = "MD_Metadata/distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource";
        final String link = ci_olr + "/linkage/URL[text() = '%s']";
        final String onlineResource = ci_olr + "/%s/CharacterString[text() = '%s']";

        assertXpath(dom, link, linkage);
        assertXpath(dom, onlineResource, "protocol", protocol);
        assertXpath(dom, onlineResource, "name", name);
        assertXpath(dom, onlineResource, "description", description);
    }

    private void assertXpath(Document dom, String xpath, Object... args) {
        String finalXpath = format(xpath, args);
        assertThat(dom, hasXPath(finalXpath));
    }

    @Test
    public void testInspireTopicCategoriesAddedFromKeywords() {
        final MetadataRecordProperties mdprops = new MetadataPropertiesTestSupport().createTestProps();
        // For topic category mapping, "Land use" should result in a "planningCadastre"
        // topic category code, and "Geographical names" in a "location" topic category
        mdprops.setKeywords(
                Arrays.asList("Land use", "Geographical names", "Keyword with no matching topic category code"));

        String record = mapper.apply(mdprops).get();
        log.info(record);
        Document dom = parse(record);

        final String xpath = "MD_Metadata/identificationInfo/MD_DataIdentification/topicCategory/"
                + "MD_TopicCategoryCode[text() = '%s']";

        assertXpath(dom, xpath, "planningCadastre");
        assertXpath(dom, xpath, "location");
    }
}
