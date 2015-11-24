<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.analytics.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>

<%
String defaultLanguage = null, defaultInstanceName = null, instanceName = null;

String defaultConfigJs = "resources/js/app/config.js";
try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  if (ctx.getBean(GeorchestraConfiguration.class).activated()) {
    defaultLanguage = ctx.getBean(GeorchestraConfiguration.class).getProperty("language");
    defaultInstanceName = ctx.getBean(GeorchestraConfiguration.class).getProperty("instance");
    defaultConfigJs = "ws/app/js/GEOR_custom.js";
  }
} catch (Exception e) {}


String lang = request.getParameter("lang");
if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("fr") && !lang.equals("de"))) {
  if (defaultLanguage == null) {
    lang = "en";
  } else {
    lang = defaultLanguage;
  }
}

if (defaultInstanceName == null) {
  instanceName = "geOrchestra";
} else {
  instanceName = defaultInstanceName;
}

Locale l = new Locale(lang);
ResourceBundle resource = Utf8ResourceBundle.getBundle("analytics.i18n.index",l);
javax.servlet.jsp.jstl.core.Config.set(
            request,
            javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
            new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource));

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
  "http://www.w3.org/TR/html4/strict.dtd">
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
        <title lang="<%= lang%>" dir="ltr"><fmt:message key="title.analytics"/> - <%= instanceName %></title>
        <link rel="stylesheet" type="text/css" href="resources/js/lib/external/ext/resources/css/ext-all-gray.css" />
        <link rel="stylesheet" type="text/css" href="resources/css/app.css" />
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
    </head>
    <body>

        <%@ include file="header.jsp" %>

        <div id="waiter" style="display:none;">
            <span><fmt:message key="loading"/></span>
        </div>
        <div id="loading">
            <img src="resources/images/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
            <span id="loading-msg"><fmt:message key="loading"/></span>
        </div>
        
        <script type="text/javascript" src="resources/js/app/Lang.js"></script>
        <script type="text/javascript" src="resources/js/lib/external/ext/ext.js"></script>
        <script type="text/javascript" src="resources/js/lib/external/ext/locale/ext-lang-<%= lang %>.js"></script>
        <script type="text/javascript" src="resources/js/app/GEOR_Lang/<%= lang %>.js"></script>
        <script type="text/javascript">
            Ext.onReady(function() {
                Ext.get("loading").remove();
                // lang
                Analytics.Lang.setCode('<%= lang %>');
            });
        </script>
        <script type="text/javascript" src="resources/js/app/Application.js"></script>
        <script type="text/javascript" src="<%= defaultConfigJs %>"></script>

        <noscript><p><fmt:message key="need.javascript"/></p></noscript>
    </body>
</html>
