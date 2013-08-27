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
	<link href='css/bootstrap.min.css' rel="stylesheet" />
	<link href='css/ldapadmin.css' rel="stylesheet" />
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
				<td><a href='#'>indexdev</a></td>
			</tr>
			<tr>
				<td>Create Account</td>
				<td><a href='account/new'>account/new</a></td>
			</tr>
			<tr>
				<td>Lost Password</td>
				<td><a href='account/lostPassword'>account/lostPassword</a></td>
			</tr>
			<tr>
				<td>Edit "testuser" details</td>
				<td><a href='account/userdetails?uid=testuser'>account/userdetails?uid=testuser</a></td>
			</tr>
			<tr>
				<td>Change "testuser" password</td>
				<td><a href='account/changePassword?uid=testuser'>account/changePassword?uid=testuser</a></td>
			</tr>
			<tr>
				<td>Set new password</td>
				<td><a href='account/newPassword?token=d6091eb6-1f8a-4ab6-a4d4-57fdfb46b409'>account/newPassword?token=d6091eb6-1f8a-4ab6-a4d4-57fdfb46b409</a><br>(the token is created by the <a href='account/lostPassword'>Lost Password</a> page and sent by e-mail)</td>
			</tr>
		</table>

		<h2>Private pages</h2>
		<table class="table">
			<tr>
				<th>Private pages</th>
				<th>Link</th>
			</tr>
			<tr>
				<td>Administration</td>
				<td><a href='/'>/</a></td>
			</tr>
			<tr>
				<td>REST GET users</td>
				<td><a href='private/users'>private/users</a></td>
			</tr>
			<tr>
				<td>REST GET user</td>
				<td><a href='private/users/testuser'>private/users/testuser</a></td>
			</tr>
			<tr>
				<td>REST GET groups</td>
				<td><a href='private/groups'>private/groups</a></td>
			</tr>
			<tr>
				<td>REST GET group</td>
				<td><a href='private/groups/SV_ADMIN'>private/groups/SV_ADMIN</a></td>
			</tr>
		</table>
	</div> <!-- /container -->

    <script src="//code.jquery.com/jquery.js"></script>
    <script src='js/bootstrap.min.js'></script>
</body>

</html>
