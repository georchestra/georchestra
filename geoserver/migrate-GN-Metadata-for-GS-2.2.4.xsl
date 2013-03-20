<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco">

  <!-- When using geonetwork 2.2.4 with georchestra geonetwork virtual services drop the namespace prefix
      This means that for geonetwork to be able to send layers to mapfishapp or extractorapp the links must be modified
      so the layernames do not have a prefix.  
      
      This transform makes this change.
      
      Steps:
      
      1.  Login as Administrator
      2.  Put this file in the schema plugin directory for all iso19139 based schemas that need to be updated
      3.  Perform a search and select all metadata to update
      4.  Run http://<host>/geonetwork/srv/eng/metadata.batch.processing?process=migrate-GN-Metadata-for-GS-2.2.4
      
      This will update all selected metadata that have this xsl file in their schema_plugins directory
   -->

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template priority="5" match="
		gmd:CI_OnlineResource[
			contains(normalize-space(gmd:protocol/gco:CharacterString), 'get-map') and 
			substring-before(normalize-space(gmd:name/gco:CharacterString), ':') != '' and
			contains(normalize-space(gmd:linkage/gmd:URL), concat('/', substring-before(normalize-space(gmd:name/gco:CharacterString), ':'), '/'))]" />

		<xsl:copy>
			<xsl:apply-templates mode="updateLinks" select="@*|node()">
				<xsl:with-param name="prefix" select="$prefix" />
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="updateLinks" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="updateLinks" match="gmd:name">
		<xsl:param name="prefix" />

		<xsl:variable name="prefixWithColon" select="concat($prefix, ':')" />
		<gmd:name>
			<gco:CharacterString>
				<xsl:value-of
					select="substring-after(normalize-space(gco:CharacterString), $prefixWithColon)" />
			</gco:CharacterString>
		</gmd:name>
	</xsl:template>

</xsl:stylesheet>