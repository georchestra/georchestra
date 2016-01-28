angular.module('admin_console')
.controller('UserController', [
  '$routeParams', '$q',
  'Flash', 'User', 'Group', 'Email',
  'groupAdminList', 'groupAdminFilter',
  UserController
]);

function UserController($routeParams, $q, Flash, User, Group, Email,
    groupAdminList, groupAdminFilter) {
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
      var notAdmin = [];
      this.groups = Group.query();
      $q.all([
        this.user.$promise,
        this.groups.$promise
      ]).then(function() {
        this.user.groups = this.user.groups || [];
        this.user.adminGroups = this.user.adminGroups || [];
        this.groups.forEach(function(group) {
          if (group.users.indexOf(this.user.uid)>=0) {
            if (groupAdminFilter(group)) {
              this.user.adminGroups.push(group.cn);
            } else {
              this.user.groups.push(group.cn);
            }
          }
          if (!groupAdminFilter(group)) {
            notAdmin.push(group.cn);
          }
        }.bind(this));
        this.groups = notAdmin;
      }.bind(this));
      break;
    default:
  }
}

UserController.prototype.activate = [ '$scope', '$cacheFactory',
    'LDAP_BASE_URI', 'GroupsUsers', 'Flash',
    function($scope, $cacheFactory, baseUri, GroupsUsers, Flash) {
  $scope.$watch(function(){
    return this.user.groups;
  }.bind(this), function(newVal, oldVal) {
    if (!newVal || !oldVal) {
      return;
    }
    var toAdd = newVal.filter(function(a) {
      return oldVal.indexOf(a) == -1;
    });
    var toRemove = oldVal.filter(function(a) {
      return newVal.indexOf(a) == -1;
    });
    if (toAdd.length == 0 && toRemove.length == 0) {
      return;
    }
    var $httpDefaultCache = $cacheFactory.get('$http');
    GroupsUsers.save({
      users: [ this.user.uid ],
      PUT: toAdd,
      DELETE: toRemove
    }, function() {
      Flash.create('success', 'Groups updated');
      // $httpDefaultCache.remove(baseUri + 'groups');
      $httpDefaultCache.removeAll();
    }, function() {
      Flash.create('error', 'Error associating to groups');
    });
  }.bind(this));
}];

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
