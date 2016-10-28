require('components/orgs/orgs.tpl')
require('services/orgs')

class OrgsController {

  static $inject = [ '$injector', '$routeParams' ]

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.org = $routeParams.org
    this.orgs = this.$injector.get('Orgs').query(() => {
      if (this.org === 'pending') {
        this.orgs = this.orgs.filter(o => o.status !== 'REGISTERED')
      }
      this.orgs.forEach(org => {
        org.membersCount = org.members.length
        delete org.members
      })
    })

    this.q = ''
    this.itemsPerPage = 15

    this.newOrg = this.$injector.get('$location').$$search['new'] === 'org'
    if (this.newOrg) {
      const Org = this.$injector.get('Orgs')
      this.newInstance = new Org({})
    }

    this.required = this.$injector.get('OrgsRequired').get()

    let translate = this.$injector.get('translate')
    this.i18n = {}
    translate('org.created', this.i18n)
    translate('org.updated', this.i18n)
    translate('org.deleted', this.i18n)
    translate('org.error', this.i18n)
    translate('org.deleteError', this.i18n)
  }

  create () {
    const Org = this.$injector.get('Orgs')
    this.newInstance = new Org({})
    let $location = this.$injector.get('$location')
    $location.search('new', 'org')
  }

  saveOrg () {
    let flash = this.$injector.get('Flash')
    let $router = this.$injector.get('$router')
    let $location = this.$injector.get('$location')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

    this.newInstance.$save(
      () => {
        flash.create('success', this.i18n.created)
        $httpDefaultCache.removeAll()
        $router.navigate($router.generate('org', {
          org: this.newInstance.id,
          tab: 'infos'
        }))
        $location.url($location.path())
      },
      flash.create.bind(flash, 'danger', this.i18n.error)
    )
  }

  close () {
    this.newOrg = false
    let $location = this.$injector.get('$location')
    $location.url($location.path())
  }

  activate ($scope) {
    let $location = this.$injector.get('$location')
    $scope.$watch(() => $location.search()['new'], (v) => {
      this.newOrg = v === 'org'
    })
  }

}

OrgsController.prototype.activate.$inject = [ '$scope' ]

angular.module('admin_console').controller('OrgsController', OrgsController)
