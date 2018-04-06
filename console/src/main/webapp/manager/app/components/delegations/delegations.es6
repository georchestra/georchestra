import 'components/delegations/delegations.tpl'
import 'services/delegations'

class DelegationsController {
  static $inject = [ 'Delegations' ]

  constructor (Delegations) {
    this.delegations = Delegations.query()
    this.q = ''
    this.itemsPerPage = 15
  }
}

angular
  .module('manager')
  .controller('DelegationsController', DelegationsController)
