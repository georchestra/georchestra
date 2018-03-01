<%--

 Copyright (C) 2009-2017 by the geOrchestra PSC

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

<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link href='css/bootstrap.min.css' rel="stylesheet" />
    <link href="css/select2.css" rel="stylesheet" />
    <link href='css/console.css' rel="stylesheet" />
    <title><s:message code="createAccountForm.title"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@ include file="header.jsp" %>

    <fieldset class="container">
        <div class="page-header">
            <h1><s:message code="createAccountForm.title"/> <small><s:message code="createAccountForm.subtitle" /></small></h1>
        </div>
        <p class="lead"><s:message code="createAccountForm.description" /></p>
        <form:form id="form" name="form" method="post" action="new" modelAttribute="accountFormBean" cssClass="form-horizontal col-lg-10 col-lg-offset-1" onsubmit="return validate();">

            <c:if test="${not empty message}">
            <div id="message" class="alert alert-dismissable alert-info">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                ${message}
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
                <legend><s:message code="createAccountForm.fieldset.userDetails"/></legend>
                <t:input path="firstName" required="${firstNameRequired}" onkeyup="makeUid();" onchange="makeUid();">
                    <jsp:attribute name="label"><s:message code="firstName.label" /></jsp:attribute>
                </t:input>
                <t:input path="surname" required="${surnameRequired}" onkeyup="makeUid();" onchange="makeUid();">
                    <jsp:attribute name="label"><s:message code="surname.label" /></jsp:attribute>
                </t:input>
                <t:input path="email" required="${emailRequired}">
                    <jsp:attribute name="label"><s:message code="email.label" /></jsp:attribute>
                </t:input>
                <t:input path="phone" required="${phoneRequired}">
                    <jsp:attribute name="label"><s:message code="phone.label" /></jsp:attribute>
                </t:input>
                <t:list path="org" required="${orgRequired}" items="${orgs}">
                    <jsp:attribute name="label"><s:message code="org.label" /></jsp:attribute>
                </t:list>

                <div id="org_checkbox_div" class="form-group" style="margin-top: -15px;">
                    <div class="col-lg-8 col-lg-offset-4" >
                        <form:checkbox id="createOrg" path="createOrg"/>
                        <label for="createOrg" style="font-weight:normal"><s:message code="org.cannot_find_org_in_list" /></label>
                    </div>
                </div>

                <div id="create_org_div" class="create_org_block">

                    <t:input path="orgName" required="true">
                        <jsp:attribute name="label"><s:message code="org.creation.label"/></jsp:attribute>
                    </t:input>

                    <t:input path="orgShortName" required="${orgShortNameRequired}">
                        <jsp:attribute name="label"><s:message code="org.creation.shortLabel"/></jsp:attribute>
                    </t:input>

                    <t:textarea path="orgAddress" required="${orgAddressRequired}">
                        <jsp:attribute name="label"><s:message code="org.creation.address"/></jsp:attribute>
                    </t:textarea>

                    <t:list path="orgType" required="${orgTypeRequired}" items="${orgTypes}">
                        <jsp:attribute name="label"><s:message code="org.creation.orgType"/></jsp:attribute>
                    </t:list>


                    <link rel="stylesheet" href="/console/console/public/libraries.css">
                    <link rel="stylesheet" href="/console/console/public/app.css">
                    <style>
                      .area {
                        margin: 2em;
                        background: white;
                      }
                      .area > .col-md-4 {
                        border-right: 1em solid white;
                        border-bottom: 1em solid white;
                      }
                    </style>
                    <script src="/console/console/public/libraries.js"></script>
                    <script src="/console/console/public/templates.js"></script>
                    <script src="/console/console/public/app.js"></script>
                    <script>require('app')</script>

                    <div ng-app="admin_console" ng-strict-di ng-controller="StandaloneController">
                      <areas item="org"></areas>
                    </div>

                </div>

                <t:input path="title" required="${titleRequired}">
                    <jsp:attribute name="label"><s:message code="title.label" /></jsp:attribute>
                </t:input>
                <t:textarea path="description" required="${descriptionRequired}">
                    <jsp:attribute name="label"><s:message code="description.label" /></jsp:attribute>
                </t:textarea>
            </fieldset>

            <fieldset>
                <legend><s:message code="createAccountForm.fieldset.credentials"/></legend>
                <t:input path="uid" required="${uidRequired}" appendIcon="user">
                    <jsp:attribute name="label"><s:message code="uid.label" /></jsp:attribute>
                </t:input>
                <t:password path="password" required="${passwordRequired}" spanId="pwdQuality" appendIcon="lock" onblur="passwordOnBlur();" onchange="cleanConfirmPassword();feedbackPassStrength('pwdQuality', value);" onkeypress="cleanConfirmPassword();" onkeyup="feedbackPassStrength('pwdQuality', value);">
                    <jsp:attribute name="label"><s:message code="password.label" /></jsp:attribute>
                </t:password>
                <t:password path="confirmPassword" required="${confirmPasswordRequired}" onblur="confirmPasswordOnBlur();">
                    <jsp:attribute name="label"><s:message code="confirmPassword.label" /></jsp:attribute>
                </t:password>
            </fieldset>

            <fieldset>
                <legend><s:message code="createAccountForm.fieldset.reCaptcha"/></legend>
                <t:recaptcha path="recaptcha_response_field" />
            </fieldset>

            <fieldset>
                <div class="form-group">
                    <div class="col-lg-8 col-lg-offset-4 text-right">
                        <button type="submit" class="btn btn-primary btn-lg"><s:message code="submit.label"/> </button>
                    </div>
                </div>
            </fieldset>
        </form:form>
    </fieldset>
    <script src="js/jquery.js"></script>
    <script src='js/bootstrap.min.js'></script>
    <script src="js/select2.full.js"></script>
    <%@ include file="validation.jsp" %>
    <script type="text/javascript">
    /* to be called when either Firstname or Surname is modified
     * ("keyup" or "change" event - "input" event is not available with this version of spring)
     */
    function makeUid(){
        var name = document.form.firstName.value;
        var surname = document.form.surname.value;
        var str = stringDeaccentuate(name.toLowerCase().charAt(0)+ surname.toLowerCase());
        str = str.replace(/\W*/g, '');
        document.form.uid.value = str;
    }
    /**
     * stringDeaccentuate
     * Returns a string without accents
     *
     * Parameters:
     * str - {String}
     *
     * Returns:
     * {String}
     */
    function stringDeaccentuate(str) {
        str = str.replace(/ç/, 'c');
        str = str.replace(/(á|à|ä|â|å|Â|Ä|Á|À|Ã)/, 'a');
        str = str.replace(/(é|è|ë|ê|Ê|Ë|É|È|Ę)/, 'e');
        str = str.replace(/(í|ì|ï|î|Î|Ï|Í|Ì|Į)/, 'i');
        str = str.replace(/(ó|ò|ö|ô|ø|Ô|Ö|Ó|Ò)/, 'o');
        return str.replace(/(ú|ù|ü|û|Û|Ü|Ú|Ù|Ų)/, 'u');
    }
    function confirmPasswordOnBlur() {
        if (!testConfirmPassword()) {
            document.form.password.focus();
        }
    }
    function passwordOnBlur() {
        if (!testPassword()) {
            document.form.password.focus();
        }
    }
    function cleanConfirmPassword(){
        document.getElementById("confirmPassword").value="";
        removeError("confirmPassword");
    }
    /* Validate the form */
    function validate() {
        if (testFirstname() & testSurname() & testEmail() & testUid() & testPassword() & testConfirmPassword() &
                testRecaptcha() & testField("phone") & testField("title") & testField("description") & testOrg()
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
        $("input#email").attr("placeholder", "<s:message code="email.placeholder" />");
        $("input#phone").attr("placeholder", "<s:message code="phone.placeholder" />");
        $("input#org").attr("placeholder", "<s:message code="org.placeholder" />");
        $("input#title").attr("placeholder", "<s:message code="title.placeholder" />");
        $("textarea#description").attr("placeholder", "<s:message code="description.placeholder" />");
        $("input#uid").attr("placeholder", "<s:message code="uid.placeholder" />");
        $("input#password").attr("placeholder", "<s:message code="password.placeholder" />");
        $("input#confirmPassword").attr("placeholder", "<s:message code="confirmPassword.placeholder" />");
        $("#org").select2();
        $("#orgType").select2({width: "100%"});

        // Animate org creation form
        $( "#createOrg" ).change(function() {
            if($( "#createOrg" ).prop( "checked" ) ){
                setTimeout(function() {
                  angular.element($('areas .map')[0]).scope().area.map.updateSize();
                }, 300);
                $("#create_org_div").show(200);
                $("#org").prop("disabled", true);
            } else {
                $("#create_org_div").hide(200);
                $("#org").prop("disabled", false);
            }
        });

    });
    </script>
</body>
</html>
