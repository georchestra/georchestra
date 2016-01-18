"use strict";

angular.module('admin_console', [
  'userServices',
  'groupServices',
  'ngNewRouter'
]).controller('AppController', [
  '$router', AppController
]).constant('LDAP_BASE_URI', 'http://localhost:8286/ldapadmin/private/');

function AppController($router) {
  $router.config([
    { path: '/'                 , redirectTo: '/home' },
    { path: '/home'             , component: 'home' },
    { path: '/groups/:id/users' , component: 'users' },
    { path: '/users/:id/:tab'   , component: 'user' },
  ]);
}
