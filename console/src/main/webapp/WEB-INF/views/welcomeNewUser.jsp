<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='css/bootstrap.min.css' rel="stylesheet" />
	<link href='css/console.css' rel="stylesheet" />
	<title><s:message code="newUserWelcome.title" /></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
    <%@ include file="header.jsp" %>

	<div class="jumbotron">
		<div class="container">
			<h1><s:message code="newUserWelcome.title" /></h1>
			<p><s:message code="newUserWelcome.body" /></p>
			<p><a class="btn btn-primary btn-lg" href='/cas/login'><s:message code="newUserWelcome.link" /></a></p>
		</div>
	</div>
	<script src="//code.jquery.com/jquery.js"></script>
	<script src='js/bootstrap.min.js'></script>
</body>
</html>
