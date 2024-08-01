<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common" 
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0" 
                exclude-result-prefixes="#all">

<!-- 
Default template to apply MetadataRecordProperties.java properties to a record template adhering to http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
 -->

  <xsl:strip-space elements="*" xmlns="http://www.isotc211.org/2005/gmd" />
  <xsl:output indent="yes" standalone="yes" />

  <!-- Whole document used as xsl parameter, see sample_md_properties.xml for an example of its contents -->
  <xsl:param name="props" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:fileIdentifier/gco:CharacterString">
    <gco:CharacterString>
      <xsl:value-of select="$props//metadataId" />
    </gco:CharacterString>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue">
    <xsl:attribute name="codeListValue">
      <xsl:value-of select="$props//metadataLanguage" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="gmd:dateStamp">
    <gmd:dateStamp>
      <gco:DateTime>
        <xsl:value-of select="$props//metadataTimestamp" />
      </gco:DateTime>
    </gmd:dateStamp>
  </xsl:template>

  <xsl:template
    match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
    <gco:CharacterString>
      <xsl:value-of select="$props//title" />
    </gco:CharacterString>
  </xsl:template>

  <xsl:template match="//gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString">
    <gco:CharacterString>
      <xsl:value-of select="$props//abstract" />
    </gco:CharacterString>
  </xsl:template>

  <xsl:template match="//gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords">
    <gmd:MD_Keywords>
      <xsl:for-each select="$props//keywords//keyword">
        <gmd:keyword>
          <gco:CharacterString>
            <xsl:value-of select="." />
          </gco:CharacterString>
        </gmd:keyword>
      </xsl:for-each>
      <gmd:type>
        <gmd:MD_KeywordTypeCode
          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"
          codeListValue="theme" />
      </gmd:type>
      <gmd:thesaurusName>
        <gmd:CI_Citation>
          <gmd:title>
            <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>
          </gmd:title>
          <gmd:date>
            <gmd:CI_Date>
              <gmd:date>
                <gco:Date>2008-06-01</gco:Date>
              </gmd:date>
              <gmd:dateType>
                <gmd:CI_DateTypeCode
                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                  codeListValue="publication" />
              </gmd:dateType>
            </gmd:CI_Date>
          </gmd:date>
          <gmd:identifier>
            <gmd:MD_Identifier>
              <gmd:code>
                <gmx:Anchor
                  xlink:href="https://sdi.eea.europa.eu/catalogue/srv/api/registries/vocabularies/external.theme.httpinspireeceuropaeutheme-theme">geonetwork.thesaurus.external.theme.httpinspireeceuropaeutheme-theme</gmx:Anchor>
              </gmd:code>
            </gmd:MD_Identifier>
          </gmd:identifier>
        </gmd:CI_Citation>
      </gmd:thesaurusName>
    </gmd:MD_Keywords>
  </xsl:template>

  <xsl:template
    match="//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date[1]/gmd:CI_Date/gmd:date/gco:Date">
    <gco:Date>
      <xsl:value-of select="$props//creationDate" />
    </gco:Date>
  </xsl:template>
  <xsl:template
    match="//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date[2]/gmd:CI_Date/gmd:date/gco:Date">
    <gco:Date>
      <xsl:value-of select="$props//metadataPublicationDate" />
    </gco:Date>
  </xsl:template>

  <xsl:template
    match="//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
    <gco:CharacterString>
      <xsl:value-of select="$props//dataIdentifier" />
    </gco:CharacterString>
  </xsl:template>


  <xsl:template
    match="//gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
    <xsl:if test="$props//showExtent='true'">
    <gmd:EX_GeographicBoundingBox>
        <gmd:westBoundLongitude>
          <gco:Decimal>
            <xsl:value-of select="$props//westBoundLongitude" />
          </gco:Decimal>
        </gmd:westBoundLongitude>
        <gmd:eastBoundLongitude>
          <gco:Decimal>
            <xsl:value-of select="$props//eastBoundLongitude" />
          </gco:Decimal>
        </gmd:eastBoundLongitude>
        <gmd:southBoundLatitude>
          <gco:Decimal>
            <xsl:value-of select="$props//southBoundLatitude" />
          </gco:Decimal>
        </gmd:southBoundLatitude>
        <gmd:northBoundLatitude>
          <gco:Decimal>
            <xsl:value-of select="$props//northBoundLatitude" />
          </gco:Decimal>
        </gmd:northBoundLatitude>
      </gmd:EX_GeographicBoundingBox>
    </xsl:if>
  </xsl:template>

  <xsl:template
    match="//gmd:MD_DataIdentification/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue">
    <xsl:attribute name="codeListValue">
      <xsl:value-of select="$props//spatialRepresentation" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//gmd:MD_DataIdentification/gmd:spatialResolution">
    <gmd:spatialResolution>
      <gmd:MD_Resolution>
        <gmd:equivalentScale>
          <gmd:MD_RepresentativeFraction>
            <gmd:denominator>
              <gco:Integer>
                <xsl:value-of select="$props//spatialResolution" />
              </gco:Integer>
            </gmd:denominator>
          </gmd:MD_RepresentativeFraction>
        </gmd:equivalentScale>
      </gmd:MD_Resolution>
    </gmd:spatialResolution>
  </xsl:template>

  <xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact">
    <gmd:pointOfContact>
      <xsl:call-template name="contactInfo">
        <xsl:with-param name="individualName" select="$props//datasetResponsibleParty//individualName" />
        <xsl:with-param name="organizationName"
          select="$props//datasetResponsibleParty//organizationName" />
        <xsl:with-param name="deliveryPoint"
          select="$props//datasetResponsibleParty//address//deliveryPoint" />
        <xsl:with-param name="city" select="$props//datasetResponsibleParty//address//city" />
        <xsl:with-param name="postalCode" select="$props//datasetResponsibleParty//address//postalCode" />
        <xsl:with-param name="country" select="$props//datasetResponsibleParty//address//country" />
        <xsl:with-param name="email" select="$props//datasetResponsibleParty//email" />
        <xsl:with-param name="protocol" select="$props//datasetResponsibleParty//protocol" />
        <xsl:with-param name="linkage" select="$props//datasetResponsibleParty//linkage" />
      </xsl:call-template>
    </gmd:pointOfContact>
  </xsl:template>

  <xsl:template match="//gmd:contact">
    <gmd:contact>
      <xsl:call-template name="contactInfo">
        <xsl:with-param name="individualName" select="$props//metadataResponsibleParty//individualName" />
        <xsl:with-param name="organizationName"
          select="$props//metadataResponsibleParty//organizationName" />
        <xsl:with-param name="deliveryPoint"
          select="$props//metadataResponsibleParty//address//deliveryPoint" />
        <xsl:with-param name="city" select="$props//metadataResponsibleParty//address//city" />
        <xsl:with-param name="postalCode" select="$props//metadataResponsibleParty//address//postalCode" />
        <xsl:with-param name="country" select="$props//metadataResponsibleParty//address//country" />
        <xsl:with-param name="email" select="$props//metadataResponsibleParty//email" />
        <xsl:with-param name="protocol" select="$props//metadataResponsibleParty//protocol" />
        <xsl:with-param name="linkage" select="$props//metadataResponsibleParty//linkage" />
      </xsl:call-template>
    </gmd:contact>
  </xsl:template>

  <xsl:template
    match="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString">
    <gco:CharacterString>
      <xsl:value-of select="$props//lineage" />
    </gco:CharacterString>
  </xsl:template>

  <xsl:template match="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
    <xsl:attribute name="codeListValue">
      <xsl:value-of select="$props//resourceType" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template
    match="//gmd:distributionInfo//gmd:MD_Distribution//gmd:transferOptions//gmd:MD_DigitalTransferOptions">
    <gmd:MD_DigitalTransferOptions>
      <xsl:for-each select="$props//onlineResources//onlineResource">
        <gmd:onLine>
          <xsl:call-template name="onlineResource">
            <xsl:with-param name="linkage" select="linkage" />
            <xsl:with-param name="protocol" select="protocol" />
            <xsl:with-param name="name" select="name" />
            <xsl:with-param name="description" select="description" />
          </xsl:call-template>
        </gmd:onLine>
      </xsl:for-each>
    </gmd:MD_DigitalTransferOptions>
  </xsl:template>

  <xsl:template match="//gmd:characterSet//gmd:MD_CharacterSetCode/@codeListValue">
    <xsl:attribute name="codeListValue">
      <xsl:value-of select="$props//charsetEncoding" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template
    match="gmd:referenceSystemInfo//gmd:MD_ReferenceSystem//gmd:referenceSystemIdentifier//gmd:RS_Identifier/gmd:code">
    <gmd:code>
      <gco:CharacterString>
        <xsl:value-of select="$props//coordinateReferenceSystem" />
      </gco:CharacterString>
    </gmd:code>
  </xsl:template>
   

  <!-- Creates a CI_ResponsibleParty -->
  <xsl:template name="contactInfo">
    <xsl:param name="individualName" />
    <xsl:param name="organizationName" />
    <xsl:param name="deliveryPoint" />
    <xsl:param name="city" />
    <xsl:param name="postalCode" />
    <xsl:param name="country" />
    <xsl:param name="email" />
    <xsl:param name="protocol" />
    <xsl:param name="linkage" />
    <gmd:CI_ResponsibleParty>
      <gmd:individualName>
        <gco:CharacterString><xsl:value-of select="$individualName" /></gco:CharacterString>
      </gmd:individualName>
      <gmd:organisationName>
        <gco:CharacterString><xsl:value-of select="$organizationName" /></gco:CharacterString>
      </gmd:organisationName>
      <gmd:contactInfo>
        <gmd:CI_Contact>
          <gmd:address>
            <gmd:CI_Address>
              <gmd:deliveryPoint>
                <gco:CharacterString><xsl:value-of select="$deliveryPoint" /></gco:CharacterString>
              </gmd:deliveryPoint>
              <gmd:city>
                <gco:CharacterString><xsl:value-of select="$city" /></gco:CharacterString>
              </gmd:city>
              <gmd:postalCode>
                <gco:CharacterString><xsl:value-of select="$postalCode" /></gco:CharacterString>
              </gmd:postalCode>
              <gmd:country>
                <gco:CharacterString><xsl:value-of select="$country" /></gco:CharacterString>
              </gmd:country>
              <gmd:electronicMailAddress>
                <gco:CharacterString><xsl:value-of select="$email" /></gco:CharacterString>
              </gmd:electronicMailAddress>
            </gmd:CI_Address>
          </gmd:address>
          <gmd:onlineResource>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>
                  <xsl:value-of select="$linkage" />
                </gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString><xsl:value-of select="$protocol" /></gco:CharacterString>
              </gmd:protocol>
              <gmd:name>
                <gco:CharacterString><xsl:value-of select="$organizationName" /></gco:CharacterString>
              </gmd:name>
            </gmd:CI_OnlineResource>
          </gmd:onlineResource>
          <gmd:contactInstructions />
        </gmd:CI_Contact>
      </gmd:contactInfo>
      <gmd:role>
        <gmd:CI_RoleCode
          codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode"
          codeListValue="pointOfContact" />
      </gmd:role>
    </gmd:CI_ResponsibleParty>
  </xsl:template>

  <!-- creates a CI_OnlineResource with the given parameters -->
  <xsl:template name="onlineResource">
    <xsl:param name="linkage" />
    <xsl:param name="protocol" />
    <xsl:param name="name" />
    <xsl:param name="description" />
    <gmd:CI_OnlineResource>
      <gmd:linkage>
        <gmd:URL>
          <xsl:value-of select="$linkage" />
        </gmd:URL>
      </gmd:linkage>
      <gmd:protocol>
        <gco:CharacterString><xsl:value-of select="$protocol" /></gco:CharacterString>
      </gmd:protocol>
      <gmd:name>
        <gco:CharacterString><xsl:value-of select="$name" /></gco:CharacterString>
      </gmd:name>
      <gmd:description>
        <gco:CharacterString><xsl:value-of select="$description" /></gco:CharacterString>
      </gmd:description>
    </gmd:CI_OnlineResource>
  </xsl:template>

  <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue">
    <xsl:attribute name="codeListValue">
      <xsl:value-of select="$props//datasetLanguage" />
    </xsl:attribute>
  </xsl:template>

  <!-- 
    Generate one gmd:topicCategory/gmd:MD_TopicCategoryCode for each gmd:keyword/gco:CharacterString
    that matches an entry in rdf:Description rdf:about="<id>"/skos:prefLabel[text()] in inspire/themes.rdf
  -->
  <xsl:import href="inspire/topic_category.xsl"/>
  <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory">
    <xsl:call-template name="inspire_topic_category"/>
  </xsl:template>


  <!--
   Generate one gmd:MD_Format for each distributionFormat in the metadata properties
  -->
  <xsl:template match="gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name">
    <gmd:name>
      <gco:CharacterString><xsl:value-of select="$props//distributionFormat" /></gco:CharacterString>
    </gmd:name>
  </xsl:template>


</xsl:stylesheet>