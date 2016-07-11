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

<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra._header.Utf8ResourceBundle" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;

/*
response.setDateHeader("Expires", 31536000);
response.setHeader("Cache-Control", "private, max-age=31536000");
*/

// Using georchestra autoconf
String georLanguage = null;
String georLdapadminPublicContextPath = null;
String ldapadm = null;
try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  georLanguage = ctx.getBean(GeorchestraConfiguration.class).getProperty("language");
  georLdapadminPublicContextPath = ctx.getBean(GeorchestraConfiguration.class).getProperty("ldapadminPublicContextPath");
} catch (Exception e) {}

// to prevent problems with proxies, and for now:
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
response.setHeader("Pragma", "no-cache"); // HTTP 1.0
response.setDateHeader("Expires", 0); // Proxies.

String active = request.getParameter("active");
if (active == null) {
    active = "none";
}

String lang = request.getParameter("lang");
if (lang == null || (!lang.equals("en") && !lang.equals("es") && !lang.equals("ru") && !lang.equals("fr") && !lang.equals("de"))) {
    if (georLanguage != null)
        lang = georLanguage;
    else
        lang = "${language}";
}

if (georLdapadminPublicContextPath != null)
    ldapadm = georLdapadminPublicContextPath;
else
    ldapadm = "${ldapadminPublicContextPath}";


Locale l = new Locale(lang);
ResourceBundle resource = org.georchestra._header.Utf8ResourceBundle.getBundle("_header.i18n.index",l);
javax.servlet.jsp.jstl.core.Config.set(
    request,
    javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
    new javax.servlet.jsp.jstl.fmt.LocalizationContext(resource)
);

Boolean extractor = false;
Boolean admin = false;
Boolean catadmin = false;
Boolean ldapadmin = false;
Boolean analyticsadmin = false;
Boolean extractorappadmin = false;
String sec_roles = request.getHeader("sec-roles");
if(sec_roles != null) {
    String[] roles = sec_roles.split(";");
    for (int i = 0; i < roles.length; i++) {
        // ROLE_ANONYMOUS is added by the security proxy:
        if (roles[i].equals("ROLE_ANONYMOUS")) {
            //response.setHeader("Cache-Control", "public, max-age=31536000");
            break;
        }
        if (roles[i].equals("ROLE_GN_EDITOR") || roles[i].equals("ROLE_GN_REVIEWER") || roles[i].equals("ROLE_GN_ADMIN") || roles[i].equals("ROLE_ADMINISTRATOR") || roles[i].equals("ROLE_USER")) {
            anonymous = false;
        }
        if (roles[i].equals("ROLE_MOD_EXTRACTORAPP")) {
            extractor = true;
        }
        if (roles[i].equals("ROLE_MOD_LDAPADMIN")) {
            admin = true;
            ldapadmin = true;
        }
        if (roles[i].equals("ROLE_GN_ADMIN")) {
            admin = true;
            catadmin = true;
        }
        if (roles[i].equals("ROLE_ADMINISTRATOR")) {
            admin = true;
            extractorappadmin = true;
        }
        if (roles[i].equals("ROLE_MOD_ANALYTICS")) {
            admin = true;
            analyticsadmin = true;
        }
    }
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <style type="text/css">
        /* see https://github.com/georchestra/georchestra/issues/147 for missing http protocol */
        @import url(//fonts.googleapis.com/css?family=Yanone+Kaffeesatz);

        html, body {
            padding     : 0;
            margin      : 0;
            background  : #fff;
        }
        #go_head {
            padding     : 0;
            margin      : 0;
            font-family : 'Yanone Kaffeesatz', arial,verdana,helvetica;
            background  : #fff;
        }
        #go_home {
            float  : left;
            margin : 20px 0 0 10px;
        }
        #go_home img {
            border : none;
        }
        #go_head ul {
            float      : left;
            list-style : none;
            margin     : 20px 0 0 10px;
            padding    : 0;
            font-size  : 18px;
            display    : inline;
        }
        #go_head li {
            right      : 100%;
            left       : 0;
            margin     : 0;
            padding    : 0;
            position   : relative;
            display    : inline-block;
            transition : right .3s ease, left .3s ease, background .3s ease;
            background : transparent;
        }
        #go_head li a {
            color              : #666;
            display            : inline-block;
            background         : #fff;
            padding            : 0 0.3em;
            margin             : 0 0.3em;
            border-bottom      : 1px dotted #ddd;
            height             : 52px;
            line-height        : 52px;
            text-decoration    : none;
            border-radius      : .3em .3em 0 0;
            transition         : background .3s ease-in;
            transition-property         : background,color,border-radius;
        }
        #go_head li a:hover {
            background-color : #666;
            color            : #fff;
            border-radius    : 0.3em;
            border-bottom    : none;
        }
        #go_head ul li.active a {
            background-color : rgb(85,0,106);
            padding          : 0 0.6em;
            border-radius    : 0.3em;
            color            : #fff;
            border           : 0;
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
            font-size     : 16px;
        }
        #go_head .logged span{
            color: #000;
        }
        #go_head .logged span.light{
            color: #ddd;
        }
        #go_head .logged {
            position : relative;
        }
        #go_head .logged div {
            position : absolute;
            top      : 16px;
            right    : 10px;
        }
        #go_head .logged a {
            text-decoration : underline;
            color           : rgb(84, 0, 105);
        }
        #go_head ul ul {
            display: none;
        }
        #go_head li li {
            right: auto;
        }
        #go_head .expanded {
            position    : absolute;
            right       : 0;
            left        : 200px;
            top         : 20px;
            background  : white;
            z-index     : 1;
            min-width   : 20em;
        }
        #go_head .expanded ul{
            display: block;
        }
        #go_head .expanded > a,
        #go_head .expanded ul{
            margin-top: 0;
            float: right;
        }
        #go_head .expanded > a {
            color: white;
            background: #666;
        }
        #go_head .group > a:after {
            content: ' »';
        }
        #go_head .expanded > a:before {
            content: '« ';
        }
        #go_head .expanded > a:after {
            content: '';
        }
    </style>

