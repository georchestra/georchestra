<%@page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Insert title here</title>
    <link href='<c:url value="/css/bootstrap.min.css" />' rel="stylesheet" />
</head>
<body>
	<div class="container">
	<h1>ldap admin (under construction)</h1>

<%
Locale l = request.getLocale();
out.println("locale: " + l);
%>
    <ul>
		<li><a href="/ldapadmin/public/accounts/new">Create Account (example: user1, user1@gmail.com)</a></li>
		<li><a href="/ldapadmin/public/accounts/lostPassword">Lost Password (example: email=user1@gmail.com)</a></li>
        <li><a href="/ldapadmin/public/accounts/userdetails?uid=834b77b2-676a-43a2-802e-3cbf14df6c57">Edit testuser details (set uid parameter before execute this)</a></li>
	</ul>
	</div> <!-- /container -->
	
    <script src="http://code.jquery.com/jquery.js"></script>
    <script src='<c:url value="/js/bootstrap.min.js" />'></script>
</body>

</html>
