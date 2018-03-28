import 'components/roles/roles.tpl'
import 'services/roles'

class RolesController {
  static $inject = [ '$injector' ]

  constructor ($injector) {
    this.$injector = $injector
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
  }

  isExpanded (role, active) {
    return (role.children.length > 0 &&
      (active && active.cn.indexOf(role.cn) === 0))
  }

  createRole () {
    let $location = this.$injector.get('$location')
    $location.search('new', 'role')
  }
}

angular.module('admin_console')
  .component('roles', {
    bindings: {
      roles: '=',
      activePromise: '=',
      index: '=?'
    },
    controller: RolesController,
    controllerAs: 'roles',
    templateUrl: 'components/roles/roles.tpl.html'
  })
