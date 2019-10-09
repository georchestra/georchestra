require('components/user/user.tpl')

require('services/util')
require('services/contexts')

class UserController {
  static $inject = [ '$routeParams', '$injector', 'User', 'Role', 'Orgs' ]

  constructor ($routeParams, $injector, User, Role, Orgs) {
    this.$injector = $injector

    let translate = $injector.get('translate')
    this.i18n = {}
    let strings = [
      'user.updated', 'user.error', 'user.deleted', 'user.content',
      'org.select', 'delegation.dupdated', 'delegation.ddeleted' ]
    strings.map(str => translate(str, this.i18n))

    this.tabs = ['infos', 'roles', 'analytics', 'messages', 'logs', 'manage']
    this.tab = $routeParams.tab

    this.user = User.get({id: $routeParams.id}, (user) => {
      user.originalID = user.uid
      if (user.org && user.org !== '') {
        user.orgObj = Orgs.get({'id': user.org}, org => {
          user.validOrg = !org.pending
        })
      } else {
        user.validOrg = true
      }
      if (this.tab === 'delegations') {
        const Delegations = $injector.get('Delegations')
        Delegations.query(resp => {
          let deleg = resp.find(x => x.uid === this.user.uid)
          let options = deleg || { orgs: [], roles: [], uid: this.user.uid }
          this.delegation = new Delegations(options)
          this.activeDelegation = this.hasDelegation()
          $injector.get('Orgs').query(orgs => {
            this.orgs = orgs.filter(o => !o.pending)
          })
          Role.query(roles => { this.allRoles = roles.map(r => r.cn) })
        })
      }
      if (this.tab === 'messages') {
        this.messages = this.$injector.get('Email').query({id: this.user.uid})
      }
    })
    this.adminRoles = this.$injector.get('roleAdminList')()
    switch (this.tab) {
      case 'infos':
        this.contexts = $injector.get('Contexts').query()
        break
      case 'messages':
        this.templates = this.$injector.get('Templates').query()
        this.attachments = this.$injector.get('Attachments').query()
        break
      default:
    }
    this.bindRoles()

    this.required = $injector.get('UserRequired').get()
  }

  hasDelegation () {
    if (!this.delegation) return false
    return (this.delegation.orgs.length !== 0) && (this.delegation.roles.length !== 0)
  }

  // search each choosen span elements and set title manually
  // to display roles description on hover
  setTitles () {
    if (this.roleDescriptions) {
      [].forEach.call(
        document.querySelectorAll('li.search-choice span'),
        span => span.setAttribute('title', this.roleDescriptions[span.innerHTML])
      )
    }
  }

  bindRoles () {
    const TMP_ROLE = 'TEMPORARY'

    // Load role infos for every tab (for confirmation)
    let Role = this.$injector.get('Role')
    this.roles = Role.query()
    Role.query(roles => {
      this.allroles = roles.map(r => r.cn)
      // get roles informations to get description from template
      this.roleDescriptions = {}
      roles.map(r => {
        this.roleDescriptions[r.cn] = r.description
      })
    })
    this.user.$promise.then(() => {
      let roleAdminFilter = this.$injector.get('roleAdminFilter')
      let notAdmin = []
      this.$injector.get('$q').all([
        this.user.$promise,
        this.roles.$promise
      ]).then(() => {
        this.user.roles = this.user.roles || []
        this.user.adminRoles = this.user.adminRoles || {}
        this.roles.forEach((role) => {
          if (role.users.indexOf(this.user.uid) >= 0) {
            if (roleAdminFilter(role)) {
              this.user.adminRoles[role.cn] = true
            } else {
              this.user.roles.push(role.cn)
            }
          }
          if (!roleAdminFilter(role) && role.cn !== TMP_ROLE) {
            notAdmin.push(role.cn)
          }
        })
        this.roles = notAdmin
      })
    })
  }

  loadAnalytics ($scope) {
    let date = this.$injector.get('date')

    this.date = {
      start: date.getFromDiff('year'),
      end: date.getEnd()
    }

    this.config = {
      layers: [ 'layer', 'count' ],
      requests: [ 'date', 'count' ],
      extractions: [ 'layer', 'count' ]
    }
    this.loadAnalyticsData()
  }

