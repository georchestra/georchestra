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
	<link href='${publicContextPath}/account/css/bootstrap.min.css' rel="stylesheet" />
	<link href='${publicContextPath}/account/css/console.css' rel="stylesheet" />
	<title><s:message code="resourceNotFound.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="resourceNotFound.title"/></h1>
		</div>
		<pre>
<% 
try {
    // The Servlet spec guarantees this attribute will be available
    Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception"); 

    if (exception != null) {
        if (exception instanceof ServletException) {
            // It's a ServletException: we should extract the root cause
            ServletException sex = (ServletException) exception;
            Throwable rootCause = sex.getRootCause();
            if (rootCause == null)
                rootCause = sex;
            out.println("** Root cause is: "+ rootCause.getMessage());
            rootCause.printStackTrace(new java.io.PrintWriter(out)); 
        }
        else {
            // It's not a ServletException, so we'll just show it
            exception.printStackTrace(new java.io.PrintWriter(out)); 
        }
    } 
    else  {
        out.println("No error information available");
    } 

    // Display cookies
    out.println("\nCookies:\n");
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (int i = 0; i < cookies.length; i++) {
            out.println(cookies[i].getName() + "=[" + cookies[i].getValue() + "]");
        }
    }
        
} catch (Exception ex) { 
    ex.printStackTrace(new java.io.PrintWriter(out));
}
%>
		</pre>
	</div>
	<script src="//code.jquery.com/jquery.js"></script>
	<script src='${publicContextPath}/account/js/bootstrap.min.js'></script>
</body>
</html>
