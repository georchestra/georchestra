<?xml version="1.0"?>
<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--xsl:output method="html" encoding="ISO-8859-1"/-->

	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- XML formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!--
	draws an element as xml document
	-->
	<xsl:template match="/">
        <div class="captioneddiv">
        <h3>XML presentation</h3>
        <br/>
	<xsl:apply-templates mode="showXMLElement" select="."/>
        </div>
	</xsl:template>


	<!--
	draws an element in xml
	-->
	<xsl:template mode="showXMLElement" match="*">
		<xsl:choose>
			
			<!-- has children -->
			<xsl:when test="*">
				<xsl:call-template name="showXMLStartTag"/>
				<dl>
					<xsl:for-each select="*">
						<dd>
							<xsl:apply-templates select="." mode="showXMLElement"/>
						</dd>
					</xsl:for-each>
				</dl>
				<xsl:call-template name="showEndTag"/>
			</xsl:when>
			
			<!-- no children but text -->
			<xsl:when test="text()">
				<xsl:call-template name="showXMLStartTag"/>
				<xsl:value-of select="text()"/>
				<xsl:call-template name="showEndTag"/>
			</xsl:when>
			
			<!-- empty element -->
			<xsl:otherwise>
				<xsl:call-template name="showXMLStartEndTag"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
	draws the start tag of an element
	-->
	<xsl:template name="showXMLStartTag">
		<font color="#4444ff">
	           <xsl:text>&lt;</xsl:text>
		   <xsl:value-of select="name(.)"/>
		  <xsl:call-template name="showXMLNamespaces"/>
		  <xsl:call-template name="showXMLAttributes"/>
		  <xsl:text>&gt;</xsl:text>
		</font>
	</xsl:template>

	<!--
	draws the end tag of an element
	-->
	<xsl:template name="showEndTag">
		<font color="#4444ff">
			<xsl:text>&lt;/</xsl:text>
				<xsl:value-of select="name(.)"/>
			<xsl:text>&gt;</xsl:text>
		</font>
	</xsl:template>
	
	<!--
	draws the empty tag of an element
	-->
	<xsl:template name="showXMLStartEndTag">
		<font color="#4444ff">
		  <xsl:text>&lt;</xsl:text>
		  <xsl:value-of select="name(.)"/>
		</font>
		  <xsl:call-template name="showXMLNamespaces"/>
		  <xsl:call-template name="showXMLAttributes"/>
		<font color="#4444ff">
		  <xsl:text>/&gt;</xsl:text>
		</font>
	</xsl:template>
	
	<!--
	draws attributes of an element
	-->
	<xsl:template name="showXMLAttributes">
		<xsl:for-each select="@*">
			<xsl:text> </xsl:text>
			<font color="#44aa44">
			   <xsl:value-of select="name(.)"/>
			</font>
			<xsl:text>=</xsl:text>
			<font color="#ff4444">
				<xsl:text>"</xsl:text>
				<xsl:value-of select="string()"/>
				<xsl:text>"</xsl:text>
			</font>
		</xsl:for-each>
	</xsl:template>
	
	<!--
	draws namespaces of an element
	-->
	<xsl:template name="showXMLNamespaces">
		<xsl:variable name="parent" select=".."/>
		<xsl:for-each select="namespace::*">
			<xsl:if test="not(.=$parent/namespace::*) and name()!='geonet'">
				<xsl:text> xmlns</xsl:text>
				<xsl:if test="name()">
					<xsl:text>:</xsl:text>
					<xsl:value-of select="name()"/>
				</xsl:if>
				<xsl:text>=</xsl:text>
				<font color="#888844">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="string()"/>
					<xsl:text>"</xsl:text>
				</font>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
	
