import 'components/role/role.tpl'
import 'templates/roleForm.tpl'
import 'services/roles'

class RoleController {
  static $inject = [ '$injector', '$routeParams' ]

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.tabs = ['infos', 'manage']
    this.tab = $routeParams.tab

    let translate = $injector.get('translate')
    this.i18n = {}
    translate('role.updated', this.i18n)
    translate('role.error', this.i18n)
    translate('role.deleted', this.i18n)
    translate('role.deleteError', this.i18n)

    this.role = $injector.get('Role').get({id: $routeParams.role})
  }

  save () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.role.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

  delete () {
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let flash = this.$injector.get('Flash')
    this.role.$delete(() => {
      $httpDefaultCache.removeAll()
      let $router = this.$injector.get('$router')
      $router.navigate($router.generate('roles', { id: 'all' }))
      flash.create('success', this.i18n.deleted)
    }, flash.create.bind(flash, 'danger', this.i18n.deleteError))
  }

  confirm () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.role.status = 'REGISTERED'
    this.role.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }
}

angular.module('manager').controller('RoleController', RoleController)
