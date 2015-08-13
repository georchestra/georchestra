<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.catalogapp.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8"%>
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
ResourceBundle resource = Utf8ResourceBundle.getBundle("catalogapp.i18n.index",l);
javax.servlet.jsp.jstl.core.Config.set(
    request,
    javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
    new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource)
);

String sec_roles = request.getHeader("sec-roles");
if(sec_roles != null) {
    String[] roles = sec_roles.split(";");
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
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%= lang %>" xml:lang="<%= lang %>">

<head>
    <meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
    <title lang="<%= lang %>" dir="ltr"><fmt:message key="title.catalogue"/> - ${instance}</title>
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
    </style>
    <link rel="stylesheet" type="text/css" href="app/css/main.css" />
    <script type="text/javascript">
        GEOR = {
            header: <%= request.getParameter("noheader") == null %>
        };
    </script>
</head>

<body>

    <%@ include file="header.jsp" %>

    <div id="waiter" style="display:none;">
        <span><fmt:message key="loading"/></span>
    </div>
    <div id="loading">
        <img src="app/img/loading.gif" alt="<fmt:message key='loading'/>" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
        <span id="loading-msg"><fmt:message key="loading"/></span>
    </div>
    <div id="dataview-contentel" class="x-hidden">
        <p><fmt:message key="default.help"/></p>
    </div>
    <script type="text/javascript">
        document.getElementById('loading-msg').innerHTML = '<fmt:message key="loading"/>';
    </script>
    
    <script type="text/javascript" src="lib/externals/ext/adapter/ext/ext-base.js"></script>
    
    <!--
        loading custom parameters (see build profile)
    -->
    <script type="text/javascript" src="app/js/GEOR_custom.js"></script>
    <c:choose>
        <c:when test='<%= request.getParameter("debug") != null %>'>
    <%@ include file="debug-includes.jsp" %>
            </c:when>
        <c:otherwise>
    <script type="text/javascript" src="lib/externals/ext/ext-all.js"></script>
    <script type="text/javascript" src="build/catalogapp.js"></script>
    <script type="text/javascript" src="build/lang/<%= lang %>.js"></script>
        </c:otherwise>
    </c:choose>

    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        <% 
          String proxyHost = "/proxy/?url=";
          if(request.getHeader("sec-proxy") == null) {
            proxyHost = "ws/ogcproxy/?url=";
          }
        %>

        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';
        
        // lang
        GEOR.config.LANG = '<%= lang %>';
    </script>
    <noscript><p><fmt:message key="need.javascript"/></p></noscript>
</body>
</html>
