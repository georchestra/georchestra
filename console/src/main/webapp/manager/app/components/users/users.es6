require('components/users/users.tpl')

require('services/users')
require('services/roles_users')
require('services/logs')
require('services/messages')

class UsersController {
  static $inject = [ '$routeParams', '$injector', 'User', 'Role' ]

  constructor ($routeParams, $injector, User, Role) {
    this.$injector = $injector

    this.q = ''
    this.itemsPerPage = 25

    this.newRole = this.$injector.get('$location').$$search['new'] === 'role'
    this.newRoleName = ''

    this.users = User.query(() => {
      this.allUsers = this.users.slice()
    })

    this.roles = Role.query()
    this.activePromise = this.roles.$promise.then(() => {
      this.activeRole = this.roles.filter(g => g.cn === $routeParams.id)[0]
      if (this.activeRole) {
        this.filter(this.activeRole)
      }
      return this.activeRole
    })
  }

  filter (role) {
    this.users.$promise.then(() => {
      this.users = this.allUsers.filter(
        user => (role.users.indexOf(user.uid) >= 0)
      )
    })
  }

  close () {
    this.newRole = false
    this.newRoleName = ''
    let $location = this.$injector.get('$location')
    $location.url($location.path())
  }

  saveRole () {
    let flash = this.$injector.get('Flash')
    let $router = this.$injector.get('$router')
    let $location = this.$injector.get('$location')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

    let role = new (this.$injector.get('Role'))()
    role.cn = this.newRoleName
    role.description = this.newRoleDesc

    role.$save(
      () => {
        flash.create('success', this.i18n.created)
        $httpDefaultCache.removeAll()
        $router.navigate($router.generate('users', {id: role.cn}))
        $location.url($location.path())
      },
      flash.create.bind(flash, 'danger', this.i18n.error)
    )
  }

  activate ($scope) {
    let $location = this.$injector.get('$location')
    $scope.$watch(() => $location.search()['new'], (v) => {
      this.newRole = v === 'role'
    })
  }
}

UsersController.prototype.activate.$inject = [ '$scope' ]

angular.module('manager')
  .controller('UsersController', UsersController)
  .directive('validateRole', () => ({
    require: 'ngModel',
    link: (scope, elm, attrs, ctrl) => {
      ctrl.$validators.validateRole = (modelValue, viewValue) => {
        let roles = scope.$eval(attrs['validateRole'])
        let prefix = viewValue.substr(0, viewValue.lastIndexOf('_'))
        return prefix === '' || roles.some(g => g.cn === prefix)
      }
    }
  }))
