require('components/user/user.tpl')

require('services/util')

class UserController {

  static $inject = [ '$routeParams', '$injector', 'User', 'Group' ]

  constructor($routeParams, $injector, User, Group) {

    this.$injector = $injector
    let groupAdminFilter = $injector.get('groupAdminFilter')

    let translate = $injector.get('translate');
    this.messages = {}
    translate('user.updated', this.messages)
    translate('user.error', this.messages)
    translate('user.deleted', this.messages)


    this.tabs  = ['infos', 'groups', 'analytics', 'messages', 'logs', 'manage']
    this.tab   = $routeParams.tab

    this.user = User.get({id : $routeParams.id}, (user) => {
      if (this.tab == 'messages') {
        this.messages = this.$injector.get('Email').query({id: this.user.uid})
      }
    })
    this.adminGroups = this.$injector.get('groupAdminList')()
    switch (this.tab) {
      case 'infos':
        break;
      case 'groups':
        let notAdmin = [];
        this.groups = Group.query()
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
              notAdmin.push(group.cn)
            }
          })
          this.groups = notAdmin
        })
        break;
      case 'messages':
        this.templates = this.$injector.get('Templates').query()
        this.attachments = this.$injector.get('Attachments').query()
        break;
      default:
    }
  }

  loadAnalytics($scope) {
    let date    = this.$injector.get('date')

    this.date = {
      start : date.getFromDiff('year'),
      end   : date.getEnd()
    }

    this.config = {
      layers   : [ 'layer', 'count' ],
      requests : [ 'date', 'count' ]
    }
    this.loadAnalyticsData()

  }

  loadAnalyticsData() {
    let i18n        = {}
    let i18nPromise = this.$injector.get('translate')('analytics.errorload', i18n)
    let flash = this.$injector.get('Flash')

    this.$injector.get('$q').all([
      this.user.$promise,
      i18nPromise
    ]).then(() => {
      let error = flash.create.bind(flash, 'danger', i18n.errorload)
      let Analytics = this.$injector.get('Analytics')
      let options = {
        service   : 'combinedRequests',
        user      : this.user.uid,
        startDate : this.date.start,
        endDate   : this.date.end
      }
      this.requests   = Analytics.get(options, () => {}, error)
      options.service = 'layersUsage'
      options.limit   = 10
      this.layers     = Analytics.get(options, () => {}, error)
    })
  }

  loadLogs($scope) {
    let i18n = {}
    let flash = this.$injector.get('Flash')

    this.$injector.get('$q').all([
      this.user.$promise,
      this.$injector.get('translate')('analytics.errorload', i18n)
    ]).then(() => {
      this.logs = this.$injector.get('UserLogs').query(
        {
          id    : this.user.uid,
          limit : 100000,
          page  : 0
        },
        () => { },
        flash.create.bind(flash, 'danger', i18n.errorload)
      )
    })
  }

  save() {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.user.$update(() => {
        $httpDefaultCache.removeAll()
        flash.create('success', this.messages.updated)
      },
      flash.create.bind(flash, 'danger', this.messages.error)
    )
  }

  delete() {
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let flash = this.$injector.get('Flash')
    this.user.$delete(() => {
        $httpDefaultCache.removeAll()
        let $router = this.$injector.get('$router')
        $router.navigate($router.generate('users', { id: 'all'}))
        flash.create('success', this.messages.deleted)
      },
      flash.create.bind(flash, 'danger', this.messages.error)
    )
  }

  openMessage(message) {
    this.message = message
  }

  closeMessage(message) {
    delete this.message
    delete this.compose
  }

  loadTemplate() {
    this.compose.subject = this.compose.template.name
    this.compose.content = this.compose.template.content
  }

  sendMail() {
    let flash = this.$injector.get('Flash')
    let Mail  = this.$injector.get('Mail')
    let i18n  = {}
    this.$injector.get('translate')('msg.sent', i18n)
    this.$injector.get('translate')('msg.error', i18n)
    let attachments = []
    for (let attach_id in this.compose.attachments) {
      if (this.compose.attachments[attach_id]) { attachments.push(attach_id) }
    }
    (new Mail({
      id: this.user.uid,
      subject:this.compose.subject,
      content: this.compose.content,
      attachments: attachments.join(',')
    })).$save((r) => {
        delete this.compose
        flash.create('success', i18n.sent)
      },
      () => { flash.create('danger', i18n.error) }
    )
  }

  activate($scope) {

    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let flash             = this.$injector.get('Flash')

    let saveGroups = function(newVal, oldVal) {
      if (!newVal || !oldVal) { return }

      let toPut = newVal.filter(a => oldVal.indexOf(a) == -1)
      let toDel = oldVal.filter(a => newVal.indexOf(a) == -1)

      if (toPut.length == 0 && toDel.length == 0) { return }
      if (toPut.length > 1 || toDel.length > 1) { return } // Batch operations are wrong artifacts

      this.$injector.get('GroupsUsers').save(
        {
          users  : [ this.user.uid ],
          PUT    : toPut,
          DELETE : toDel
        }, () => {
          flash.create('success', 'Groups updated')
          $httpDefaultCache.removeAll()
        }, flash.create.bind(flash, 'danger', 'Error associating to groups')
      )
    }

  this.contexts =[
    {
      "keywords": ["relief"],
      "label": "geOrchestra World",
      "thumbnail": "context/image/relief.png",
      "tip": "This map displays geOrchestra instances on top of a world DEM baselayer",
      "wmc": "context/relief.wmc"
    },
    {
      "keywords": ["default"],
      "label": "OSM Map over Western Europa",
      "thumbnail": "context/image/default.png",
      "tip": "Uses a layer made from regularly updated OpenStreetMap data, rendered with a GoogleMaps-inspired style.",
      "wmc": "context/default.wmc"
    }
  ];


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

UserController.prototype.activate.$inject = [ '$scope' ]

angular.module('admin_console')
.controller('UserController', UserController)
.filter('encodeURIComponent', () => window.encodeURIComponent )
