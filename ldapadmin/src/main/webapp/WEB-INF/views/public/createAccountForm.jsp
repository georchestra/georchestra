<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>


<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href='<c:url value="/css/bootstrap.min.css" />' rel="stylesheet" />
	<title>Create Account Form</title>
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
				/* TODO: i18n */
			  document.getElementById("passwordError").innerHTML = "The passwords are not equals";
			  document.createForm.password.focus();
			  return false;
		}
		return true;
	}
	function cleanPasswordError(){
		document.getElementById("passwordError").innerHTML="";
	}
	function strongPassword(){
		var score = scorePassword(document.createForm.password.value);
		if(score < 60){
			document.getElementById("passwordError").innerHTML = "A better password is required (<b>good</b> or <b>strong</b> level are acceptable).";
			return false;
		}
		return true;
	}

	/**
	 * Validates the form
	 */
	function validateForm(){
		if( !equalsPasswords() ) return false;
		if( !strongPassword() ) return false;
		return true;
	}
	</script>
</head>

<body>
	<div class="container" id="formsContent" style="center">
		<h2 class="text-center"><s:message code="createAccountFrom.title"/></h2>
		<form:form id="createForm" name="createForm" method="post" modelAttribute="accountFormBean" cssClass="form-horizontal" onsubmit="return validateForm();" >

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

				<div class="control-group">
					<form:label path="firstName" cssClass="control-label"><s:message code="firstName.label" /> *</form:label>
					<div class="controls">
						<form:input path="firstName" size="30" maxlength="80" onkeyup="makeUid();" onchange="makeUid();" />
						<form:errors path="firstName" cssClass="help-inline"/>
					</div>
				</div>
				<div class="control-group">
					<form:label path="surname" cssClass="control-label"><s:message code="surname.label"/> *</form:label>
					<div class="controls">
						<form:input path="surname" size="30" maxlength="80" onkeyup="makeUid();" onchange="makeUid();" />
						<form:errors path="surname" cssClass="help-inline" />
					</div>
				</div>
				<div class="control-group">
					<form:label path="email" cssClass="control-label"> <s:message code="email.label" /> *</form:label>
					<div class="controls">
						<form:input path="email" size="30" maxlength="80"/>
						<form:errors path="email" cssClass="help-inline" />
					</div>
				</div>
			</fieldset>

			<fieldset>
				<div class="control-group">
					<form:label path="phone" cssClass="control-label"><s:message code="phone.label"/> </form:label>
					<div class="controls">
						<form:input path="phone" size="30" maxlength="80"/>
						<form:errors path="phone" cssClass="help-inline" />
					</div>
				</div>
				<div class="control-group">
					<form:label path="org" cssClass="control-label"><s:message code="organization.label" />  </form:label>
					<div class="controls">
						<form:input path="org" size="30" maxlength="80"/>
					</div>
				</div>
				<div class="control-group">
					<form:label path="details" cssClass="control-label"><s:message code="details.label" />  </form:label>
					<div class="controls">
						<form:textarea path="details" rows="3" cols="30" />
					</div>
				</div>
			</fieldset>

			<fieldset>
				<div class="control-group">
					<form:label path="uid" cssClass="control-label"><s:message code="uid.label" /> *</form:label>
					<div class="controls">
						<form:input path="uid" size="30" maxlength="80" />
						<form:errors path="uid" cssClass="help-inline" />
					</div>
				</div>
				<div class="control-group">
					<form:label path="password" cssClass="control-label"><s:message code="password.label" /> *</form:label>
					<div class="controls">
						<form:password path="password" size="30" maxlength="80" onchange="feedbackPassStrength(password, pwdQuality, value);" onkeypress="cleanPasswordError();" onkeyup="feedbackPassStrength(password, pwdQuality, value);" />
						<span id="pwdQuality" class="help-inline"></span>
						<form:errors path="password" cssClass="help-inline" />
					</div>
				</div>
				<div class="control-group">
					<form:label path="confirmPassword" cssClass="control-label"><s:message code="confirmPassword.label" /> *</form:label>
					<div class="controls">
						<form:password path="confirmPassword" size="30" maxlength="80" onblur="equalsPasswords();" />
						<span id="passwordError" class="help-inline"></span>
						<form:errors path="confirmPassword" cssClass="help-inline" />
					</div>
				</div>
			</fieldset>

			<fieldset>
				<div class="control-group">
					<div class="controls">
						<%
						ReCaptcha c = ReCaptchaFactory.newReCaptcha("6Lf0h-MSAAAAAOQ4YyRtbCNccU87dlGmokmelZjh", "6Lf0h-MSAAAAAI2nHJfNPDaEXXjsdmn8eKSZUrQZ", false);
						out.print(c.createRecaptchaHtml(null, null));
						%>
						<form:errors path="recaptcha_response_field" cssClass="help-inline" />
					</div>
				</div>
			</fieldset>

			<div class="form-actions">
				<button type="submit" class="btn btn-primary"><s:message code="submit.label"/> </button>
			</div>
		</form:form>
	</div>
	<script src="http://code.jquery.com/jquery.js"></script>
	<script src='<c:url value="/js/bootstrap.min.js" />'></script>
</body>
</html>
