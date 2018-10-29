<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta name="robots" content="none" />
    <meta name="googlebot" content="noarchive" />
    <title><s:message code="503.title"/></title>
    <style type="text/css">
      body {
        background-color:#e6e6e6;
        font-family:Calibri;
        text-align:center;
      }
      #wrapper {
        background:#fff;
        width:492px;
        position:relative;
        margin-left:auto;
        margin-right:auto;
        text-align:left;
        border:3px solid #999;
        overflow:hidden;
	    padding: 30px;
        border-bottom-right-radius: 16px;
        border-bottom-left-radius: 16px;
      }
      #wrapper p {
        padding:5px;
      }
    </style>
  </head>
  <body lang=FR>
    <div id="wrapper">
      <img src="https://www.georchestra.org/public/logos/georchestra_logo.png" alt="geOrchestra" />
      <s:message code="503.body"/>
    </div>
  </body>
  <script>
    window.setTimeout(function() {window.location.href = window.location.href}, 5000);
  </script>
</html>
