class UsersController {

  constructor($routeParams, User, Group) {

    this.users = User.query(function() {
      this.allUsers = this.users.slice()
    }.bind(this))

    this.groups = Group.query(function(){
      this.activeGroup = this.groups.filter(function(g) {
        return g.cn == $routeParams.id }
      )[0];
      if (this.activeGroup) {
        this.filter(this.activeGroup)
      }
    }.bind(this))

  }

  filter(group) {
    if (!this.allUsers) { return; }
    this.users = this.allUsers.filter(function(user) {
      return group.users.indexOf(user.uid) >= 0;
    })
  }

}

UsersController.$inject = [ '$routeParams', 'User', 'Group' ]

angular.module('admin_console').controller('UsersController', UsersController)
