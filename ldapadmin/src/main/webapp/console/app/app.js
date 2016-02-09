"use strict";

angular.module('admin_console', [
  'ngResource',
  'ngNewRouter',
  'angular-chosen',
  'flash',
  'pascalprecht.translate'
]).controller(
  'AppController', [ '$router', AppController ]
).constant(
  'LDAP_BASE_URI', '/ldapadmin/private/'
).config(['$translateProvider', function ($translateProvider) {

  $translateProvider
    .preferredLanguage('en')
    .useSanitizeValueStrategy('escape')
    .useStaticFilesLoader({
      prefix: '../public/lang/',
      suffix: '.json'
    });

}]);

require('./components/users/users');
require('./components/groups/groups');
require('./components/user/user');
require('./components/home/home');
require('./services/groups');
require('./services/groups_users');
require('./services/messages');
require('./services/users');

function AppController($router) {
  $router.config([
    { path: '/'                 , redirectTo: '/home' },
    { path: '/home'             , component: 'home' },
    { path: '/groups/:id/users' , component: 'users' },
    { path: '/users/:id/:tab'   , component: 'user' },
  ]);
}
