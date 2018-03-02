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
    this.delete = this.$injector.get('$location').$$search['delete'] === 'true'
    this.canDelete = false

    this.users = User.query(() => {
      this.allUsers = this.users.slice()
    })

    this.roles = Role.query()
    this.activePromise = this.roles.$promise.then(() => {
      this.activeRole = this.roles.filter(g => g.cn === $routeParams.id)[0]
      if (this.activeRole) {
        this.filter(this.activeRole)
        this.canDelete = !this.$injector.get('roleAdminFilter')(
          this.activeRole
        )
      }
      return this.activeRole
    })

    let translate = this.$injector.get('translate')
    this.i18n = {}
    translate('role.created', this.i18n)
    translate('role.updated', this.i18n)
    translate('role.deleted', this.i18n)
    translate('role.error', this.i18n)
    translate('role.deleteError', this.i18n)
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
    this.delete = false
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
    $scope.$watch(() => $location.search()['delete'], (v) => {
      this.delete = v === 'true'
    })
  }

  deleteRole () {
    let $location = this.$injector.get('$location')
    $location.search('delete', 'true')
  }

  confirmDeleteRole () {
    const flash = this.$injector.get('Flash')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    const $router = this.$injector.get('$router')
    const $location = this.$injector.get('$location')
    this.activeRole.$delete(
      () => {
        flash.create('success', this.i18n.deleted)
        $httpDefaultCache.removeAll()
        $router.navigate($router.generate('users', {id: 'all'}))
        $location.url($location.path())
      },
      flash.create.bind(flash, 'danger', this.i18n.deleteError)
    )
  }

  initEditable () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    $('.content-description').on('blur', (e) => {
      this.$injector.get('Role').get(
        {id: this.activeRole.cn},
        (role) => {
          role.description = e.target.innerText
          role.$update(() => {
            $httpDefaultCache.removeAll()
            flash.create('success', this.i18n.updated)
          }, flash.create.bind(flash, 'danger', this.i18n.error))
        }
      )
    })
  }

}

UsersController.prototype.activate.$inject = [ '$scope' ]

angular.module('admin_console')
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
