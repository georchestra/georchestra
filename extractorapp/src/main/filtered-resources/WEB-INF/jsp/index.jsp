<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.extractorapp.ws.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;
Boolean admin = false;
Boolean editor = false;

String lang = request.getParameter("lang");
if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("fr") && !lang.equals("de"))) {
    lang = "${language}";
}
Locale l = new Locale(lang);
ResourceBundle resource = org.georchestra.extractorapp.ws.Utf8ResourceBundle.getBundle("org.georchestra.extractorapp.i18n.index",l);
javax.servlet.jsp.jstl.core.Config.set(
    request,
    javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
    new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource)
);

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
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%= lang %>" xml:lang="<%= lang %>">

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
    </style>
    <link rel="stylesheet" type="text/css" href="resources/app/css/main.css" />

    <title lang="<%= lang %>" dir="ltr"><fmt:message key="title"/> - ${instance}</title>
    <script type="text/javascript">
        GEOR = {
            header: <%= request.getParameter("noheader") == null %>
        };
    </script>
</head>

<body>

    <%@ include file="header.jsp" %>

    <div id="waiter">
        <span><fmt:message key="loading"/></span>
    </div>
    <div id="loading">
        <img src="resources/app/img/loading.gif" alt="<fmt:message key='loading'/>" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/> 
        <span id="loading-msg"><fmt:message key="loading"/></span>
    </div>
    <script type="text/javascript">
        document.getElementById('loading-msg').innerHTML = "<fmt:message key='loading'/>";
    </script>

    <script type="text/javascript" src="resources/lib/externals/ext/adapter/ext/ext-base.js"></script>

    <!--
        loading custom parameters (see build profile)
    -->	
    <script type="text/javascript" src="resources/app/js/GEOR_custom.js"></script>

    <c:choose>
        <c:when test='${c.debug}'>
    <%@ include file="debug-includes.jsp" %>
        </c:when>
        <c:otherwise>
    <script type="text/javascript" src="resources/lib/externals/ext/ext-all.js"></script>
    <script type="text/javascript" src="resources/build/extractorapp.js"></script>
    <script type="text/javascript" src="resources/build/lang/<%= lang %>.js"></script>
        </c:otherwise>
    </c:choose>
    <script type="text/javascript" src="resources/lib/externals/ext/src/locale/ext-lang-<%= lang %>.js"></script>
    
    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        // Lang
        GEOR.config.LANG = '<%= lang %>';

        <% 
          String proxyHost = "/proxy/?url=";
          Boolean jettyrun = false;
          if(request.getHeader("sec-proxy") == null) {
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
    <noscript><p><fmt:message key="need.javascript"/></p></noscript>
</body>
</html>
