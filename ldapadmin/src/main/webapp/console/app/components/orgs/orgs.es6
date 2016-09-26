require('components/orgs/orgs.tpl')

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
  }

  create () {
    let $location = this.$injector.get('$location')
    $location.search('new', 'org')
  }

}

angular.module('admin_console').controller('OrgsController', OrgsController)
