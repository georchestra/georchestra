angular.module('admin_console')
.controller('UserController', [
  '$routeParams', 'User',
  UserController
]);

function UserController($routeParams, User) {
  console.log('user', $routeParams);
  this.user = User.get({id : $routeParams.id});
  this.tab = $routeParams.tab;
  this.tabs = [ 'infos', 'groups', 'analytics', 'messages', 'logs', 'manage' ];
}
