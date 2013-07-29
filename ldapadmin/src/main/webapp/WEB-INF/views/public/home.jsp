<%@page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<title>Insert title here</title>
</head>
<body>
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
	
</body>

</html>