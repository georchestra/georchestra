<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
 xmlns:gco="http://www.isotc211.org/2005/gco"
 xmlns:gmd="http://www.isotc211.org/2005/gmd"
 xmlns:dc="http://purl.org/dc/elements/1.1/"
 xmlns:dct="http://purl.org/dc/terms/"
 xmlns:ows="http://www.opengis.net/ows"
 xmlns:cat="http://www.esri.com/metadata/csw/">

<xsl:output method="xml" encoding="ISO-8859-1"/>


<xsl:template match="/">
  <div>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="/*[local-name()='GetRecordByIdResponse']">
    <xsl:apply-templates select="cat:FullRecord"/>
    <xsl:apply-templates select="*[local-name()='Record']"/>
    <xsl:apply-templates select="*[local-name()='SummaryRecord']"/>
    <xsl:apply-templates select="*[local-name()='BriefRecord']"/>
    <xsl:apply-templates select="gmd:MD_Metadata"/>
    <xsl:apply-templates select="Metadata"/>
    <xsl:apply-templates select="metadata"/>
</xsl:template>

<xsl:template match="cat:FullRecord">
    <xsl:apply-templates select="metadata"/>
</xsl:template>


<!-- Start Metadata ISO19139 -->
<xsl:template match="gmd:MD_Metadata">
    <!-- First the Identification block -->
    <xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification"/>
    <xsl:apply-templates select="./gmd:distributionInfo/gmd:MD_Distribution"/>

<!-- Metadata block -->
<div class="captioneddiv">
<h3>Metadonnée</h3>
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Identifiant'"/>
      <xsl:with-param name="cvalue" select="./gmd:fileIdentifier/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Langue'"/>
      <xsl:with-param name="cvalue" select="./gmd:language/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Encodage'"/>
      <xsl:with-param name="cvalue" select="./gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Date stamp'"/>
      <xsl:with-param name="cvalue" select="./gmd:dateStamp/gco:Date"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Standard de métadonnée'"/>
      <xsl:with-param name="cvalue" select="./gmd:metadataStandardName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Version du standard'"/>
      <xsl:with-param name="cvalue" select="./gmd:metadataStandardVersion/gco:CharacterString"/>
      </xsl:call-template>
</table>
    <xsl:apply-templates select="./gmd:contact"/>
</div>
</xsl:template>

<!-- 'Metadata->Metadata author' block -->
<xsl:template match="gmd:contact">
<div class="captioneddiv">
<h3>Auteur de la métadonnée</h3>
<table class="meta">
<tr>
<td class="meta" valign="top">
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Nom de la personne'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Organisation'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Position'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:positionName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Rôle'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
      </xsl:call-template>
</table></td>
<td class="meta" valign="top">
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Téléphone'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Fax'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Adresse'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Commune'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Code postal'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Pays'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Email'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
      </xsl:call-template>
</table></td>
</tr>
</table>
</div>
</xsl:template>

<!-- 'Identification' block -->
<xsl:template match="gmd:MD_DataIdentification">
<div class="captioneddiv">
<h3>Information de l'identification</h3>
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Titre'"/>
      <xsl:with-param name="cvalue" select="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Date'"/>
      <xsl:with-param name="cvalue" select="./gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:data/gco:Date"/>
      </xsl:call-template>
      <!--xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Presentation form'"/>
      <xsl:with-param name="cvalue" select="./idCitation/presForm/PresFormCd/@value"/>
      </xsl:call-template-->
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Nom de la personne'"/>
      <xsl:with-param name="cvalue" select="./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Organisation'"/>
      <xsl:with-param name="cvalue" select="./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
      </xsl:call-template>

      <!--abstract is handled seperately because of text formatting-->
      <tr>
      <td class="meta-param">Résumé:</td>
      <td class="meta-value">
      <xsl:apply-templates select="./gmd:abstract"/>
      </td>
      </tr>
</table>
    <xsl:apply-templates select="./gmd:extent"/>
    <xsl:apply-templates select="./gmd:pointOfContact"/>
