<%@tag description="Extended recaptcha tag to allow for sophisticated errors" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="path" required="true" type="java.lang.String"%>
<%@attribute name="spanId" required="false" type="java.lang.String"%>
<s:bind path="${path}">
	<div class="control-group">
		<div class="controls">
			<a id="recaptcha_image" href="#" class="thumbnail"></a>
			<div class="recaptcha_only_if_incorrect_sol" style="color:red"><s:message code="recaptcha.incorrect" /></div>
		</div>
	</div>
	<div class="control-group ${status.error ? 'error' : '' }">
		<label class="recaptcha_only_if_image control-label"><s:message code="recaptcha.words" />&nbsp;*</label>
		<label class="recaptcha_only_if_audio control-label"><s:message code="recaptcha.numbers" />&nbsp;*</label>
		<div class="controls">
			<div class="input-append">
				<input type="text" size="30" maxlength="80" id="recaptcha_response_field" name="recaptcha_response_field" class="input-xlarge input-recaptcha" />
				<a class="btn" href="javascript:Recaptcha.reload()"><i class="icon-refresh"></i></a>
				<a class="btn recaptcha_only_if_image" href="javascript:Recaptcha.switch_type('audio')"><i title='<s:message code="recaptcha.audio" />' class="icon-headphones"></i></a>
				<a class="btn recaptcha_only_if_audio" href="javascript:Recaptcha.switch_type('image')"><i title='<s:message code="recaptcha.image" />' class="icon-picture"></i></a>
				<a class="btn" href="javascript:Recaptcha.showhelp()"><i class="icon-question-sign"></i></a>
			</div>
            <c:if test="${status.error}">
                <span class="help-inline">${status.errorMessage}</span>
            </c:if>
		</div>
	</div>
</s:bind>
