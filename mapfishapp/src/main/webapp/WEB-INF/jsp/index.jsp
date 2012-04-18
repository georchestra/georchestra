<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%
String LOGIN_URL = "?login";
Boolean anonymous = true;
Boolean editor = false;
Boolean admin = false;
String sec_roles = request.getHeader("sec-roles");
String js_roles = "";
if(sec_roles != null) {
    String[] roles = sec_roles.split(",");
    String[] js_roles_array = new String[roles.length];
    for (int i = 0; i < roles.length; i++) {
        // ROLE_ANONYMOUS is added by the security proxy:
        if (roles[i].equals("ROLE_ANONYMOUS")) {
            js_roles_array[0] = "'ROLE_ANONYMOUS'";
            break;
        }
        if (roles[i].equals("ROLE_SV_ADMIN")) {
            admin = true;
        }
        if (roles[i].equals("ROLE_SV_EDITOR") || roles[i].equals("ROLE_SV_REVIEWER") || roles[i].equals("ROLE_SV_ADMIN")) {
            editor = true;
            anonymous = false;
        }
        if (roles[i].equals("ROLE_SV_USER")) {
            anonymous = false;
        }
        js_roles_array[i] = "'"+roles[i]+"'";
    }
    js_roles = StringUtils.join(js_roles_array, ", ");
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
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <meta name="apple-mobile-web-app-capable" content="yes">
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
        #go_head ul {
            float: left;
            list-style: none;
            margin: 20px 0 0 10px;
            padding: 0;
            font-size: 18px;
            display: inline;
        }
        #go_head li {
            margin: 0;
            padding: 0;
            display: inline-block;
        }
        #go_head .logged {
            margin        : 20px 15px 0 0;
            border        : 1px dotted #ddd;
            border-radius : 0.3em;
            padding       : 0 0.6em;
            width         : auto;
            float         : right;
            height        : 52px;
            line-height   : 52px;
        }
    </style>
<c:choose>
    <c:when test='${c.edit != null}'>
    <title lang="fr" dir="ltr">Editeur - geOrchestra</title>
    </c:when>
    <c:otherwise>
    <title lang="fr" dir="ltr">Visualiseur - geOrchestra</title>
    </c:otherwise>
</c:choose>
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/xtheme-gray.css" />
    <link rel="stylesheet" type="text/css" href="lib/styler/theme/css/styler.css" />
    <link rel="stylesheet" type="text/css" href="lib/Ext.ux/lib/Ext.ux/widgets/palettecombobox/palettecombobox.ux.css" />
    <link rel="stylesheet" type="text/css" href="lib/Ext.ux/lib/Ext.ux/widgets/colorpicker/colorpicker.css" />
    <link rel="stylesheet" type="text/css" href="lib/Ext.ux/lib/Ext.ux/widgets/spinner/Spinner.css" />
    <link rel="stylesheet" type="text/css" href="app/openlayers_gray_theme/style.css" />
    <link rel="stylesheet" type="text/css" href="app/css/main.css" />
<c:choose>
    <c:when test='<%= request.getParameter("noheader") != null %>'>
    <script type="text/javascript">
        GEOR = {
            header: false
        };
    </script>
    </c:when>
    <c:otherwise>
    <!-- 
     * The following resource will be loaded only when geOrchestra's "static" module
     *  is deployed alongside with mapfishapp
     *-->
    <link rel="stylesheet" type="text/css" href="/static/css/header.css" />
    <script type="text/javascript">
        GEOR = {
            header: true
        };
    </script>
    </c:otherwise>
</c:choose>
</head>

<body>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <a href="#" id="go_home" title="retourner à l’accueil">
            <img src="/static/img/logo.png" alt="geOrchestra" height="50"/>
        </a>
        <ul>
            <li><a href="/geonetwork/srv/fr/main.home">catalogue</a></li>
        <c:choose>
            <c:when test='${c.edit != null}'>
            <li><a href="/mapfishapp">visualiseur</a></li>
            </c:when>
            <c:otherwise>
            <li class="active"><a href="#">visualiseur</a></li>
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test='<%= editor == true %>'>
                <c:choose>
                    <c:when test='${c.edit != null}'>
            <li class="active"><a href="#">éditeur</a></li>
                    </c:when>
                    <c:otherwise>
            <li><a href="/mapfishapp/edit">éditeur</a></li>
                    </c:otherwise>
                </c:choose>
            </c:when>
        </c:choose>
            <li><a href="/extractorapp/">extracteur</a></li>
            <li><a href="/geoserver/web/">services</a></li>
        <c:choose>
            <c:when test='<%= admin == true %>'>
            <li><a href="/phpldapadmin">utilisateurs</a></li>
            </c:when>
        </c:choose>
        </ul>
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
        <p class="logged">
            <%=request.getHeader("sec-username") %><span class="light"> | </span><a href="/j_spring_security_logout">déconnexion</a>
        </p>
        </c:when>
        <c:otherwise>
        <p class="logged">
            <a href="?login">connexion</a>
        </p>
        </c:otherwise>
    </c:choose>
    </div>

    <script>
        (function(){
            if (!window.addEventListener || !document.querySelectorAll) return;
            var each = function(els, callback) {
                for (var i = 0, l=els.length ; i<l ; i++) {
                    callback(els[i]);
                }
            }
            each(document.querySelectorAll('#go_head li a'), function(li){
                li.addEventListener('click', function(e) {
                    each(
                        document.querySelectorAll('#go_head li'),
                        function(l){ l.className = '';}
                    );
                    li.parentNode.className = 'active';
                });
            });
        })();
    </script>
    </c:when>
