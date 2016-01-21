"use strict";
angular.module('admin_console', [
  'ngResource',
  'ngNewRouter'
]).controller('AppController', [
  '$router', AppController
]).constant('LDAP_BASE_URI', '/ldapadmin/private/');

require('./components/users/users');
require('./components/groups/groups');
require('./components/user/user');
require('./components/home/home');
require('./services/groups');
require('./services/users');

function AppController($router) {
  $router.config([
    { path: '/'                 , redirectTo: '/home' },
    { path: '/home'             , component: 'home' },
    { path: '/groups/:id/users' , component: 'users' },
    { path: '/users/:id/:tab'   , component: 'user' },
  ]);
}
