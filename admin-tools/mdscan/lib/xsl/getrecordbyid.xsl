<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xlink="http://www.w3.org/1999/xlink">

  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>

  <!-- Match Root -->
  <xsl:template match="/defaults">

  <csw:GetRecordById 
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
    service = "CSW">
    <xsl:attribute name="version">
      <xsl:value-of select="./version"/>
    </xsl:attribute>
    <xsl:attribute name="outputFormat">
      <xsl:value-of select="./outputformat"/>
    </xsl:attribute>
    <xsl:attribute name="outputSchema">
      <xsl:value-of select="./outputschema"/>
    </xsl:attribute>
    <csw:Id>
      <xsl:value-of select="./id"/>
    </csw:Id>
    <csw:ElementSetName>full</csw:ElementSetName>
  </csw:GetRecordById>

 </xsl:template>
</xsl:stylesheet>