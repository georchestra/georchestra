<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <geor-header legacy-header="${useLegacyHeader}" legacy-url="${headerUrl}" style="width:100%;height:${headerHeight}px;border:none;" logo-url="${logoUrl}" stylesheet="${georchestraStylesheet}"  active-app="analytics"></geor-header>
        <script src="<c:out value="${headerScript}" />"></script>
    </div>
    </c:when>
</c:choose>
