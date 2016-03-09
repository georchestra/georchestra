angular.module('admin_console')
.controller('UserController', [
  '$routeParams', '$q', '$injector',
  'Flash', 'User', 'Group', 'Email', 'Attachments', 'Templates',
  'groupAdminList', 'groupAdminFilter',
  UserController
]);

function UserController($routeParams, $q, $injector, Flash, User, Group, Email, Attachments,
    Templates, groupAdminList, groupAdminFilter) {
  this.tab = $routeParams.tab;
  this.flash = Flash;
  this.$injector = $injector;
  this.user = User.get({id : $routeParams.id}, function(user) {
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
        this.user.adminGroups = this.user.adminGroups || {};
        this.groups.forEach(function(group) {
          if (group.users.indexOf(this.user.uid)>=0) {
            if (groupAdminFilter(group)) {
              this.user.adminGroups[group.cn] = true;
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
    case 'messages':
      Templates.query(function(r) { this.templates = r.templates; }.bind(this));
      //Attachments.query(function(r) { this.attachments = r.attachments; }.bind(this));
      this.attachments = [{"id":2,
          "name":"Licence.pdf",
        "mimeType": "application/pdf"},
        {"id":3,
          "name":"Admin.pdf",
        "mimeType": "image/jpeg"}
        ];
      break;
    default:
  }
}

UserController.prototype.activate = [
    '$scope', '$cacheFactory', 'LDAP_BASE_URI', 'GroupsUsers',
    function($scope, $cacheFactory, baseUri, GroupsUsers) {

  var $httpDefaultCache = $cacheFactory.get('$http');
  var saveGroups = function(newVal, oldVal) {
    if (!newVal || !oldVal) { return; }

    var toPut = newVal.filter(function(a) { return oldVal.indexOf(a) == -1; });
    var toDel = oldVal.filter(function(a) { return newVal.indexOf(a) == -1; });

    if (toPut.length == 0 && toDel.length == 0) { return; }
    if (toPut.length > 1 || toDel.length > 1) { return; } // Batch operations are wrong artifacts

    GroupsUsers.save({
      users: [ this.user.uid ],
      PUT: toPut,
      DELETE: toDel
    }, function() {
      this.flash.create('success', 'Groups updated');
      $httpDefaultCache.removeAll(); // $httpDefaultCache.remove(baseUri + 'groups');
    }.bind(this), function() {
      this.flash.create('error', 'Error associating to groups');
    }.bind(this));
  }

  $scope.$watch(function(){
    return this.user.groups;
  }.bind(this), saveGroups.bind(this));

  $scope.$watchCollection(function(){
    var groups = [];
    for (g in this.user.adminGroups) {
      if (this.user.adminGroups[g]) {
        groups.push(g);
      }
    }
    return groups;
  }.bind(this), saveGroups.bind(this));

  if (this.tab == 'analytics') {
    this.loadAnalytics($scope);
  }
  if (this.tab == 'logs') {
    this.loadLogs($scope);
  }

}];

UserController.prototype.loadAnalytics = function($scope) {
  var $translate = this.$injector.get('$translate');
  this.data = {};
  this.config = {
    layers   : [ 'layer', 'count' ],
    requests : [ 'date', 'count' ]
  };
  this.data.layers = this.$injector.get('Analytics').get({
      service   : 'combinedRequests',
      user      : this.user.uuid,
      startDate : '15-01-01',
      endDate   : '16-03-09'
    }, function() {},
    this.flash.create.bind(this, 'error', $translate('analytics.errorload'))
  );
  this.data.requests = this.$injector.get('Analytics').get({
      service   : 'layersUsage',
      user      : this.user.uuid,
      startDate : '15-01-01',
      endDate   : '16-03-09'
    }, function() {},
    this.flash.create.bind(this, 'error', $translate('analytics.errorload'))
  );
};

UserController.prototype.loadLogs = function($scope) {
  this.$injector.get('Logs').query({user: this.user.uuid}, function() {
    console.log('success', arguments);
  },function(){
    console.log('error',arguments);
  });
  this.logs = [
    {
      "admin": "98192574-18d0-1035-8e10-c310a114ab8f",
      "date": "2015-12-01T13:48:18Z",
      "target": "98192574-18d0-1035-8e10-c310a114ab8f",
      "type": "Email sent"
    },
    {
      "admin": "9818af68-18d0-1035-8e0e-999999999999",
      "date": "2015-11-30T16:37:00Z",
      "target": "98192574-18d0-1035-8e10-c310a114ab8f",
      "type": "Email sent"
    },
    {
      "admin": "98192574-18d0-1035-8e10-c310a114ab8f",
      "date": "2015-11-30T17:37:50Z",
      "target": "98192574-18d0-1035-8e10-c310a114ab8f",
      "type": "Email sent"
    }
  ];
};

UserController.prototype.save = function() {
  var $translate = this.$injector.get('$translate');
  this.user.$update(function() {
      $httpDefaultCache.removeAll();
      this.flash.create.bind(this, 'success', $translate('user.updated'))
    }.bind(this),
    this.flash.create.bind(this, 'error', $translate('user.error'))
  );
};

UserController.prototype.openMessage = function(message) {
  this.message = message;
};

UserController.prototype.closeMessage = function(message) {
  delete this.message;
  delete this.compose;
};

UserController.prototype.loadTemplate = function() {
  this.compose.subject = this.compose.template.name;
  this.compose.content = this.compose.template.content;
}

UserController.prototype.sendMail = function() {
  var Mail = this.$injector.get('Mail');
  var $translate = this.$injector.get('$translate');
  var attachments = [];
  for (var attach_id in this.compose.attachments) {
    if (this.compose.attachments[attach_id]) { attachments.push(attach_id); }
  }
  (new Mail({
    id: this.user.uuid,
    subject:this.compose.subject,
    content: this.compose.content,
    attachments: attachments.join(',')
  })).$save(function(r) {
    delete this.compose;
    this.flash.create('success', $translate('msg.sent'));
  }.bind(this),
    this.flash.create.bind(this, 'error', $translate('msg.error'))
  );
}