  loadAnalyticsData () {
    let i18n = {}
    let i18nPromise = this.$injector.get('translate')('analytics.errorload', i18n)
    let flash = this.$injector.get('Flash')

    this.$injector.get('$q').all([
      this.user.$promise,
      i18nPromise
    ]).then(() => {
      let error = flash.create.bind(flash, 'danger', i18n.errorload)
      let Analytics = this.$injector.get('Analytics')
      let options = {
        service: 'combinedRequests.json',
        user: this.user.uid,
        startDate: this.date.start,
        endDate: this.date.end
      }
      this.requests = Analytics.get(options, () => {}, error)

      let usageOptions = {
        ...options,
        service: 'layersUsage.json',
        limit: 10
      }
      this.layers = Analytics.get(usageOptions, () => {}, error)

      this.usageOptions = { ...usageOptions }
      delete this.usageOptions.limit
      this.usageOptions.service = 'layersUsage.csv'

      let extractionOptions = {
        ...options,
        service: 'layersExtraction.json',
        limit: 10
      }

      this.extractions = Analytics.get(extractionOptions, () => {}, error)
      this.extractionOptions = { ...extractionOptions }
      delete this.extractionOptions.limit
      this.extractionOptions.service = 'layersExtraction.csv'
    })
  }

  loadLogs ($scope) {
    let i18n = {}
    let flash = this.$injector.get('Flash')

    this.$injector.get('$q').all([
      this.user.$promise,
      this.$injector.get('translate')('analytics.errorload', i18n)
    ]).then(() => {
      this.logs = this.$injector.get('UserLogs').query(
        {
          id: this.user.uid,
          limit: 100000,
          page: 0
        },
        () => { this.logs.reverse() },
        flash.create.bind(flash, 'danger', i18n.errorload)
      )
    })
  }

  save () {
    const flash = this.$injector.get('Flash')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    const $router = this.$injector.get('$router')
    this.user.$update(() => {
      $httpDefaultCache.removeAll()
      this.user.originalID = this.user.uid
      flash.create('success', this.i18n.updated)
      // To update URI if uid has changed
      $router.navigate($router.generate('user', {
        id: this.user.uid,
        tab: 'infos'
      }))
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

  delete () {
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let flash = this.$injector.get('Flash')
    this.user.$delete(() => {
      $httpDefaultCache.removeAll()
      let $router = this.$injector.get('$router')
      $router.navigate($router.generate('users', { id: 'all' }))
      flash.create('success', this.i18n.deleted)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

  initCompose () {
    this.quill = new Quill(document.querySelector('#compose_content'), {
      modules: {
        toolbar: [
          [ { header: [ 1, 2, false ] } ],
          [ 'bold', 'italic', 'underline', 'image', {'color': []}, {'align': []} ]
        ]
      },
      placeholder: this.i18n.content,
      theme: 'snow'
    })
    this.quill.on('text-change', () => {
      this.compose.content = this.quill.container.firstChild.innerHTML
    })
  }

  openMessage (message) {
    message.trusted = this.$injector.get('$sce').trustAsHtml(message.body)
    this.message = message
  }

  closeMessage (message) {
    delete this.message
    delete this.compose
  }

  loadTemplate () {
    this.compose.subject = this.compose.template.name
    this.quill.setText(this.compose.template.content)
  }

  sendMail () {
    let flash = this.$injector.get('Flash')
    let Mail = this.$injector.get('Mail')
    let i18n = {}
    this.$injector.get('translate')('msg.sent', i18n)
    this.$injector.get('translate')('msg.error', i18n)
    let attachments = []
    for (let attachId in this.compose.attachments) {
      if (this.compose.attachments[attachId]) { attachments.push(attachId) }
    }
    (new Mail({
      id: this.user.uid,
      subject: this.compose.subject,
      content: this.compose.content,
      attachments: attachments.join(',')
    })).$save((r) => {
      delete this.compose
      flash.create('success', i18n.sent)
      let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
      $httpDefaultCache.removeAll()
      this.messages = this.$injector.get('Email').query({id: this.user.uid})
    }, () => { flash.create('danger', i18n.error) })
  }

  confirm () {
    this.user.pending = false
    this.save()
  }

  deleteDelegation () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.delegation.$delete(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.ddeleted)
      this.delegation = new (this.$injector.get('Delegations'))({
        uid: this.user.uid, roles: [], orgs: []
      })
      this.activeDelegation = false
    }, flash.create.bind(flash, 'danger', this.i18n.derror))
  }

  saveDelegation () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.delegation.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.dupdated)
      this.activeDelegation = this.hasDelegation()
    }, flash.create.bind(flash, 'danger', this.i18n.derror))
  }

  activate ($scope) {
    const TMP_ROLE = 'TEMPORARY'
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let flash = this.$injector.get('Flash')

    $scope.$watch(() => $scope.profile, p => {
      if (p !== 'SUPERUSER' || this.tabs.indexOf('delegations') !== -1) return
      this.tabs.splice(3, 0, 'delegations')
    })

    let saveRoles = function (newVal, oldVal) {
      if (!newVal || !oldVal) { return }
      let removeTmp = g => g !== TMP_ROLE
      newVal = newVal.filter(removeTmp)
      oldVal = oldVal.filter(removeTmp)

      let toPut = newVal.filter(a => oldVal.indexOf(a) === -1)
      let toDel = oldVal.filter(a => newVal.indexOf(a) === -1)

      if (toPut.length === 0 && toDel.length === 0) { return }
      if (toPut.length > 1 && toDel.length === 0) { return } // Wrong artifacts

      let i18n = {}
      this.$injector.get('translate')('users.roleUpdated', i18n)
      this.$injector.get('translate')('users.roleUpdateError', i18n)

      this.rolePromise = this.$injector.get('RolesUsers').save(
        {
          users: [ this.user.uid ],
          PUT: toPut,
          DELETE: toDel
        },
        () => {
          flash.create('success', i18n.roleUpdated)
          $httpDefaultCache.removeAll()
        },
        () => {
          flash.create('danger', i18n.roleUpdateError)
        }
      )
    }

    this.$injector.get('$q').all([
      this.user.$promise,
      this.roles.$promise
    ]).then(() => {
      $scope.$watch(() => this.user.roles, saveRoles.bind(this))

      let previousRoles
      $scope.$watchCollection(() => {
        let roles = []
        for (let g in this.user.adminRoles) {
          if (this.user.adminRoles[g]) { roles.push(g) }
        }
        if (this.user.adminRoles) {
          previousRoles = roles
          // to manually display roles description on roles multi select elements
          this.setTitles()
          return roles
        } else {
          return previousRoles
        }
      }, saveRoles.bind(this))
    })

    if (this.tab === 'analytics') {
      this.loadAnalytics($scope)
    }
    if (this.tab === 'logs') {
      this.loadLogs($scope)
    }
  }
}

