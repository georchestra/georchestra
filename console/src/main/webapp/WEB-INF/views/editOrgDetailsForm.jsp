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
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link href='css/bootstrap.min.css' rel="stylesheet"/>
  <link href='css/console.css' rel="stylesheet"/>
  <title><s:message code="editOrgDetailsForm.title"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
<%@ include file="header.jsp" %>

<div class="container">
  <div class="page-header">
    <h1>${name}</h1>
  </div>
  <div class="row">
    <div class="col-md-6 col-sm-6 col-lg-6">
      <form:form id="form" name="form" method="post" action="orgdetails"
                 modelAttribute="editOrgDetailsFormBean" cssClass="form-horizontal"
                 enctype="multipart/form-data">

        <c:if test="${not empty success}">
          <div id="message" class="alert alert-dismissable alert-success">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
              &times;
            </button>
            <s:message code="editOrgDetailsForm.success"/>
          </div>
        </c:if>

        <s:bind path="*">
          <c:if test="${status.error}">
            <div id="message" class="alert alert-dismissable alert-danger">
              <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
                &times;
              </button>
              <s:message code="form.error"/>
            </div>
          </c:if>
        </s:bind>

        <fieldset>
          <input type="hidden" name="id" value="${id}"/>
          <legend><s:message code="editOrgDetailsForm.fieldset.orgDetails"/></legend>
          <t:input path="name" required="${nameRequired}">
            <jsp:attribute name="label"><s:message code="name.label"/></jsp:attribute>
          </t:input>
          <t:textarea path="description" required="${descriptionRequired}">
            <jsp:attribute name="label"><s:message code="description.label"/></jsp:attribute>
          </t:textarea>
          <t:textarea path="address" required="${addressRequired}">
            <jsp:attribute name="label"><s:message code="address.label"/></jsp:attribute>
          </t:textarea>
          <t:input path="url" required="${urlRequired}">
            <jsp:attribute name="label"><s:message code="url.label"/></jsp:attribute>
          </t:input>
          <div class="form-group">
            <label for="logo" class="control-label col-lg-4"><s:message
                code="logo.label"/></label>
            <input type="file" id="logo" name="logo" class="col-lg-8">
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
    </div>
    <div class="col-lg-6">
      <c:if test="${not empty logo}">
        <img class="preview img-responsive" src="data:image/jpeg;base64,${logo}" alt="logo"/>
      </c:if>
    </div>
  </div>
</div>

</body>

