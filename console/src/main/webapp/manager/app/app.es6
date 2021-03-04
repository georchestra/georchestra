class AppController {
  static $inject = [
    '$scope', '$router', '$location', '$translate', 'roleAdminList', 'Profile', 'PlatformInfos'
  ]

  constructor ($scope, $router, $location, $translate, roleAdminList, Profile, PlatformInfos) {
    $router.config([
      {
        path: '/',
        redirectTo: '/home'
      },
      {
        path: '/home',
        component: 'home'
      },
      {
        path: '/analytics/:role',
        component: 'analytics'
      },
      {
        path: '/orgs/:org',
        component: 'orgs'
      },
      {
        path: '/org/:org/:tab',
        component: 'org'
      },
      {
        path: '/roles/:role',
        component: 'roles'
      },
      {
        path: '/role/:role/:tab',
        component: 'role'
      },
      {
        path: '/delegations',
        component: 'delegations'
      },
      {
        path: '/browse/:id/users',
        component: 'users'
      },
      {
        path: '/users/:id/:tab',
        component: 'user'
      },
      {
        path: '/users/add',
        component: 'newUser'
      },
      {
        path: '/logs',
        component: 'logs'
      }
    ])

    $scope.isActive = (routes) => routes.some(
      route => $location.$$path.indexOf(route) === 1
    )

    $scope.isSuperUser = (user) => user.adminRoles && user.adminRoles.SUPERUSER

    Profile.get((p) => {
      $scope.profile = p.roles.indexOf('SUPERUSER') === -1
        ? 'DELEGATED' : 'SUPERUSER'
    })
    $scope.platformInfos = PlatformInfos.get()

    $scope.isProtectedRole = role => roleAdminList().indexOf(role.cn) !== -1
    $scope.$translate = $translate
  }
}

class StandaloneController {
  static $inject = ['$injector', '$window', '$http', '$scope', 'Orgs', 'User']

  constructor ($injector, $window, $http, $scope, Org) {
    if (!$window.org) {
      $scope.org = new Org()
      return
    }

    $scope.org = $window.org
    $scope.users = $window.org.members
    $scope.isReferentOrSuperUser = $window.isReferentOrSuperUser
    $scope.gdprAllowAccountDeletion = $window.gdprAllowAccountDeletion
    $scope.deleteURI = $injector.get('CONSOLE_BASE_PATH') + 'account/gdpr/delete'

    const i18n = {}
    $injector.get('translate')('editUserDetailsForm.deleteConfirm', i18n)
    $injector.get('translate')('editUserDetailsForm.deleteFail', i18n)

    $scope.deleteUser = function () {
      if (!$window.confirm(i18n.deleteConfirm)) return false
      $http.post($scope.deleteURI)
        .then(
          function success (response) { $window.location.href = '/logout' },
          function error () { $window.alert(i18n.deleteFail) }
        )
    }
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
        .registerAvailableLanguageKeys(['en', 'fr', 'de', 'es', 'nl'], {
          'en_*': 'en',
          'fr_*': 'fr',
          'de_*': 'de',
          'es_*': 'es',
          'nl_*': 'nl',
          '*': 'en'
        })
        .determinePreferredLanguage()
        .fallbackLanguage('en')
      $location.html5Mode({
        enabled: true,
        requireBase: false
      })
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
      $httpProvider.defaults.headers.get.Pragma = 'no-cache'
      // }}}
    }])
  .filter('dateFormat', ['$translate', ($translate) => {
    moment.locale($translate.use())
    return (date, format) => {
      const m = moment(date)
      return `<span title="${m.format('lll')}">${!format ? m.fromNow() : m.format(format)}</span>`
    }
  }])
  .directive('shortname', () => ({
    require: 'ngModel',
    restrict: 'A',
    link: (scope, elm, attrs, ctrl) => {
      const regexp = ('shortnameLower' in attrs) ? /^[A-Za-z0-9-_]+$/ : /^[A-Z0-9-_]+$/
      const alphanum = v => v && v.match(regexp)
      ctrl.$validators.shortname = alphanum
    }
  }))

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
require('components/logger/logger')
require('components/user/user')
require('components/users/users')

require('templates/dirPagination.tpl')
