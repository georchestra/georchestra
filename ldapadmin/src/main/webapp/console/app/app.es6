"use strict";

class AppController {

  constructor($scope, $router, $location) {

    $router.config([
      { path: '/'                 , redirectTo: '/home' },
      { path: '/home'             , component: 'home' },
      { path: '/analytics/:group' , component: 'analytics' },
      { path: '/groups/:id/users' , component: 'users' },
      { path: '/users/:id/:tab'   , component: 'user' },
      { path: '/users/add'        , component: 'newUser' },
      { path: '/logs'             , component: 'logs' },
    ])

    $scope.isActive = (routes) => routes.some(
      route => $location.$$path.indexOf(route) == 1
    )
  }

}

AppController.$inject = [ '$scope', '$router', '$location' ]

angular.module('admin_console', [
  'ngResource',
  'ngNewRouter',
  'inline',
  'localytics.directives',
  'flash',
  'angularUtils.directives.dirPagination',
  'pascalprecht.translate'
])
.controller('AppController'    , AppController)
.constant('LDAP_BASE_URI'      , '/ldapadmin/private/')
.constant('ANALYTICS_BASE_URI' , '/analytics/ws/')
.config([ '$componentLoaderProvider', '$translateProvider',
  '$locationProvider', 'paginationTemplateProvider',
  ($componentLoader, $translate, $location, paginationTemplate) => {

  $componentLoader.setTemplateMapping(
    (name) => 'components/' + name + '/' + name + '.tpl.html')
  $translate
    .preferredLanguage('en')
    .useSanitizeValueStrategy('escape')
    .useStaticFilesLoader({ prefix: 'lang/', suffix: '.json' })
  $location.html5Mode(false)
  paginationTemplate.setPath('templates/dirPagination.tpl.html')

}])

require('components/analytics/analytics')
require('components/home/home')
require('components/groups/groups')
require('components/logs/logs')
require('components/newUser/newUser')
require('components/stats/stats')
require('components/user/user')
require('components/users/users')

require('templates/dirPagination.tpl')

