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
	<link href='css/bootstrap.min.css' rel="stylesheet" />
	<link href='css/ldapadmin.css' rel="stylesheet" />
	<title><s:message code="editUserDetailsForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="editUserDetailsForm.title"/></h1>
		</div>
		<form:form id="form" name="form" method="post" action="userdetails" modelAttribute="editUserDetailsFormBean" cssClass="form-horizontal" >

			<c:if test="${not empty success}">
			<div id="message" class="alert alert-dismissable alert-success">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
				<s:message code="editUserDetailsForm.success" />
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
				<legend><s:message code="editUserDetailsForm.fieldset.userDetails"/></legend>
				<t:input path="firstName" required="true">
					<jsp:attribute name="label"><s:message code="firstName.label" /></jsp:attribute>
				</t:input>
				<t:input path="surname" required="true">
					<jsp:attribute name="label"><s:message code="surname.label" /></jsp:attribute>
				</t:input>
				<t:input path="title">
					<jsp:attribute name="label"><s:message code="title.label" /></jsp:attribute>
				</t:input>
				<t:input path="phone">
					<jsp:attribute name="label"><s:message code="phone.label" /></jsp:attribute>
				</t:input>
				<t:input path="org">
					<jsp:attribute name="label"><s:message code="organization.label" /></jsp:attribute>
				</t:input>
				<t:input path="description">
					<jsp:attribute name="label"><s:message code="description.label" /></jsp:attribute>
				</t:input>
				<t:textarea path="postalAddress">
					<jsp:attribute name="label"><s:message code="postalAddress.label" /></jsp:attribute>
				</t:textarea>
			</fieldset>

			<fieldset class="col-lg-6 col-lg-offset-1">
				<legend><s:message code="editUserDetailsForm.fieldset.credentials"/></legend>
				<div class="form-group">
					<label class="col-lg-4 control-label"><s:message code="uid.label" /></label>
					<div class="col-lg-8">
						<p class="form-control-static">
							${editUserDetailsFormBean.uid}
						</p>
					</div>
				</div>
				<div class="form-group">
					<label class="col-lg-4 control-label"><s:message code="password.label" /></label>
					<div class="col-lg-8">
						<p class="form-control-static">
							<a href='<c:out value="/ldapadmin/account/changePassword?uid=${editUserDetailsFormBean.uid}" />'  >
								<s:message code="editUserDetailsForm.changePassword.link" />
							</a>
						</p>
					</div>
				</div>
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
	<script src='js/bootstrap.min.js'></script>
</body>
</html>