</head>


<body>

    <div id="go_head">
        <a href="#" id="go_home" title="<fmt:message key='go.home'/>">
            <img src="img/logo.png" alt="<fmt:message key='logo'/>" height="50"/>
        </a>
        <ul>
        <c:choose>
            <c:when test='<%= active.equals("geonetwork") %>'>
            <li class="active"><a href="/geonetwork/"><fmt:message key="catalogue"/></a></li>
            </c:when>
            <c:otherwise>
            <li><a href="/geonetwork/"><fmt:message key="catalogue"/></a></li>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test='<%= active.equals("mapfishapp") %>'>
            <li class="active"><a><fmt:message key="viewer"/></a></li>
            </c:when>
            <c:otherwise>
            <li><a href="/mapfishapp/"><fmt:message key="viewer"/></a></li>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test='<%= extractor == true %>'>
            <c:choose>
                <c:when test='<%= active.equals("extractorapp") %>'>
            <li class="active"><a><fmt:message key="extractor"/></a></li>
                </c:when>
                <c:otherwise>
            <li><a href="/extractorapp/"><fmt:message key="extractor"/></a></li>
                </c:otherwise>
            </c:choose>
            </c:when>
        </c:choose>

        <c:choose>
            <c:when test='<%= active.equals("geoserver") %>'>
            <li class="active"><a href="/geoserver/web/"><fmt:message key="services"/></a></li>
            </c:when>
            <c:otherwise>
            <li><a href="/geoserver/web/"><fmt:message key="services"/></a></li>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test='<%= admin == true %>'>
            <li class="group"> 
                <a href="#admin"><fmt:message key="admin"/></a>
                <ul>

                    <c:choose>
                        <c:when test='<%= catadmin == true %>'>
                        <c:choose>
                            <c:when test='<%= active.equals("geonetwork") %>'>
                        <!-- GN2 or GN3 -->
                        <!--li class="active"><a href="/geonetwork/srv/<%= lang %>/admin"><fmt:message key="catalogue"/></a></li-->
                        <li class="active"><a href="/geonetwork/srv/<%= lang %>/admin.console"><fmt:message key="catalogue"/></a></li>
                            </c:when>
                            <c:otherwise>
                        <!-- GN2 or GN3 -->
                        <!--li><a href="/geonetwork/srv/<%= lang %>/admin"><fmt:message key="catalogue"/></a></li-->
                        <li><a href="/geonetwork/srv/<%= lang %>/admin.console"><fmt:message key="catalogue"/></a></li>
                            </c:otherwise>
                        </c:choose>
                        </c:when>
                    </c:choose>

                    <c:choose>
                        <c:when test='<%= extractorappadmin == true %>'>
                        <c:choose>
                            <c:when test='<%= active.equals("extractorappadmin") %>'>
                        <li class="active"><a href="/extractorapp/admin/"><fmt:message key="extractor"/></a></li>
                            </c:when>
                            <c:otherwise>
                        <li><a href="/extractorapp/admin/"><fmt:message key="extractor"/></a></li>
                            </c:otherwise>
                        </c:choose>
                        </c:when>
                    </c:choose>

                    <c:choose>
                        <c:when test='<%= analyticsadmin == true %>'>
                        <c:choose>
                            <c:when test='<%= active.equals("analytics") %>'>
                        <li class="active"><a href="/analytics/">analytics</a></li>
                            </c:when>
                            <c:otherwise>
                        <li><a href="/analytics/">analytics</a></li>
                            </c:otherwise>
                        </c:choose>
                        </c:when>
                    </c:choose>

                    <c:choose>
                        <c:when test='<%= ldapadmin == true %>'>
                        <c:choose>
                            <c:when test='<%= active.equals("ldapadmin") %>'>
                        <li class="active"><a><fmt:message key="users"/></a></li>
                            </c:when>
                            <c:otherwise>
                        <li><a href="<%= ldapadm %>/privateui/"><fmt:message key="users"/></a></li>
                            </c:otherwise>
                        </c:choose>
                        </c:when>
                    </c:choose>

                </ul>
            </li>
            </c:when>
        </c:choose>
        </ul>

        <c:choose>
            <c:when test='<%= anonymous == false %>'>
        <p class="logged">
            <a href="<%=ldapadm %>/account/userdetails"><%=request.getHeader("sec-username") %></a><span class="light"> | </span><a href="/j_spring_security_logout"><fmt:message key="logout"/></a>
        </p>
            </c:when>
            <c:otherwise>
        <p class="logged">
            <a id="login_a"><fmt:message key="login"/></a>
        </p>
            </c:otherwise>
        </c:choose>
    </div>

    <script>
        (function(){
            // required to get the correct redirect after login, see https://github.com/georchestra/georchestra/issues/170
            var url,
                a = document.getElementById("login_a"),
                cnxblk = document.querySelector('#go_head p.logged');
            if (a !== null) {
                url = parent.window.location.href;
                if (/\/cas\//.test(url)) {
                    a.href = "/cas/login";
                } else {
                    // removing any existing anchor from URL first:
                    // see https://github.com/georchestra/georchestra/issues/1032
                    var p = url.split('#', 2),
                    /* Taken from https://github.com/openlayers/openlayers/blob/master/lib/OpenLayers/Util.js#L557 */
                    paramStr = "login", parts = (p[0] + " ").split(/[?&]/);
                    a.href = p[0] + (parts.pop() === " " ?
                        paramStr :
                        parts.length ? "&" + paramStr : "?" + paramStr) +
                        // adding potential anchor
                        (p.length == 2 ? "#" + p[1] : "");
                }
            }

            // handle menus
            if (!window.addEventListener || !document.querySelectorAll) return;
            var each = function(els, callback) {
                for (var i = 0, l=els.length ; i<l ; i++) {
                    callback(els[i]);
                }
            }
            each(document.querySelectorAll('#go_head li a'), function(a){
                var li = a.parentNode;
                var ul = li.querySelectorAll('ul');
                a.addEventListener('click', function(e) {
                    each(
                        document.querySelectorAll('#go_head li'),
                        function(l){ l.classList.remove('active');}
                    );
                    if (ul[0]) {
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                        e.preventDefault();
                        li.classList.toggle('expanded');
                        // hide/show connexion block:
                        cnxblk.style.visibility = 
                            cnxblk.style.visibility == '' ? 'hidden' : '';
                    } else {
                        a.parentNode.className = 'active';
                    }
                });
            });
        })();
    </script>

</body>
</html>
