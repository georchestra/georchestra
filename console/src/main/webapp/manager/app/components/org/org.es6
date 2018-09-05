require('components/org/org.tpl')
require('templates/orgForm.tpl')
require('components/area/area')
require('services/orgs')

class OrgController {
  static $inject = [ '$injector', '$routeParams' ]

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.tabs = ['infos', 'area', 'users', 'manage']
    this.tab = $routeParams.tab

    let translate = $injector.get('translate')
    this.i18n = {}
    translate('org.updated', this.i18n)
    translate('org.error', this.i18n)
    translate('org.deleted', this.i18n)
    translate('org.deleteError', this.i18n)

    this.org = $injector.get('Orgs').get({id: $routeParams.org})
    this.required = $injector.get('OrgsRequired').query()
    this.orgTypeValues = $injector.get('OrgsType').query()

    // check if org is under delegation
    const Delegations = $injector.get('Delegations')
    Delegations.query(resp => {
      this.delegations = resp.filter(d => d.orgs.indexOf($routeParams.org) !== -1)
    })
    const User = $injector.get('User')
    User.query(users => {
      this.users = users.filter(u => u.org === this.org.name)
    })
  }

  save () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.org.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

  delete () {
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    let flash = this.$injector.get('Flash')
    this.org.$delete(() => {
      $httpDefaultCache.removeAll()
      let $router = this.$injector.get('$router')
      $router.navigate($router.generate('orgs', { id: 'all' }))
      flash.create('success', this.i18n.deleted)
    }, flash.create.bind(flash, 'danger', this.i18n.deleteError))
  }

  confirm () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')
    this.org.status = 'REGISTERED'
    this.org.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }
}

angular.module('manager').controller('OrgController', OrgController)
