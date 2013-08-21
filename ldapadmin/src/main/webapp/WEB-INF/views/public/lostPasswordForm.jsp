<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='<c:url value="/css/bootstrap.min.css" />' rel="stylesheet" />
	<link href='<c:url value="/css/ldapadmin.css" />' rel="stylesheet" />
	<title><s:message code="lostPasswordForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
	<div class="container">
		<div class="page-header">
			<h1><s:message code="lostPasswordForm.title"/></h2>
		</div>
		<form:form id="form" name="form" method="post" modelAttribute="lostPasswordFormBean" cssClass="form-horizontal" >

			<c:if test="${not empty message}">
			<div id="message" class="alert alert-dismissable alert-info">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
				${message}
			</div>
			</c:if>

			<s:bind path="*">
			<c:if test="${status.error}">
			<div id="message" class="alert alert-dismissable alert-danger">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
				<s:message code="form.error" />
			</div>
			</c:if>
			</s:bind>

			<fieldset class="col-lg-6 col-lg-offset-1">
				<legend><s:message code="lostPasswordForm.fieldset.email"/></legend>
				<t:input path="email" required="true">
					<jsp:attribute name="label"><s:message code="email.label" /></jsp:attribute>
				</t:input>
			</fieldset>
			<fieldset class="col-lg-6 col-lg-offset-1">
				<div class="form-group">
					<div class="col-lg-8 col-lg-offset-4 text-right">
						<button type="submit" class="btn btn-primary btn-lg"><s:message code="submit.label"/> </button>
					</div>
				</div>
			</div>
		</form:form>
    </div>
	<script src="//code.jquery.com/jquery.js"></script>
	<script src='<c:url value="/js/bootstrap.min.js" />'></script>
</body>
</html>
