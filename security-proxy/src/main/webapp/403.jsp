<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><s:message code="403.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href='/_static/bootstrap_3.0.0/css/bootstrap.min.css' rel="stylesheet" />
</head>

<body>
	<%@ include file="header.jsp" %>
	<div class="container">
		<div class="page-header">
			<h1><s:message code="403.title"/></h1>
		</div>
		<p class="lead"><s:message code="403.body"/></p>
	</div>
</body>
</html>
