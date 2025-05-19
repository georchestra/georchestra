require('components/org/org.tpl')
require('templates/orgForm.tpl')
require('components/area/area')
require('services/orgs')
require('components/imageinput/imageinput')
require('services/roles_orgs')

class OrgController {
  static $inject = ['$injector', '$routeParams']

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.q = ''

    // Initialize TMP_ROLE constant
    this.TMP_ROLE = this.$injector.get('temporaryRole')

    $injector.get('PlatformInfos').get().$promise.then((platformInfos) => {
      this.tabs = platformInfos.competenceAreaEnabled ? ['infos', 'area', 'users', 'roles', 'manage'] : ['infos', 'users', 'roles', 'manage']
    })

    this.tab = $routeParams.tab

    this.itemsPerPage = 15

    const translate = $injector.get('translate')
    this.i18n = {}
    translate('org.updated', this.i18n)
    translate('org.error', this.i18n)
    translate('org.deleted', this.i18n)
    translate('org.deleteError', this.i18n)
    translate('org.userremoved', this.i18n)
    translate('org.useradded', this.i18n)
    translate('org.delete', this.i18n)
    translate('user.remove', this.i18n)

    this.org = $injector.get('Orgs').get({
      id: $routeParams.org
    }, () => this.loadUsers())
    this.required = $injector.get('OrgsRequired').query()
    this.orgTypeValues = $injector.get('OrgsType').query()

    this.bindRoles()
    // check if org is under delegation
    const Delegations = $injector.get('Delegations')
    Delegations.query(resp => {
      this.delegations = resp.filter(d => d.orgs.indexOf($routeParams.org) !== -1)
    })
  }

  bindRoles () {
    // Load role infos for every tab (for confirmation)
    const Role = this.$injector.get('Role')
    this.roles = Role.query(roles => {
      this.allroles = roles.map(r => r.cn)
      // get roles informations to get description from template
      this.roleDescriptions = {}
      // Add role descriptions
      roles.forEach(role => {
        this.roleDescriptions[role.cn] = role.description
      })
    })
    this.org.$promise.then(() => {
      this.$injector.get('$q').all([
        this.org.$promise,
        this.roles.$promise
      ]).then(() => {
        this.org.roles = this.org.roles || []
        this.roles.forEach((role) => {
          if (role.orgs.indexOf(this.org.id) >= 0) {
            this.org.roles.push(role.cn)
          }
        })
        this.roles = this.roles.map(r => r.cn)
      })
    })
  }

  loadUsers () {
    const User = this.$injector.get('User')
    User.query(users => {
      this.users = users.filter(u => u.org === this.org.name)
      this.notUsers = users.filter(u => u.org !== this.org.name)
      this.usersNames = users.reduce((acc, u) => {
        acc[u.uid] = u.sn + ' ' + u.givenName
        return acc
      }, {})
    })
  }

  save () {
    const flash = this.$injector.get('Flash')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.org.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

  delete () {
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    const flash = this.$injector.get('Flash')
    this.org.$delete(() => {
      $httpDefaultCache.removeAll()
      const $router = this.$injector.get('$router')
      $router.navigate($router.generate('orgs', { id: 'all' }))
      flash.create('success', this.i18n.deleted)
    }, flash.create.bind(flash, 'danger', this.i18n.deleteError))
  }

  confirm () {
    const flash = this.$injector.get('Flash')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.org.pending = false
    this.org.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

  associate (uid, unassociate = false) {
    if (!uid) uid = this.user
    if (!uid) return
    const User = this.$injector.get('User')
    const flash = this.$injector.get('Flash')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

    User.update({
      uid: uid,
      originalID: uid,
      org: unassociate ? '' : this.org.id
    }).$promise.then(() => {
      $httpDefaultCache.removeAll()
      this.loadUsers()
      flash.create('success', unassociate ? this.i18n.userremoved : this.i18n.useradded)
    })
  }

  setTitles () {
    if (this.roleDescriptions) {
      [].forEach.call(
        document.querySelectorAll('li.search-choice span'),
        span => span.setAttribute('title', this.roleDescriptions[span.innerHTML])
      )
    }
  }

  activate ($scope) {
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    const flash = this.$injector.get('Flash')

    const saveRoles = function (newVal, oldVal) {
      if (!newVal || !oldVal) {
        return
      }
      const removeTmp = g => g !== this.TMP_ROLE
      newVal = newVal.filter(removeTmp)
      oldVal = oldVal.filter(removeTmp)

      const toPut = newVal.filter(a => oldVal.indexOf(a) === -1)
      const toDel = oldVal.filter(a => newVal.indexOf(a) === -1)

      if (toPut.length === 0 && toDel.length === 0) {
        return
      }
      if (toPut.length > 1 && toDel.length === 0) {
        return
      } // Wrong artifacts

      const i18n = {}
      this.$injector.get('translate')('users.roleUpdated', i18n)
      this.$injector.get('translate')('users.roleUpdateError', i18n)

      // Make sure we're using the correct property for the organization ID
      this.rolePromise = this.$injector.get('RolesOrgs').save(
        {
          orgs: [this.org.id],
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
      this.org.$promise,
      this.roles.$promise
    ]).then(() => {
      $scope.$watch(() => this.org.roles, saveRoles.bind(this))

      let previousRoles
      $scope.$watchCollection(() => {
        this.setTitles()
        return previousRoles
      }, saveRoles.bind(this))
    })
  }

  isUnassignableRole (role) {
    return this.$injector.get('readonlyRoleList').includes(role)
  }
}

OrgController.prototype.activate.$inject = ['$scope']

angular
  .module('manager')
  .controller('OrgController', OrgController)
