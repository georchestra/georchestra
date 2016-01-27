angular.module('admin_console')
.controller('UserController', [
  '$routeParams', 'User', 'Group', 'Email', 'groupAdminList', 'groupAdminFilter',
  UserController
]);

function UserController($routeParams, User, Group, Email, groupAdminList, groupAdminFilter) {
  this.tab = $routeParams.tab;
  this.user = User.get({id : $routeParams.id}, function() {
    if (this.tab == 'messages') {
      Email.query({id: this.user.uuid}, function(r) {
        // this.messages = r.emails;
        this.messages =  [{
          "sender": "98192574-18d0-1035-8e10-c310a114ab8f",
          "id": 51,
          "body": "qsdfqsdfqsf",
          "subject": "Hello",
          "attachments": [
            {
              "id": 42,
              "name": "intelij.jpeg",
              "mimeType": "image/jpeg",
              "size": 30218
            },
            {
              "id": 43,
              "name": "intelij.jpeg",
              "mimeType": "image/jpeg",
              "size": 30218
            }
          ],
          "date": "2007-03-01T13:00:00Z",
        "recipient": "9818af68-18d0-1035-8e0e-c310a114ab8f"},
        {
          "sender": "98192574-18d0-1035-8e10-c310a114ab8f",
          "id": 52,
          "body": "Hello ça va ?",
          "subject": "Hi men :!",
          "attachments": [{
            "id": 44,
            "name": "intelij.jpeg",
            "mimeType": "image/jpeg",
            "size": 30218
          }],
          "date": "2015-11-23T16:44:18.00Z",
        "recipient": "9818af68-18d0-1035-8e0e-c310a114ab8f"}
        ];
      }.bind(this));
    }
  }.bind(this));
  this.tabs = [ 'infos', 'groups', 'analytics', 'messages', 'logs', 'manage' ];
  this.adminGroups = groupAdminList();
  switch (this.tab) {
    case 'groups':
      this.groups = Group.query(function(){
        this.groups = this.groups.filter(function(g) {
          return !groupAdminFilter(g);}
        );
      }.bind(this));
      break;
    default:
  }
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

UserController.prototype.openMessage = function(message) {
  this.message = message;
}
UserController.prototype.closeMessage = function(message) {
  delete this.message;
}
