<%@ page language="java" %>
<%@ page pageEncoding="UTF-8"%>

    <script type="text/javascript">
        var headerHeight = ${headerHeight};
    </script>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="${headerUrl}?lang=<%= lang %>&active=mapfishapp" style="width:100%;height:${headerHeight}px;border:none;overflow:hidden;" scrolling="no" frameborder="0"></iframe>
    </div>
    </c:when>
</c:choose>
