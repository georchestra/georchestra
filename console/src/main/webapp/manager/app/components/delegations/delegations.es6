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

    let translate = this.$injector.get('translate')
    this.i18n = {}
    translate('role.created', this.i18n)
    translate('role.updated', this.i18n)
    translate('role.deleted', this.i18n)
    translate('role.error', this.i18n)
    translate('role.deleteError', this.i18n)
  }
}

angular.module('admin_console').controller('DelegationsController', DelegationsController)
