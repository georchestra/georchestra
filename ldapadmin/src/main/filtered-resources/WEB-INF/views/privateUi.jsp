<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%
// the context path (might not be the public context path ! -> to be improved with https://github.com/georchestra/georchestra/issues/227)
String context = request.getContextPath().split("-")[0]; // eg /ldapadmin
%>
<!DOCTYPE html>
<html lang="en" ng-app="ldapadmin">
  <head>
    <meta charset="UTF-8">
    <title>LDAPadmin - geOrchestra</title> <!-- TODO: dynamic instance name -->
    <link rel="stylesheet" href="lib/bootstrap/css/bootstrap.min.css" />

    <link rel="stylesheet" href="css/main.css" />
  </head>
  <body>

    <%@ include file="header-privateui.jsp" %>

    <div id="container" class="container-fluid" ng-controller="UsersCtrl">
      <!-- Subscribe to success flash messages. -->
      <div flash-alert="success" active-class="in" class="alert hide">
        <span class="alert-message">{{flash.message}}</span>
      </div>
      <!-- Subscribe to error flash messages. -->
      <div flash-alert="error" active-class="in" class="alert hide">
        <strong class="alert-heading">Boo!</strong>
        <span class="alert-message">{{flash.message}}</span>
      </div>

      <div class="row-fluid">
        <div id="sidebar" class="span3">
          <div class="toolbar shadow">
            <a id="new_user" href="#/users/new" class="btn">
              <i class="icon-plus-sign"></i>
              New user
            </a>
            <a id="new_group" href="#/groups/new" class="btn">
              <i class="icon-plus-sign"></i>
              New group
            </a>
          </div>
          <!--Sidebar content-->
          <div class="content">
            <i class="icon-blank"></i>
            <a href="#/" ng-class="{active: selectedGroup == null}">
              All users ({{users.length}})
            </a>
            <br />
            <br />
            <script type="text/ng-template" id="group_item_renderer.html">
              <a href="#/groups/{{data.group.cn}}" ng-if="data.group != null" ng-class="{active: data.group==selectedGroup}">
                <i class="icon-blank"></i>
                {{data.group.cn}} <small>({{data.group.users.length || 0}})</small>
              </a>
              <div ng-if="!data.group && data.nodes.length" class="accordion-group">
                <div class="accordion-heading">
                  <a class="accordion-toggle" onclick="$('#collapse{{data.name}}').collapse('toggle');" data-toggle="collapse">
                    <i class="icon-chevron-right"></i>
                    {{data.group.cn || data.name}}
                  </a>
                </div>
                <div id="collapse{{data.name}}" class="accordion-body collapse">
                  <div class="accordion-inner">
                    <div ng-repeat="data in data.nodes" ng-include="'group_item_renderer.html'"></div>
                  </div>
                </div>
              </div>
            </script>
            <div class="groups" ng-controller="GroupsCtrl">
              <div class="accordion" ng-repeat="data in groups_tree" ng-include="'group_item_renderer.html'">
              </div>
            </div>
            
            <br />
            <i class="icon-blank"></i>
            <a href="#/groups/none" ng-class="{active: selectedGroup == 'none'}">
              Users with no access
            </a>
            
          </div>
        </div>
        <div id="content" class="span9" ng-view>
        <!--Body content-->
        </div>
      </div>
    </div>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.1.5/angular.min.js"></script>
    <!-- for debug purposes:
    <script src="lib/angular/angular.js"></script>
    -->
    <script src="lib/angular-flash.min.js"></script>
    <script type="text/javascript">
    var GEOR_config = {
        publicContextPath: "<%= context %>"
    };
    </script>
    <script src="js/app.js"></script>
    <script src="js/services.js"></script>
    <script src="js/controllers.js"></script>
    <script src="js/filters.js"></script>
    <script src="js/directives.js"></script>
    <script src="lib/bootstrap/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="//cdn.jsdelivr.net/underscorejs/1.4.3/underscore-min.js"></script>
    <script type="text/javascript" src="//cdn.jsdelivr.net/restangular/latest/restangular.min.js"></script>
  </body>
</html>
