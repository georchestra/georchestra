<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <geor-header legacy_url="${headerUrl}" legacy_style="width:100%;height:${headerHeight}px;border:none;overflow:hidden;"></geor-header>
        <script src="${headerScript}"></script>
    </div>
    </c:when>
</c:choose>
