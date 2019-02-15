<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="${headerUrl}" style="width:100%;height:${headerHeight}px;border:none;overflow:hidden;" scrolling="no"></iframe>
    </div>
    </c:when>
</c:choose>
