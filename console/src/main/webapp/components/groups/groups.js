angular.module('admin_console')
.component('groups', {
  bindings    : {
    groups      : '=',
    activeGroup : '=',
    index       : '=?'
  },
  controller  : GroupsController,
  templateUrl : 'components/groups/groups.html'
})
.filter('unprefix', GroupUnprefix);

function GroupsController() {
  var root = [];
  var index = {};
  this.q = (this.q) || '';

  if (this.index) { // child
    this.tree = this.groups;
  } else { // root
    this.groups.forEach(function(group) {
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
    });
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

}

GroupsController.prototype.filter = function(group, active) {
  var result = (
    !active || // All
    ((active.parent == group.parent) && (active.children.length == 0)) || // Common ancestor without child
    (group.cn == active.cn) || // Active
    group.cn.substr(0, active.cn.length) == active.cn || // Active is prefix of group
    active.cn.indexOf(group.cn) == 0 || // Leafs
    active.cn.substr(0, active.cn.lastIndexOf('_')) == group.cn // Group prefix of active
  );
  return (this.q != '') ?
    (group.cn.toLowerCase().indexOf(this.q.toLowerCase())>=0) :
    result;
};

GroupsController.prototype.isExpanded = function(group, active) {
  return (group.children.length > 0 &&
    (active && active.cn.indexOf(group.cn) == 0));
};

function GroupUnprefix() {
  return function(input, active) {
    if (!active) { return input.cn; }
    return (input.cn == active.cn) ? input.cn : input.cn.substr(active.cn.length + 1);
  }
}
