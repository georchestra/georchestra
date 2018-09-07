import 'components/role/role.tpl'
import 'templates/roleForm.tpl'
import 'services/roles'

class RoleController {
  static $inject = [ '$injector', '$routeParams' ]

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.tabs = ['infos', 'users', 'manage']
    this.tab = $routeParams.tab

    this.itemsPerPage = 15

    let translate = $injector.get('translate')
    this.i18n = {}
    translate('role.updated', this.i18n)
    translate('role.error', this.i18n)
    translate('role.deleted', this.i18n)
    translate('role.deleteError', this.i18n)
    translate('role.userremoved', this.i18n)
    translate('role.useradded', this.i18n)

    this.loadRoleAndUsers($routeParams.role)
  }

  loadRoleAndUsers (id) {
    // Save original cn in case we change cn, because we need to
    // use original cn with PUT request
    this.role = this.$injector.get('Role').get({id: id}, role => {
      role.originalID = role.cn
    })

    this.role.$promise.then(() => {
      const User = this.$injector.get('User')
      User.query(users => {
        this.users = users.filter(u => this.role.users.indexOf(u.uid) >= 0)
        this.notUsers = users.filter(u => this.role.users.indexOf(u.uid) === -1)
      })
    })
  }

  save () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let $router = this.$injector.get('$router')
    this.role.$update(() => {
      $httpDefaultCache.removeAll()
      this.role.originalID = this.role.cn
      flash.create('success', this.i18n.updated)
      $router.navigate($router.generate('role', {
        role: this.role.cn,
        tab: 'infos'
      }))
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

  associate (uid, unassociate = false) {
    if (!uid) uid = this.user
    if (!uid) return
    const flash = this.$injector.get('Flash')
    const RolesUsers = this.$injector.get('RolesUsers')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    const $router = this.$injector.get('$router')

    RolesUsers.save({
      users: [ uid ],
      PUT: unassociate ? [] : [this.role.cn],
      DELETE: unassociate ? [this.role.cn] : []
    }, () => {
      $httpDefaultCache.removeAll()
      this.loadRoleAndUsers(this.role.cn)
      flash.create('success', unassociate ? this.i18n.userremoved : this.i18n.useradded)
    }, () => {
      flash.create('danger', 'FAIL')
    })

  }
}

angular.module('manager').controller('RoleController', RoleController)
