<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <geor-header config-file="${headerConfigFile}" legacy-header="${useLegacyHeader}" legacy-url="${headerUrl}" logo-url="${logoUrl}" stylesheet="${georchestraStylesheet}"  height="${headerHeight}" active-app="analytics"></geor-header>
        <script src="<c:out value="${headerScript}" />"></script>
    </div>
    </c:when>
</c:choose>
