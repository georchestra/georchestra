angular.module('admin_console.groups', [])
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

function GroupsController($scope) {
  var root = [];
  var index = {};

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

  $scope.isRoot = this.groups.length == Object.keys(index).length;
  $scope.enableBack = $scope.isRoot && this.activeGroup;

  if (this.activeGroup) {
    $scope.prefix = (this.activeGroup.children.length == 0) ?
        this.index[this.activeGroup.parent] : this.activeGroup;
    $scope.enableBack = $scope.enableBack && $scope.prefix;
  }

}

GroupsController.$inject = [ '$scope' ];

GroupsController.prototype.filter = function(group, active) {
  return (
    !active || // All
    ((active.parent == group.parent) && (active.children.length == 0)) || // Common ancestor without child
    (group.cn == active.cn) || // Active
    group.cn.substr(0, active.cn.length) == active.cn || // Active is prefix of group
    active.cn.substr(0, active.cn.lastIndexOf('_')) == group.cn // Group prefix of active
  );
};

GroupsController.prototype.isExpanded = function(group, active) {
  return (group.children.length > 0 &&
    (active && active.cn.indexOf(group.cn) == 0));
};

function GroupUnprefix() {
  return function(input, active) {
    if (!active) { return input.cn; }
    return (input.children.length==0) ? input.cn : input.cn.substr(active.length + 1);
  }
}
