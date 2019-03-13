<%@tag description="Link to the privacy policy and checkbox to capture the user agreement" pageEncoding="UTF-8"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@attribute name="path" required="true" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String"%>
<%@attribute name="checkboxLabel" required="false" type="java.lang.String"%>
<%@attribute name="required" required="false" type="java.lang.Boolean"%>
<spring:bind path="${path}">
	<!-- TODO: add a link to the Privacy Policy document -->
	<div id="div-${path}" class="form-group ${status.error ? 'has-error' : '' }">
		<label class="control-label col-lg-4" for="${path}">${label}<c:if test="${required}"><span class="required">&nbsp;*</span></c:if></label>
                <div class="col-lg-8" >
			<form:checkbox id="${path}" path="${path}" />
                        <label for="${path}" style="font-weight:normal">${checkboxLabel}</label>
			<c:if test="${status.error}">
				<span id="span-error" class="help-block">${status.errorMessage}</span>
			</c:if>
                </div>
	</div>
</spring:bind>
