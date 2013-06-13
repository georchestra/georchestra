<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.georchestra._static.Utf8ResourceBundle" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;

String lang = request.getParameter("lang");
String active = request.getParameter("active");
if (active == null) {
    active = "none";
}

if (lang == null || (!lang.equals("en") && !lang.equals("es")  && !lang.equals("fr"))) {
    lang = "fr";
}
Locale l = new Locale(lang);
ResourceBundle resource = org.georchestra._static.Utf8ResourceBundle.getBundle("_static.i18n.index",l);
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
        if (roles[i].equals("ROLE_SV_EDITOR") || roles[i].equals("ROLE_SV_REVIEWER") || roles[i].equals("ROLE_SV_ADMIN") || roles[i].equals("ROLE_SV_ADMINISTRATOR") || roles[i].equals("ROLE_SV_USER")) {
            anonymous = false;
        }
    }
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
  
    <style type="text/css">
        @import url(http://fonts.googleapis.com/css?family=Yanone+Kaffeesatz);

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
            height      : 90px;
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
            margin  : 0;
            padding : 0;
            display : inline-block;
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
            -moz-transition    : background .3s ease-in;
            -webkit-transition : background .3s ease-in;
            -o-transition      : background .3s ease-in;
            transition         : background .3s ease-in;
            -moz-transition-property    : background,color,border-radius;
            -webkit-transition-property : background,color,border-radius;
            -o-transition-property      : background,color,border-radius;
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
            <li class="active"><a><fmt:message key="catalogue"/></a></li>
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
            <c:when test='<%= active.equals("extractorapp") %>'>
            <li class="active"><a><fmt:message key="extractor"/></a></li>
            </c:when>
            <c:otherwise>
            <li><a href="/extractorapp/"><fmt:message key="extractor"/></a></li>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test='<%= active.equals("geoserver") %>'>
            <li class="active"><a><fmt:message key="services"/></a></li>
            </c:when>
            <c:otherwise>
            <li><a href="/geoserver/web/"><fmt:message key="services"/></a></li>
            </c:otherwise>
        </c:choose>

        </ul>
        <c:choose>
            <c:when test='<%= anonymous == false %>'>
        <p class="logged">
            <%=request.getHeader("sec-username") %><span class="light"> | </span><a href="/j_spring_security_logout"><fmt:message key="logout"/></a>
        </p>
            </c:when>
            <c:otherwise>
        <p class="logged">
            <a href="?login"><fmt:message key="login"/></a>
        </p>
            </c:otherwise>
        </c:choose>
    </div>

    <script>
        (function(){
            if (!window.addEventListener || !document.querySelectorAll) return;
            var each = function(els, callback) {
                for (var i = 0, l=els.length ; i<l ; i++) {
                    callback(els[i]);
                }
            }
            each(document.querySelectorAll('#go_head li a'), function(li){
                li.addEventListener('click', function(e) {
                    each(
                        document.querySelectorAll('#go_head li'),
                        function(l){ l.className = '';}
                    );
                    li.parentNode.className = 'active';
                });
            });
        })();
    </script>

</body>
</html>
