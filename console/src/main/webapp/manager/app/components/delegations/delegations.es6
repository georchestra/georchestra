import 'components/delegations/delegations.tpl'
import 'services/delegations'

class DelegationsController {
  static $inject = ['Delegations', 'Orgs']

  constructor (Delegations, Orgs) {
    this.delegations = Delegations.query()
    this.orgs = {}
    Orgs.query(orgs => orgs.forEach(org => (this.orgs[org.id] = org)))
    this.q = ''
    this.itemsPerPage = 15
  }
}

angular
  .module('manager')
  .controller('DelegationsController', DelegationsController)
