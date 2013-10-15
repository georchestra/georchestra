<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='css/bootstrap.min.css' rel="stylesheet" />
	<link href='css/ldapadmin.css' rel="stylesheet" />
	<title><s:message code="createAccountForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="createAccountForm.title"/> <small><s:message code="createAccountForm.subtitle" /></small></h1>
		</div>
		<p class="lead"><s:message code="createAccountForm.description" /></p>
		<form:form id="form" name="form" method="post" action="new" modelAttribute="accountFormBean" cssClass="form-horizontal col-lg-6 col-lg-offset-1" onsubmit="return validate();">

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
				<legend><s:message code="createAccountForm.fieldset.userDetails"/></legend>
				<t:input path="firstName" required="true" onkeyup="makeUid();" onchange="makeUid();">
					<jsp:attribute name="label"><s:message code="firstName.label" /></jsp:attribute>
				</t:input>
				<t:input path="surname" required="true" onkeyup="makeUid();" onchange="makeUid();">
					<jsp:attribute name="label"><s:message code="surname.label" /></jsp:attribute>
				</t:input>
				<t:input path="email" required="true">
					<jsp:attribute name="label"><s:message code="email.label" /></jsp:attribute>
				</t:input>
				<t:input path="phone">
					<jsp:attribute name="label"><s:message code="phone.label" /></jsp:attribute>
				</t:input>
				<t:input path="org">
					<jsp:attribute name="label"><s:message code="organization.label" /></jsp:attribute>
				</t:input>
				<t:input path="title">
					<jsp:attribute name="label"><s:message code="title.label" /></jsp:attribute>
				</t:input>
				<t:textarea path="description">
					<jsp:attribute name="label"><s:message code="description.label" /></jsp:attribute>
				</t:textarea>
			</fieldset>

			<fieldset>
				<legend><s:message code="createAccountForm.fieldset.credentials"/></legend>
				<t:input path="uid" required="true" appendIcon="user">
					<jsp:attribute name="label"><s:message code="uid.label" /></jsp:attribute>
				</t:input>
				<t:password path="password" required="true" spanId="pwdQuality" appendIcon="lock" onblur="passwordOnBlur();" onchange="cleanConfirmPassword();feedbackPassStrength('pwdQuality', value);" onkeypress="cleanConfirmPassword();" onkeyup="feedbackPassStrength('pwdQuality', value);">
					<jsp:attribute name="label"><s:message code="password.label" /></jsp:attribute>
				</t:password>
				<t:password path="confirmPassword" required="true" onblur="confirmPasswordOnBlur();">
					<jsp:attribute name="label"><s:message code="confirmPassword.label" /></jsp:attribute>
				</t:password>
			</fieldset>

			<fieldset>
				<legend><s:message code="createAccountForm.fieldset.reCaptcha"/></legend>
				<t:recaptcha path="recaptcha_response_field" />
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
    /* to be called when either Firstname or Surname is modified
     * ("keyup" or "change" event - "input" event is not available with this version of spring)
     */
    function makeUid(){
        var name = document.form.firstName.value;
        var surname = document.form.surname.value;
        var str = stringDeaccentuate(name.toLowerCase().charAt(0)+ surname.toLowerCase());
        str = str.replace(/\W*/g, '');
        document.form.uid.value = str;
    }
    /**
     * stringDeaccentuate
     * Returns a string without accents
     *
     * Parameters:
     * str - {String}
     *
     * Returns:
     * {String}
     */
    function stringDeaccentuate(str) {
        str = str.replace(/ç/, 'c');
        str = str.replace(/(á|à|ä|â|å|Â|Ä|Á|À|Ã)/, 'a');
        str = str.replace(/(é|è|ë|ê|Ê|Ë|É|È|Ę)/, 'e');
        str = str.replace(/(í|ì|ï|î|Î|Ï|Í|Ì|Į)/, 'i');
        str = str.replace(/(ó|ò|ö|ô|ø|Ô|Ö|Ó|Ò)/, 'o');
        return str.replace(/(ú|ù|ü|û|Û|Ü|Ú|Ù|Ų)/, 'u');
    }
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
        if (testFirstname() & testSurname() & testEmail() & testUid() & testPassword() & testConfirmPassword() & testRecaptcha()) {
            return true;
        } else {
            setFormError();
            return false;
        }
    }
	</script>
</body>
</html>
