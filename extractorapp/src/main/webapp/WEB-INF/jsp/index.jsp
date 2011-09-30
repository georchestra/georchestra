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
            <li class="active"><a href="#">extracteur</a></li>
            <li><a href="/geoserver/web/">services</a></li>
            <li><a href="/phpldapadmin">utilisateurs</a></li>
        </ul>
        <!-- this won't work => we just need to include a mapfishapp/?login link if not logged / else display the username
        <form method="post" action="/cas/login?service=%2Fj_spring_cas_security_check">
            <input name="username" placeholder="nom d’utilisateur"/>
            <input name="password" type="password" placeholder="mot de passe"/>
            <button name="submit" value="LOGIN" type="submit">connection</button>
        </form>
        -->
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
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/Firebug/firebug.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="resources/lib/externals/openlayers/lib/OpenLayers/Lang/fr.js"></script>
    <script type="text/javascript" src="resources/lib/externals/geoext/lib/GeoExt.js"></script>
    <script type="text/javascript" src="resources/lib/GeoExt.ux/lib/GeoExt.ux.js"></script>
    
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
    <script type="text/javascript" src="resources/app/js/OpenLayers.Control.OutOfRangeLayers.js"></script>
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
