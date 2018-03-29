import 'components/roles/roles.tpl'
import 'services/roles'

class RolesController {
  static $inject = [ '$injector', '$routeParams' ]

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.role = $routeParams.role
    this.roles = this.$injector.get('Role').query(() => {
      this.roles.forEach(r => {
        r.usersCount = r.users.length
        delete r.users
      })
    })

    this.q = ''
    this.itemsPerPage = 15

    this.newRole = this.$injector.get('$location').$$search['new'] === 'role'
    if (this.newRole) {
      const Role = this.$injector.get('Role')
      this.newInstance = new Role({})
    }

    let translate = this.$injector.get('translate')
    this.i18n = {}
    translate('role.created', this.i18n)
    translate('role.updated', this.i18n)
    translate('role.deleted', this.i18n)
    translate('role.error', this.i18n)
    translate('role.deleteError', this.i18n)
  }

  create () {
    const Role = this.$injector.get('Role')
    this.newInstance = new Role({})
    let $location = this.$injector.get('$location')
    $location.search('new', 'role')
  }

  saveRole () {
    let flash = this.$injector.get('Flash')
    let $router = this.$injector.get('$router')
    let $location = this.$injector.get('$location')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

    this.newInstance.$save(
      () => {
        flash.create('success', this.i18n.created)
        $httpDefaultCache.removeAll()
        $router.navigate($router.generate('role', {
          role: this.newInstance.cn,
          tab: 'infos'
        }))
        $location.url($location.path())
      },
      flash.create.bind(flash, 'danger', this.i18n.error)
    )
  }

  close () {
    this.newRole = false
    let $location = this.$injector.get('$location')
    $location.url($location.path())
  }

  activate ($scope) {
    let $location = this.$injector.get('$location')
    $scope.$watch(() => $location.search()['new'], (v) => {
      this.newRole = v === 'role'
    })
  }
}

RolesController.prototype.activate.$inject = [ '$scope' ]

angular.module('admin_console').controller('RolesController', RolesController)
