<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><s:message code="404.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
	<%@ include file="header.jsp" %>
	<div class="container">
		<div class="page-header">
			<h1><s:message code="404.title"/></h1>
		</div>
	</div>
</body>
</html>
