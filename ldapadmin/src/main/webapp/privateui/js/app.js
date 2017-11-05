/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    RestangularProvider.setBaseUrl(GEOR_config.publicContextPath + "/private");
  })
  .filter('encodeURIComponent', function() {
    return window.encodeURIComponent;
  });
