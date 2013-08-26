<%@ page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<!--TODO: i18n if this page is used-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='<c:url value="/css/bootstrap.min.css" />' rel="stylesheet" />
	<link href='<c:url value="/css/ldapadmin.css" />' rel="stylesheet" />
	<title>ldapadmin (under construction)</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>



<body>
	<div class="container">
		<div class="page-header">
			<h1>ldapadmin (under construction)</h1>
		</div>


		<p>This <strong>temporary</strong> page links to the public pages of the ldapadmin module. The current locale is:
<%
Locale l = request.getLocale();
out.println(l);
%>
		</p>
		<h2>Public pages</h2>
		<table class="table">
			<tr>
				<th>Public pages</th>
				<th>Link</th>
			</tr>
			<tr class="active">
				<td>Home</td>
				<td><a href='#'><c:url value="/public" /></a></td>
			</tr>
			<tr>
				<td>Create Account</td>
				<td><a href='<c:url value="/public/accounts/new" />'><c:url value="/public/accounts/new" /></a></td>
			</tr>
			<tr>
				<td>Lost Password</td>
				<td><a href='<c:url value="/public/accounts/lostPassword" />'><c:url value="/public/accounts/lostPassword" /></a></td>
			</tr>
			<tr>
				<td>Edit "testuser" details</td>
				<td><a href='<c:url value="/public/accounts/userdetails?uid=testuser" />'><c:url value="/public/accounts/userdetails?uid=testuser" /></a></td>
			</tr>
			<tr>
				<td>Change "testuser" password</td>
				<td><a href='<c:url value="/public/accounts/changePassword?uid=testuser" />'><c:url value="/public/accounts/changePassword?uid=testuser" /></a></td>
			</tr>
			<tr>
				<td>Set new password</td>
				<td><a href='<c:url value="/public/accounts/newPassword?token=d6091eb6-1f8a-4ab6-a4d4-57fdfb46b409" />'><c:url value="/public/accounts/newPassword?token=d6091eb6-1f8a-4ab6-a4d4-57fdfb46b409" /></a><br>(the token is created by the <a href='<c:url value="/public/accounts/lostPassword" />'>Lost Password</a> page and sent by e-mail)</td>
			</tr>
		</table>
	</div> <!-- /container -->

    <script src="http://code.jquery.com/jquery.js"></script>
    <script src='<c:url value="/js/bootstrap.min.js" />'></script>
</body>

</html>
