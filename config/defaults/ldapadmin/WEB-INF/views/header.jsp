<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>

<%
String headerHeight = "${headerHeight}";

try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  if ((ctx.getBean(GeorchestraConfiguration.class) != null)
        && (((GeorchestraConfiguration) ctx.getBean(GeorchestraConfiguration.class)).activated())) {
        headerHeight = ctx.getBean(GeorchestraConfiguration.class).getProperty("headerHeight");
  }
} catch (Exception e) {}

%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <iframe src="/header/?active=console" style="width:100%;height:<%= headerHeight %>px;border:none;overflow:hidden;" scrolling="no"></iframe>
    </div>
    </c:when>
</c:choose>
