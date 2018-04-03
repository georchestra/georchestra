import 'components/delegations/delegations.tpl'
import 'services/delegations'

class DelegationsController {
  static $inject = [ '$injector' ]

  constructor ($injector) {
    this.$injector = $injector

    // this.delegations = this.$injector.get('Delegations').query()
    this.$injector.get('Delegations').query(r => {
      this.delegations = r.delegations
    })

    this.q = ''
    this.itemsPerPage = 15
  }
}

angular.module('admin_console').controller('DelegationsController', DelegationsController)
