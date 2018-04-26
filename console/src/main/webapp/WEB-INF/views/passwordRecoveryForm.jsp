<%--

 Copyright (C) 2009-2018 by the geOrchestra PSC

 This file is part of geOrchestra.

 geOrchestra is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option)
 any later version.

 geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 more details.

 You should have received a copy of the GNU General Public License along with
 geOrchestra.  If not, see <http://www.gnu.org/licenses/>.

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
	<title><s:message code="passwordRecoveryForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>
<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="passwordRecoveryForm.title"/> <small><s:message code="passwordRecoveryForm.subtitle" /></small></h1>
		</div>
		<p class="lead"><s:message code="passwordRecoveryForm.description" /></p>
		<form:form id="form" name="form" method="post" action="passwordRecovery" modelAttribute="passwordRecoveryFormBean" cssClass="form-horizontal col-lg-6 col-lg-offset-1" onsubmit="return validate();">
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
				<legend><s:message code="passwordRecoveryForm.fieldset.email"/></legend>
				<t:input path="email" required="true">
					<jsp:attribute name="label"><s:message code="email.label" /></jsp:attribute>
				</t:input>
			</fieldset>

			<fieldset>
				 <t:recaptcha path="g-recaptcha" />
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
    /* Validate the form */
    function validate() {
        if (testEmail() & testRecaptcha()) {
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
        $("input#email").attr("placeholder", "<s:message code="email.placeholder" />");
    });
	</script>
</body>
</html>
