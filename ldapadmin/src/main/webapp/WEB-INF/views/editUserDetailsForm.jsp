<%--

 Copyright (C) 2009-2016 by the geOrchestra PSC

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
	<link href='css/ldapadmin.css' rel="stylesheet" />
	<title><s:message code="editUserDetailsForm.title"/></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

	<div class="container">
		<div class="page-header">
			<h1><s:message code="editUserDetailsForm.title"/> <small><s:message code="editUserDetailsForm.subtitle" /></small></h1>
		</div>
		<p class="lead"><s:message code="editUserDetailsForm.description" /></p>
		<form:form id="form" name="form" method="post" action="userdetails" modelAttribute="editUserDetailsFormBean" cssClass="form-horizontal col-lg-6 col-lg-offset-1" onsubmit="return validate();">

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

			<fieldset>
				<legend><s:message code="editUserDetailsForm.fieldset.userDetails"/></legend>
				<t:input path="firstName" required="${firstNameRequired}">
					<jsp:attribute name="label"><s:message code="firstName.label" /></jsp:attribute>
				</t:input>
				<t:input path="surname" required="${surnameRequired}">
					<jsp:attribute name="label"><s:message code="surname.label" /></jsp:attribute>
				</t:input>
				<div class="form-group">
					<label class="col-lg-4 control-label"><s:message code="email.label" /></label>
					<div class="col-lg-8">
						<p class="form-control-static">
							${editUserDetailsFormBean.email}
						</p>
					</div>
				</div>
				<t:input path="phone" required="${phoneRequired}">
					<jsp:attribute name="label"><s:message code="phone.label" /></jsp:attribute>
				</t:input>
				<t:input path="facsimile" required="${facsimileRequired}">
					<jsp:attribute name="label"><s:message code="facsimile.label" /></jsp:attribute>
				</t:input>
				<div class="form-group">
					<label class="col-lg-4 control-label"><s:message code="org.label" /></label>
					<div class="col-lg-8">
						<p class="form-control-static">
							<c:choose>
								<c:when test="${!empty editUserDetailsFormBean.org}">
									${editUserDetailsFormBean.org}
								</c:when>
								<c:otherwise>
									<span style="color: rgb(186, 186, 186);">
										<s:message code="password.label.empty"/>
									</span>
								</c:otherwise>
							</c:choose>
						</p>
					</div>
				</div>
				<t:input path="title" required="${titleRequired}">
					<jsp:attribute name="label"><s:message code="title.label" /></jsp:attribute>
				</t:input>
				<t:textarea path="description" required="${descriptionRequired}">
					<jsp:attribute name="label"><s:message code="description.label" /></jsp:attribute>
				</t:textarea>
				<t:textarea path="postalAddress" required="${postalAddressRequired}">
					<jsp:attribute name="label"><s:message code="postalAddress.label" /></jsp:attribute>
				</t:textarea>
			</fieldset>

			<fieldset>
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
							<a href='<c:out value="${publicContextPath}/account/changePassword?uid=${editUserDetailsFormBean.uid}" />'  >
								<s:message code="editUserDetailsForm.changePassword.link" />
							</a>
						</p>
					</div>
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
    /* Validate the form */
    function validate() {
        if (testFirstname() & testSurname() &
			testField("phone") & testField("org") & testField("title") & testField("description") &
			testField("facsimile") & testField("postalAddress")
			) {
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
        $("input#firstName").attr("placeholder", "<s:message code="firstName.placeholder" />");
        $("input#surname").attr("placeholder", "<s:message code="surname.placeholder" />");
        $("input#phone").attr("placeholder", "<s:message code="phone.placeholder" />");
        $("input#facsimile").attr("placeholder", "<s:message code="facsimile.placeholder" />");
        $("input#org").attr("placeholder", "<s:message code="org.placeholder" />");
        $("input#title").attr("placeholder", "<s:message code="title.placeholder" />");
        $("textarea#postalAddress").attr("placeholder", "<s:message code="postalAddress.placeholder" />");
        $("textarea#description").attr("placeholder", "<s:message code="description.placeholder" />");
    });
	</script>
</body>
</html>
