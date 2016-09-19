<%@ page pageEncoding="UTF-8"%>
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>
<%@ page contentType="text/html; charset=UTF-8"%>

<%

String headerHeight = "@shared.header.height@";
try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  headerHeight = ctx.getBean(GeorchestraConfiguration.class).getProperty("headerHeight");
} catch (Exception e) {}

%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="/header/?lang=<%= lang %>" style="width:100%;height:<%= headerHeight %>px;border:none;overflow:hidden;" scrolling="no" frameborder="0"></iframe>
    </div>
    </c:when>
</c:choose>
