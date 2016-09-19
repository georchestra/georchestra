<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="/header/?lang=<%= lang %>&active=extractorapp" style="width:100%;height:@shared.header.height@px;border:none;overflow:hidden;" scrolling="no" frameborder="0"></iframe>
    </div>
    </c:when>
</c:choose>
