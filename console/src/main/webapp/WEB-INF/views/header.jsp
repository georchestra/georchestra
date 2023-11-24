<%@ page language="java" pageEncoding="UTF-8" %>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
    </div>
    </c:when>
</c:choose>
