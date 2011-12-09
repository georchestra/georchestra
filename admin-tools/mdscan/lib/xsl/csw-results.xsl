<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0" xmlns:dc="http://purl.org/dc/elements/1.1/"
xmlns:dct="http://purl.org/dc/terms/">
  <!--xsl:output method="html" encoding="ISO-8859-1"/-->
  <xsl:strip-space  elements="*"/>
  <xsl:variable name="geonetworkUrl">http://geobretagne.fr/geonetwork</xsl:variable>
  <xsl:variable name="pageUrl">
    <xsl:text>javascript:(csw_client.getRecords</xsl:text>
    <xsl:text>('</xsl:text>
  </xsl:variable>
  <xsl:template match="/results/*[local-name()='GetRecordsResponse']">

    <xsl:apply-templates select="./*[local-name()='SearchResults']" />
  </xsl:template>
  <xsl:template match="*[local-name()='SearchResults']">
    <xsl:variable name="start">
      <xsl:value-of select="../../request/@start" />
    </xsl:variable>
    <!-- because GeoNetwork does not return nextRecord we have to do some calculation -->
    <xsl:variable name="next">
      <xsl:choose>
        <xsl:when test="@nextRecord">
          <xsl:value-of select="@nextRecord" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="number(@numberOfRecordsMatched) &gt;= (number($start) + number(@numberOfRecordsReturned))">

              <xsl:value-of select="number($start) + number(@numberOfRecordsReturned)" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="0" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div class="captioneddiv">
      <!--xsl:if test="number(@numberOfRecordsMatched) > number(@numberOfRecordsReturned)"-->
      <!-- because ESRI GPT returns always numberOfRecordsMatched = 0 -->
      <xsl:if test="number(@numberOfRecordsReturned) &gt; 0 and ($start &gt; 1 or number($next) &gt; 0)">

        <h3 style="float:right;top: -2.5em;">
          <xsl:if test="$start &gt; 1">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$pageUrl" />
                <xsl:value-of select="number($start)-number(../../request/@maxrecords)" />
                <xsl:text>'))</xsl:text>
              </xsl:attribute>
              <xsl:text>&lt;&lt; précédent</xsl:text>
            </a>
          </xsl:if>
          <xsl:text>||</xsl:text>
          <xsl:if test="number($next) &gt; 0">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$pageUrl" />
                <xsl:value-of select="$next" />
                <xsl:text>'))</xsl:text>
              </xsl:attribute>
              <xsl:text>suivant &gt;&gt;</xsl:text>
            </a>
          </xsl:if>
        </h3>
      </xsl:if>
      <h3>Enregistrements trouvés:
      <xsl:value-of select="@numberOfRecordsReturned" />(sur
      <xsl:value-of select="@numberOfRecordsMatched" />)</h3>
      <br />
      <!--
      liste
      -->
        <p>
      <input type="checkbox" checked="checked" disabled="disabled" title="afficher informations niveau DEBUG (non fonctionnel)" />
      <span class="md-test debug">déboguage</span>
      <input type="checkbox" checked="checked" disabled="disabled" title="afficher informations niveau INFO (non fonctionnel)" />
      <span class="md-test info">information</span>
      <input type="checkbox" checked="checked" disabled="disabled" title="afficher informations niveau WARNING (non fonctionnel)" />
      <span class="md-test warning">erreur mineure</span>
      <input type="checkbox" checked="checked" disabled="disabled" title="afficher informations niveau ERROR (non fonctionnel)" />
      <span class="md-test error">erreur bloquante</span>
      <input type="checkbox" checked="checked" disabled="disabled" title="afficher informations niveau CRITICAL (non fonctionnel)" />
      <span class="md-test critical">erreur majeure</span>
        </p>
      <!-- debut table -->
      <table>
        <xsl:attribute name="start">
          <xsl:value-of select="$start" />
        </xsl:attribute>
        <caption>résultats de la recherche</caption>
        <thead>
            <tr>
                <th class="title">titre</th>
                <th>checks</th>
                <th>actions</th>
            </tr>
        </thead>
        <tbody>
            <!-- boucle metadonnees -->
            <xsl:for-each select="./*[local-name()='SummaryRecord']|./*[local-name()='BriefRecord']|./*[local-name()='Record']">
                <xsl:if test="dc:type='dataset'">
                  <tr>
                    <xsl:if test="position() mod 2=0">
                        <xsl:attribute name="class">odd</xsl:attribute>
                    </xsl:if>
                    <td >
                        <xsl:call-template name="md-title" />
                    </td>
                    <td>
                      <xsl:call-template name="test-all" />
                    </td>
                    <td>
                        <xsl:call-template name="md-action-geonetwork-edit" />
                        <xsl:call-template name="md-action-geoserver-edit" />
                        <xsl:call-template name="md-action-download" />
                    </td>
                  </tr>
                </xsl:if>
            </xsl:for-each>
        </tbody>
      </table>
    </div>
  </xsl:template>



  <!-- template dc:title -->
  <xsl:template name="md-title">
    <span class="md-title">
      <a>
        <xsl:attribute name="href">
          <xsl:text>javascript:(csw_client.getRecordById</xsl:text>
          <xsl:text>('</xsl:text>
          <xsl:value-of select="./dc:identifier" />
          <xsl:text>'))</xsl:text>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="./dc:title">
            <xsl:value-of select="./dc:title" />
          </xsl:when>
          <xsl:otherwise>
            <span class="err critical">NOTITLE</span>
          </xsl:otherwise>
        </xsl:choose>
      </a>
    </span>
  </xsl:template>


  <!--template dc:type -->
  <xsl:template match="dc:type">
    <span class="md-info type">
        <xsl:choose>
          <xsl:when test=".='dataset'">DS</xsl:when>
          <xsl:when test=".='service'">SV</xsl:when>
          <xsl:when test=".='feature'">FE</xsl:when>
          <xsl:otherwise>??</xsl:otherwise>
        </xsl:choose>
    </span>
  </xsl:template>

  <!-- boutons d'action -->

  <!-- action geonetwork -->
  <xsl:template name="md-action-geonetwork-edit">
        <a target="gn" class="md-action" title="éditer la métadonnée dans GeoNetwork">
            <xsl:attribute name="href">
                <xsl:value-of select="concat($geonetworkUrl,'?uuid=',./dc:identifier)" />
            </xsl:attribute>
            <xsl:text>GN</xsl:text>
        </a>
  </xsl:template>

  <!-- action geoserver -->
  <xsl:template name="md-action-geoserver-edit">
    <xsl:for-each select="dc:URI[@protocol='OGC:WMS-1.1.1-http-get-map']">
        <xsl:if test="contains(.,'/geoserver/') and @name!=''">
            <a class="md-action" target="gs">
                <xsl:attribute name="href">
                    <xsl:value-of select="substring-before(.,'/geoserver/')" />
                    <xsl:text>/geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.data.resource.ResourceConfigurationPage&amp;name=</xsl:text>
                    <xsl:value-of select="@name" />
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>éditer [</xsl:text>
                    <xsl:value-of select="@name" />
                    <xsl:text>] dans GeoServer</xsl:text>
                </xsl:attribute>
                <xsl:text>GS</xsl:text>
            </a>
        </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- action telechargement -->
  <xsl:template name="md-action-download">
    <xsl:for-each select="dc:URI[@protocol='WWW:DOWNLOAD-1.0-http--download']">
        <xsl:if test=".!=''">
            <a class="md-action" target="dl">
                <xsl:attribute name="href">
                    <xsl:value-of select="." />
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>télécharger </xsl:text>
                    <xsl:value-of select="." />
                </xsl:attribute>
                <xsl:text>DL</xsl:text>
            </a>
        </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <!-- template dessinant les vignettes de test -->
  <xsl:template name="flag-span">
    <xsl:param name="severity" select="undefined" />
    <xsl:param name="title" select="undefined" />
    <xsl:param name="content" select="undefined" />
    <span>
        <xsl:attribute name="class">
            <xsl:value-of select="concat('md-test ', $severity)" />
        </xsl:attribute>
        <xsl:attribute name="title">
            <xsl:value-of select="$title" />
        </xsl:attribute>
        <xsl:value-of select="$content" />
    </span>
  </xsl:template>



  <!-- /////////////////////////////////////////////////////////////// -->

  <!-- template regroupant les tests -->
  <xsl:template name="test-all">
    <xsl:call-template name="test-subject" />
    <xsl:call-template name="test-source" />
    <xsl:call-template name="test-rights" />
    <xsl:call-template name="test-link-wms" />
    <xsl:call-template name="test-link-wfs" />
    <xsl:call-template name="test-link-download" />
  </xsl:template>


  <!-- test des keywords -->
  <xsl:template name="test-subject">
    <xsl:choose>
        <xsl:when test="count(dc:subject)&lt;2">
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">error</xsl:with-param>
                <xsl:with-param name="title">pas assez de mots clefs</xsl:with-param>
                <xsl:with-param name="content">KW</xsl:with-param>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">debug</xsl:with-param>
                <xsl:with-param name="title">
                    <xsl:for-each select="dc:subject">
                        <xsl:apply-templates />
                        <xsl:text>, </xsl:text>
                    </xsl:for-each>
                </xsl:with-param>
                <xsl:with-param name="content">
                    <xsl:value-of select="count(./dc:subject)" />
                <xsl:text>KW</xsl:text>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- test sur la source decrite dans les metadonnees -->
  <xsl:template name="test-source">
    <xsl:choose>
        <xsl:when test="count(dc:source)=0">
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">warning</xsl:with-param>
                <xsl:with-param name="title">dc:source est vide</xsl:with-param>
                <xsl:with-param name="content">source</xsl:with-param>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="starts-with(dc:source,'-- ')">
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">warning</xsl:with-param>
                <xsl:with-param name="title">
                    <xsl:text>dc:source est peut-être sur une valeur par défaut : </xsl:text>
                    <xsl:value-of select="dc:source" />
                </xsl:with-param>
                <xsl:with-param name="content">source</xsl:with-param>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">info</xsl:with-param>
                <xsl:with-param name="title">
                    <xsl:for-each select="dc:source">
                        <xsl:value-of select="." />
                        <xsl:text>------------------</xsl:text>
                    </xsl:for-each>
                </xsl:with-param>
                <xsl:with-param name="content">source</xsl:with-param>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- test sur les droits decrits dans les metadonnees -->
  <xsl:template name="test-rights">
    <xsl:choose>
        <xsl:when test="count(dc:rights)=0">
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">warning</xsl:with-param>
                <xsl:with-param name="title">dc:rights est vide</xsl:with-param>
                <xsl:with-param name="content">rights</xsl:with-param>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="flag-span">
                <xsl:with-param name="severity">info</xsl:with-param>
                <xsl:with-param name="title">
                    <xsl:for-each select="dc:rights">
                        <xsl:value-of select="." />
                        <xsl:text>------------------</xsl:text>
                    </xsl:for-each>
                </xsl:with-param>
                <xsl:with-param name="content">rights</xsl:with-param>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- test sur la description des WMS -->
  <xsl:template name="test-link-wms">
    <xsl:for-each select="dc:URI[@protocol='OGC:WMS-1.1.0-http-get-map'
    |@protocol='OGC:WMS-1.1.1-http-get-map'
    |@protocol='OGC:WMS-1.3.0-http-get-map']">
        <span target="_blank" class="md-test info">
          <xsl:attribute name="title">
            <xsl:value-of select="." />
          </xsl:attribute>
          <xsl:choose>
            <xsl:when test="@name='' or not(@name)">
                <xsl:attribute name="class">md-test error</xsl:attribute>
                <xsl:attribute name="title">le nom de la couche est vide</xsl:attribute>
            </xsl:when>
            <xsl:when test="substring(.,string-length(.)-1)!='?'">
                <xsl:attribute name="class">md-test error</xsl:attribute>
                <xsl:attribute name="title">l'adresse du service ne finit pas par ? ou &amp;</xsl:attribute>
            </xsl:when>
            <xsl:when test="@description=@name">
                <xsl:attribute name="class">md-test warning</xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>le titre est mal défini : </xsl:text>
                    <xsl:value-of select="@name" />
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="@description='' or not(@description)">
                <xsl:attribute name="class">md-test warning</xsl:attribute>
                <xsl:attribute name="title">le titre est vide</xsl:attribute>
            </xsl:when>
          </xsl:choose>
        <xsl:text>WMS</xsl:text>
        <xsl:text><xsl:value-of select="substring(.,string-length(.)-3)"></xsl:text>
      </span>
    </xsl:for-each>
  </xsl:template>

  <!-- test sur la description des WFS -->
  <xsl:template name="test-link-wfs">
    <xsl:for-each select="dc:URI[@protocol='OGC:WFS-1.0.0-http-get-capabilities'
    |@protocol='OGC:WFS-1.1.0-http-get-capabilities']">
        <span target="_blank" class="md-test info">
          <xsl:attribute name="title">
            <xsl:value-of select="." />
          </xsl:attribute>
          <xsl:choose>
            <xsl:when test="@name='' or not(@name)">
                <xsl:attribute name="class">md-test error</xsl:attribute>
                <xsl:attribute name="title">le nom de la couche est vide</xsl:attribute>
            </xsl:when>
            <xsl:when test="@description=@name">
                <xsl:attribute name="class">md-test warning</xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>le titre est mal défini : </xsl:text>
                    <xsl:value-of select="@name" />
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="not(@description)">
                <xsl:attribute name="class">md-test warning</xsl:attribute>
                <xsl:attribute name="title">le titre est vide</xsl:attribute>
            </xsl:when>
          </xsl:choose>
        <xsl:text>WFS</xsl:text>
      </span>
    </xsl:for-each>
  </xsl:template>

  <!-- test sur la description des downloads -->
  <xsl:template name="test-link-download">
    <xsl:for-each select="dc:URI[@protocol='WWW:DOWNLOAD-1.0-http--download']">
        <span class="md-test info">
          <xsl:attribute name="title">
            <xsl:value-of select="." />
          </xsl:attribute>
        <xsl:choose>
            <xsl:when test=".=''">
                <xsl:attribute name="class">md-test error</xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>adresse de téléchargement manquante</xsl:text>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="substring(.,1,4)!='http'">
                <xsl:attribute name="class">md-test error</xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:text>adresse de téléchargement erronée : </xsl:text>
                    <xsl:value-of select="." />
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="contains(.,'version=1.00')">
                <xsl:attribute name="class">md-test warning</xsl:attribute>
                <xsl:attribute name="title">version WFS erronée</xsl:attribute>
            </xsl:when>
        </xsl:choose>
        <xsl:text>DL</xsl:text>
      </span>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
