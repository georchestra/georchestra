<%@ page pageEncoding="UTF-8"%>

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
     *  is deployed alongside with mapfishapp
     *-->
    <link rel="stylesheet" type="text/css" href="/static/css/header.css" />
    
    <c:set var="lang" value='<%= lang %>' />
    <c:set var="anonymous" value='<%= anonymous %>' />
    <jsp:include page="header-${lang}.jsp">
        <jsp:param name="anonymous" value="${anonymous}" />
        <jsp:param name="edit" value="${c.edit}" />
    </jsp:include>
    
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
