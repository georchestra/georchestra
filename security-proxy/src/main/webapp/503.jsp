<%--

 Copyright (C) 2009-2025 by the geOrchestra PSC

 This file is part of geOrchestra.

 geOrchestra is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option)
 any later version.

 geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 more details.

 You should have received a copy of the GNU General Public License along with
 geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 
--%>

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
