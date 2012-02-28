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
   
  <!-- liste keywords INSPIRE -->
  <xsl:variable name="kwtheme">
Activités économiques,
Agriculture,
Démographie,
Eau,
Economie numérique,
Emploi,
Energie,
Enseignement,
Entreprises,
Equipements de mesure,
Equipements publics,
Finances locales,
Foncier,
Habitat,
Infrastructures de transpport,
Limites administratives,
Mer et littoral,
Patrimoine architectural urbain et culturel,
Patrimoine naturel,
Risques naturels,
Risques sanitaires,
Risques technologiques,
Référentiel,
Urbanisme,
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

    <fieldset class="captioneddiv">
        <legend>Résultats</legend>

      <!--
      liste
      -->

      <!-- debut table -->
      <table>
        <xsl:attribute name="start">
          <xsl:value-of select="$start" />
        </xsl:attribute>
        <!-- titre table -->
        <caption>

            <!--xsl:if test="number(@numberOfRecordsMatched) > number(@numberOfRecordsReturned)"-->
            <!-- because ESRI GPT returns always numberOfRecordsMatched = 0 -->
            <xsl:if test="number(@numberOfRecordsReturned) &gt; 0 and ($start &gt; 1 or number($next) &gt; 0)">
                <span>
                    <xsl:if test="$start &gt; 1">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$pageUrl" />
                                <xsl:value-of select="number($start)-number(../../request/@maxrecords)" />
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                            <xsl:text>&lt;&lt; précédent</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                    </xsl:if>
                    <xsl:text>enregistrements </xsl:text>
                    <xsl:value-of select="@numberOfRecordsReturned" />
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@numberOfRecordsMatched" />
                    <xsl:if test="number($next) &gt; 0">
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$pageUrl" />
                                <xsl:value-of select="$next" />
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                            <xsl:text>suivant &gt;&gt;</xsl:text>
                        </a>
                    </xsl:if>
                </span>
            </xsl:if>
        </caption>
        <thead>
            <tr>
                <th class="title">titre</th>
                <th class="tutke">classement</th>
                <th>validation</th>
                <th> </th>
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
                    <td class="title">
                        <xsl:call-template name="md-title" />
                    </td>
                    <td class="subject">
                        <xsl:call-template name="md-theme" />
                    </td>
                    <td class="tests">
                      <xsl:call-template name="test-all" />
                    </td>
                    <td>
                        <xsl:call-template name="md-action-wms-group" />
                    </td>
                  </tr>
                </xsl:if>
            </xsl:for-each>
        </tbody>
      </table>
    </fieldset>
    <!-- overlay external pages -->
    <div class="apple_overlay" id="overlay">
        <div class="contentWrap"></div>
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

  <!--
  boutons d'action
  -->


  <!-- actions -->
  <xsl:template name="md-action-wms-group">
    <a href="#" class="layersmenu">adm</a>

    <div class="tooltip">
        <h4><xsl:value-of select="./dc:identifier" />
            <a target="gn" title="éditer dans GeoNetwork">
                <xsl:attribute name="href">
                    <xsl:value-of select="concat($geonetworkUrl,'?uuid=',./dc:identifier)" />
                </xsl:attribute>
                <xsl:text> [éditer] </xsl:text>
            </a>
        </h4>

        <!-- téléchargement -->
        <ul>
            <xsl:for-each select="dc:URI[@protocol='WWW:DOWNLOAD-1.0-http--download']">
                <xsl:if test=".!=''">
                    <li>
                        <a target="dl">
                            <xsl:attribute name="href">
                                <xsl:value-of select="." />
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:text>télécharger </xsl:text>
                                <xsl:value-of select="." />
                            </xsl:attribute>
                            <xsl:text>télécharger </xsl:text>
                        </a>
                    </li>
                </xsl:if>
            </xsl:for-each>
        </ul>

        <!-- services -->
        <ul>
            <xsl:for-each select="dc:URI[@protocol='OGC:WMS-1.1.1-http-get-map'
            or @protocol='OGC:WMS-1.3.0-http-get-map']">
                <xsl:if test=".!='' and @name and @name!=''">
                    <li>
                        OGC:WMS
                        <span>
                            <xsl:attribute name="title">
                                <xsl:value-of select="@description" />
                            </xsl:attribute>
                            <xsl:value-of select="@name" />
                        </span>
                        |
                        <!-- getLegendGraphic -->
                        <a target="_blank" title="getLegendGraphic">
                            <xsl:attribute name="href">
                                <xsl:value-of select="." />
                                <xsl:if test="not(contains(.,'?'))">?</xsl:if>
                                <xsl:value-of select="concat(
                                '&amp;service=WMS&amp;VERSION=',substring(@protocol,9,5),
                                '&amp;request=getLegendGraphic&amp;FORMAT=image/png&amp;LAYER=',@name)" />
                            </xsl:attribute>
                            <xsl:text>légende</xsl:text>
                        </a>
                        <!-- geoserver -->
                        <xsl:if test="contains(.,'/geoserver/') and @name!=''">
                            |
                            <a target="_blank" title="administrer dans GeoServer">
                                <xsl:attribute name="href">
                                    <xsl:value-of select="substring-before(.,'/geoserver/')" />
                                    <xsl:text>/geoserver/web/?wicket:bookmarkablePage=:org.geoserver.web.data.resource.ResourceConfigurationPage&amp;name=</xsl:text>
                                    <xsl:value-of select="@name" />
                                </xsl:attribute>
                                <xsl:text>GS</xsl:text>
                            </a>
                        </xsl:if>
                    </li>
                </xsl:if>
            </xsl:for-each>
        </ul>
    </div>
  </xsl:template>



  <!-- action telechargement -->
  <xsl:template name="md-action-download">
    <xsl:for-each select="dc:URI[@protocol='WWW:DOWNLOAD-1.0-http--download']">
        <xsl:if test="position() mod 4=0">
            <br />
        </xsl:if>
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


  <!--
  template dessinant les vignettes de test
  -->
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

  <!-- classification par theme -->
  <xsl:template name="md-theme">
           <xsl:for-each select="dc:subject">
              <xsl:if test="contains($kwtheme,concat(.,','))">
                  <xsl:value-of select="." />
                  <xsl:text> </xsl:text>
              </xsl:if>
           </xsl:for-each>
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
    or @protocol='OGC:WMS-1.1.1-http-get-map'
    or @protocol='OGC:WMS-1.3.0-http-get-map']">
        <xsl:if test="position() mod 4=0">
            <br />
        </xsl:if>
        <xsl:choose>
            <xsl:when test="@name='' or not(@name)">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">error</xsl:with-param>
                    <xsl:with-param name="title">le nom de la couche est vide</xsl:with-param>
                    <xsl:with-param name="content">WMS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="substring(.,string-length(.))!='?' and substring(.,string-length(.))!='&amp;'">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:text>l'adresse ne finit pas par ?&amp; : </xsl:text>
                        <xsl:value-of select="." />
                    </xsl:with-param>
                    <xsl:with-param name="content">WMS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="@description=@name">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:text>le titre est mal défini : </xsl:text>
                        <xsl:value-of select="@name" />
                    </xsl:with-param>
                    <xsl:with-param name="content">WMS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="not(@description) or @description=''">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">le titre est vide</xsl:with-param>
                    <xsl:with-param name="content">WMS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">info</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:value-of select="." />
                    </xsl:with-param>
                    <xsl:with-param name="content">WMS</xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- test sur la description des WFS -->
  <xsl:template name="test-link-wfs">
    <xsl:for-each select="dc:URI[@protocol='OGC:WFS-1.0.0-http-get-capabilities'
        or @protocol='OGC:WFS-1.1.0-http-get-capabilities']">
        <xsl:if test="position() mod 4=0">
            <br />
        </xsl:if>
        <xsl:choose>
            <xsl:when test="@name='' or not(@name)">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">error</xsl:with-param>
                    <xsl:with-param name="title">le nom de la couche est vide</xsl:with-param>
                    <xsl:with-param name="content">WFS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="@description=@name">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:text>le titre est mal défini : </xsl:text>
                        <xsl:value-of select="@name" />
                    </xsl:with-param>
                    <xsl:with-param name="content">WFS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="not(@description) or @description=''">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">le titre est vide</xsl:with-param>
                    <xsl:with-param name="content">WFS</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">info</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:value-of select="." />
                    </xsl:with-param>
                    <xsl:with-param name="content">WFS</xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- test sur la description des downloads -->
  <xsl:template name="test-link-download">
    <xsl:for-each select="dc:URI[@protocol='WWW:DOWNLOAD-1.0-http--download']">
        <xsl:if test="position() mod 4=0">
            <br />
        </xsl:if>
        <xsl:choose>
            <xsl:when test=".=''">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">error</xsl:with-param>
                    <xsl:with-param name="title">adresse de téléchargement manquante</xsl:with-param>
                    <xsl:with-param name="content">DL</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="substring(.,1,4)!='http'">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">error</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:text>adresse de téléchargement erronée : </xsl:text>
                        <xsl:value-of select="." />
                    </xsl:with-param>
                    <xsl:with-param name="content">DL</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains(.,'version=1.00')">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">version WFS getFeature erronée</xsl:with-param>
                    <xsl:with-param name="content">DL</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="not(@description) or @description=''">
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">warning</xsl:with-param>
                    <xsl:with-param name="title">le titre est vide</xsl:with-param>
                    <xsl:with-param name="content">DL</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="flag-span">
                    <xsl:with-param name="severity">info</xsl:with-param>
                    <xsl:with-param name="title">
                        <xsl:value-of select="." />
                    </xsl:with-param>
                    <xsl:with-param name="content">DL</xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
