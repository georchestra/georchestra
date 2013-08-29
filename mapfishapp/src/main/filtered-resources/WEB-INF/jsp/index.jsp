<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra.mapfishapp.ws.Utf8ResourceBundle" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;
Boolean editor = false;
Boolean admin = false;

String lang = request.getParameter("lang");
if (lang == null || (!lang.equals("en") && !lang.equals("es")  && !lang.equals("fr"))) {
    lang = "${language}";
}
Locale l = new Locale(lang);
ResourceBundle resource = org.georchestra.mapfishapp.ws.Utf8ResourceBundle.getBundle("mapfishapp.i18n.index",l);
javax.servlet.jsp.jstl.core.Config.set(
    request,
    javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
    new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource)
);

String sec_roles = request.getHeader("sec-roles");
String js_roles = "";
if(sec_roles != null) {
    String[] roles = sec_roles.split(",");
    String[] js_roles_array = new String[roles.length];
    for (int i = 0; i < roles.length; i++) {
        // ROLE_ANONYMOUS is added by the security proxy:
        if (roles[i].equals("ROLE_ANONYMOUS")) {
            js_roles_array[0] = "'ROLE_ANONYMOUS'";
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
        js_roles_array[i] = "'"+roles[i]+"'";
    }
    js_roles = StringUtils.join(js_roles_array, ", ");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%= lang %>" xml:lang="<%= lang %>">

<c:choose>
    <c:when test='${c.edit != null && !c.edit}'>
<head>
    <title>Accès refusé</title>
</head>
        <c:choose>
            <c:when test='<%= anonymous == true %>'>
    <script type="text/javascript">
        // anonymous users cannot access this protected edit page
        window.location = "?login";
    </script>
            </c:when>
            <c:otherwise>
    <% response.sendError(403); %>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
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
<c:choose>
    <c:when test='${c.edit != null}'>
    <title lang="<%= lang %>" dir="ltr"><fmt:message key="title.editor"/> - ${instance}</title>
    </c:when>
    <c:otherwise>
    <title lang="<%= lang %>" dir="ltr"><fmt:message key="title.visual"/> - ${instance}</title>
    </c:otherwise>
</c:choose>
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/ext/resources/css/xtheme-gray.css" />
    <link rel="stylesheet" type="text/css" href="lib/externals/styler/theme/css/styler.css" />
    <link rel="stylesheet" type="text/css" href="lib/Ext.ux/lib/Ext.ux/widgets/palettecombobox/palettecombobox.ux.css" />
    <link rel="stylesheet" type="text/css" href="lib/Ext.ux/lib/Ext.ux/widgets/colorpicker/colorpicker.css" />
    <link rel="stylesheet" type="text/css" href="lib/Ext.ux/lib/Ext.ux/widgets/spinner/Spinner.css" />
    <link rel="stylesheet" type="text/css" href="app/openlayers_gray_theme/style.css" />
    <link rel="stylesheet" type="text/css" href="app/css/main.css" />
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
        <img src="app/img/loading.gif" alt="<fmt:message key='loading'/>" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
        <span id="loading-msg"><fmt:message key="loading"/></span>
    </div>
    
    <!-- invisible iframe for actions such as "load in JOSM" -->
    <iframe style="position: absolute; width: 1px; height: 1px; top: -1em;visibility:hidden;" tabindex="-1" aria-hidden="true" frameborder="0" width="0" height="0" marginheight="0" marginwidth="0" scrolling="no"></iframe>
    

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
            <c:choose>
                <c:when test='${c.edit}'>
    <script type="text/javascript" src="build/mapfisheditapp.js"></script>
                </c:when>
                <c:otherwise>
    <script type="text/javascript" src="build/mapfishapp.js"></script>
                </c:otherwise>
            </c:choose>
    <script type="text/javascript" src="build/lang/<%= lang %>.js"></script>
        </c:otherwise>
    </c:choose>

    <script type="text/javascript">
        // remove the loading element
        Ext.get("loading").remove();

        <% 
          String proxyHost = "/proxy/?url=";
          if(request.getContextPath().equals("/mapfishapp")) {
            proxyHost = "ws/ogcproxy/?url=";
          }
        %>
        // set proxy host
        OpenLayers.ProxyHost = '<%= proxyHost %>';

        // mapfishapp initial state: open a WMC, or a mix of WMS layers and servers
        GEOR.initstate = ${c.data};

        // custom WMC loader 
    <c:choose>
        <c:when test='<%= request.getParameter("wmc") != null %>'>
        GEOR.config.CUSTOM_WMC = '<%=request.getParameter("wmc") %>';
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

        GEOR.config.maxDocAgeInMinutes = ${maxDocAgeInMinutes};

        // security stuff
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
        GEOR.config.ANONYMOUS = false;
        GEOR.config.USERNAME = "<%=request.getHeader("sec-username") %>";
        </c:when>
    </c:choose>
        GEOR.config.ROLES = [<%= js_roles %>];
    </script>
    <noscript><p><fmt:message key="need.javascript"/></p></noscript>
</body>
    </c:otherwise>
</c:choose>
</html>
