<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- 
 * The following css is used to get the bare minimum header style when running with jetty
 *-->     
<style type="text/css">
    #go_head ul {
        float: left;
        list-style: none;
        margin: 20px 0 0 10px;
        padding: 0;
        font-size: 18px;
        display: inline;
    }
    #go_head li {
        margin: 0;
        padding: 0;
        display: inline-block;
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
    }
</style>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <!-- 
     * The following resource will be loaded only when geOrchestra's "static" module
     *  is deployed alongside with extractorapp
     *-->
    <link rel="stylesheet" type="text/css" href="/static/css/header.css" />

    <div id="go_head">
        <a href="#" id="go_home" title="<fmt:message key='go.home'/>">
            <img src="/static/img/logo.png" alt="<fmt:message key='logo'/>" height="50"/>
        </a>
        <ul>
            <li><a href="/geonetwork/srv/<%= lang %>/main.home"><fmt:message key="catalogue"/></a></li>
            <li><a href="/mapfishapp/"><fmt:message key="viewer"/></a></li>
        <c:choose>
            <c:when test='<%= editor == true %>'>
            <li><a href="/mapfishapp/edit/"><fmt:message key="editor"/></a></li>
            </c:when>
        </c:choose>
            <li class="active"><a><fmt:message key="extractor"/></a></li>
            <li><a href="/geoserver/web/"><fmt:message key="services"/></a></li>
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
    </c:when>
</c:choose>
