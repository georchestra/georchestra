require('components/groups/groups.tpl')

require('services/groups')

class GroupsController {

  static $inject = [ '$injector', 'groupAdminList' ]

  constructor($injector, groupAdminList) {
    this.$injector = $injector

    if (this.groups.$promise) {
      $injector.get('$q').all([
        this.groups.$promise,
        this.activePromise
      ]).then(this.initialize.bind(this, groupAdminList))
    } else {
      this.initialize(groupAdminList)
    }
  }

  initialize(groupAdminList) {
    this.activeGroup = this.activePromise.$$state.value

    let root = [];
    let index = {};
    this.q = (this.q) || '';

    if (this.index) { // child
      this.tree = this.groups;
    } else { // root
      this.groups.forEach((group) => {
        // Store for quick access
        index[group.cn] = group;
        // Set parent for each group
        group.parent = (group && group.cn.indexOf('_') > 0) ?
          group.cn.substr(0, group.cn.lastIndexOf('_')) : 'all';
        // Initialize children
        group.children = group.children || [];
        if (group.cn.split('_').length == 1) {
          root.push(group);
        } else {
          index[group.parent].children.push(group);
        }
      })
      this.tree = root;
      this.index = index;
    }

    this.isRoot = this.groups.length == Object.keys(index).length;
    this.enableBack = this.isRoot && this.activeGroup;

    if (this.activeGroup) {
      this.prefix = (this.activeGroup.children.length == 0) ?
          this.index[this.activeGroup.parent] : this.activeGroup;
      this.enableBack = this.enableBack && this.prefix;
    }

    let fullAdminList = groupAdminList();
    this.adminList = [];
    for (let idx in this.index) {
      let group = this.index[idx];
      if (fullAdminList.indexOf(group.cn) >= 0) {
        this.adminList.push(group);
      }
    }

  }

  filter(group, active) {
    let result = (
      !active || // All
      ((active.parent == group.parent) && (active.children.length == 0)) || // Common ancestor without child
      (group.cn == active.cn) || // Active
      group.cn.substr(0, active.cn.length) == active.cn || // Active is prefix of group
      active.cn.indexOf(group.cn) == 0 || // Leafs
      active.cn.substr(0, active.cn.lastIndexOf('_')) == group.cn // Group prefix of active
    );

    return result && this.adminList.concat(
      [ {cn: 'MOD'}, {cn: 'GN'} ] // Avoid empty parents
    ).every(g => g.cn != group.cn)
  }

  isExpanded(group, active) {
    return (group.children.length > 0 &&
      (active && active.cn.indexOf(group.cn) == 0));
  }

  createGroup() {
    let $location = this.$injector.get('$location')
    $location.search('new', 'group')
  }

}

angular.module('admin_console')
.component('groups', {
  bindings    : {
    groups        : '=',
    activePromise : '=',
    index         : '=?'
  },
  controller   : GroupsController,
  controllerAs : 'groups',
  templateUrl  : 'components/groups/groups.tpl.html'
})
.filter('unprefix', () => (input, active) => {
  if (!active) { return input.cn; }
  return (input.cn == active.cn) ? input.cn : input.cn.substr(active.cn.length + 1);
});
