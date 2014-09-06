<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <link rel="stylesheet" type="text/css" href="/header/css/style.css" />
    <script type="text/javascript" src="/header/?lang=<%= lang %>""></script>
    <script type="text/javascript" src="/header/js/header.js?active=mapfishapp"></script>
    </c:when>
</c:choose>
