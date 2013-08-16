<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link href="<c:url value="/styles/ldapadmin.css" />" rel="stylesheet"  type="text/css" />     
    <title>Create Account Form</title>
    
    <script type="text/javascript"  src="<c:url value="/js/passwordutils.js" />" > </script>
    
    <script type="text/javascript">
    
    function makeUid(){
    	
    	var name = document.createForm.firstName.value;
    	var surname = document.createForm.surname.value;
    	
        document.createForm.uid.value = name.toLowerCase().charAt(0)+ surname.toLowerCase(); // strategy 1
    	
        //document.createForm.uid.value = name +"."+ surname;  // strategy 2
    }
    
    function equalsPasswords() {
    	
    	var pwd1 = document.createForm.password.value;
        var pwd2 = document.createForm.confirmPassword.value;
    	
        if((pwd1 == "") || (pwd2 == "")) return false; 

    	if (pwd1 != pwd2) {

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
    <div id="formsContent" style="center">
        <h2><s:message code="createAccountFrom.title"/></h2>
        <form:form id="createForm" name="createForm" method="post" modelAttribute="accountFormBean" cssClass="cleanform" onsubmit="return validateForm();" >

            <div class="header">
                <c:if test="${not empty message}">
                    <div id="message" class="success">${message}</div>  
                </c:if>
                <s:bind path="*">
                    <c:if test="${status.error}">
                        <div id="message" class="error"><s:message code="form.error" /></div>
                    </c:if>
                </s:bind>
            </div>
            
			<fieldset>

				<p>
					<form:label path="firstName"><s:message code="firstName.label" /> *</form:label>
					<form:input path="firstName" size="30" maxlength="80" onkeyup="makeUid();" />
				</p>
				<p>
					<form:errors path="firstName" cssClass="error"/>
				</p>

                <p>
                    <form:label path="surname"><s:message code="surname.label"/> *</form:label>
                    <form:input path="surname" size="30" maxlength="80" onkeyup="makeUid();" />
                </p>
                <p>
                    <form:errors path="surname" cssClass="error" />
                </p>

				<p>
					<form:label path="email"> <s:message code="email.label" /> *</form:label>
					<form:input path="email" size="30" maxlength="80"/>
				</p>
				<p>
					<form:errors path="email" cssClass="error" />
				</p>

				<p>
					<form:label path="phone"><s:message code="phone.label"/> </form:label>
					<form:input path="phone" size="30" maxlength="80"/>
				</p>
				<p>
					<form:errors path="phone" cssClass="error" />
				</p>
				
				<p>
					<form:label path="org"><s:message code="organization.label" />  </form:label>
					<form:input path="org" size="30" maxlength="80"/>
				</p>

				<p>
					<form:label path="details"><s:message code="details.label" />  </form:label>
					<form:textarea path="details" rows="3" cols="30" />
				</p>

			</fieldset>

			<fieldset>
                <p>
                    <form:label path="uid"><s:message code="uid.label" /> *</form:label>
                    <form:input path="uid" size="30" maxlength="80"/>
                </p>
                <p>
                    <form:errors path="uid" cssClass="error" />
                </p>
				<p>
					<form:label path="password"><s:message code="password.label" /> *</form:label>
					<form:password path="password" size="30" maxlength="80" onkeypress="cleanPasswordError();" onkeyup="feedbackPassStrength(password, pwdQuality, value);" />
				</p>
				<p>
					<form:errors path="password" cssClass="error" />
				</p>
                <p>
                    <div id="pwdQuality"></div>
                </p>

				<p>
					<form:label path="confirmPassword"><s:message code="confirmPassword.label" /> *</form:label>
					<form:password path="confirmPassword" size="30" maxlength="80" onblur="equalsPasswords();"/>
				</p>
				<p>
					<form:errors path="confirmPassword" cssClass="error" />
				</p>
				<p>
				    <div id="passwordError"></div>
				</p>

			</fieldset>

			<fieldset>
			
			     <p>
					<%
					 ReCaptcha c = ReCaptchaFactory.newReCaptcha("6Lf0h-MSAAAAAOQ4YyRtbCNccU87dlGmokmelZjh", "6Lf0h-MSAAAAAI2nHJfNPDaEXXjsdmn8eKSZUrQZ", false);
					 out.print(c.createRecaptchaHtml(null, null));
					%>
			     </p>
                <p>
                    <form:errors path="recaptcha_response_field" cssClass="error" />
                </p>
			</fieldset>

			<p>
				<button type="submit"><s:message code="submit.label"/> </button>
			</p>
		</form:form>
    </div>
</body>
</html>