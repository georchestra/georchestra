import 'components/browse/browse.tpl'
import 'services/roles'

class BrowseController {
  static $inject = [ '$injector' ]

  constructor ($injector) {
    this.$injector = $injector
    this.pendingCount = 0
    const PROTECTED = this.$injector.get('readonlyRoleList')()
    this.filterRole = (protecteds, role) => protecteds ^ !PROTECTED.includes(role)

    // Rebind method for outer use
    this.protected = this.protected.bind(this)
    this.unprotected = this.unprotected.bind(this)
  }

  $onInit () {
    let roleAdminList = this.$injector.get('roleAdminList')
    if (this.roles.$promise) {
      this.$injector.get('$q').all([
        this.roles.$promise,
        this.activePromise
      ]).then(this.initialize.bind(this, roleAdminList))
    } else {
      this.initialize(roleAdminList)
    }
  }

  initialize (roleAdminList) {
    this.activeRole = this.activePromise.$$state.value

    let index = {}
    this.q = (this.q) || ''

    this.roles.forEach(role => { index[role.cn] = role })
    this.index = index

    let fullAdminList = roleAdminList()
    this.adminList = []
    for (let idx in this.index) {
      let role = this.index[idx]
      if (fullAdminList.indexOf(role.cn) >= 0) {
        this.adminList.push(role)
      }
    }
    this.favoriteRole = this.favoriteRole.bind(this)
    this.$injector.get('User').query(users => (
      this.pendingCount = users.filter(u => u.pending).length
    ))
  }

  favoriteRole (role) {
    return role.isFavorite && this.adminList.indexOf(role) === -1
  }

  createRole () {
    let $location = this.$injector.get('$location')
    $location.search('new', 'role')
  }

  protected (role) {
    return this.filterRole(true, role.cn)
  }

  unprotected (role) {
    return this.filterRole(false, role.cn)
  }
}

angular.module('manager')
  .component('browse', {
    bindings: {
      roles: '=',
      activePromise: '=',
      index: '=?'
    },
    controller: BrowseController,
    controllerAs: 'roles',
    templateUrl: 'components/browse/browse.tpl.html'
  })
