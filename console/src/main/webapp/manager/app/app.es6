class AppController {
  static $inject = [
    '$scope', '$router', '$location', '$translate', 'roleAdminList', 'Profile'
  ]

  constructor ($scope, $router, $location, $translate, roleAdminList, Profile) {
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

    $scope.isSuperUser = (user) => user.adminRoles && user.adminRoles.SUPERUSER

    Profile.get((p) => {
      $scope.profile = p.roles.indexOf('SUPERUSER') === -1
        ? 'DELEGATED' : 'SUPERUSER'
    })

    $scope.isProtectedRole = role => roleAdminList().indexOf(role.cn) !== -1
    $scope.$translate = $translate
  }
}

class StandaloneController {
  static $inject = [ '$scope', 'Orgs', 'User' ]

  constructor ($scope, Org, User) {
    if (!window.org) {
      $scope.org = new Org()
      return
    }

    $scope.org = window.org
    $scope.users = window.org.members
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
    '$httpProvider',
    ($componentLoader, $translate, $location, paginationTemplate, $uri, $qP, $httpProvider) => {
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
      // see https://github.com/georchestra/georchestra/issues/1695 {{{
      if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {}
      }
      // disable IE ajax request caching
      $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT'
      // extra
      $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache'
      $httpProvider.defaults.headers.get['Pragma'] = 'no-cache'
      // }}}
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