UserController.prototype.activate.$inject = [ '$scope' ]

angular.module('manager')
  .controller('UserController', UserController)
  .filter('encodeURIComponent', () => window.encodeURIComponent)
  .directive('managers', [ '$timeout', 'User', ($timeout, User) => ({
    link: (scope, elm, attrs, ctrl) => {
      let promise = scope.$eval(attrs['promise'])
      let selUsers = []
      User.query((users) => {
        users.map((u) => {
          let id = u.uid
          selUsers.push({
            id: id,
            text: (u.sn || '') + ' ' + (u.givenName || '')
          })
        })
        elm.select2({
          placeholder: '',
          allowClear: true,
          data: selUsers
        })
        let cb = () => { $timeout(() => { elm.trigger('change') }) }
        if (promise) {
          promise.then(cb)
        } else {
          cb()
        }
      })
    }
  })])
  .directive('organizations', [ '$timeout', '$router', 'Orgs', ($timeout, $router, Orgs) => ({
    link: (scope, elm, attrs, ctrl) => {
      let promise = scope.$eval(attrs['promise'])
      let user = scope.$eval(attrs['model'])

      // Initialize pending value for new user
      if (user.pending === undefined) {
        user.pending = false
      }
      let selOrgs = []
      Orgs.query((orgs) => {
        orgs.forEach((o) => {
          if (user.pending || !o.pending) {
            selOrgs.push({
              id: o.id,
              text: o.name
            })
          }
        })
        // create template to format selected element
        const formatSelected = (state) => {
          if (!state.id) return state.text
          const route = $router.generate('org', {org: state.id, tab: 'infos'})
          return $(`<a href="#!${route}">${state.text}</a>`)
        }
        elm.select2({
          templateSelection: formatSelected,
          placeholder: '',
          allowClear: true,
          data: selOrgs
        })
        let cb = () => {
          $timeout(() => {
            elm.trigger('change')
          })
        }
        if (promise) {
          promise.then(cb)
        } else {
          cb()
        }
      })
    }
  })])
