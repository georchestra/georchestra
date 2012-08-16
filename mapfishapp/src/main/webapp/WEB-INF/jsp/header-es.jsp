<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <div id="go_head">
        <a href="#" id="go_home" title="Inicio">
            <img src="/static/img/logo.png" alt="geOrchestra" height="50"/>
        </a>
        <ul>
            <li><a href="/geonetwork/srv/es/main.home">Catálogo</a></li>
<c:choose>
    <c:when test='<%= request.getParameter("edit").equals("true") %>'>
            <li><a href="/mapfishapp">Visualizador</a></li>
    </c:when>
    <c:otherwise>
            <li class="active"><a href="#">Visualizador</a></li>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test='<%= request.getParameter("edit").equals("true") %>'>
            <li class="active"><a href="#">Editor</a></li>
    </c:when>
    <c:otherwise>
            <li><a href="/mapfishapp/edit">Editor</a></li>
    </c:otherwise>
</c:choose>

            <li><a href="/extractorapp/">Extractor</a></li>
            <li><a href="/geoserver/web/">Servicios</a></li>
            <li><a href="/analytics/">Estadisticas</a></li>
        </ul>
<c:choose>
    <c:when test='<%= request.getParameter("anonymous").equals("false") %>'>
        <p class="logged">
            <%=request.getHeader("sec-username") %><span class="light"> | </span><a href="/j_spring_security_logout">Cerrar sesión</a>
        </p>
    </c:when>
    <c:otherwise>
        <p class="logged">
            <a href="?login">Iniciar sesión</a>
        </p>
    </c:otherwise>
</c:choose>
    </div>
