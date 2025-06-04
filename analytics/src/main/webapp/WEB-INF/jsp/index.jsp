<%--

 Copyright (C) 2009-2025 by the geOrchestra PSC

 This file is part of geOrchestra.

 geOrchestra is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later
 version.

 geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License along with
 geOrchestra. If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.analytics.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<%
Locale rLocale = request.getLocale();
ResourceBundle bundle = Utf8ResourceBundle.getBundle("analytics.i18n.index", rLocale);

String detectedLanguage = rLocale.getLanguage();
String forcedLang = request.getParameter("lang");

String lang = (String) request.getAttribute("defaultLanguage");

if (forcedLang != null) {
    if (forcedLang.equals("en") || forcedLang.equals("es") || forcedLang.equals("ru") || forcedLang.equals("fr") || forcedLang.equals("de") || forcedLang.equals("nl")) {
        lang = forcedLang;
    }
} else {
    if (detectedLanguage.equals("en") || detectedLanguage.equals("es") || detectedLanguage.equals("ru") || detectedLanguage.equals("fr") || detectedLanguage.equals("de") || detectedLanguage.equals("nl")) {
        lang = detectedLanguage;
    }
}

javax.servlet.jsp.jstl.core.Config.set(
    request,
    javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
    new javax.servlet.jsp.jstl.fmt.LocalizationContext(bundle)
);

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
  "http://www.w3.org/TR/html4/strict.dtd">
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
        <title lang="<%= lang %>" dir="ltr"><fmt:message key="title.analytics"/> - ${instanceName}</title>
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
        <script type="text/javascript" src="ws/app/js/GEOR_custom.js"></script>

        <noscript><p><fmt:message key="need.javascript"/></p></noscript>
    </body>
</html>
