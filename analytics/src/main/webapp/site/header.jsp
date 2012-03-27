<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <link rel="stylesheet" type="text/css" href="/static/css/header.css" />
    <div id="go_head">
        <a href="#" id="go_home" title="retourner à l’accueil">
            <img src="/static/img/logo.png" alt="geOrchestra" height="50"/>
        </a>
        <ul>
            <li><a href="/geonetwork/srv/fr/main.home">catalogue</a></li>
            <li><a href="/mapfishapp/">visualiseur</a></li>
        <c:choose>
            <c:when test='<%= editor == true %>'>
            <li><a href="/mapfishapp/edit">éditeur</a></li>
            </c:when>
        </c:choose>
            <li><a href="#">extracteur</a></li>
            <li><a href="/geoserver/web/">services</a></li>
        <c:choose>
            <c:when test='<%= admin == true %>'>
            <li><a href="/phpldapadmin">utilisateurs</a></li>
            </c:when>
        </c:choose>
        <c:choose>
            <c:when test='<%= admin == true %>'>
            <li class="active"><a href="/analytics">statistiques</a></li>
            </c:when>
        </c:choose>
        </ul>
    <c:choose>
        <c:when test='<%= anonymous == false %>'>
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