</div>
</xsl:template>

<!-- 'Identification->Point of Contact' block -->
<xsl:template match="gmd:pointOfContact">
<div class="captioneddiv">
<h3>Point de contact</h3>
<table class="meta">
<tr>
<td class="meta" valign="top">
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Nom de la personne'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Organisation'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Position'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:positionName/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Rôle'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
      </xsl:call-template>
</table></td>
<td class="meta" valign="top">
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Téléphone'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Fax'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Delivery Point'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Commune'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Code postal'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Pays'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Email'"/>
      <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
      </xsl:call-template>
</table></td>
</tr>
</table>
</div>
</xsl:template>

<!-- 'Identification->Geographic box' block -->
<xsl:template match="gmd:extent">
<xsl:if test="./gmd:EX_Extent/gmd:geographicElement">
<div class="captioneddiv">
<h3>Boîte géographique</h3>
<br/>
<table class="meta" width="100%" align="center"><tr></tr>
<tr>
<td></td><td class="meta-param" align="center">Nord<br/>
<font color="#000000"><xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/></font></td><td></td>
</tr>
<tr>
<td class="meta-param" align="center">Ouest<br/>
<font color="#000000"><xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/></font></td>
<td></td>
<td class="meta-param" align="center">Est<br/>
<font color="#000000"><xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/></font></td>
</tr>
<tr>
<td></td><td class="meta-param" align="center">Sud<br/>
<font color="#000000"><xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/></font></td><td></td>
</tr>
</table>
</div>
</xsl:if>
</xsl:template>

