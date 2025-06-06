<%--

 Copyright (C) 2009-2025 by the geOrchestra PSC

 This file is part of geOrchestra.

 geOrchestra is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later
 version.

 geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License along with
 geOrchestra. If not, see <http://www.gnu.org/licenses/>.

--%>

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
	<link href='css/console.css' rel="stylesheet" />
	<title><s:message code="newPasswordForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="newPasswordForm.title"/> <small><s:message code="newPasswordForm.subtitle" /></small></h1>
		</div>
		<p class="lead"><s:message code="newPasswordForm.description" /></p>
		<form:form id="form" name="form" method="post" action="newPassword" modelAttribute="newPasswordFormBean" cssClass="form-horizontal col-lg-6 col-lg-offset-1" onsubmit="return validate();">

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

			<fieldset>
				<legend><s:message code="newPasswordForm.fieldset.password"/></legend>
				<t:password path="password" required="true" spanId="pwdQuality" appendIcon="lock" onblur="passwordOnBlur();" onchange="cleanConfirmPassword();feedbackPassStrength('pwdQuality', value);" onkeypress="cleanConfirmPassword();" onkeyup="feedbackPassStrength('pwdQuality', value);">
					<jsp:attribute name="label"><s:message code="password.label" /></jsp:attribute>
				</t:password>
				<t:password path="confirmPassword" required="true" onblur="confirmPasswordOnBlur();">
					<jsp:attribute name="label"><s:message code="confirmPassword.label" /></jsp:attribute>
				</t:password>
			</fieldset>
			<fieldset>
			<div>
				<small>
					<p><strong><s:message code="password.policy"/>:</strong></p>
					<div style="padding-left: 15px">
						<p><s:message code="password.error.sizeError" arguments="${pwdUtils.minimumLength}"/></p>
						<c:if test="${pwdUtils.requireLowers}" >
							<p><s:message code="password.error.requireLowers"/></p>
						</c:if>
						<c:if test="${pwdUtils.requireUppers}" >
							<p><s:message code="password.error.requireUppers"/></p>
						</c:if>
						<c:if test="${pwdUtils.requireDigits}" >
							<p><s:message code="password.error.requireDigits"/></p>
						</c:if>
						<c:if test="${pwdUtils.requireSpecials}" >
							<p><s:message code="password.error.requireSpecials"/></p>
						</c:if>
					</div>
				</small>
			</div>
		</fieldset>
			<fieldset>
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
	<%@ include file="validation.jsp" %>
	<script type="text/javascript">
    function confirmPasswordOnBlur() {
        if (!testConfirmPassword()) {
            document.form.password.focus();
        }
    }
    function passwordOnBlur() {
        if (!testPassword()) {
            document.form.password.focus();
        }
    }
    function cleanConfirmPassword(){
        document.getElementById("confirmPassword").value="";
        removeError("confirmPassword");
    }
    /* Validate the form */
    function validate() {
        if (testPassword() & testConfirmPassword()) {
            return true;
        } else {
            setFormError();
            return false;
        }
    }
    /* The current Spring version does not include placeholder in
     * form:input, form:textarea, or form:password
     * We then add placeholder afterwards by javascript
     */
    $(document).ready(function(){
        $("input#password").attr("placeholder", "<s:message code="password.placeholder" />");
        $("input#confirmPassword").attr("placeholder", "<s:message code="confirmPassword.placeholder" />");
    });
	</script>
</body>
</html>
