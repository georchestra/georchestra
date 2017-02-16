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
<%@ page import="org.georchestra.mapfishapp.ws.Utf8ResourceBundle" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>


<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;
Boolean admin = false;
Boolean georDatadirActivated = false;

String instanceName = null;
String defaultLanguage = null;
String georCustomPath = "/app/js/GEOR_custom.js";

try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  instanceName = ctx.getBean(GeorchestraConfiguration.class).getProperty("instance");
  defaultLanguage = ctx.getBean(GeorchestraConfiguration.class).getProperty("language");
  if ((ctx.getBean(GeorchestraConfiguration.class) != null)
    && (((GeorchestraConfiguration) ctx.getBean(GeorchestraConfiguration.class)).activated())) {
      georDatadirActivated = true;
      georCustomPath = "/ws/app/js/GEOR_custom.js";
    }
} catch (Exception e) {
}

// the context path (might not be the public context path ! -> to be improved with https://github.com/georchestra/georchestra/issues/227)
String context = request.getContextPath().split("-")[0]; // eg /mapfishapp

String lang = request.getParameter("lang");
if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("ru") && !lang.equals("fr") && !lang.equals("de"))) {
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
ResourceBundle resource = org.georchestra.mapfishapp.ws.Utf8ResourceBundle.getBundle("org.georchestra.mapfishapp.i18n.index",l);
javax.servlet.jsp.jstl.core.Config.set(
    request,
    javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
    new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource)
);

String sec_roles = request.getHeader("sec-roles");
String js_roles = "";
if(sec_roles != null) {
    String[] roles = sec_roles.split(";");
    String[] js_roles_array = new String[roles.length];
    for (int i = 0; i < roles.length; i++) {
        if (roles[i].equals("ROLE_GN_ADMIN")) {
            admin = true;
        }
        if (roles[i].equals("ROLE_GN_EDITOR") || roles[i].equals("ROLE_GN_REVIEWER") || roles[i].equals("ROLE_GN_ADMIN")) {
            anonymous = false;
        }
        if (roles[i].equals("ROLE_USER")) {
            anonymous = false;
        }
        js_roles_array[i] = "'"+roles[i]+"'";
    }
    js_roles = StringUtils.join(js_roles_array, ", ");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%= lang %>" xml:lang="<%= lang %>">

<head>
    <meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <meta name="apple-mobile-web-app-capable" content="yes">
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
    <title lang="<%= lang %>" dir="ltr"><fmt:message key="title.visual"/> - <%= instanceName %></title>

    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/externals/ext/resources/css/xtheme-gray.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/externals/styler/theme/css/styler.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/externals/geoext/resources/css/popup.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/Ext.ux/lib/Ext.ux/widgets/palettecombobox/palettecombobox.ux.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/Ext.ux/lib/Ext.ux/widgets/colorpicker/colorpicker.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/lib/Ext.ux/lib/Ext.ux/widgets/spinner/Spinner.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/app/openlayers_gray_theme/style.css" />
    <link rel="stylesheet" type="text/css" href="<%= context %>/app/css/main.css" />
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
        <img src="<%= context %>/app/img/loading.gif" alt="<fmt:message key='loading'/>" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
        <span id="loading-msg"><fmt:message key="loading"/></span>
    </div>
    
    <!-- invisible iframe for actions such as "load in JOSM" -->
    <iframe style="position: absolute; width: 1px; height: 1px; top: -1em;visibility:hidden;" tabindex="-1" aria-hidden="true" frameborder="0" width="0" height="0" marginheight="0" marginwidth="0" scrolling="no"></iframe>

    <script type="text/javascript" src="<%= context %>/lib/externals/ext/adapter/ext/ext-base.js"></script>
    
    <!--
        loading custom parameters (see build profile)
    -->
    <script type="text/javascript" src="<%= context %><%= georCustomPath %>"></script>
    
    <c:choose>
        <c:when test='<%= request.getParameter("debug") != null %>'>
    <%@ include file="debug-includes.jsp" %>
        </c:when>
        <c:otherwise>
    <script type="text/javascript" src="<%= context %>/lib/externals/ext/ext-all.js"></script>
    <script type="text/javascript" src="<%= context %>/build/mapfishapp.js"></script>
    <script type="text/javascript" src="<%= context %>/build/lang/<%= lang %>.js"></script>
        </c:otherwise>
    </c:choose>

    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        <% 
          String proxyHost = "/proxy/?url=";
          if(request.getHeader("sec-proxy") == null) {
            proxyHost = context + "/ws/ogcproxy/?url=";
          }
        %>
        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';

        // mapfishapp initial state: open a WMC, or a mix of WMS layers and servers
        GEOR.initstate = ${c.data};
        GEOR.initsearch = ${c.search};

        // custom WMC loader 
    <c:choose>
        <c:when test='<%= request.getParameter("wmc") != null %>'>
        GEOR.config.CUSTOM_WMC = '<%=request.getParameter("wmc") %>';
        </c:when>
    </c:choose>
    <c:choose>
        <c:when test='<%= request.getParameter("file") != null %>'>
        GEOR.config.CUSTOM_FILE = '<%=request.getParameter("file") %>';
        </c:when>
    </c:choose>
        // custom startup zoom parameters (override the WMC bbox):
        GEOR.config.CUSTOM_BBOX = "${c.bbox}";
        GEOR.config.CUSTOM_CENTER = "${c.lon},${c.lat}";
        GEOR.config.CUSTOM_RADIUS = "${c.radius}";

        // formats managed by server for file upload
        GEOR.config.FILE_FORMAT_LIST = ${c.fileFormatList};

        // lang
        GEOR.config.LANG = '<%= lang %>';

        // security stuff
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
        GEOR.config.ANONYMOUS = false;
        GEOR.config.USERNAME = "<%=request.getHeader("sec-username") %>";
        GEOR.config.USEREMAIL = "<%=request.getHeader("sec-email") %>";
        GEOR.config.USERFIRSTNAME = "<%=(request.getHeader("sec-firstname") == null ? "" : request.getHeader("sec-firstname"))%>";
        GEOR.config.USERLASTNAME = "<%=(request.getHeader("sec-lastname") == null ? "" : request.getHeader("sec-lastname"))%>";
        GEOR.config.USERORG = "<%=(request.getHeader("sec-orgname") == null ? "" : request.getHeader("sec-orgname"))%>";
        GEOR.config.USERTEL = "<%=(request.getHeader("sec-tel") == null ? "" : request.getHeader("sec-tel"))%>";
        </c:when>
    </c:choose>
        GEOR.config.ROLES = [<%= js_roles %>];
        GEOR.config.PATHNAME = '<%= context %>';
        GEOR.config.CONTEXTS = ${c.contexts};
        GEOR.config.ADDONS = ${c.addons};
    </script>
    <noscript><p><fmt:message key="need.javascript"/></p></noscript>
</body>
</html>
