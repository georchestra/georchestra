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
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='<c:url value="/css/bootstrap.min.css" />' rel="stylesheet" />
	<title>Create Account Form</title>
</head>

<body>
	<div class="container" id="formsContent" style="center">
		<div class="page-header">
			<h1><s:message code="createAccountFrom.title"/></h2>
		</div>
		<form:form id="createForm" name="createForm" method="post" modelAttribute="accountFormBean" cssClass="form-horizontal" >

			<c:if test="${not empty message}">
			<div id="message" class="alert alert-info">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
				${message}
			</div>
			</c:if>

			<s:bind path="*">
			<c:if test="${status.error}">
			<div id="message" class="alert alert-error">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
				<s:message code="form.error" />
			</div>
			</c:if>
			</s:bind>

			<fieldset>
				<legend>User details</legend>
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
				<t:input path="details">
					<jsp:attribute name="label"><s:message code="details.label" /></jsp:attribute>
				</t:input>
			</fieldset>

			<fieldset>
				<legend>Credentials</legend>
				<t:input path="uid" required="true" appendIcon="icon-user">
					<jsp:attribute name="label"><s:message code="uid.label" /></jsp:attribute>
				</t:input>
				<t:password path="password" required="true" spanId="pwdQuality" appendIcon="icon-lock" onchange="cleanPasswordError();feedbackPassStrength('pwdQuality', value);" onkeypress="cleanPasswordError();" onkeyup="feedbackPassStrength('pwdQuality', value);">
					<jsp:attribute name="label"><s:message code="password.label" /></jsp:attribute>
				</t:password>
				<t:password path="confirmPassword" required="true" spanId="passwordError" onblur="equalsPasswords();">
					<jsp:attribute name="label"><s:message code="confirmPassword.label" /></jsp:attribute>
				</t:password>
			</fieldset>

			<fieldset>
				<legend>ReCaptcha verification</legend>
				<t:recaptcha path="recaptcha_response_field" />
			</fieldset>

			<div class="form-actions">
				<button type="submit" class="btn btn-primary"><s:message code="submit.label"/> </button>
			</div>
		</form:form>
	</div>
	<script src="http://code.jquery.com/jquery.js"></script>
	<script src='<c:url value="/js/bootstrap.min.js" />'></script>
	<script type="text/javascript"  src="<c:url value="/js/passwordutils.js" />" > </script>
	<script type="text/javascript">
    /* to be called when either Firstname or Surname is modified
     * ("keyup" or "change" event - "input" event is not available with this version of spring)
     */
    function makeUid(){
        var name = document.createForm.firstName.value;
        var surname = document.createForm.surname.value;
        document.createForm.uid.value = name.toLowerCase().charAt(0)+ surname.toLowerCase(); // strategy 1
        //document.createForm.uid.value = name +"."+ surname;  // strategy 2
    }
    /* to be called when the password confirmation field loses focus */
    function equalsPasswords() {
        var pwd1 = document.createForm.password.value;
        var pwd2 = document.createForm.confirmPassword.value;
        if (pwd1 != pwd2) {
            document.getElementById("passwordError").innerHTML = '<s:message code="confirmPassword.error.pwdNotEquals.tag" />';
            document.createForm.password.focus();
            return false;
        }
        return true;
    }
    /* to be called when the password field is modified */
    function cleanPasswordError(){
        document.getElementById("passwordError").innerHTML="";
        document.getElementById("confirmPassword").value="";
    }
    var RecaptchaOptions = {
        theme : 'custom',
        custom_theme_widget: 'recaptcha_widget'
    };
   	</script>
	<!-- TODO: put the key in config module -->
	<script type="text/javascript" src="http://www.google.com/recaptcha/api/challenge?k=${reCaptchaPublicKey}"></script>
</body>
</html>
