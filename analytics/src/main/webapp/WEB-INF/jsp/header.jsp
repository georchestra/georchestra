<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="<%= headerUrl %>?lang=<%= lang %>" style="width:100%;height:<%= headerHeight %>px;border:none;overflow:hidden;" scrolling="no" frameborder="0"></iframe>
    </div>
    </c:when>
</c:choose>
