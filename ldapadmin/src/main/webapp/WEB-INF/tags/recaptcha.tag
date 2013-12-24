<%@tag description="Extended recaptcha tag to allow for sophisticated errors" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="path" required="true" type="java.lang.String"%>
<%@attribute name="spanId" required="false" type="java.lang.String"%>
<s:bind path="${path}">

<div id="recaptcha_widget" style="display:none">
	<div class="form-group">
		<div class="col-lg-8 col-lg-offset-4">
			<div class="img-thumbnail col-lg-12" >
				<span id="recaptcha_image"></span>
			</div>
			<div class="recaptcha_only_if_incorrect_sol" style="color:red"><s:message code="recaptcha.incorrect" /></div>
		</div>
	</div>
	<div id="div-${path}" class="form-group ${status.error ? 'has-error' : '' }">
		<label class="recaptcha_only_if_image control-label control-label col-lg-4"><s:message code="recaptcha.words" />&nbsp;*</label>
		<label class="recaptcha_only_if_audio control-label control-label col-lg-4"><s:message code="recaptcha.numbers" />&nbsp;*</label>
		<div class="col-lg-8">
			<div class="input-group" id="recaptcha_response_field">
				<input type="text" size="30" maxlength="80" name="recaptcha_response_field" class="form-control input-recaptcha" />
				<a class="input-group-addon btn" href="javascript:Recaptcha.reload()"><i class="glyphicon glyphicon-refresh"></i></a>
				<a class="input-group-addon btn recaptcha_only_if_image" href="javascript:Recaptcha.switch_type('audio')"><i title='<s:message code="recaptcha.audio" />' class="glyphicon glyphicon-headphones"></i></a>
				<a class="input-group-addon btn recaptcha_only_if_audio" href="javascript:Recaptcha.switch_type('image')"><i title='<s:message code="recaptcha.image" />' class="glyphicon glyphicon-picture"></i></a>
				<a class="input-group-addon btn" href="javascript:Recaptcha.showhelp()"><i class="glyphicon glyphicon-question-sign"></i></a>
			</div>
			<c:if test="${status.error}">
				<span id="span-error" class="help-block">${status.errorMessage}</span>
			</c:if>
		</div>
	</div>
</div>
<script type="text/javascript">
var RecaptchaOptions = {
    theme : 'custom',
    custom_theme_widget: 'recaptcha_widget'
};
</script>
<script type="text/javascript" src="//www.google.com/recaptcha/api/challenge?k=${reCaptchaPublicKey}"></script>

<noscript>
<div class="panel panel-default">
	<div class="panel-heading"><s:message code="recaptcha.noscript.title" /></div>
	<div class="panel-body">
		<iframe src="//www.google.com/recaptcha/api/noscript?k=${reCaptchaPublicKey}"
			class="col-lg-12" height="300" frameborder="0"></iframe>
		<br>
		<textarea name="recaptcha_challenge_field" class="form-control" rows="3"></textarea>
		<input type="hidden" name="recaptcha_response_field" value="manual_challenge">
	</div>
</div>
</noscript>

</s:bind>
