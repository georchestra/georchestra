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
).constant(
  'ANALYTICS_BASE_URI', 'http://localhost:8280/analytics/ws/'
).config(['$componentLoaderProvider', function ($componentLoaderProvider) {
  $componentLoaderProvider.setTemplateMapping(function (name) {
    return 'components/' + name + '/' + name + '.tpl.html';
  });
}]).config(['$translateProvider', function ($translateProvider) {

  $translateProvider
    .preferredLanguage('en')
    .useSanitizeValueStrategy('escape')
    .useStaticFilesLoader({
      prefix: 'lang/',
      suffix: '.json'
    });

}]);

require('./components/users/users');
require('./components/users/users.tpl');
require('./components/groups/groups');
require('./components/groups/groups.tpl');
require('./components/user/user');
require('./components/user/user.tpl');
require('./components/home/home');
require('./components/home/home.tpl');
require('./services/analytics');
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
