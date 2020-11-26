<%--

 Copyright (C) 2009-2018 by the geOrchestra PSC

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
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<!--TODO: set appropriate lang-->
<!--TODO: favicon-->
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link href='css/bootstrap.min.css' rel="stylesheet"/>
  <link href='css/console.css' rel="stylesheet"/>
  <title><s:message code="editUserDetailsForm.title"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
<%@ include file="header.jsp" %>


<script>var org = ${org};
var isReferentOrSuperUser = ${isReferentOrSuperUser};
var gdprAllowAccountDeletion = ${gdprAllowAccountDeletion};
</script>

<div class="container">
  <div class="page-header">
    <h1><s:message code="editUserDetailsForm.title"/> <small><s:message
        code="editUserDetailsForm.subtitle"/></small></h1>
  </div>
  <p class="lead"><s:message code="editUserDetailsForm.description"/></p>
  <form:form id="form" name="form" method="post" action="userdetails"
             modelAttribute="editUserDetailsFormBean" cssClass="form-horizontal col-lg-6"
             onsubmit="return validate();">

    <c:if test="${not empty success}">
      <div id="message" class="alert alert-dismissable alert-success">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <s:message code="editUserDetailsForm.success"/>
      </div>
    </c:if>

    <s:bind path="*">
      <c:if test="${status.error}">
        <div id="message" class="alert alert-dismissable alert-danger">
          <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;
          </button>
          <s:message code="form.error"/>
        </div>
      </c:if>
    </s:bind>

    <fieldset>
      <legend><s:message code="editUserDetailsForm.fieldset.userDetails"/></legend>
      <t:input path="firstName" required="${firstNameRequired}">
        <jsp:attribute name="label"><s:message code="firstName.label"/></jsp:attribute>
      </t:input>
      <t:input path="surname" required="${surnameRequired}">
        <jsp:attribute name="label"><s:message code="surname.label"/></jsp:attribute>
      </t:input>
      <div class="form-group">
        <label class="col-lg-4 control-label"><s:message code="email.label"/></label>
        <div class="col-lg-8">
          <p class="form-control-static">
              ${editUserDetailsFormBean.email}
          </p>
        </div>
      </div>
      <t:input path="phone" required="${phoneRequired}">
        <jsp:attribute name="label"><s:message code="phone.label"/></jsp:attribute>
      </t:input>
      <t:input path="facsimile" required="${facsimileRequired}">
        <jsp:attribute name="label"><s:message code="facsimile.label"/></jsp:attribute>
      </t:input>
      <div class="form-group">
        <label class="col-lg-4 control-label"><s:message code="org.label"/></label>
        <div class="col-lg-8">
          <p class="form-control-static">
            <c:choose>
              <c:when test="${!empty editUserDetailsFormBean.org}">
                ${editUserDetailsFormBean.org}
              </c:when>
              <c:otherwise>
									<span style="color: rgb(186, 186, 186);">
										<s:message code="org.empty"/>
									</span>
              </c:otherwise>
            </c:choose>
          </p>
        </div>
      </div>
      <t:input path="title" required="${titleRequired}">
        <jsp:attribute name="label"><s:message code="title.label"/></jsp:attribute>
      </t:input>
      <t:textarea path="description" required="${descriptionRequired}">
        <jsp:attribute name="label"><s:message code="description.label"/></jsp:attribute>
      </t:textarea>
      <t:textarea path="postalAddress" required="${postalAddressRequired}">
        <jsp:attribute name="label"><s:message code="postalAddress.label"/></jsp:attribute>
      </t:textarea>
    </fieldset>

    <fieldset>
      <legend><s:message code="editUserDetailsForm.fieldset.credentials"/></legend>
      <div class="form-group">
        <label class="col-lg-4 control-label"><s:message code="uid.label"/></label>
        <div class="col-lg-8">
          <p class="form-control-static">
              ${editUserDetailsFormBean.uid}
          </p>
        </div>
      </div>
      <div class="form-group">
        <label class="col-lg-4 control-label"><s:message code="password.label"/></label>
        <div class="col-lg-8">
          <p class="form-control-static">
            <a href='<c:out value="${publicContextPath}/account/changePassword?uid=${editUserDetailsFormBean.uid}" />'>
              <s:message code="editUserDetailsForm.changePassword.link"/>
            </a>
          </p>
        </div>
      </div>
    </fieldset>

    <fieldset>
      <div class="form-group">
        <div class="col-lg-8 col-lg-offset-4 text-right">
          <button type="submit" class="btn btn-primary btn-lg"><s:message
              code="submit.label"/></button>
        </div>
      </div>
    </fieldset>
  </form:form>

  <!-- angularjs app dependencies -->
  <link rel="stylesheet" href="/console/manager/public/libraries.css">
  <link rel="stylesheet" href="/console/manager/public/app.css">
  <style>
    .area {
      margin: 2em;
      background: white;
    }

    .area .map {
      height: 200px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .area .map canvas {
      border-radius: 3px;
    }
  </style>
  <script src="/console/manager/public/libraries.js"></script>
  <script src="/console/manager/public/templates.js"></script>
  <script src="/console/manager/public/app.js"></script>
  <script>require('app')</script>
  <!-- /angularjs app dependencies -->

  <div class="col-lg-5 col-lg-offset-1" ng-app="manager" ng-strict-di
       ng-controller="StandaloneController">
    <fieldset ng-if="org.id">
      <legend>
        <s:message code="editUserDetailsForm.organisation"/> «{{org.name}}»
				<a ng-if="isReferentOrSuperUser"
					 href="/console/account/orgdetails"
					 title="<s:message code="editUserDetailsForm.editOrg"/>"
					 class="small pull-right" aria-label="<s:message code="editUserDetailsForm.editOrg"/>"
				>
						<span class="glyphicon glyphicon-edit" aria-hidden="true"></span>
				</a>
      </legend>

      <ul class="list-unstyled">
        <li ng-if="org.description">
					<p>{{org.description}}</p>
        </li>
        <li ng-if="org.url && org.logo">
          <a href="{{org.url}}">
            <img alt="org logo" src="data:image/jpeg;base64,{{org.logo}}" class="org-logo"/>
          </a>
        </li>
        <li ng-if="org.url && !org.logo">
          <a href="{{org.url}}">
            <s:message code="editUserDetailsForm.url"/>
          </a>
        </li>
        <li ng-if="!org.url && org.logo">
          <img alt="logo" src="data:image/jpeg;base64,{{org.logo}}"/>
        </li>
      </ul>

      <h4><s:message code="editUserDetailsForm.areaOfCompetence"/></h4>
      <areas item="org" readonly="'true'"></areas>
      <br>

      <h4><s:message code="editUserDetailsForm.members"/> <span
          class="badge">{{ users.length }}</span></h4>
      <ul>
        <li dir-paginate="user in users | itemsPerPage: 10">
          {{::user.sn}} {{::user.givenName}}
        </li>
      </ul>

      <dir-pagination-controls></dir-pagination-controls>
    </fieldset>
    <fieldset class="gdpr">
      <legend><s:message code="editUserDetailsForm.gdpr"/></legend>
      <div
          class="panel panel-default">
        <div class="panel-body">
          <p>
            <s:message code="editUserDetailsForm.downloadMsg"/>
          </p>
          <p>
            <a class="btn btn-primary"
               href="<c:out value="${publicContextPath}/account/gdpr/download" />" target="_blank">
              <i class="glyphicon glyphicon-download-alt"></i> <s:message
                code="editUserDetailsForm.download"/>
            </a>
          </p>
        </div>
      </div>
      <div class="panel panel-default" *ng-if="gdprAllowAccountDeletion">
        <div class="panel-body">
          <p>
            <s:message code="editUserDetailsForm.deleteMsg"/>
          </p>
          <p>
            <button class="btn btn-danger">
              <i class="glyphicon glyphicon-exclamation-sign"></i> <s:message
                code="editUserDetailsForm.delete"/>
            </button>
          </p>
        </div>
      </div>
    </fieldset>
  </div>
</div>
<script src="//code.jquery.com/jquery.js"></script>
<script src='js/bootstrap.min.js'></script>
<%@ include file="validation.jsp" %>
<script type="text/javascript">
  (function () {
    var deleteURI = "<c:out value="${publicContextPath}/account/gdpr/delete" />"
    $('.gdpr .btn-danger').on('click', function () {
      if (!window.confirm('<s:message code="editUserDetailsForm.deleteConfirm" />')) return false
      fetch(deleteURI, {method: 'POST'})
        .then(function (response) {
          if (response.ok) {
            window.location.href = '/logout'
          } else {
            alert('<s:message code="editUserDetailsForm.deleteFail" />')
          }
        })
        .catch(function () {
          alert('<s:message code="editUserDetailsForm.deleteFail" />')
        })
    })
  })()

  /* Validate the form */
  function validate() {
    if (testFirstname() & testSurname() &
      testField("phone") & testField("org") & testField("title") & testField("description") &
      testField("facsimile") & testField("postalAddress")
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
  $(document).ready(function () {
    $("input#firstName").attr("placeholder", "<s:message code="firstName.placeholder" />");
    $("input#surname").attr("placeholder", "<s:message code="surname.placeholder" />");
    $("input#phone").attr("placeholder", "<s:message code="phone.placeholder" />");
    $("input#facsimile").attr("placeholder", "<s:message code="facsimile.placeholder" />");
    $("input#org").attr("placeholder", "<s:message code="org.placeholder" />");
    $("input#title").attr("placeholder", "<s:message code="title.placeholder" />");
    $("textarea#postalAddress").attr("placeholder", "<s:message code="postalAddress.placeholder" />");
    $("textarea#description").attr("placeholder", "<s:message code="description.placeholder" />");
  });
</script>
</body>
</html>
