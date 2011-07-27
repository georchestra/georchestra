<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr" xml:lang="fr">

<head>
    <meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="resources/lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="resources/lib/externals/ext/resources/css/xtheme-gray.css" />
    <link rel="stylesheet" type="text/css" href="resources/lib/externals/mapfish/mapfish.css" />
    <link rel="stylesheet" type="text/css" href="resources/app/openlayers_gray_theme/style.css" />
    <style type="text/css">
        body {
            background: #ffffff;
        }
        #loading {
            position: absolute;
            left: 45%;
            top: 40%;
            padding: 2px;
            z-index: 20001;
            height: auto;
        }
        #loading-msg {
            font: normal 12px arial,tahoma,sans-serif;
        }
    </style>
    <link rel="stylesheet" type="text/css" href="resources/app/css/main.css" />
    <title lang="fr" dir="ltr">geOrchestra - extracteur</title>
</head>

<body>
    <div id="waiter">
        <span>Chargement ...</span>
    </div>
    <div id="loading">
        <img src="resources/app/img/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/> 
        <span id="loading-msg">Chargement...</span>
    </div>
    <script type="text/javascript">
        document.getElementById('loading-msg').innerHTML = 'Chargement...';
    </script>

    <script type="text/javascript" src="resources/lib/externals/ext/adapter/ext/ext-base.js"></script>

    <c:choose>
        <c:when test='${c.debug}'>
    <script type="text/javascript" src="resources/lib/externals/geoext/lib/overrides/override-ext-ajax.js"></script>
    <script type="text/javascript" src="resources/lib/externals/ext/ext-all-debug.js"></script>
    <script type="text/javascript" src="resources/lib/Ext.ux/lib/Ext.ux.js"></script>
    <script type="text/javascript" src="resources/lib/proj4js/lib/proj4js.js"></script>
    <script type="text/javascript" src="resources/lib/proj4js/lib/projCode/lcc.js"></script>
    <script type="text/javascript" src="resources/lib/proj4js/lib/projCode/merc.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/Firebug/firebug.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/OpenLayers/Lang/fr.js"></script>
    <script type="text/javascript" src="resources/lib/externals/geoext/lib/GeoExt.js"></script>
    <script type="text/javascript" src="resources/lib/GeoExt.ux/lib/GeoExt.ux.js"></script>
    <script type="text/javascript" src="resources/lib/externals/mapfish/MapFish.js"></script>
    <script type="text/javascript" src="resources/lib/externals/mapfish/lang/fr.js"></script>
    
    <script type="text/javascript" src="resources/lib/addins/loadingPanel/trunk/lib/OpenLayers/Control/LoadingPanel.js"></script>

    <script type="text/javascript" src="resources/app/js/GEOR_util.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_ows.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_waiter.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_data.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_config.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_proj4jsdefs.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_toolbar.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_map.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_layerstree.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_layeroptions.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_referentials.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_ajaxglobal.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR.js"></script>
        </c:when>
        <c:otherwise>
    <script type="text/javascript" src="resources/lib/externals/ext/ext-all.js"></script>
    <script type="text/javascript" src="resources/build/extractorapp.js"></script>
        </c:otherwise>
    </c:choose>
    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        <% 
          String proxyHost = "/proxy/?url=";
          if(request.getContextPath().equals("/extractorapp")) {
            proxyHost = "/extractorapp/ws/ogcproxy/?url=";
          }
        %>
        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';
        GEOR.data.debug = ${c.debug};
    <c:choose>
        <c:when test='${c.fake}'>
        GEOR.data.services = [
            /*{
                text: "BRGM Risques",
                owstype: "WMS",
                owsurl: "http://geoservices.brgm.fr/risques"
            }, {
                text: "Gest'eau",
                owstype: "WMS",
                owsurl: "http://gesteau.oieau.fr/service"
            }*/
        ];

        GEOR.data.layers = [
            {
                layername: "ortho",
                owstype: "WMS",
                owsurl: "http://bmo.openstreetmap.fr/ows"
            },{
                owstype: "WMS",
                owsurl: "http://geolittoral.application.equipement.gouv.fr/wms/metropole",
                layername: "Sentiers_littoraux"
            }, {
                owstype: "WMS",
                owsurl: "http://sd1878-2.sivit.org/geoserver/wms",
                layername: "topp:RCLC90_L2E"
            }, {
                owstype: "WMS",
                owsurl: "http://geoservices.brgm.fr/risques",
                layername: "BASIAS_LOCALISE"
            }
        ];
        // we want all layers unchecked by default
        GEOR.config.LAYERS_CHECKED = false;
        </c:when>
        <c:otherwise>
        GEOR.data.layers = ${c.layers};
        GEOR.data.services = ${c.services};
        // layers come from catalog: we want them all checked
        GEOR.config.LAYERS_CHECKED = true;
        </c:otherwise>
    </c:choose>
    </script>
    <c:choose>
        <c:when test='<%= request.getParameter("jsc") != null %>'>
        <!-- Force GEOR.data.services and GEOR.data.layers from an external JS file -->
    <script type="text/javascript" src="<%=request.getParameter("jsc") %>"></script>
    <script type="text/javascript">
        // we want all layers unchecked by default
        GEOR.config.LAYERS_CHECKED = false;
    </script>
        </c:when>
    </c:choose>
    <%
    String roles = request.getHeader("sec-roles");
    Boolean anonymous = false;
    if((roles == null) || roles.equals("ROLE_ANONYMOUS")) {
        anonymous = true;
    }
    %>
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
    <script type="text/javascript">
        GEOR.data.anonymous = false;
        GEOR.data.username = "<%=request.getHeader("sec-username") %>";
        GEOR.data.email = "<%=request.getHeader("sec-email") %>";
    </script>
        </c:when>
    </c:choose>
    <noscript><p>Cette application nécessite le support de JavaScript par votre navigateur. Merci de l'activer.</p></noscript>
</body>
</html>
