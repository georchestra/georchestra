<%@tag description="Extended recaptcha tag to allow for sophisticated errors" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="path" required="true" type="java.lang.String"%>
<s:bind path="${path}">

	<div id="div-${path}" class="form-group ${status.error ? 'has-error' : '' }">
		<input id="recaptcha_response_field" name="recaptcha_response_field" type="hidden" value=""/>
		<div id="g-recaptcha" class="g-recaptcha" data-sitekey="${reCaptchaPublicKey}" />
		<c:if test="${status.error}">
			<span id="span-error" class="help-block">${status.errorMessage}</span>
		</c:if>
	</div>
	
	<noscript>
	<div>
	    <div style="width: 302px; height: 422px; position: relative;">
	      <div style="width: 302px; height: 422px; position: absolute;">
	        <iframe src="https://www.google.com/recaptcha/api/fallback?k=${reCaptchaPublicKey}"
	                frameborder="0" scrolling="no" style="width: 302px; height:422px; border-style: none;">
	        </iframe>
	      </div>
	    </div>
	    <div style="width: 300px; height: 60px; border-style: none;
	                   bottom: 12px; left: 25px; margin: 0px; padding: 0px; right: 25px;
	                   background: #f9f9f9; border: 1px solid #c1c1c1; border-radius: 3px;">
	      <textarea id="g-recaptcha-response" name="g-recaptcha-response"
	                   class="g-recaptcha-response"
	                   style="width: 250px; height: 40px; border: 1px solid #c1c1c1;
	                          margin: 10px 25px; padding: 0px; resize: none;" >
	      </textarea>
	    </div>
	  </div>
	</noscript>

</s:bind>
