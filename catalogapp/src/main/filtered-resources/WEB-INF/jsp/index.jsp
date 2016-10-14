<%--

 Copyright (C) 2009-2016 by the geOrchestra PSC

 This file is part of geOrchestra.

 geOrchestra is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option)
 any later version.

 geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 more details.

 You should have received a copy of the GNU General Public License along with
 geOrchestra.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>
<%@ page import="org.georchestra.catalogapp.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%

Boolean anonymous = true;
Boolean admin = false;
Boolean editor = false;

String instanceName = null;
String defaultLanguage = null;
String georCustomPath  = "app/js/GEOR_custom.js";

try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  instanceName = ctx.getBean(GeorchestraConfiguration.class).getProperty("instance");
  defaultLanguage = ctx.getBean(GeorchestraConfiguration.class).getProperty("language");
  if (ctx.getBean(GeorchestraConfiguration.class).activated()) {
    georCustomPath = "ws" + georCustomPath ;
  }
} catch (Exception e) {}

String lang = request.getParameter("lang");
if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("fr") && !lang.equals("de"))) {
  if (defaultLanguage != null) {
    lang = defaultLanguage;
  }
  else {
    lang = "${language}";
  }
}

if (instanceName == null) {
  instanceName = "${instance}";
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
        if (roles[i].equals("ROLE_GN_ADMIN")) {
            admin = true;
        }
        if (roles[i].equals("ROLE_GN_EDITOR") || roles[i].equals("ROLE_GN_REVIEWER") || roles[i].equals("ROLE_GN_ADMIN")) {
            editor = true;
            anonymous = false;
        }
        if (roles[i].equals("ROLE_USER")) {
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
    <title lang="<%= lang %>" dir="ltr"><fmt:message key="title.catalogue"/> - <%= instanceName %></title>
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/xtheme-gray.css" />
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
    <script type="text/javascript" src="<%= georCustomPath %>"></script>
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
