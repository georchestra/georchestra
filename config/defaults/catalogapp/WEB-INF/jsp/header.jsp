<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <script type="text/javascript" src="/header/js/header.js"></script>
    <link rel="stylesheet" type="text/css" href="/header/css/style.css" />
    <script type="text/javascript" src="/header/?lang=<%= lang %>""></script>
    <script type="text/javascript" src="/header/js/header.js?active=extractorapp"></script>
    </c:when>
</c:choose>