<!-- 'Distribution Info' block -->
<xsl:template match="gmd:MD_Distribution">
<div class="captioneddiv">
<h3>Informations sur la distribution</h3>
<table class="meta"><tr></tr>
    <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
  	    <xsl:choose>
  		    <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'WWW:DOWNLOAD-') and contains(./gmd:protocol/gco:CharacterString,'http--download') and ./gmd:name/gco:CharacterString">
			    <tr>
			      <td class="meta-param">Téléchargement:</td>
			      <td class="meta-value">
			        <a><xsl:attribute name="href">
				     <xsl:value-of select="gmd:linkage/gmd:URL"/>
				   </xsl:attribute>
			           <xsl:value-of select="gmd:name/gco:CharacterString"/>
				</a>
			      </td>
			    </tr>
  		    </xsl:when>
  		    <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'ESRI:AIMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-image') and ./gmd:name/gco:CharacterString">
			    <tr>
			      <td class="meta-param">Esri ArcIms:</td>
			      <td class="meta-value">
			        <a><xsl:attribute name="href">
				     <xsl:value-of select="gmd:linkage/gmd:URL"/>
				   </xsl:attribute>
			           <xsl:value-of select="gmd:name/gco:CharacterString"/>
				</a>
			      </td>
			    </tr>
  		    </xsl:when>
  		    <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-map') and ./gmd:name/gco:CharacterString">
			    <tr>
			      <td class="meta-param">OGC-WMS:</td>
			      <td class="meta-value">
			        <a><xsl:attribute name="href">
				     <xsl:text>javascript:void(window.open('</xsl:text>
				     <xsl:value-of select="gmd:linkage/gmd:URL"/>
			         <xsl:text>'))</xsl:text>
					 </xsl:attribute>
			           <xsl:value-of select="gmd:name/gco:CharacterString"/>
				</a>
			      </td>
			    </tr>
  		    </xsl:when>
  		    <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-capabilities') and ./gmd:name/gco:CharacterString">
			    <tr>
			      <td class="meta-param">OGC-WMS Capabilities:</td>
			      <td class="meta-value">
			        <a><xsl:attribute name="href">
				     <xsl:value-of select="gmd:linkage/gmd:URL"/>
				   </xsl:attribute>
			           <xsl:value-of select="gmd:name/gco:CharacterString"/>
				</a>
			      </td>
			    </tr>
  		    </xsl:when>
  		    <!--xsl:when test="linkage[text()]">
  			    <link type="url"><xsl:value-of select="linkage[text()]"/></link>
  		    </xsl:when-->
  	    </xsl:choose>
    </xsl:for-each>
</table>
</div>
</xsl:template>

<!-- 'Identification->Abstract -->
<xsl:template match="gmd:abstract">
<xsl:apply-templates select="./gco:CharacterString/text()"/>
</xsl:template>
<!-- End Metadata ISO19139 -->


<!-- StartMetadata Dublin Core -->

<!-- 'Identification' block -->
<xsl:template match="*[local-name()='Record']|*[local-name()='SummaryRecord']|*[local-name()='BriefRecord']">
<div class="captioneddiv">
<h3>Identification info</h3>
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Titre'"/>
      <xsl:with-param name="cvalue" select="./dc:title"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Date'"/>
      <xsl:with-param name="cvalue" select="./dc:date"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Presentation form'"/>
      <xsl:with-param name="cvalue" select="./dc:format"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Nom de la personne'"/>
      <xsl:with-param name="cvalue" select="./dc:publisher"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Identifier'"/>
      <xsl:with-param name="cvalue" select="./dc:identifier"/>
      </xsl:call-template>
<xsl:if test="./dct:abstract">
<tr><!-- this "tr" causes problems for new line replacement by "p" -->
<td class="meta-param">Abstract:</td><td class="meta-value"><xsl:apply-templates select="./dct:abstract"/></td>
</tr>
</xsl:if>
      <xsl:for-each select="./dc:subject">
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Keyword'"/>
      <xsl:with-param name="cvalue" select="."/>
      </xsl:call-template>
      </xsl:for-each>
</table>
<xsl:apply-templates select="./ows:BoundingBox"/>
<xsl:apply-templates select="./ows:WGS84BoundingBox"/>
</div>
</xsl:template>


<xsl:template match="dct:abstract">
<!--xsl:value-of select="."/-->
<xsl:apply-templates select="text()"/>
</xsl:template>

<!-- 'Identification->Geographic box' block -->
<xsl:template match="ows:BoundingBox|ows:WGS84BoundingBox">
<div class="captioneddiv">
<h3>Geographic box</h3>
<table class="meta"><tr></tr>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Lower corner'"/>
      <xsl:with-param name="cvalue" select="./ows:LowerCorner"/>
      </xsl:call-template>
      <xsl:call-template name="tablerow">
      <xsl:with-param name="cname" select="'Upper corner'"/>
      <xsl:with-param name="cvalue" select="./ows:UpperCorner"/>
      </xsl:call-template>
</table>
</div>
</xsl:template>
<!-- End Metadata Dublin Core -->

<!-- Start Utills -->
<xsl:template  match="text()">
  <xsl:call-template name="to-para">
    <xsl:with-param name="from" select="'&#10;&#10;'"/>
    <xsl:with-param name="string" select="."/>
  </xsl:call-template>
</xsl:template>

<!-- replace all occurences of the character(s) `from'
                   by  <p/> in the string `string'.-->
<xsl:template name="to-para" >
  <xsl:param name="string"/>
  <xsl:param name="from"/>
  <xsl:choose>
    <xsl:when test="contains($string,$from)">
      <xsl:value-of select="substring-before($string,$from)"/>
      <!-- output a <p/> tag instead of `from' -->
      <p/>
      <xsl:call-template name="to-para">
      <xsl:with-param name="string" select="substring-after($string,$from)"/>
      <xsl:with-param name="from" select="$from"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$string"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template name="tablerow" >
  <xsl:param name="cname"/>
  <xsl:param name="cvalue"/>
  <xsl:choose>
    <xsl:when test="string($cvalue)">
	<tr>
    <td class="meta-param"><xsl:value-of select="$cname"/><xsl:text>: </xsl:text></td>
    <td class="meta-value"><xsl:value-of select="$cvalue"/></td>
	</tr>
    </xsl:when>
    <xsl:otherwise>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
<!-- End Utills -->

</xsl:stylesheet>
