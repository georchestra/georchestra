import 'components/delegations/delegations.tpl'
import 'services/delegations'

class DelegationsController {
  static $inject = [ 'Delegations' ]

  constructor (Delegations) {
    // this.delegations = Delegations.query()
    Delegations.query(resp => { this.delegations = resp.delegations })

    this.q = ''
    this.itemsPerPage = 15
  }
}

angular
  .module('admin_console')
  .controller('DelegationsController', DelegationsController)
