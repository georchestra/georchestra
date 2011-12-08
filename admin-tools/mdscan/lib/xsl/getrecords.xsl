<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:ows="http://www.opengis.net/ows"
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>


  <!-- Match Root -->
  <xsl:template match="/defaults">

  <csw:GetRecords
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
    service = "CSW">
    <xsl:attribute name="version">
      <xsl:value-of select="./version"/>
    </xsl:attribute>
    <xsl:attribute name="maxRecords">
      <xsl:value-of select="./maxrecords"/>
    </xsl:attribute>
    <xsl:attribute name="startPosition">
      <xsl:value-of select="./startposition"/>
    </xsl:attribute>
    <xsl:attribute name="outputFormat">
      <xsl:value-of select="./outputformat"/>
    </xsl:attribute>
    <xsl:attribute name="outputSchema">
      <xsl:value-of select="./outputschema"/>
    </xsl:attribute>
    <xsl:attribute name="resultType">
      <xsl:value-of select="./resulttype"/>
    </xsl:attribute>
    <csw:Query typeNames="csw:Record">
      <csw:ElementSetName>full</csw:ElementSetName>
	  <!-- Don't add Constraint if  searh term is empty; this keeps Geonetwork happy -->
	  <xsl:if test="./literal !=''">
        <csw:Constraint version="1.1.0">
          <ogc:Filter xmlns:ogc="http://www.opengis.net/ogc" xmlns="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <ogc:PropertyIsLike escape="\" singleChar="_" wildCard="%">
              <ogc:PropertyName>
			  <xsl:value-of select="./propertyname"/>
			  </ogc:PropertyName>
              <ogc:Literal>
              <xsl:value-of select="./literal"/>
              </ogc:Literal>
            </ogc:PropertyIsLike>
          </ogc:Filter>
        </csw:Constraint>
	  </xsl:if>
      <ogc:SortBy xmlns:ogc="http://www.opengis.net/ogc">
        <xsl:if test="./sortby !=''">
          <ogc:SortProperty>
            <ogc:PropertyName>
              <xsl:value-of select="./sortby"/>
            </ogc:PropertyName>
            <ogc:SortOrder>
              <xsl:value-of select="./sortorder"/>
            </ogc:SortOrder>
          </ogc:SortProperty>
        </xsl:if>
      </ogc:SortBy>
    </csw:Query>
  </csw:GetRecords>

 </xsl:template>
</xsl:stylesheet>
