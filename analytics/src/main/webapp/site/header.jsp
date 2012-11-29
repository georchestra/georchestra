<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <link rel="stylesheet" type="text/css" href="/static/css/header.css" />
    <div id="go_head">
        <a href="/" id="go_home" title="Portada">
            <img src="/static/img/logo.png" alt="GeoBolivia" height="50"/>
        </a>
        <ul>
            <li><a href="/">Portada</a></li>
            <li><a href="/?-Institucional-">InstituciÃ³n</a></li>
            <li><a href="/geonetwork">CatÃ¡logo</a></li>
            <li><a href="/mapfishapp">Visualizador</a></li>
			<li><a href="/extractorapp">Descargas</a></li>
            <li><a href="/geoserver">Servicios</a></li>
            <c:if test='<%= admin == true %>'>
            <li class="active"><a href="/analytics">Estadisticas</a></li>
            </c:if>
            <li><a href="/?-Contacto-14-">Contacto</a></li>
            <li><a href="/?-Ayuda-">Ayuda</a></li>
        </ul>
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
        <p class="logged">
            <%=request.getHeader("sec-username") %><span class="light"> | </span><a href="/j_spring_security_logout">DesconexiÃ³n</a>
        </p>
        </c:when>
        <c:otherwise>
        <p class="logged">
            <a href="?login">Iniciar sesiÃ³n</a>
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