<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.analytics.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
    String lang = request.getParameter("lang");
    if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("fr"))) {
        lang = "${language}";
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
        <title lang="<%= lang%>" dir="ltr"><fmt:message key="title.analytics"/> - ${instance}</title>
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
            .admin_only {
                padding-left: 40px;
                color:red;
                font-family : 'Yanone Kaffeesatz', arial,verdana,helvetica;
                font-size: 13px;

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
        <script type="text/javascript" src="resources/js/app/config.js"></script>

        <noscript><p><fmt:message key="need.javascript"/></p></noscript>
    </body>
</html>
