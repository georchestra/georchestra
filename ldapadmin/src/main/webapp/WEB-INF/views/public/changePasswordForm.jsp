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
    <title>Change password form</title>
    
    <script type="text/javascript">
    
    function equalsPasswords() {
        
        var pwd1 = document.changePasswordForm.password.value;
        var pwd2 = document.changePasswordForm.confirmPassword.value;
        
        if(pwd1 == "" && pwd2 == "") return false; 

        if (pwd1 != pwd2) {

              document.getElementById("passordError").innerHTML = "The passwords are not equals";
              document.changePasswordForm.password.focus();
              return false;
        }
        return true;
    }
    function cleanPasswordError(){
        document.getElementById("passordError").innerHTML="";
    }                
    
    function strongPassword(){
        return true; // TODO 
    }
    
    /**
     * Validates the fom
     */
    function validateForm(){
        
        if( ! equalsPasswords() ) return false;
        
        if( !strongPassword() ) return false;
        
        return true;
    }
    </script>
    
</head>
<body>
    <div id="formsContent" style="center">
        <h2>Change Password</h2>
        <form:form id="changePasswordForm" name="changePasswordForm" method="post" modelAttribute="changePasswordFormBean" cssClass="cleanform">

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
					<form:label path="password"><s:message code="password.label"/> *</form:label>
					<form:password path="password" size="30" maxlength="80" onkeypress="cleanPasswordError();"/>
				</p>
				<p>
					<form:errors path="password" cssClass="error" />
				</p>

				<p>
					<form:label path="confirmPassword"><s:message code="confirmPassword.label"/> *</form:label>
					<form:password path="confirmPassword" size="30" maxlength="80" onblur="equalsPasswords();"/>
				</p>
				<p>
					<form:errors path="confirmPassword" cssClass="error" />
				</p>
                <p>
                    <div id="passordError"></div>
                </p>
				

			</fieldset>

			<p>
				<button type="submit"><s:message code="submit.label" /></button>
			</p>
		</form:form>
    </div>
</body>
</html>