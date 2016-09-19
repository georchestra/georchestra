<%@ page pageEncoding="UTF-8" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>

<%
String headerHeight = "@shared.header.height@";
String contextPath  = "@shared.ldapadmin.contextpath@";
try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  if ((ctx.getBean(GeorchestraConfiguration.class) != null)
        && (((GeorchestraConfiguration) ctx.getBean(GeorchestraConfiguration.class)).activated())) {
        headerHeight = ctx.getBean(GeorchestraConfiguration.class).getProperty("headerHeight");
        contextPath  = ctx.getBean(GeorchestraConfiguration.class).getProperty("publicContextPath");
  }
} catch (Exception e) {}

%>


    <style type="text/css">
    #container {
      bottom: 0;
      width: 100%;
      top: <%= headerHeight %>px;
      position: absolute;
      margin: 0;
      padding: 0;
    }
    </style>
    <div id="go_head">
        <iframe src="/header/?active=ldapadmin" style="width:100%;height:<%= headerHeight %>px;border:none;overflow:hidden;" scrolling="no"></iframe>
    </div>
