'use strict';

/* App Module */

angular.module('ldapadmin', ['ldapadmin.filters', 'ldapadmin.controllers', 'ldapadmin.directives', 'restangular', 'angular-flash.flash-alert-directive']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
        when('/users', {templateUrl: 'partials/users-list.html',   controller: 'UsersListCtrl'}).
        when('/users/new', {templateUrl: 'partials/user-edit.html', controller: 'UserCreateCtrl'}).
        when('/users/:userId', {templateUrl: 'partials/user-edit.html', controller: 'UserEditCtrl'}).
        when('/groups/:group', {templateUrl: 'partials/users-list.html', controller: 'UsersListCtrl'}).
        otherwise({redirectTo: '/users'});
    }
  ])
  .config(function(RestangularProvider) {
    RestangularProvider.setBaseUrl("/users");
  });
