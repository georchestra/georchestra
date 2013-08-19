<%@tag description="Extended password tag to allow for sophisticated errors" pageEncoding="UTF-8"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@attribute name="path" required="true" type="java.lang.String"%>
<%@attribute name="cssClass" required="false" type="java.lang.String"%>
<%@attribute name="label" required="false" type="java.lang.String"%>
<%@attribute name="required" required="false" type="java.lang.Boolean"%>
<%@attribute name="onblur" required="false" type="java.lang.String"%>
<%@attribute name="onchange" required="false" type="java.lang.String"%>
<%@attribute name="onkeypress" required="false" type="java.lang.String"%>
<%@attribute name="onkeyup" required="false" type="java.lang.String"%>
<%@attribute name="appendIcon" required="false" type="java.lang.String"%>
<%@attribute name="spanId" required="false" type="java.lang.String"%>
<c:if test="${empty label}">
    <c:set var="label" value="${fn:toUpperCase(fn:substring(path, 0, 1))}${fn:toLowerCase(fn:substring(path, 1,fn:length(path)))}" />
</c:if>
<spring:bind path="${path}">
    <div class="control-group ${status.error ? 'error' : '' }">
        <label class="control-label" for="${path}"><s:message code="firstName.label" />${label}<c:if test="${required}"><span class="required">&nbsp;*</span></c:if></label>
        <div class="controls">
			<c:if test="${not empty appendIcon}">
				<div class="input-append">
			</c:if>
				<form:password path="${path}" size="30" maxlength="80" cssClass="${empty cssClass ? 'input-xlarge' : cssClass}" onblur="${onblur}" onchange="${onchange}" onkeypress="${onkeypress}" onkeyup="${onkeyup}" />
			<c:if test="${not empty appendIcon}">
					<span class="add-on"><i class="${appendIcon}"></i></span>
				</div>
			</c:if>
            <c:if test="${not empty spanId}">
                <span id="${spanId}" class="label label-important"></span>
            </c:if>
            <c:if test="${status.error}">
                <span class="help-inline">${status.errorMessage}</span>
            </c:if>
        </div>
    </div>
</spring:bind>
