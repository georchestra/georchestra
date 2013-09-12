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
	<title><s:message code="newPasswordForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
	<div class="container">
		<div class="page-header">
			<h1><s:message code="newPasswordForm.title"/></h1>
		</div>
		<form:form id="form" name="form" method="post" action="newPassword" modelAttribute="newPasswordFormBean" cssClass="form-horizontal" >

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
				<legend><s:message code="newPasswordForm.fieldset.password"/></legend>
				<t:password path="password" required="true" spanId="pwdQuality" appendIcon="lock" onchange="cleanPasswordError();feedbackPassStrength('pwdQuality', value);" onkeypress="cleanPasswordError();" onkeyup="feedbackPassStrength('pwdQuality', value);">
					<jsp:attribute name="label"><s:message code="password.label" /></jsp:attribute>
				</t:password>
				<t:password path="confirmPassword" required="true" spanId="passwordError" onblur="equalPasswords();">
					<jsp:attribute name="label"><s:message code="confirmPassword.label" /></jsp:attribute>
				</t:password>
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
	<script type="text/javascript"  src="js/passwordutils.js" > </script>
	<script type="text/javascript">
    /* to be called when either Firstname or Surname is modified
     * ("keyup" or "change" event - "input" event is not available with this version of spring)
     */
    function makeUid(){
        var name = document.form.firstName.value;
        var surname = document.form.surname.value;
        document.form.uid.value = name.toLowerCase().charAt(0)+ surname.toLowerCase(); // strategy 1
        //document.form.uid.value = name +"."+ surname;  // strategy 2
    }
    /* to be called when the password confirmation field loses focus */
    function equalPasswords() {
        var pwd1 = document.form.password.value;
        var pwd2 = document.form.confirmPassword.value;
        if (pwd1 != pwd2) {
            document.getElementById("passwordError").innerHTML = '<s:message code="confirmPassword.error.pwdNotEquals.tag" />';
            document.form.password.focus();
            return false;
        }
        return true;
    }
    /* to be called when the password field is modified */
    function cleanPasswordError(){
        document.getElementById("passwordError").innerHTML="";
        document.getElementById("confirmPassword").value="";
    }
	</script>
</body>
</html>
