<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcterms="http://purl.org/dc/terms/" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:util="java:org.fao.geonet.util.XslUtil" version="2.0" exclude-result-prefixes="#all">

<!-- 
Template to add a <gmd:topicCategory> for each 
gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString
matching the mappings defined here from keyword to RDF entry

The document must have an empty gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory
 -->
  <xsl:template name="inspire_topic_category">
    <xsl:variable name="inspire-themes" select="document('themes.rdf')"/>

    <xsl:for-each select="$props//keywords//keyword">
      <xsl:variable name="kw" select="."/>
      <xsl:variable name="rdf_about_id" select="$inspire-themes//rdf:Description[skos:prefLabel=$kw]/@rdf:about" />
      <xsl:choose>
        <xsl:when test="$rdf_about_id != ''">
          <xsl:variable name="topiccat_code" select="$topiccat-map/entry[@rdf:about=$rdf_about_id]/@topiccatcode"/>
          <xsl:choose>
            <xsl:when test="$topiccat_code != ''">
              <gmd:topicCategory>
                <gmd:MD_TopicCategoryCode>
                  <xsl:value-of select="$topiccat_code" />
                </gmd:MD_TopicCategoryCode>
              </gmd:topicCategory>
            </xsl:when> 
            <xsl:otherwise>
              <xsl:comment>
                No mapping exists for rdf:about= '<xsl:value-of select="$rdf_about_id"/>'.
                Keyword: '<xsl:value-of select="$kw" />'
              </xsl:comment>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
            <xsl:comment>No matching topic category for keyword '<xsl:value-of select="$kw" />'</xsl:comment>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <!-- 
    Mapping of <rdf:Description rdf:about="<id>"> in themes.rdf to topic category codes
   -->
  <xsl:variable name="topiccat-map">
    <entry topiccatcode="location" rdf:about="3" />
    <entry topiccatcode="boundaries" rdf:about="4" />
    <entry topiccatcode="location" rdf:about="5" />
    <entry topiccatcode="planningCadastre" rdf:about="6" />
    <entry topiccatcode="transportation" rdf:about="7" />
    <entry topiccatcode="inlandWaters" rdf:about="8" />
    <entry topiccatcode="environment" rdf:about="9" />
    <entry topiccatcode="elevation" rdf:about="10" />
    <entry topiccatcode="imageryBaseMapsEarthCover" rdf:about="11" />
    <entry topiccatcode="imageryBaseMapsEarthCover" rdf:about="12" />
    <entry topiccatcode="geoscientificInformation" rdf:about="13" />
    <entry topiccatcode="boundaries" rdf:about="14" />
    <entry topiccatcode="structure" rdf:about="15" />
    <entry topiccatcode="geoscientificInformation" rdf:about="16" />
    <entry topiccatcode="planningCadastre" rdf:about="17" />
    <entry topiccatcode="health" rdf:about="18" />
    <entry topiccatcode="utilitiesCommunication" rdf:about="19" />
    <entry topiccatcode="structure" rdf:about="20" />
    <entry topiccatcode="structure" rdf:about="21" />
    <entry topiccatcode="farming" rdf:about="22" />
    <entry topiccatcode="society" rdf:about="23" />
    <entry topiccatcode="planningCadastre" rdf:about="24" />
    <entry topiccatcode="geoscientificInformation" rdf:about="25" />
    <entry topiccatcode="climatologyMeteorologyAtmosphere" rdf:about="26" />
    <entry topiccatcode="climatologyMeteorologyAtmosphere" rdf:about="27" />
    <entry topiccatcode="oceans" rdf:about="28" />
    <entry topiccatcode="oceans" rdf:about="29" />
    <entry topiccatcode="biota" rdf:about="30" />
    <entry topiccatcode="biota" rdf:about="31" />
    <entry topiccatcode="biota" rdf:about="32" />
    <entry topiccatcode="economy" rdf:about="33" />
    <entry topiccatcode="economy" rdf:about="34" />
  </xsl:variable>
</xsl:stylesheet>