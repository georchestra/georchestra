<%@ page language="java" %>
<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <geor-header stylesheet="${georchestraStylesheet}" config-file="${headerConfigFile}" height="${headerHeight}"></geor-header>
        <script src="<c:out value="${headerScript}" />"></script>
    </div>
    </c:when>
</c:choose>
