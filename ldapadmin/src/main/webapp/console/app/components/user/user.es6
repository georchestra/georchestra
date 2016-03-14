class UserController {

  constructor($routeParams, $injector, User, Group, groupAdminFilter) {

    this.tabs = ['infos', 'groups', 'analytics', 'messages', 'logs', 'manage']

    this.$injector = $injector
    this.tab = $routeParams.tab
    this.flash = this.$injector.get('Flash')

    this.user = User.get({id : $routeParams.id}, (user) => {
      if (this.tab == 'messages') {
        this.$injector.get('Email').query({id: this.user.uuid}, (r) => {
          // this.messages = r.emails;
          this.messages =  [{ "sender": "98192574-18d0-1035-8e10-c310a114ab8f", "id": 51, "body": "qsdfqsdfqsf", "subject": "Hello", "attachments": [ { "id": 42, "name": "intelij.jpeg", "mimeType": "image/jpeg", "size": 30218 }, { "id": 43, "name": "intelij.jpeg", "mimeType": "image/jpeg", "size": 30218 } ], "date": "2007-03-01T13:00:00Z", "recipient": "9818af68-18d0-1035-8e0e-c310a114ab8f"}, { "sender": "98192574-18d0-1035-8e10-c310a114ab8f", "id": 52, "body": "Hello ça va ?", "subject": "Hi men :!", "attachments": [{ "id": 44, "name": "intelij.jpeg", "mimeType": "image/jpeg", "size": 30218 }], "date": "2015-11-23T16:44:18.00Z", "recipient": "9818af68-18d0-1035-8e0e-c310a114ab8f"} ];
        });
      }
    });
    this.adminGroups = this.$injector.get('groupAdminList')()
    switch (this.tab) {
      case 'groups':
        let notAdmin = [];
        this.groups = Group.query();
        this.$injector.get('$q').all([
          this.user.$promise,
          this.groups.$promise
        ]).then(() => {
          this.user.groups = this.user.groups || [];
          this.user.adminGroups = this.user.adminGroups || {};
          this.groups.forEach((group) => {
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
          })
          this.groups = notAdmin;
        })
        break;
      case 'messages':
        this.templates = this.$injector.get('Templates').query()
        // this.attachments = this.$injector.get('Attachments').query()
        this.attachments = { "attachments" :[{"id":2, "name":"Licence.pdf", "mimeType": "application/pdf"}, {"id":3, "name":"Admin.pdf", "mimeType": "image/jpeg"} ]}
        break;
      default:
    }
  }

  loadAnalytics($scope) {
    let $translate = this.$injector.get('$translate');
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
  }

  loadLogs($scope) {
    let $translate = this.$injector.get('$translate');
    this.$injector.get('Logs').query({user: this.user.uuid}, () => {
      this.logs = [ { "admin": "98192574-18d0-1035-8e10-c310a114ab8f", "date": "2015-12-01T13:48:18Z", "target": "98192574-18d0-1035-8e10-c310a114ab8f", "type": "Email sent" }, { "admin": "9818af68-18d0-1035-8e0e-999999999999", "date": "2015-11-30T16:37:00Z", "target": "98192574-18d0-1035-8e10-c310a114ab8f", "type": "Email sent" }, { "admin": "98192574-18d0-1035-8e10-c310a114ab8f", "date": "2015-11-30T17:37:50Z", "target": "98192574-18d0-1035-8e10-c310a114ab8f", "type": "Email sent" } ];
    },
    this.flash.create.bind(this, 'error', $translate('analytics.errorload'))
    );
  }

  save() {
    let $translate = this.$injector.get('$translate');
    this.user.$update(() => {
        $httpDefaultCache.removeAll();
        this.flash.create.bind(this, 'success', $translate('user.updated'))
      },
      this.flash.create.bind(this, 'error', $translate('user.error'))
    );
  }

  openMessage(message) {
    this.message = message;
  }

  closeMessage(message) {
    delete this.message;
    delete this.compose;
  }

  loadTemplate() {
    this.compose.subject = this.compose.template.name;
    this.compose.content = this.compose.template.content;
  }

  sendMail() {
    let Mail = this.$injector.get('Mail');
    let $translate = this.$injector.get('$translate');
    let attachments = [];
    for (let attach_id in this.compose.attachments) {
      if (this.compose.attachments[attach_id]) { attachments.push(attach_id); }
    }
    (new Mail({
      id: this.user.uuid,
      subject:this.compose.subject,
      content: this.compose.content,
      attachments: attachments.join(',')
    })).$save((r) => {
        delete this.compose;
        this.flash.create('success', $translate('msg.sent'));
      },
      this.flash.create.bind(this, 'error', $translate('msg.error'))
    );
  }

  activate($scope) {

    // let $scope = this.$injector.get('$scope')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

    let saveGroups = function(newVal, oldVal) {
      if (!newVal || !oldVal) { return; }

      let toPut = newVal.filter(a => oldVal.indexOf(a) == -1)
      let toDel = oldVal.filter(a => newVal.indexOf(a) == -1)

      if (toPut.length == 0 && toDel.length == 0) { return; }
      if (toPut.length > 1 || toDel.length > 1) { return; } // Batch operations are wrong artifacts

      this.$injector.get('GroupsUsers').save({
        users  : [ this.user.uid ],
        PUT    : toPut,
        DELETE : toDel
      }, () => {
        this.flash.create('success', 'Groups updated')
        $httpDefaultCache.removeAll()
      }, () => {
        this.flash.create('error', 'Error associating to groups')
      })
    }

    $scope.$watch(() => this.user.groups, saveGroups.bind(this))

    $scope.$watchCollection(() => {
      let groups = []
      for (let g in this.user.adminGroups) {
        if (this.user.adminGroups[g]) { groups.push(g) }
      }
      return groups;
    }, saveGroups.bind(this));

    if (this.tab == 'analytics') {
      this.loadAnalytics($scope);
    }
    if (this.tab == 'logs') {
      this.loadLogs($scope);
    }

  }

}

UserController.$inject = [
  '$routeParams', '$injector', 'User', 'Group', 'groupAdminFilter'
]

UserController.prototype.activate.$inject = [ '$scope' ]



angular.module('admin_console')
.controller('UserController', UserController);

