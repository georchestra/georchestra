require('components/roles/roles.tpl')

require('services/roles')

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

    let root = []
    let index = {}
    this.q = (this.q) || ''

    if (this.index) { // child
      this.tree = this.roles
    } else { // root
      this.roles.forEach((role) => {
        // Store for quick access
        index[role.cn] = role
        // Set parent for each role
        role.parent = (role && role.cn.indexOf('_') > 0)
          ? role.cn.substr(0, role.cn.lastIndexOf('_')) : 'all'
        // Initialize children
        role.children = role.children || []
        if (role.cn.split('_').length === 1) {
          root.push(role)
        } else {
          index[role.parent].children.push(role)
        }
      })
      this.tree = root
      this.index = index
    }

    this.isRoot = this.roles.length === Object.keys(index).length
    this.enableBack = this.isRoot && this.activeRole

    if (this.activeRole) {
      this.prefix = (this.activeRole.children.length === 0)
        ? this.index[this.activeRole.parent] : this.activeRole
      this.enableBack = this.enableBack && this.prefix
    }

    let fullAdminList = roleAdminList()
    this.adminList = []
    for (let idx in this.index) {
      let role = this.index[idx]
      if (fullAdminList.indexOf(role.cn) >= 0) {
        this.adminList.push(role)
      }
    }
  }

  filter (role, active) {
    let result = (
      !active || // All
      ((active.parent === role.parent) && (active.children.length === 0)) || // Common ancestor without child
      (role.cn === active.cn) || // Active
      role.cn.substr(0, active.cn.length) === active.cn || // Active is prefix of role
      active.cn.indexOf(role.cn) === 0 || // Leafs
      active.cn.substr(0, active.cn.lastIndexOf('_')) === role.cn // Role prefix of active
    )

    return result && this.adminList.concat(
      [ {cn: 'MOD'}, {cn: 'GN'} ] // Avoid empty parents
    ).every(g => g.cn !== role.cn)
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
.filter('unprefix', () => (input, active) => {
  if (!active) { return input.cn }
  return (input.cn === active.cn) ? input.cn : input.cn.substr(active.cn.length + 1)
})
