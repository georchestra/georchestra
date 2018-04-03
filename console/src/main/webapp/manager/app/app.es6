class AppController {
  static $inject = [ '$scope', '$router', '$location', 'Profile' ]

  constructor ($scope, $router, $location, Profile) {
    $router.config([
      { path: '/',
        redirectTo: '/home' },
      { path: '/home',
        component: 'home' },
      { path: '/analytics/:role',
        component: 'analytics' },
      { path: '/orgs/:org',
        component: 'orgs' },
      { path: '/org/:org/:tab',
        component: 'org' },
      { path: '/roles/:role',
        component: 'roles' },
      { path: '/role/:role/:tab',
        component: 'role' },
      { path: '/delegations/:delegation',
        component: 'delegations' },
      { path: '/browse/:id/users',
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

    Profile.get((p) => {
      $scope.profile = p.roles.indexOf('SUPERUSER') === -1
        ? 'index.delegated' : 'index.superuser'
    })
  }
}

class StandaloneController {
  static $inject = [ '$scope', 'Orgs' ]

  constructor ($scope, Org) {
    $scope.org = new Org()
  }
}

angular.module('manager', [
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
  .constant('CONSOLE_BASE_PATH', '/console/')
  .constant('CONSOLE_PRIVATE_PATH', '/console/private/')
  .constant('CONSOLE_PUBLIC_PATH', '/console/public/')
  .constant('VIEWER_SERVICES_PATH', '/mapfishapp/ws/')
  .constant('ANALYTICS_SERVICES_PATH', '/analytics/ws/')
  .config([
    '$componentLoaderProvider',
    '$translateProvider',
    '$locationProvider',
    'paginationTemplateProvider',
    'CONSOLE_BASE_PATH',
    '$qProvider',
    ($componentLoader, $translate, $location, paginationTemplate, $uri, $qP) => {
      $componentLoader.setTemplateMapping(
        (name) => 'components/' + name + '/' + name + '.tpl.html')
      $translate
        .useSanitizeValueStrategy('escape')
        .useStaticFilesLoader({ prefix: $uri + 'manager/public/lang/', suffix: '.json' })
        .registerAvailableLanguageKeys(['en', 'fr', 'de', 'es'], {
          'en_*': 'en',
          'fr_*': 'fr',
          'de_*': 'de',
          'es_*': 'es',
          '*': 'en'
        })
        .determinePreferredLanguage()
        .fallbackLanguage('en')
      $location.html5Mode(false)
      paginationTemplate.setPath('templates/dirPagination.tpl.html')
      $qP.errorOnUnhandledRejections(false)
    }])

require('components/analytics/analytics')
require('components/orgs/orgs')
require('components/org/org')
require('components/roles/roles')
require('components/role/role')
require('components/delegations/delegations')
require('components/home/home')
require('components/browse/browse')
require('components/logs/logs')
require('components/newUser/newUser')
require('components/stats/stats')
require('components/user/user')
require('components/users/users')

require('templates/dirPagination.tpl')
