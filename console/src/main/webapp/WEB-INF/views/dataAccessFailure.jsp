<%--

 Copyright (C) 2009-2018 by the geOrchestra PSC

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='css/bootstrap.min.css' rel="stylesheet" />
	<link href='css/console.css' rel="stylesheet" />
	<title><s:message code="dataAccessFailure.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="dataAccessFailure.title"/></h1>
		</div>
<%
Exception ex = (Exception) request.getAttribute("exception");
%>
		<pre>
<%= ex.getMessage() %>
		</pre>
		<pre>
<%
ex.printStackTrace(new java.io.PrintWriter(out));
%>
		</pre>
	</div>
	<script src="//code.jquery.com/jquery.js"></script>
	<script src='js/bootstrap.min.js'></script>
</body>
</html>
