<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;
Boolean admin = false;
Boolean editor = false;
String sec_roles = request.getHeader("sec-roles");
if(sec_roles != null) {
    String[] roles = sec_roles.split(",");
    for (int i = 0; i < roles.length; i++) {
        // ROLE_ANONYMOUS is added by the security proxy:
        if (roles[i].equals("ROLE_ANONYMOUS")) {
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
    }
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr" xml:lang="fr">

<head>
    <meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="resources/lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="resources/lib/externals/ext/examples/ux/css/MultiSelect.css"/>
    <link rel="stylesheet" type="text/css" href="resources/lib/externals/ext/resources/css/xtheme-gray.css" />
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
    <link rel="stylesheet" type="text/css" href="resources/app/css/main.css" />

    <title lang="fr" dir="ltr">Extracteur - geOrchestra</title>
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
            <li><a href="/mapfishapp/">visualiseur</a></li>
        <c:choose>
            <c:when test='<%= editor == true %>'>
            <li><a href="/mapfishapp/edit">éditeur</a></li>
            </c:when>
        </c:choose>
            <li class="active"><a href="#">extracteur</a></li>
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
        <img src="resources/app/img/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/> 
        <span id="loading-msg">Chargement...</span>
    </div>
    <script type="text/javascript">
        document.getElementById('loading-msg').innerHTML = 'Chargement...';
    </script>

    <script type="text/javascript" src="resources/lib/externals/ext/adapter/ext/ext-base.js"></script>

    <!--
        loading custom parameters (see build profile)
    -->	
    <script type="text/javascript" src="resources/app/js/GEOR_custom.js"></script>

    <c:choose>
        <c:when test='${c.debug}'>
    <script type="text/javascript" src="resources/lib/externals/geoext/lib/overrides/override-ext-ajax.js"></script>
    <script type="text/javascript" src="resources/lib/externals/ext/ext-all-debug.js"></script>
    <script type="text/javascript" src="resources/lib/Ext.ux/lib/Ext.ux.js"></script>
    <script type="text/javascript" src="resources/lib/proj4js/lib/proj4js-compressed.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/OpenLayers/Lang/fr.js"></script>
    <script type="text/javascript" src="resources/lib/externals/geoext/lib/GeoExt.js"></script>
    <script type="text/javascript" src="resources/lib/GeoExt.ux/lib/GeoExt.ux.js"></script>
    
    <script type="text/javascript" src="resources/lib/addins/loadingPanel/trunk/lib/OpenLayers/Control/LoadingPanel.js"></script>
    <script type="text/javascript" src="resources/app/js/OpenLayers.Control.OutOfRangeLayers.js"></script>
    
    <script type="text/javascript" src="resources/app/js/GEOR_util.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_ows.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_waiter.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_data.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_config.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_dlform.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_proj4jsdefs.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_toolbar.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_map.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_layerstree.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_layeroptions.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_referentials.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR_ajaxglobal.js"></script>
    <script type="text/javascript" src="resources/app/js/GEOR.js"></script>
    <script type="text/javascript" src="https://getfirebug.com/firebug-lite-beta.js"></script>
    
    <script type="text/javascript" src="resources/lib/externals/ext/examples/ux/MultiSelect.js"></script>
    
        </c:when>
        <c:otherwise>
    <script type="text/javascript" src="resources/lib/externals/ext/ext-all.js"></script>
    <script type="text/javascript" src="resources/build/extractorapp.js"></script>
        </c:otherwise>
    </c:choose>
    <script type="text/javascript" src="resources/lib/externals/ext/src/locale/ext-lang-fr.js"></script>
    
    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        <% 
          String proxyHost = "/proxy/?url=";
          Boolean jettyrun = false;
          if(request.getContextPath().equals("/extractorapp")) {
            proxyHost = "/extractorapp/ws/ogcproxy/?url=";
            jettyrun = true;
          }
        %>
        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';
        GEOR.data.debug = ${c.debug};
        GEOR.data.jettyrun = <%= jettyrun %>;
    <c:choose>
        <c:when test='${c.fake}'>
        GEOR.data.services = GEOR.config.STARTUP_SERVICES;
        GEOR.data.layers = GEOR.config.STARTUP_LAYERS;
        // we want all layers unchecked by default:
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
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
    <script type="text/javascript">
        GEOR.data.anonymous = false;
        GEOR.data.username = "<%=(request.getHeader("sec-username") == null ? "" : request.getHeader("sec-username"))%>";
        GEOR.data.email = "<%=(request.getHeader("sec-email") == null ? "" : request.getHeader("sec-email"))%>";
        GEOR.data.first_name = "<%=(request.getHeader("sec-firstname") == null ? "" : request.getHeader("sec-firstname"))%>";
        GEOR.data.last_name = "<%=(request.getHeader("sec-lastname") == null ? "" : request.getHeader("sec-lastname"))%>";
        GEOR.data.company = "<%=(request.getHeader("sec-org") == null ? "" : request.getHeader("sec-org"))%>";
        GEOR.data.tel = "<%=(request.getHeader("sec-tel") == null ? "" : request.getHeader("sec-tel"))%>";
    </script>
        </c:when>
    </c:choose>
    <noscript><p>Cette application nécessite le support de JavaScript par votre navigateur. Merci de l'activer.</p></noscript>
</body>
</html>
