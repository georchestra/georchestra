<%@ page pageEncoding="UTF-8" %>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="/header/" style="width:100%;height:90px;border:none;overflow:none;" scrolling="no"></iframe>
    </div>
    </c:when>
</c:choose>
