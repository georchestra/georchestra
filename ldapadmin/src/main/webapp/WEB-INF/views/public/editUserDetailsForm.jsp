<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link href="<c:url value="/styles/ldapadmin.css" />" rel="stylesheet"  type="text/css" />     
    <title>User Details</title>
</head>
<body>
    <div id="formsContent" style="center">
        <h2><s:message code="userDetailsForm.title"/> </h2>
        <form:form id="form" method="post" modelAttribute="editUserDetailsFormBean" cssClass="cleanform">

            <div class="header">
                <c:if test="${not empty message}">
                    <div id="message" class="success">${message}</div>  
                </c:if>
                <s:bind path="*">
                    <c:if test="${status.error}">
                        <div id="message" class="error"><s:message code="form.error"/></div>
                    </c:if>
                </s:bind>
            </div>

            <fieldset>

                <p>
                    <form:label path="firstName"> <s:message code="firstName.label"/> </form:label>
                    <form:input path="firstName" size="30" maxlength="80"/>
                </p>
                <p>
                    <form:errors path="firstName" cssClass="error" />
                </p>

				<p>
					<form:label path="surname"><s:message code="surname.label"/>  </form:label>
					<form:input path="surname" size="30" maxlength="80"/>
				</p>
				<p>
					<form:errors path="surname" cssClass="error" />
				</p>

                <p>
                    <form:label path="org"><s:message code="organization.label" />  </form:label>
                    <form:input path="org" size="30" maxlength="80"/>
                </p>

				<p>
					<form:label path="title" ><s:message code="title.label" /> </form:label>
					<form:input path="title" size="30" maxlength="80"/>
				</p>
				
			</fieldset>
			
			<fieldset>
			    <legend> <s:message code="address.label" /> </legend>
                <p>
                    <form:label path="postOfficeBox"> <s:message code="postOfficeBox.label" /> </form:label>
                    <form:input path="postOfficeBox" size="20" maxlength="20"/>
                </p>

                <p>
                    <form:label path="postalAddress"> <s:message code="postalAddress.label"/> </form:label>
                    <form:textarea path="postalAddress" rows="4" cols="30"/>
                </p>

                <p>
                    <form:label path="postalCode"> <s:message code="postalCode.label"/> </form:label>
                    <form:input path="postalCode" size="20" maxlength="20"/>
                </p>
                
                <p>
                    <form:label path="registeredAddress"> <s:message code="registeredAddress.label"/> </form:label>
                    <form:textarea path="registeredAddress" rows="4" cols="40"/>
                </p>

                <p>
                    <form:label path="physicalDeliveryOfficeName"><s:message code="physicalDeliveryOfficeName.label" /> </form:label>
                    <form:input path="physicalDeliveryOfficeName" size="20" maxlength="20"/>
                </p>
                
                <p>
	                <a href=<c:out value="/ldapadmin/public/accounts/changePassword?uid=${editUserDetailsFormBean.uid}" />  > 
	                    <s:message code="changePassword.link" /> 
	                </a>
                </p>
            </fieldset>

			<p>
				<button type="submit"><s:message code="submit.label" /></button>
			</p>
		</form:form>
    </div>
</body>
</html>