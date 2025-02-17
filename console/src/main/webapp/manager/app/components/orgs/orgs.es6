require('components/orgs/orgs.tpl')
require('services/orgs')

class OrgsController {
  static $inject = ['$injector', '$routeParams']

  constructor ($injector, $routeParams) {
    this.$injector = $injector

    this.org = $routeParams.org
    this.orgs = this.$injector.get('Orgs').query(() => {
      if (this.org === 'pending') {
        this.orgs = this.orgs.filter(o => o.pending)
      } else {
        // display no pendings orgs
        this.orgs = this.orgs.filter(o => !o.pending)
      }
      this.orgs.forEach(org => {
        org.membersCount = org.members.length
        delete org.members
      })
      this.simplifiedOrgs = this.orgs.map((org) => {
        return { id: org.id, name: org.name, shortName: org.shortName, membersCount: org.membersCount, pending: org.pending, status: org.status, orgUniqueId: org.orgUniqueId }
      })
    })

    this.q = ''
    this.itemsPerPage = 15

    this.newOrg = this.$injector.get('$location').$$search.new === 'org'
    if (this.newOrg) {
      const Org = this.$injector.get('Orgs')
      this.newInstance = new Org({})
    }

    this.required = $injector.get('OrgsRequired').query()
    this.orgTypeValues = $injector.get('OrgsType').query()

    const translate = this.$injector.get('translate')
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
    const $location = this.$injector.get('$location')
    $location.search('new', 'org')
  }

  saveOrg () {
    const flash = this.$injector.get('Flash')
    const $router = this.$injector.get('$router')
    const $location = this.$injector.get('$location')
    const $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

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
    const $location = this.$injector.get('$location')
    $location.url($location.path())
  }

  activate ($scope) {
    const $location = this.$injector.get('$location')
    $scope.$watch(() => $location.search().new, (v) => {
      this.newOrg = v === 'org'
    })
  }
}

OrgsController.prototype.activate.$inject = ['$scope']

angular.module('manager').controller('OrgsController', OrgsController)
