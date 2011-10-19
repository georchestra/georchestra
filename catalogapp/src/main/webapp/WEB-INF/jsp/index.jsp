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
    <title lang="fr" dir="ltr">Catalogue - geOrchestra</title>
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/xtheme-gray.css" />
    <!--
    <link rel="stylesheet" type="text/css" href="app/openlayers_gray_theme/style.css" />-->
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
            <li class="active"><a href="#">catalogue</a></li>
            <li><a href="/mapfishapp/">visualiseur</a></li>
        <c:choose>
            <c:when test='<%= editor == true %>'>
            <li><a href="/mapfishapp/edit">éditeur</a></li>
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

    <div id="waiter" style="display:none;">
        <span>Chargement ...</span>
    </div>
    <div id="loading">
        <img src="app/img/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
        <span id="loading-msg">Chargement...</span>
    </div>
    <div id="dataview" class="x-hide-display">
        <p>Trouvez des données en cherchant par mots clés ou par région.</p>
    </div>
    <script type="text/javascript">
        document.getElementById('loading-msg').innerHTML = 'Chargement...';
    </script>
    
    <script type="text/javascript" src="lib/externals/ext/adapter/ext/ext-base.js"></script>
    
    <!--
        loading custom parameters (see build profile)
    -->
    <script type="text/javascript" src="app/js/GEOR.custom.js"></script>
    <c:choose>
        <c:when test='<%= request.getParameter("debug") != null %>'>
    <script type="text/javascript" src="lib/externals/geoext/lib/overrides/override-ext-ajax.js"></script>
    <script type="text/javascript" src="lib/externals/ext/ext-all-debug.js"></script>
    <script type="text/javascript" src="lib/Ext.ux/lib/Ext.ux.js"></script>
    <script type="text/javascript" src="lib/externals/ext/src/locale/ext-lang-fr.js"></script>

    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers/Lang/fr.js"></script>
    <script type="text/javascript" src="lib/externals/geoext/lib/GeoExt.js"></script>
    
    <script type="text/javascript" src="app/js/GeoExt.data.CSW.js"></script>

    <script type="text/javascript" src="app/js/GEOR.config.js"></script>
    <script type="text/javascript" src="app/js/GEOR.waiter.js"></script>
    <script type="text/javascript" src="app/js/GEOR.nav.js"></script>
    <script type="text/javascript" src="app/js/GEOR.dataview.js"></script>
    <script type="text/javascript" src="app/js/GEOR.what.js"></script>
    <script type="text/javascript" src="app/js/GEOR.where.js"></script>
    <script type="text/javascript" src="app/js/GEOR.csw.js"></script>
    <script type="text/javascript" src="app/js/GEOR.js"></script>
    
    <script type="text/javascript" src="https://getfirebug.com/firebug-lite-beta.js"></script>
            </c:when>
        <c:otherwise>
    <script type="text/javascript" src="lib/externals/ext/ext-all.js"></script>
    <script type="text/javascript" src="build/catalogapp.js"></script>
        </c:otherwise>
    </c:choose>

    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();
        
        <% 
          String proxyHost = "/proxy/?url=";
          if(request.getContextPath().equals("/catalogapp")) {
            proxyHost = "ws/ogcproxy/?url=";
          }
        %>
        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';
        
    </script>
    <noscript><p>Cette application nécessite le support de JavaScript par votre navigateur. Merci de l'activer.</p></noscript>
</body>
</html>
