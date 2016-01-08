angular.module('admin_console.users', [
]).controller('UsersController', [
  '$routeParams', 'User', 'Group',
  UsersController
]);

function UsersController($routeParams, User, Group) {
  this.users = User.query(function() {
    this.allUsers = this.users.slice();
  }.bind(this));

  this.groups = Group.query(function(){
    if ($routeParams.groupid == 'all') { return; }
    this.filter(
      this.groups.filter(function(g) { return g.cn == $routeParams.groupid })[0]
    );
  }.bind(this));
}

UsersController.prototype.filter = function(group, evt) {
  if (evt) {
    evt.preventDefault();
  }
  if (!this.allUsers) { return; }
  this.users = this.allUsers.filter(function(user) {
    return group.users.indexOf(user.uid) >= 0;
  });
};
