<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <div id="go_head">
        <a href="#" id="go_home" title="retourner à l’accueil">
            <img src="/static/img/logo.png" alt="geOrchestra" height="50"/>
        </a>
        <ul>
            <li><a href="/geonetwork/srv/fr/main.home">catalogue</a></li>
<c:choose>
    <c:when test='<%= request.getParameter("edit").equals("true") %>'>
            <li><a href="/mapfishapp">visualiseur</a></li>
    </c:when>
    <c:otherwise>
            <li class="active"><a href="#">visualiseur</a></li>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test='<%= request.getParameter("edit").equals("true") %>'>
            <li class="active"><a href="#">éditeur</a></li>
    </c:when>
    <c:otherwise>
            <li><a href="/mapfishapp/edit">éditeur</a></li>
    </c:otherwise>
</c:choose>

            <li><a href="/extractorapp/">extracteur</a></li>
            <li><a href="/geoserver/web/">services</a></li>
            <li><a href="/analytics/">statistiques</a></li>
        </ul>
<c:choose>
    <c:when test='<%= request.getParameter("anonymous").equals("false") %>'>
        <p class="logged">
            <%=request.getHeader("sec-username") %><span class="light"> | </span><a href="/j_spring_security_logout">déconnexion</a>
        </p>
    </c:when>
    <c:otherwise>
        <p class="logged">
            <a href="?login">connexion</a>
        </p>
    </c:otherwise>
</c:choose>
    </div>
