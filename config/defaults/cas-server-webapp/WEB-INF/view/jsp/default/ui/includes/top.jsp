<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<!DOCTYPE html>

<%@ page language="java" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.georchestra.commons.configuration.GeorchestraConfiguration" %>

<%

String sharedInstanceName = "@shared.instance.name@";
String sharedHomepageUrl = "@shared.homepage.url@";
String sharedHeaderHeight = "@shared.header.height@";
String sharedLdapadminContextpath = "@shared.console.contextpath@";

try {
  ApplicationContext ctx = RequestContextUtils.getWebApplicationContext(request);
  GeorchestraConfiguration georConfig = (GeorchestraConfiguration) ctx.getBean(GeorchestraConfiguration.class);
  if (georConfig.activated()) {
    sharedInstanceName = georConfig.getProperty("instance.name");
    sharedHomepageUrl = georConfig.getProperty("homepage.url");
    sharedHeaderHeight = georConfig.getProperty("header.height");
    sharedLdapadminContextpath = georConfig.getProperty("console.contextpath");
  }
} catch (Exception e) {
  // Ignoring and keeping the default configuration
}

%>

<html lang="en">
<head>
  <meta charset="UTF-8" />

  <title>CAS - <%= sharedInstanceName %></title>

  <spring:theme code="standard.custom.css.file" var="customCssFile" />
  <link rel="stylesheet" href="<c:url value="${customCssFile}" />" />
  <link rel="icon" href="/favicon.ico" type="image/x-icon" />

  <!--[if lt IE 9]>
    <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.1/html5shiv.js" type="text/javascript"></script>
  <![endif]-->
</head>
<body id="cas">

<!-- see http://stackoverflow.com/questions/1037839/how-to-force-link-from-iframe-to-be-opened-in-the-parent-window -->
<script type="text/javascript" src="/header/js/header.js"></script>
<iframe src="/header/" style="width:100%;height: <%= sharedHeaderHeight %>px;border:none;overflow:hidden;" scrolling="no" frameborder="0" onload="_headerOnLoad(this)"></iframe>

  <div id="container">
      <div id="content">
