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

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><s:message code="404.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href='/_static/bootstrap_3.0.0/css/bootstrap.min.css' rel="stylesheet" />
</head>

<body>
	<%@ include file="header.jsp" %>
	<div class="container">
		<div class="page-header">
			<h1><s:message code="404.title"/></h1>
		</div>
		<p class="lead"><s:message code="404.body"/></p>
	</div>
</body>
</html>
