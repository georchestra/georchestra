<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.analytics.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
    Boolean anonymous = true;
    Boolean admin = false;
    Boolean editor = false;

    String lang = request.getParameter("lang");
    if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("fr"))) {
        lang = "es";
    }
    Locale l = new Locale(lang);
    ResourceBundle resource = Utf8ResourceBundle.getBundle("analytics.i18n.index",l);
    javax.servlet.jsp.jstl.core.Config.set(
            request,
            javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
            new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource));

    String sec_roles = request.getHeader("sec-roles");
    if (sec_roles != null) {
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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
  "http://www.w3.org/TR/html4/strict.dtd">
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
        <title lang="<%= lang%>" dir="ltr"><fmt:message key="title.analytics"/> - geOrchestra</title>
        <link rel="stylesheet" type="text/css" href="resources/site/js/lib/external/ext/resources/css/ext-all-gray.css" />
        <link rel="stylesheet" type="text/css" href="resources/site/css/app.css" />
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

        <c:choose>
            <c:when test='<%= admin == true%>'>
                <div id="waiter" style="display:none;">
                    <span><fmt:message key="loading"/></span>
                </div>
                <div id="loading">
                    <img src="resources/site/images/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
                    <span id="loading-msg"><fmt:message key="loading"/></span>
                </div>
                
                <script type="text/javascript" src="resources/site/js/lib/external/openlayers/Lang.js"></script>
                <script type="text/javascript" src="resources/site/js/lib/external/ext/ext.js"></script>
                <script type="text/javascript" src="resources/site/js/lib/external/ext/locale/ext-lang-<%= lang %>.js"></script>
                <script type="text/javascript" src="resources/site/js/app/GEOR_Lang/<%= lang %>.js"></script>
                <script type="text/javascript">
                    Ext.onReady(function() {
                        Ext.get("loading").remove();
                        // lang
                        Lang.setCode('<%= lang %>');
                    });
                </script>
                <script type="text/javascript" src="resources/site/js/app/Application.js"></script>
                <script type="text/javascript" src="resources/site/js/app/config.js"></script>
            </c:when>
            <c:otherwise>
		<div class="admin_only"><b><fmt:message key="admin_only"/></b></div>
            </c:otherwise>
        </c:choose>

        <noscript><p><fmt:message key="need.javascript"/></p></noscript>
    </body>
</html>
