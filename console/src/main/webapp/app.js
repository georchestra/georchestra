"use strict";

angular.module('admin_console', [
  'admin_console.home',
  'admin_console.users',
  'userServices',
  'ngNewRouter'
]).controller('AppController', [
  '$router', AppController
]).constant('LDAP_BASE_URI', 'http://localhost:8286/ldapadmin/private/');

function AppController($router) {
  $router.config([
    { path: '/'                      , redirectTo: '/home' },
    { path: '/home'                  , component: 'home' },
    { path: '/users/:groupid'       , component: 'users' },
  ]);
}

