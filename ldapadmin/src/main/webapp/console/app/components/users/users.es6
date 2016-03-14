class UsersController {

  constructor($routeParams, User, Group) {

    this.users = User.query(() => {
      this.allUsers = this.users.slice()
    })

    this.groups = Group.query(() => {
      this.activeGroup = this.groups.filter(g => g.cn == $routeParams.id)[0];
      if (this.activeGroup) {
        this.filter(this.activeGroup)
      }
    })

  }

  filter(group) {
    this.users.$promise.then(() => {
      this.users = this.allUsers.filter(
        user => (group.users.indexOf(user.uid) >= 0)
      )
    })
  }

}

UsersController.$inject = [ '$routeParams', 'User', 'Group' ]

angular.module('admin_console').controller('UsersController', UsersController)
