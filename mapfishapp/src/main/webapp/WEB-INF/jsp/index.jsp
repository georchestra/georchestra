<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
String LOGIN_URL = "?login";
Boolean anonymous = true;
Boolean editor = false;
String sec_roles = request.getHeader("sec-roles");
if(sec_roles != null) {
    String[] roles = sec_roles.split(",");
    for (int i = 0; i < roles.length; i++) {
        if(roles[i].equals("ROLE_ANONYMOUS")) {
            // default is anonymous already
            break;
        }
        else if (roles[i].equals("ROLE_SV_EDITOR") || roles[i].equals("ROLE_SV_REVIEWER") || roles[i].equals("ROLE_SV_ADMIN")) {
            anonymous = false;
            editor = true;
        }
        else {
            anonymous = false;
        }
    }
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr" xml:lang="fr">

<c:choose>
    <c:when test='${c.edit != null && !c.edit}'>
<head>
    <title>Accès refusé</title>
</head>
        <c:choose>
            <c:when test='<%= anonymous == true %>'>
    <script type="text/javascript">
        // anonymous users cannot access this protected edit page
        window.location = "?login";
    </script>
            </c:when>
            <c:otherwise>
    <% response.sendError(403); %>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
<head>
    <meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7"/>
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
    <title lang="fr" dir="ltr">geOrchestra - visualiseur</title>
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/xtheme-gray.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/mapfish/mapfish.css" />
    <link rel="stylesheet" type="text/css" href="lib/styler/theme/css/styler.css" />
    <link rel="stylesheet" type="text/css" href="Ext.ux/lib/Ext.ux/widgets/palettecombobox/palettecombobox.ux.css" />
    <link rel="stylesheet" type="text/css" href="Ext.ux/lib/Ext.ux/widgets/colorpicker/color-picker.ux.css" />
    <link rel="stylesheet" type="text/css" href="Ext.ux/lib/Ext.ux/widgets/spinner/Spinner.css" />
    <link rel="stylesheet" type="text/css" href="app/openlayers_gray_theme/style.css" />
    <link rel="stylesheet" type="text/css" href="app/css/main.css" />
</head>

<body>
    <div id="waiter">
        <span>Chargement ...</span>
    </div>
    <div id="loading">
        <img src="app/img/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/> 
        <span id="loading-msg">Chargement...</span>
    </div>
    <script type="text/javascript">
        document.getElementById('loading-msg').innerHTML = 'Chargement...';
    </script>

    <script type="text/javascript" src="lib/externals/ext/adapter/ext/ext-base.js"></script>

    <c:choose>
        <c:when test='<%= request.getParameter("debug") != null %>'>
    <script type="text/javascript" src="lib/externals/geoext/lib/overrides/override-ext-ajax.js"></script>
    <script type="text/javascript" src="lib/externals/ext/ext-all-debug.js"></script>
    <script type="text/javascript" src="Ext.ux/lib/Ext.ux.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/proj4js.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/projCode/lcc.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/projCode/merc.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/Firebug/firebug.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers/Lang/fr.js"></script>
    <script type="text/javascript" src="lib/externals/geoext/lib/GeoExt.js"></script>
    <script type="text/javascript" src="lib/externals/mapfish/MapFish.js"></script>
    <script type="text/javascript" src="lib/externals/mapfish/lang/fr.js"></script>
    <script type="text/javascript" src="lib/externals/sandbox/ux/OpenAddressesSearchCombo/lib/GeoExt.ux/OpenAddressesSearchCombo.js"></script>

    <script type="text/javascript" src="lib/addins/loadingPanel/trunk/lib/OpenLayers/Control/LoadingPanel.js"></script>

    <script type="text/javascript" src="lib/styler/lib/Styler/Util.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/SchemaManager.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/SLDManager.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/dispatch.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/StrokeSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FillSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/PointSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/RulePanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/RuleBuilder.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/MultiSlider.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/BaseFilterPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/SpatialFilterPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FilterPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/LineSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/RuleChooser.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/form/SpatialComboBox.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/form/ComparisonComboBox.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/form/FontComboBox.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/ScaleSlider.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/LegendPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FeatureRenderer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/PolygonSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/ScaleLimitPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FilterBuilder.js"></script>
    <!--<script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/ScaleSliderTip.js"></script>-->
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/MultiSliderTip.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/SliderTip.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/TextSymbolizer.js"></script>


    <script type="text/javascript" src="app/js/GEOB_config.js"></script>
            <c:choose>
                <c:when test='${c.edit}'>
    <script type="text/javascript" src="app/js/GEOB_Editing/GEOB_LayerEditingPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOB_Editing/GEOB_EditingPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOB_editing.js"></script>
                </c:when>
                <c:otherwise>
    <script type="text/javascript" src="app/js/GEOB_querier.js"></script>
    <script type="text/javascript" src="app/js/GEOB_styler.js"></script>
    <script type="text/javascript" src="app/js/GEOB_getfeatureinfo.js"></script>
    <script type="text/javascript" src="app/js/GEOB_resultspanel.js"></script>
                </c:otherwise>
            </c:choose>
    <script type="text/javascript" src="app/js/GEOB_util.js"></script>
    <script type="text/javascript" src="app/js/GEOB_FeatureDataModel.js"></script>
    <script type="text/javascript" src="app/js/GEOB_ClassificationPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOB_ows.js"></script>
    <script type="text/javascript" src="app/js/GEOB_wmc.js"></script>
    <script type="text/javascript" src="app/js/GEOB_waiter.js"></script>
    <script type="text/javascript" src="app/js/GEOB_referentials.js"></script>
    <script type="text/javascript" src="app/js/GEOB_recenter.js"></script>
    <script type="text/javascript" src="app/js/GEOB_address.js"></script>
    <script type="text/javascript" src="app/js/GEOB_proj4jsdefs.js"></script>
    <script type="text/javascript" src="app/js/GEOB_scalecombo.js"></script>
    <script type="text/javascript" src="app/js/GEOB_toolbar.js"></script>
    <script type="text/javascript" src="app/js/GEOB_workspace.js"></script>
    <script type="text/javascript" src="app/js/GEOB_mappanel.js"></script>
    <script type="text/javascript" src="app/js/GEOB_managelayers.js"></script>
    <script type="text/javascript" src="app/js/GEOB_layerfinder.js"></script>
    <script type="text/javascript" src="app/js/GEOB_print.js"></script>
    <script type="text/javascript" src="app/js/GEOB_map.js"></script>
    <script type="text/javascript" src="app/js/GEOB_ajaxglobal.js"></script>
    <script type="text/javascript" src="app/js/GEOB_mapinit.js"></script>
    <script type="text/javascript" src="app/js/GEOB.js"></script>
    <script type="text/javascript" src="https://getfirebug.com/firebug-lite-beta.js"></script>
        </c:when>
        <c:otherwise>
    <script type="text/javascript" src="lib/externals/ext/ext-all.js"></script>
            <c:choose>
                <c:when test='${c.edit}'>
    <script type="text/javascript" src="build/mapfish/mapfisheditapp.js"></script>
                </c:when>
                <c:otherwise>
    <script type="text/javascript" src="build/mapfish/mapfishapp.js"></script>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
    <script type="text/javascript" src="lib/externals/ext/source/locale/ext-lang-fr.js"></script>

    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        // set proxy host
        OpenLayers.ProxyHost = '/proxy/?url=';
        
        // mapfishapp initial state: open a WMC, or a mix of WMS layers and servers ?
        GEOB.initstate = ${c.data};

    <c:choose>
        <c:when test='<%= anonymous == false %>'>
        // security stuff
        GEOB.config.ANONYMOUS = false;
        GEOB.config.USERNAME = "<%=request.getHeader("sec-username") %>";
        </c:when>
    </c:choose>
    
    </script>
    <noscript><p>Cette application nécessite le support de JavaScript par votre navigateur. Merci de l'activer.</p></noscript>
</body>
    </c:otherwise>
</c:choose>
</html>
