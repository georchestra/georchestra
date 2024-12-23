<%@ page language="java" pageEncoding="UTF-8" %>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <geor-header stylesheet="${georchestraStylesheet}" config-file="${headerConfigFile}" height="${headerHeight}" active-app="console"></geor-header>
        <script src="${headerScript}"></script>
    </div>
    </c:when>
</c:choose>
