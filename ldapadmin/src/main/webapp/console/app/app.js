"use strict";

angular.module('admin_console', [
  'ngResource',
  'ngNewRouter',
  'angular-chosen',
  'flash',
  'angularUtils.directives.dirPagination',
  'pascalprecht.translate'
]).controller(
  'AppController', [ '$router', AppController ]
).constant(
  'LDAP_BASE_URI', '/ldapadmin/private/'
).constant(
  'ANALYTICS_BASE_URI', '/analytics/ws/'
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

}]).config(['$locationProvider', function($locationProvider) {
  $locationProvider.html5Mode(false)
}]).config(['paginationTemplateProvider', function(paginationTemplateProvider) {
  paginationTemplateProvider.setPath('templates/dirPagination.tpl.html')
}]);

require('./components/users/users');
require('./components/users/users.tpl');
require('./components/groups/groups');
require('./components/groups/groups.tpl');
require('./components/stats/stats');
require('./components/stats/stats.tpl');
require('./components/user/user');
require('./components/user/user.tpl');
require('./components/home/home');
require('./components/home/home.tpl');
require('./templates/dirPagination.tpl');
require('./services/analytics');
require('./services/util');
require('./services/groups');
require('./services/groups_users');
require('./services/logs');
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
