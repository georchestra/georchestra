angular.module('admin_console')
.controller('UserController', [
  '$routeParams', 'User', 'Group', 'groupAdminList', 'groupAdminFilter',
  UserController
]);

function UserController($routeParams, User, Group, groupAdminList, groupAdminFilter) {
  this.user = User.get({id : $routeParams.id});
  this.tab = $routeParams.tab;
  this.tabs = [ 'infos', 'groups', 'analytics', 'messages', 'logs', 'manage' ];
  this.adminGroups = groupAdminList();
  this.groups = Group.query(function(){
    this.groups = this.groups.filter(function(g) {
      return !groupAdminFilter(g);}
    );
  }.bind(this));
}

UserController.prototype.save = function() {
  this.user.$update(function(r) {
    r.$promise.then(function(user) {
      console.log(arguments[0]);
      // console.log(arguments);
      // this.user = user;
    });
    console.log('user success');
  }, function() {
    console.log('user error');
  });
}