</c:choose>

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
    
    <!-- invisible iframe for actions such as "load in JOSM" -->
    <iframe style="position: absolute; width: 1px; height: 1px; top: -1em;visibility:hidden;" tabindex="-1" aria-hidden="true" frameborder="0" width="0" height="0" marginheight="0" marginwidth="0" scrolling="no"></iframe>
    

    <script type="text/javascript" src="lib/externals/ext/adapter/ext/ext-base.js"></script>
    
    <!--
        loading custom parameters (see build profile)
    -->
    <script type="text/javascript" src="app/js/GEOR_custom.js"></script>
    
    <c:choose>
        <c:when test='<%= request.getParameter("debug") != null %>'>
    <script type="text/javascript" src="lib/externals/geoext/lib/overrides/override-ext-ajax.js"></script>
    <script type="text/javascript" src="lib/externals/ext/ext-all-debug.js"></script>
    <script type="text/javascript" src="lib/Ext.ux/lib/Ext.ux.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/proj4js.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/projCode/lcc.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/projCode/merc.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers/Lang/fr.js"></script>
    <script type="text/javascript" src="lib/externals/geoext/lib/GeoExt.js"></script>
    <script type="text/javascript" src="lib/externals/sandbox/ux/OpenAddressesSearchCombo/lib/GeoExt.ux/OpenAddressesSearchCombo.js"></script>
    <script type="text/javascript" src="lib/externals/ext/src/locale/ext-lang-fr.js"></script>

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
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/MultiSliderTip.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/SliderTip.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/TextSymbolizer.js"></script>

    <script type="text/javascript" src="app/js/GEOR_config.js"></script>
    <script type="text/javascript" src="app/js/GEOR_Lang/fr.js"></script>
            <c:choose>
                <c:when test='${c.edit}'>
    <script type="text/javascript" src="lib/Ext.ux/lib/Ext.ux/widgets/collapsedtitle/PanelCollapsedTitle.js"></script>
    <script type="text/javascript" src="app/js/GEOR_Editing/GEOR_LayerEditingPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_Editing/GEOR_EditingPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_editing.js"></script>
                </c:when>
                <c:otherwise>
    <script type="text/javascript" src="app/js/GEOR_querier.js"></script>
    <script type="text/javascript" src="app/js/GEOR_styler.js"></script>
    <script type="text/javascript" src="app/js/GEOR_getfeatureinfo.js"></script>
    <script type="text/javascript" src="app/js/GEOR_resultspanel.js"></script>
                </c:otherwise>
            </c:choose>
    <script type="text/javascript" src="app/js/GEOR_util.js"></script>
    <script type="text/javascript" src="app/js/GEOR_FeatureDataModel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_ClassificationPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_ows.js"></script>
    <script type="text/javascript" src="app/js/GEOR_wmc.js"></script>
    <script type="text/javascript" src="app/js/GEOR_waiter.js"></script>
    <script type="text/javascript" src="app/js/GEOR_referentials.js"></script>
    <script type="text/javascript" src="app/js/GEOR_geonames.js"></script>
    <script type="text/javascript" src="app/js/GEOR_address.js"></script>
    <script type="text/javascript" src="app/js/GEOR_scalecombo.js"></script>
    <script type="text/javascript" src="app/js/GEOR_toolbar.js"></script>
    <script type="text/javascript" src="app/js/GEOR_workspace.js"></script>
    <script type="text/javascript" src="app/js/GEOR_mappanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_managelayers.js"></script>
    <script type="text/javascript" src="app/js/GEOR_layerfinder.js"></script>
    <script type="text/javascript" src="app/js/GEOR_cswbrowser.js"></script>
    <script type="text/javascript" src="app/js/GeoExt.data.CSW.js"></script>
    <script type="text/javascript" src="app/js/GEOR_cswquerier.js"></script>
    <script type="text/javascript" src="app/js/GEOR_wmsbrowser.js"></script>
    <script type="text/javascript" src="app/js/GEOR_wfsbrowser.js"></script>
    <script type="text/javascript" src="app/js/GEOR_print.js"></script>
    <script type="text/javascript" src="app/js/GEOR_map.js"></script>
    <script type="text/javascript" src="app/js/GEOR_ajaxglobal.js"></script>
    <script type="text/javascript" src="app/js/GEOR_mapinit.js"></script>
    <script type="text/javascript" src="app/js/GEOR.js"></script>
    <script type="text/javascript" src="https://getfirebug.com/firebug-lite-beta.js"></script>
        </c:when>
        <c:otherwise>
    <script type="text/javascript" src="lib/externals/ext/ext-all.js"></script>
            <c:choose>
                <c:when test='${c.edit}'>
    <script type="text/javascript" src="build/mapfisheditapp.js"></script>
                </c:when>
                <c:otherwise>
    <script type="text/javascript" src="build/mapfishapp.js"></script>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>

    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        <% 
          String proxyHost = "/proxy/?url=";
          if(request.getContextPath().equals("/mapfishapp")) {
            proxyHost = "ws/ogcproxy/?url=";
          }
        %>
        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';
        
        // mapfishapp initial state: open a WMC, or a mix of WMS layers and servers
        GEOR.initstate = ${c.data};

        // custom WMC loader 
    <c:choose>
        <c:when test='<%= request.getParameter("wmc") != null %>'>
        GEOR.config.CUSTOM_WMC = '<%=request.getParameter("wmc") %>';
        </c:when>
    </c:choose>
    
        // security stuff
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
        GEOR.config.ANONYMOUS = false;
        GEOR.config.USERNAME = "<%=request.getHeader("sec-username") %>";
        </c:when>
    </c:choose>
        GEOR.config.ROLES = [<%= js_roles %>];
    </script>
    <noscript><p>Cette application nécessite le support de JavaScript par votre navigateur. Merci de l'activer.</p></noscript>
</body>
    </c:otherwise>
</c:choose>
</html>
