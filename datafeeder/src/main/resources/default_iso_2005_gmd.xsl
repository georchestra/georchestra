<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                 xmlns:gco="http://www.isotc211.org/2005/gco"
                 xmlns:srv="http://www.isotc211.org/2005/srv"
                 xmlns:gmx="http://www.isotc211.org/2005/gmx"
                 xmlns:gts="http://www.isotc211.org/2005/gts"
                 xmlns:gsr="http://www.isotc211.org/2005/gsr"
                 xmlns:gmi="http://www.isotc211.org/2005/gmi"
                 xmlns:gml="http://www.opengis.net/gml/3.2"
                 xmlns:xlink="http://www.w3.org/1999/xlink"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd"
                 exclude-result-prefixes="gmd srv gco"
>
<!-- 
Default template to apply MetadataRecordProperties.java properties to a record template adhering to http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
 -->
  <xsl:strip-space elements="*" xmlns="http://www.isotc211.org/2005/gmd" />
  <xsl:output indent="yes" standalone="yes" />

  <!-- Whole document used as xsl parameter, see sample_md_properties.xml for an example of its contents -->
  <xsl:param name="props"/>
  
   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="gmd:fileIdentifier/gco:CharacterString">
    <gco:CharacterString><xsl:value-of select="$props//metadataId"/></gco:CharacterString>
  </xsl:template>

  <xsl:template match="gmd:language/gmd:LanguageCode">
    <gmd:LanguageCode codeListValue="{$props//metadataLanguage}" codeList="http://www.loc.gov/standards/iso639-2/"/>
  </xsl:template>

  <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
    <gco:CharacterString><xsl:value-of select="$props//title"/></gco:CharacterString>
  </xsl:template>
  
  <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString">
    <gco:CharacterString><xsl:value-of select="$props//abstract"/></gco:CharacterString>
  </xsl:template>


</xsl:stylesheet>