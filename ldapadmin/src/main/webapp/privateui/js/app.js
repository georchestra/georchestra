'use strict';

/* App Module */

angular.module('ldapadmin', ['ldapadmin.filters', 'ldapadmin.controllers', 'ldapadmin.directives', 'restangular', 'angular-flash.flash-alert-directive']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
        when('/users', {templateUrl: 'partials/users-list.html',   controller: 'UsersListCtrl'}).
        when('/users/new', {templateUrl: 'partials/user-edit.html', controller: 'UserCreateCtrl'}).
        when('/users/:userId', {templateUrl: 'partials/user-edit.html', controller: 'UserEditCtrl'}).
        when('/groups/new', {templateUrl: 'partials/group-edit.html', controller: 'GroupCreateCtrl'}).
        when('/groups/:group', {templateUrl: 'partials/users-list.html', controller: 'UsersListCtrl'}).
        when('/groups/:group/edit', {templateUrl: 'partials/group-edit.html', controller: 'GroupEditCtrl'}).
        otherwise({redirectTo: '/users'});
    }
  ])
  .config(function(RestangularProvider) {
    RestangularProvider.setBaseUrl("/ldapadmin/private");
  })
  .filter('encodeURIComponent', function() {
    return window.encodeURIComponent;
  });
