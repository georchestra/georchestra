class AppController {

  static $inject = [ '$scope', '$router', '$location' ]

  constructor ($scope, $router, $location) {
    $router.config([
      { path: '/',
        redirectTo: '/home' },
      { path: '/home',
        component: 'home' },
      { path: '/analytics/:group',
        component: 'analytics' },
      { path: '/orgs/:org',
        component: 'orgs' },
      { path: '/org/:org/:tab',
        component: 'org' },
      { path: '/groups/:id/users',
        component: 'users' },
      { path: '/users/:id/:tab',
        component: 'user' },
      { path: '/users/add',
        component: 'newUser' },
      { path: '/logs',
        component: 'logs' }
    ])

    $scope.isActive = (routes) => routes.some(
      route => $location.$$path.indexOf(route) === 1
    )
  }

}

class StandaloneController {

  static $inject = [ '$scope', 'Orgs' ]

  constructor ($scope, Org) {
    $scope.org = new Org()
  }

}

angular.module('admin_console', [
  'ngResource',
  'ngNewRouter',
  'ngSanitize',
  'inline',
  'localytics.directives',
  'flash',
  'angularUtils.directives.dirPagination',
  'pascalprecht.translate'
])
.controller('AppController', AppController)
.controller('StandaloneController', StandaloneController)
.constant('LDAP_ROOT_URI', '/ldapadmin/')
.constant('LDAP_BASE_URI', '/ldapadmin/private/')
.constant('LDAP_PUBLIC_URI', '/ldapadmin/public/')
.constant('MF_BASE_URI', '/mapfishapp/ws/')
.constant('ANALYTICS_BASE_URI', '/analytics/ws/')
.config([
  '$componentLoaderProvider',
  '$translateProvider',
  '$locationProvider',
  'paginationTemplateProvider',
  'LDAP_ROOT_URI',
  ($componentLoader, $translate, $location, paginationTemplate, $uri) => {
    $componentLoader.setTemplateMapping(
    (name) => 'components/' + name + '/' + name + '.tpl.html')
    $translate
    .useSanitizeValueStrategy('escape')
    .useStaticFilesLoader({ prefix: $uri + 'console/public/lang/', suffix: '.json' })
    .determinePreferredLanguage()
    .fallbackLanguage('en')
    $location.html5Mode(false)
    paginationTemplate.setPath('templates/dirPagination.tpl.html')
  }])

require('components/analytics/analytics')
require('components/orgs/orgs')
require('components/org/org')
require('components/home/home')
require('components/groups/groups')
require('components/logs/logs')
require('components/newUser/newUser')
require('components/stats/stats')
require('components/user/user')
require('components/users/users')

require('templates/dirPagination.tpl')
