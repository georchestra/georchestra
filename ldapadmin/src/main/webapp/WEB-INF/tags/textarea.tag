<%@tag description="Extended textarea tag to allow for sophisticated errors" pageEncoding="UTF-8"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@attribute name="path" required="true" type="java.lang.String"%>
<%@attribute name="cssClass" required="false" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String"%>
<%@attribute name="required" required="false" type="java.lang.Boolean"%>
<%@attribute name="onchange" required="false" type="java.lang.String"%>
<%@attribute name="onkeyup" required="false" type="java.lang.String"%>
<c:if test="${empty label}">
	<c:set var="label" value="${fn:toUpperCase(fn:substring(path, 0, 1))}${fn:toLowerCase(fn:substring(path, 1,fn:length(path)))}" />
</c:if>
<spring:bind path="${path}">
	<div id="div-${path}" class="form-group ${status.error ? 'has-error' : '' }">
		<label class="control-label col-lg-4" for="${path}">${label}<c:if test="${required}"><span class="required">&nbsp;*</span></c:if></label>
		<div class="col-lg-8">
			<form:textarea path="${path}" cssClass="${empty cssClass ? 'form-control' : cssClass}" onkeyup="${onkeyup}" onchange="${onchange}" />
			<c:if test="${status.error}">
				<span id="span-error" class="help-block">${status.errorMessage}</span>
			</c:if>
		</div>
	</div>
</spring:bind>
